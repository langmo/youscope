/**
 * 
 */
package ch.ethz.csb.youscope.addon.lifecelldetection;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.uielements.ImageLoadingTools;
import ch.ethz.csb.youscope.client.uielements.MeasurementConfigurationPage;

/**
 * @author langmo
 * 
 */
class StartPage extends MeasurementConfigurationPage<CellDetectionMeasurementConfiguration>
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 2394617369656492466L;

	@Override
	public void createUI(YouScopeFrame parentFrame)
	{
		setLayout(new BorderLayout(5, 5));
		setOpaque(false);
		
		JEditorPane descriptionPane = new JEditorPane();
		descriptionPane.setEditable(false);
		descriptionPane.setContentType("text/html");
		descriptionPane.setText("<html><p style=\"font-size:small;margin-top:0px;\"><b>Description:</b></p><p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">Continuously takes images in a given channel and detects the cells in it.<br>If selected, an image is automatically created highlighting all detected cells.</p></html>");
		JScrollPane descriptionScrollPane = new JScrollPane(descriptionPane);
		descriptionScrollPane.setPreferredSize(new Dimension(250, 200));
		add(descriptionScrollPane, BorderLayout.CENTER);
		
		// Descriptive image
		ImageIcon microplateMeasurementIcon = ImageLoadingTools.getResourceIcon("ch/ethz/csb/youscope/addon/lifecelldetection/images/life-cell-detection.jpg", "Life Cell Detection");
		if(microplateMeasurementIcon != null)
		{
			
			JPanel imagePanel = new JPanel(new BorderLayout());
			imagePanel.setOpaque(false);
			JLabel imageLabel = new JLabel(microplateMeasurementIcon, SwingConstants.LEFT);
			imageLabel.setBackground(Color.BLACK);
			imageLabel.setOpaque(true);
			imageLabel.setBorder(new LineBorder(Color.BLACK, 1));
			JLabel legendLabel = new JLabel("<html><b>Figure 1:</b> Picture of a microscope.</html>", SwingConstants.LEFT);
			imagePanel.add(imageLabel, BorderLayout.CENTER);
			imagePanel.add(legendLabel, BorderLayout.SOUTH);
			add(imagePanel, BorderLayout.WEST);
		}
	}

	@Override
	public void loadData(CellDetectionMeasurementConfiguration configuration)
	{
		// Do nothing.
	}

	@Override
	public boolean saveData(CellDetectionMeasurementConfiguration configuration)
	{
		// Do nothing.
		return true;
	}

	@Override
	public void setToDefault(CellDetectionMeasurementConfiguration configuration)
	{
		// Do nothing.
	}

	@Override
	public String getPageName()
	{
		return "Description";
	}

}
