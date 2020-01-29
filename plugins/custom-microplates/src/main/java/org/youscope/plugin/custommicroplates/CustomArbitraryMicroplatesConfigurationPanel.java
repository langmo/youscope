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
package org.youscope.plugin.custommicroplates;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.youscope.addon.AddonException;
import org.youscope.addon.microplate.MicroplateWellSelectionUI;
import org.youscope.addon.microplate.MicroplateWellSelectionUI.SelectionMode;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;

/**
 * @author Moritz Lang
 *
 */
class CustomArbitraryMicroplatesConfigurationPanel extends DynamicPanel
{
	
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -2178244063371000571L;

	private JTextField microplateNameField 									= new JTextField();
		
	private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>(1);
	private CustomArbitraryMicroplateDefinition microplateDefinition;
	private final MicroplateWellSelectionUI microplateUI;
	private final boolean edit;
	private final WellTable wellTable;
	CustomArbitraryMicroplatesConfigurationPanel(YouScopeClient client, final YouScopeServer server, YouScopeFrame frame)
	{
		this(client, server, frame, null);
	}
	CustomArbitraryMicroplatesConfigurationPanel(final YouScopeClient client, final YouScopeServer server, final YouScopeFrame frame, CustomArbitraryMicroplateDefinition microplateDefinition)
	{
		if(microplateDefinition == null)
		{
			edit = false;
			microplateDefinition = new CustomArbitraryMicroplateDefinition();
			this.microplateDefinition = microplateDefinition;
		}
		else
		{
			edit = true;
			this.microplateDefinition = (CustomArbitraryMicroplateDefinition) microplateDefinition.clone();
		}
		
		microplateUI = new MicroplateWellSelectionUI(client, server);
		microplateUI.setSelectionMode(SelectionMode.NONE);
		try {
			addFill(microplateUI.toPanel(frame));
		} catch (AddonException e2) {
			client.sendError("Could not add visualization of microplate.", e2);
		}
		add(new JLabel("Name of microplate type:"));
		add(microplateNameField);
		
		wellTable = new WellTable(microplateDefinition.getWellLayouts(), frame, client, server);
		wellTable.addLayoutChangedListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				CustomArbitraryMicroplatesConfigurationPanel.this.microplateDefinition.setWellLayouts(wellTable.getWellLayouts());
				updateMicroplateUI();
			}
		});
		add(new JLabel("Wells:"));
		addFill(wellTable);
        
		JButton closeButton = new JButton("Save");
		closeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	String microplateName = microplateNameField.getText();
                	if(microplateName.length()<3)
                	{
                		JOptionPane.showMessageDialog(null, "Microplate Description must be longer than three characters.", "Could not initialize microplate type", JOptionPane.INFORMATION_MESSAGE);
                		return;
                	}
                	if(edit)
                	{
                		String typeIdentifier = CustomMicroplateManager.getCustomMicroplateTypeIdentifier(CustomArbitraryMicroplatesConfigurationPanel.this.microplateDefinition.getCustomMicroplateName());
                		if(!CustomMicroplateManager.deleteCustomMicroplate(typeIdentifier))
                			client.sendError("Could not delete old microplate type definition.", null);
                	}
                	CustomArbitraryMicroplatesConfigurationPanel.this.microplateDefinition.setCustomMicroplateName(microplateName);
               	
                	try
					{
						CustomMicroplateManager.saveCustomMicroplate(CustomArbitraryMicroplatesConfigurationPanel.this.microplateDefinition);
					}
					catch(CustomMicroplateException e1)
					{
						client.sendError("Could not save microplate type definition.", e1);
						return;
					}
                	
                    frame.setVisible(false); 
                    for(ActionListener listener : listeners)
                    {
                    	listener.actionPerformed(new ActionEvent(this, 155, "Microplate type created or edited."));
                    }
                }
            });
		add(closeButton);
		
		// Load data
        microplateNameField.setText(microplateDefinition.getCustomMicroplateName());
        updateMicroplateUI();
        
	}
	private void updateMicroplateUI()
	{
		microplateUI.setMicroplateLayout(microplateDefinition);
	}
	public void addActionListener(ActionListener listener)
	{
		listeners.add(listener);
	}
	public void removeActionListener(ActionListener listener)
	{
		listeners.remove(listener);
	}
}
