/**
 * 
 */
package ch.ethz.csb.youscope.addon.measurementviewer;

import java.awt.Dimension;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.addon.postprocessing.MeasurementPostProcessorAddon;
import ch.ethz.csb.youscope.client.addon.tool.ToolAddon;
import ch.ethz.csb.youscope.client.uielements.ImageLoadingTools;
import ch.ethz.csb.youscope.shared.YouScopeServer;

/**
 * @author langmo
 *
 */
public class MeasurementViewer implements ToolAddon, MeasurementPostProcessorAddon, ViewMeasurementListener
{
	private final YouScopeClient client;
	private final YouScopeServer server;
	private final FileSystem fileSystem;
	private final String openFolder;
	
	private final JSplitPane contentSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT , false);
	
	/**
	 * Constructor.
	 * @param client Interface to the YouScope client.
	 * @param server Interface to the YouScope server.
	 * @param openFolder The folder which should be opened at startup. If null, the default folder is opened.
	 */
	public MeasurementViewer(YouScopeClient client, YouScopeServer server, String openFolder)
	{
		this.client = client;
		this.server = server;
		this.openFolder = openFolder;
		fileSystem = new FileSystem(client);
		fileSystem.addViewMeasurementListener(this);
	}
	
	/**
	 * Convenient constructor. Same as MeasurementViewer(client, server, null).
	 * @param client Interface to the YouScope client.
	 * @param server Interface to the YouScope server.
	 */
	public MeasurementViewer(YouScopeClient client, YouScopeServer server)
	{
		this(client, server, null);
	}
	
	@Override
	public void createUI(YouScopeFrame frame)
	{
		frame.setClosable(true);
		frame.setMaximizable(true);
		frame.setResizable(true);
		frame.setTitle("MeasurementViewer");
		
		File measurementFolder = null;
		if(openFolder != null)
		{
			measurementFolder = new File(openFolder);
			if(measurementFolder.exists() && measurementFolder.isDirectory())
			{
				File imagesFile = new File(measurementFolder, "images.csv");
				if(!imagesFile.exists() || !imagesFile.isFile())
					measurementFolder = null;
			}
			else
			{
				measurementFolder = null;
			}
		}
		
		if(measurementFolder == null)
		{
			ImageIcon selectMeasurementIcon = ImageLoadingTools.getResourceIcon("ch/ethz/csb/youscope/addon/measurementviewer/images/arrowLeft.png", "select previous tab first");
			JLabel selectMeasurementLabel = new JLabel("<html><p style=\"font-size:16pt\">Select measurement!</p></html>", SwingConstants.CENTER);
			selectMeasurementLabel.setOpaque(false);
			selectMeasurementLabel.setVerticalAlignment(SwingConstants.CENTER);
			selectMeasurementLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
			selectMeasurementLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			if(selectMeasurementIcon != null)
			{
				selectMeasurementLabel.setIcon(selectMeasurementIcon);
			}
			
			contentSplitPane.setRightComponent(selectMeasurementLabel);
		}
		else
		{
			MeasurementView measurementView = new MeasurementView(client, server, measurementFolder);
			contentSplitPane.setRightComponent(measurementView);
			fileSystem.setSelectedMeasurement(measurementFolder);
		}
		
		contentSplitPane.setLeftComponent(fileSystem);
		contentSplitPane.setResizeWeight(0.0);
        contentSplitPane.setOneTouchExpandable(true);
        contentSplitPane.setBorder(new EmptyBorder(2, 2, 2, 2));
		
        // Tell ImageIO to scan for plugins. This scan is necessary to load
		// or save files, e.g. in the tiff format.
        Thread.currentThread().setContextClassLoader(MeasurementViewer.class.getClassLoader());
		ImageIO.scanForPlugins();
        
        // End initializing
        frame.setContentPane(contentSplitPane);
        frame.setSize(new Dimension(900, 500));
	}

	@Override
	public void viewMeasurement(String measurementPath)
	{
		MeasurementView measurementView = new MeasurementView(client, server, new File(measurementPath));
		contentSplitPane.setRightComponent(measurementView);
		contentSplitPane.revalidate();
	}
}
