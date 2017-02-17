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
package org.youscope.plugin.lifecelldetection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import org.youscope.addon.celldetection.CellVisualizationConfiguration;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.SingleComponentDefinitionPanel;

class VisualizationAlgorithmConfigurationPanel  extends DynamicPanel {

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 1441062694566624365L;

	private final JCheckBox								createVisualizationImageField			= new JCheckBox("Create Detection Visualization Image", true);
	private final SingleComponentDefinitionPanel<CellVisualizationConfiguration> visualizationConfigurationField;
	public VisualizationAlgorithmConfigurationPanel(CellDetectionJobConfiguration configuration, YouScopeClient client, final YouScopeFrame parentFrame) 
	{
		add(createVisualizationImageField);
		visualizationConfigurationField = new SingleComponentDefinitionPanel<CellVisualizationConfiguration>(CellVisualizationConfiguration.class, configuration.getVisualizationAlgorithmConfiguration(), client, parentFrame);
		visualizationConfigurationField.setLabel("Cell visualization algorithm:");
		createVisualizationImageField.setOpaque(false);
		createVisualizationImageField.setSelected(configuration.getVisualizationAlgorithmConfiguration() != null);
		if(configuration.getVisualizationAlgorithmConfiguration() == null)
			visualizationConfigurationField.setVisible(false);
		addFill(visualizationConfigurationField);
		addFillEmpty();
		createVisualizationImageField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				boolean selected = createVisualizationImageField.isSelected();
				visualizationConfigurationField.setVisible(selected);
				parentFrame.pack();
			}
		});
	}
	public void commitChanges(CellDetectionJobConfiguration configuration)
	{
		if(createVisualizationImageField.isSelected())
			configuration.setVisualizationAlgorithmConfiguration(visualizationConfigurationField.getConfiguration());
	}
}
