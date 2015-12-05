package org.youscope.plugin.continousimaging;

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

import org.youscope.addon.measurement.MeasurementAddonUIPage;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.uielements.ImageLoadingTools;

/**
 * @author Moritz Lang
 *
 */
public class StartPage extends MeasurementAddonUIPage<ContinousImagingMeasurementConfiguration>
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 2394111369656492466L;
	@Override
	public void loadData(ContinousImagingMeasurementConfiguration configuration)
	{
		// Do nothing.
	}

	@Override
	public boolean saveData(ContinousImagingMeasurementConfiguration configuration)
	{
		// Do nothing.
		return true;
	}

	@Override
	public void setToDefault(ContinousImagingMeasurementConfiguration configuration)
	{
		// Do nothing.
	}

	@Override
	public String getPageName()
	{
		return "Description";
	}

	@Override
	public void createUI(YouScopeFrame frame)
	{
		setLayout(new BorderLayout(5, 5));
		setOpaque(false);
		
		JEditorPane descriptionPane = new JEditorPane();
		descriptionPane.setEditable(false);
		descriptionPane.setContentType("text/html");
		descriptionPane.setText("<html><p style=\"font-size:small;margin-top:0px;\"><b>Description:</b></p>" +
				"<p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">A continuous imaging measurement is used to (rapidly) take images at the current position every given period.</p>"+
				"<p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">One can select the channel, the exposure time and the imaging period. Instead of choosing an imaging period, one can also choose to \"bulk image\", which means to image as fast as possible.</p></html>");
		
		JScrollPane descriptionScrollPane = new JScrollPane(descriptionPane);
		descriptionScrollPane.setPreferredSize(new Dimension(250, 200));
		add(descriptionScrollPane, BorderLayout.CENTER);
		
		// Descriptive image
		ImageIcon measurementIcon = ImageLoadingTools.getResourceIcon("org/youscope/plugin/continousimaging/images/continous-imaging.jpg", "Continous Imaging Measurement");
		if(measurementIcon != null)
		{
			JPanel imagePanel = new JPanel(new BorderLayout());
			imagePanel.setOpaque(false);
			JLabel imageLabel = new JLabel(measurementIcon, SwingConstants.LEFT);
			imageLabel.setBackground(Color.WHITE);
			imageLabel.setOpaque(true);
			imageLabel.setBorder(new LineBorder(Color.BLACK, 1));
			JLabel legendLabel = new JLabel("<html><b>Figure 1:</b>Picture of a microscope.</html>", SwingConstants.LEFT);
			imagePanel.add(imageLabel, BorderLayout.CENTER);
			imagePanel.add(legendLabel, BorderLayout.SOUTH);
			add(imagePanel, BorderLayout.WEST);
		}
	}
}
