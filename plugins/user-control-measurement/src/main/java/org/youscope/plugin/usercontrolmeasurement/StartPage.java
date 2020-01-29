/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.youscope.plugin.usercontrolmeasurement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.Icon;
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
 * @author langmo
 *
 */
class StartPage extends MeasurementAddonUIPage<UserControlMeasurementConfiguration>
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -5407788842391715831L;

	@Override
	public void loadData(UserControlMeasurementConfiguration configuration)
	{
		// Do nothing.
	}

	@Override
	public boolean saveData(UserControlMeasurementConfiguration configuration)
	{
		// Do nothing.
		return true;
	}

	@Override
	public void setToDefault(UserControlMeasurementConfiguration configuration)
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
				"<p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">A user control measurement is a measurement in which the current microscope image is displayed to the user. The user can interactively choose the channel and the exposure. By user activation, the currently displayed image is stored on the hard-disk.</p>" +
				"<p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">The principal idea is that the user can interactively manually choose the position of the stage and adjust the focus. It is thus possible to quickly take several images at different positions, without requiring the overhead of configuring a more complex measurement type</p>" +
				"<p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">However, it is recommended not to use this measurement type exhaustively, and to rather use the more precise other measurement types.</p>" +
				"<p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">This measurement type incorporates an stage monitoring possibility to automatically detect if images were taken at the same position or not. This optional feature then automatically adjusts the file names of the stored images, such that they are easier to identify lateron.</p></html>");
		JScrollPane descriptionScrollPane = new JScrollPane(descriptionPane);
		descriptionScrollPane.setPreferredSize(new Dimension(250, 200));
		add(descriptionScrollPane, BorderLayout.CENTER);
		
		// Descriptive image
		Icon microplateMeasurementIcon = ImageLoadingTools.getResourceIcon("org/youscope/plugin/usercontrolmeasurement/images/usercontrolmeasurement.jpg", "User Control Measurement");
		if(microplateMeasurementIcon != null)
		{
			JPanel imagePanel = new JPanel(new BorderLayout());
			imagePanel.setOpaque(false);
			JLabel imageLabel = new JLabel(microplateMeasurementIcon, SwingConstants.LEFT);
			imageLabel.setBackground(Color.WHITE);
			imageLabel.setOpaque(true);
			imageLabel.setBorder(new LineBorder(Color.BLACK, 1));
			JLabel legendLabel = new JLabel("<html><b>Figure 1:</b> User Control Measurement.</html>", SwingConstants.LEFT);
			imagePanel.add(imageLabel, BorderLayout.CENTER);
			imagePanel.add(legendLabel, BorderLayout.SOUTH);
			add(imagePanel, BorderLayout.WEST);
		}
	}

}
