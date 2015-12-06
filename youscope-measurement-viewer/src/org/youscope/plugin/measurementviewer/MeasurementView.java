/**
 * 
 */
package org.youscope.plugin.measurementviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.youscope.addon.postprocessing.PostProcessorAddon;
import org.youscope.addon.postprocessing.PostProcessorAddonFactory;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.ImageLoadingTools;
import org.youscope.uielements.StandardFormats;

/**
 * @author Moritz Lang
 *
 */
class MeasurementView extends JPanel implements Runnable, ImageFolderListener
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -4049265961069834567L;

	private final YouScopeClient client;
	private final File measurementFolder;
	private final LoadMeasurementGlassPane glassPane = new LoadMeasurementGlassPane();
	private final JSplitPane contentSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT , false);
	private final JTabbedPane tabbedPane = new JTabbedPane();
	private final YouScopeServer server;
	public MeasurementView(YouScopeClient client, YouScopeServer server, File measurementFolder)
	{
		super(new BorderLayout());
		this.client = client;
		this.server = server;
		this.measurementFolder = measurementFolder;
		
		glassPane.setBackground(Color.WHITE);
		glassPane.setLoading(true);
		add(glassPane, BorderLayout.CENTER);
		
		// Start loading process
		(new Thread(this)).start();
	}
	
	@Override
	public void run()
	{
		// Load image information from images.csv file.
		ImageFolderNode rootNode;
		try
		{
			rootNode = ImagesFileProcessor.processImagesFile(new File(measurementFolder, "images.csv"));
		}
		catch(Exception e)
		{
			class ErrorRunner implements Runnable
			{
				private final Exception exception;
				ErrorRunner(Exception exception)
				{
					this.exception = exception;
				}
				@Override
				public void run()
				{
					String errorText = "<html><p style=\"font-size:small;margin-top:0px;\"><b>Could not process image definition file (images.csv).</b></p>";
					if(exception != null)
					{
						errorText += "<p style=\"margin-left:15pt;margin-top:0pt;font-family:monospace\">Details:";
						Throwable throwable = exception;
						for(; throwable != null; throwable = throwable.getCause())
						{
							if(throwable.getMessage() != null)
							{
								errorText += "<br /><span style=\"color:#666666\">" + throwable.getClass().getSimpleName() + ": " + throwable.getMessage().replace("\n", "<br />") + "</span>";
							}
							else
							{
								errorText += "<br /><span style=\"color:#666666\">" + throwable.getClass().getSimpleName() + ": No error descirption.</span>";
							}
						}
						errorText += "</p>";
					}
					errorText+="</html>";
					
					JEditorPane errorPane = new JEditorPane();
					errorPane.setEditable(false);
					errorPane.setContentType("text/html");
					errorPane.setText(errorText);
					JScrollPane errorScrollPane = new JScrollPane(errorPane);
					
					glassPane.setLoading(false);
					MeasurementView.this.removeAll();
					add(errorScrollPane, BorderLayout.CENTER);
					revalidate();
				}	
			}
			
			SwingUtilities.invokeLater(new ErrorRunner(e));
			return;
		}
		
		// Initialize content
		// Multi-threading save, since all content elements are yet not shown.
		// 1) The image viewer
		MeasurementTree measurementTree = new MeasurementTree(rootNode);
		measurementTree.addImageFolderListener(this);
		
		ImageIcon selectImagingJobIcon = ImageLoadingTools.getResourceIcon("org/youscope/plugin/measurementviewer/images/arrowLeft.png", "select previous tab first");
		JLabel selectImagingJobLabel = new JLabel("<html><p style=\"font-size:16pt\">Select an imaging job to view!</p></html>", SwingConstants.CENTER);
		selectImagingJobLabel.setOpaque(false);
		selectImagingJobLabel.setVerticalAlignment(SwingConstants.CENTER);
		selectImagingJobLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
		selectImagingJobLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		if(selectImagingJobIcon != null)
		{
			selectImagingJobLabel.setIcon(selectImagingJobIcon);
		}
		
		JPanel measurementTreePanel = new JPanel(new BorderLayout());
		measurementTreePanel.add(new JScrollPane(measurementTree), BorderLayout.CENTER);
		measurementTreePanel.setBorder(new TitledBorder("Select Imaging Job"));
		
		contentSplitPane.setRightComponent(selectImagingJobLabel);
		selectImagingJobLabel.setOpaque(false);
		contentSplitPane.setLeftComponent(measurementTreePanel);
		measurementTreePanel.setOpaque(false);
		contentSplitPane.setResizeWeight(0.0);
        contentSplitPane.setOneTouchExpandable(true);
        contentSplitPane.setBorder(new EmptyBorder(2, 2, 2, 2));
        contentSplitPane.setOpaque(false);
        
        tabbedPane.addTab("Image Viewer", contentSplitPane);
        
        // 2) The image plugins
        GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
        GridBagLayout pluginLayout = new GridBagLayout();
        JPanel pluginsPanel = new JPanel(pluginLayout);
        pluginsPanel.setOpaque(true);
        pluginsPanel.setBackground(Color.WHITE);
        Iterator<PostProcessorAddonFactory> factories = client.getMeasurementPostProcessorAddons().iterator();
		for(;factories.hasNext();)
		{
			PostProcessorAddonFactory factory = factories.next();
			for(String addonID : factory.getSupportedPostProcessorIDs())
			{
				if(addonID.equals(MeasurementViewer.TYPE_IDENTIFIER))
					continue;
				JPanel pluginPanel = new JPanel(new GridLayout(1, 2, 2, 2));
				pluginPanel.setOpaque(false);
				pluginPanel.add(new JLabel(factory.getPostProcessorName(addonID)));
				pluginPanel.add(new StartProcessorButton(factory, addonID));
				StandardFormats.addGridBagElement(pluginPanel, pluginLayout, newLineConstr, pluginsPanel);
			}
		}
		JPanel emptyPanel = new JPanel();
		emptyPanel.setOpaque(false);
		StandardFormats.addGridBagElement(emptyPanel, pluginLayout, StandardFormats.getBottomContstraint(), pluginsPanel);
		
		JEditorPane pluginDescPane = new JEditorPane();
		pluginDescPane.setEditable(false);
		pluginDescPane.setContentType("text/html");
		pluginDescPane.setText("<html><p style=\"font-size:small;margin-top:0px;\">Below there is a list of all available measurement post-processing plugins currently installed in YouScope.<br />To obtain more plugins or create own ones, please visit http://www.youscope.org .</p></html>");
		JPanel optionsPanel = new JPanel(new BorderLayout());
		optionsPanel.setOpaque(false);
		optionsPanel.add(pluginDescPane, BorderLayout.NORTH);
		JScrollPane pluginScrollPane= new JScrollPane(pluginsPanel);
		pluginScrollPane.setOpaque(false);
		optionsPanel.add(pluginScrollPane, BorderLayout.CENTER);
        
    	tabbedPane.addTab("Measurement Processing", optionsPanel);
    	
		
		// Set final layout in UI thread.
		SwingUtilities.invokeLater(layoutSetterWhenLoadingFinished);
	}
	
	private Runnable layoutSetterWhenLoadingFinished = new Runnable()
	{
		@Override
		public void run()
		{
			glassPane.setLoading(false);
			MeasurementView.this.removeAll();
			MeasurementView.this.add(tabbedPane, BorderLayout.CENTER);
			MeasurementView.this.revalidate();
		}
	};

	@Override
	public void showFolder(ImageFolderNode imagefolder)
	{
		contentSplitPane.setRightComponent(new ImageSeriesPanel(client, measurementFolder, imagefolder));
		contentSplitPane.revalidate();
	}
	
	private class StartProcessorButton extends JButton implements ActionListener
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -1189398454827661984L;
		private final PostProcessorAddonFactory addonFactory;
		private final String addonID;
		StartProcessorButton(PostProcessorAddonFactory addonFactory, String addonID)
		{
			super("Open Plugin");
			setOpaque(false);
			this.addonFactory = addonFactory;
			this.addonID = addonID;
			addActionListener(this);
		}
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			YouScopeFrame frame = client.createFrame();
			PostProcessorAddon addon = addonFactory.createMeasurementConfigurationAddon(addonID, client, server, measurementFolder.getAbsolutePath());
			addon.createUI(frame);
			frame.setVisible(true);
		}
	}
}
