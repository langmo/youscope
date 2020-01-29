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
package org.youscope.plugin.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.uielements.DynamicPanel;

class MiscConfigurationPanel extends DynamicPanel
{

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 3734765053405836997L;
	private final JLabel								controllerTableLabel				= new JLabel("Controller-table file name (without extension):");
	private final JTextField							controllerTableField				= new JTextField();
	private final JCheckBox								saveControllerTableField			= new JCheckBox("Save controller-table", true);
    
	MiscConfigurationPanel(ControllerJobConfiguration controllerConfiguration, final YouScopeFrame frame)
	{
		saveControllerTableField.setOpaque(false);
		add(saveControllerTableField);
		add(controllerTableLabel);
		add(controllerTableField);
		addFillEmpty();
		saveControllerTableField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				boolean selected = saveControllerTableField.isSelected();
				
				controllerTableLabel.setVisible(selected);
				controllerTableField.setVisible(selected);
				frame.pack();
				
			}
		});
		
		// load settings
		String controllerTableName = controllerConfiguration.getControllerTableSaveName();
		if(controllerTableName == null)
		{
			controllerTableField.setText("controller-table");
			saveControllerTableField.setSelected(false);
			controllerTableField.setVisible(false);
			controllerTableLabel.setVisible(false);
		}
		else
		{
			if (controllerTableName.length() < 1)
			{
				controllerTableName = "controller-table";
			}
			controllerTableField.setText(controllerTableName);
			saveControllerTableField.setSelected(true);
			controllerTableField.setVisible(true);
			controllerTableLabel.setVisible(true);
		}
	}
	
	void commitChanges(ControllerJobConfiguration configuration)
	{
		configuration.setControllerTableSaveName(saveControllerTableField.isSelected() ? controllerTableField.getText() : null);
	}
}
