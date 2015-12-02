/**
 * 
 */
package ch.ethz.csb.youscope.addon.composedimaging;

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

import ch.ethz.csb.youscope.client.uielements.ImageLoadingTools;

/**
 * @author langmo
 * 
 */
class StartPage extends AbstractConfigurationPage
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 2394617369656492466L;

	@Override
	public void createUI()
	{
		setLayout(new BorderLayout(5, 5));
		setOpaque(false);
		
		JEditorPane descriptionPane = new JEditorPane();
		descriptionPane.setEditable(false);
		descriptionPane.setContentType("text/html");
		descriptionPane.setText("<html><p style=\"font-size:small;margin-top:0px;\"><b>Description:</b></p><p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">A composed imaging measurement is used to to take pictures in a two dimensional spatial array.</p><p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">The pictures are taken with a given overlap such that they can be composed afterwards. This measurement type only takes the images, the composing process has to be done by an appropriate external program.</p></html>");
		JScrollPane descriptionScrollPane = new JScrollPane(descriptionPane);
		descriptionScrollPane.setPreferredSize(new Dimension(250, 200));
		add(descriptionScrollPane, BorderLayout.CENTER);
		
		// Descriptive image
		ImageIcon microplateMeasurementIcon = ImageLoadingTools.getResourceIcon("ch/ethz/csb/youscope/addon/composedimaging/images/composed-imaging.jpg", "Composed Imaging Measurement");
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
	public void loadData(ComposedImagingMeasurementConfiguration configuration)
	{
		// Do nothing.
	}

	@Override
	public void saveData(ComposedImagingMeasurementConfiguration configuration)
	{
		// Do nothing.
	}

	@Override
	public void setToDefault(ComposedImagingMeasurementConfiguration configuration)
	{
		// Do nothing.
	}

	@Override
	public String getPageName()
	{
		return "Description";
	}

}
