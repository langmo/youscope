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
package org.youscope.plugin.changepositionjob;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.job.basicjobs.ChangePositionJob;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.StandardFormats;

/**
 * UI for change position jobs.
 * @author Moritz Lang
 */
class ChangePositionJobConfigurationAddon extends ComponentAddonUIAdapter<ChangePositionJobConfiguration>
{

	private JFormattedTextField							xPositionField			= new JFormattedTextField(StandardFormats.getDoubleFormat());

	private JFormattedTextField							yPositionField			= new JFormattedTextField(StandardFormats.getDoubleFormat());

	private JCheckBox										absoluteValueCheckBox	= new JCheckBox("Absolute Value", true);

	ChangePositionJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(),  client, server);
	}
	
	static ComponentMetadataAdapter<ChangePositionJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<ChangePositionJobConfiguration>(ChangePositionJob.DEFAULT_TYPE_IDENTIFIER, 
				ChangePositionJobConfiguration.class, 
				ChangePositionJob.class, 
				"Stage Position", 
				new String[]{"Elementary"},
				"Sets the position of the stage.",
				"icons/map.png");
	}
	
	@Override
	protected Component createUI(ChangePositionJobConfiguration configuration) throws AddonException 
	{
		setTitle("Change Position Job");
		setResizable(false);
		setMaximizable(false);
		
		Point2D.Double currentPosition;
		try
		{
			currentPosition = getServer().getMicroscope().getStageDevice().getPosition();
		}
		catch(Exception e2)
		{
			getClient().sendError("Could not obtain current postion of microscope. Initilizing settings with zero position.", e2);
			currentPosition = new Point2D.Double();
		}

		xPositionField.setValue(currentPosition.x);
		yPositionField.setValue(currentPosition.y);

		DynamicPanel contentPane = new DynamicPanel();

		contentPane.add(new JLabel("X-Position (in micro meter):"));
		contentPane.add(xPositionField);
		contentPane.add(new JLabel("Y-Position (in micro meter):"));
		contentPane.add(yPositionField);
		JButton currentPositionButton = new JButton("Current Position");
		currentPositionButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				Point2D.Double currentPosition;
				try
				{
					currentPosition = getServer().getMicroscope().getStageDevice().getPosition();
				}
				catch(Exception e)
				{
					getClient().sendError("Could not obtain current postion of microscope. Initilizing settings with zero position.", e);
					currentPosition = new Point2D.Double();
				}
				
				xPositionField.setValue(currentPosition.x);
				yPositionField.setValue(currentPosition.y);
			}
		});
		contentPane.add(currentPositionButton);

		contentPane.add(absoluteValueCheckBox);

		xPositionField.setValue(configuration.getX());
		yPositionField.setValue(configuration.getY());

		absoluteValueCheckBox.setSelected(configuration.isAbsolute());
		return contentPane;
	}

	@Override
	protected void commitChanges(ChangePositionJobConfiguration configuration)
	{
		configuration.setX(((Number)xPositionField.getValue()).doubleValue());
		configuration.setY(((Number)yPositionField.getValue()).doubleValue());
		configuration.setAbsolute(absoluteValueCheckBox.isSelected());
		
	}

	@Override
	protected void initializeDefaultConfiguration(ChangePositionJobConfiguration configuration) throws AddonException {
		// do nothing.
	}	
}
