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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.youscope.addon.AddonException;
import org.youscope.addon.microplate.MicroplateWellSelectionUI;
import org.youscope.addon.microplate.MicroplateWellSelectionUI.SelectionMode;
import org.youscope.addon.microplate.RectangularMicroplateLayout;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DoubleTextField;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.IntegerTextField;

/**
 * @author Moritz Lang
 *
 */
class CustomRectangularMicroplatesConfigurationPanel extends DynamicPanel
{
	
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 6284710357242774082L;

	private DoubleTextField					widthField					= new DoubleTextField();

	private DoubleTextField					heightField					= new DoubleTextField();

	private IntegerTextField					numXField					= new IntegerTextField();

	private IntegerTextField					numYField					= new IntegerTextField();

	private JTextField microplateNameField 									= new JTextField();
		
	private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>(1);
	private CustomRectangularMicroplateDefinition microplateDefinition;
	private final MicroplateWellSelectionUI microplateUI;
	CustomRectangularMicroplatesConfigurationPanel(YouScopeClient client, final YouScopeServer server, YouScopeFrame frame)
	{
		this(client, server, frame, null);
	}
	CustomRectangularMicroplatesConfigurationPanel(final YouScopeClient client, final YouScopeServer server, final YouScopeFrame frame, CustomRectangularMicroplateDefinition microplateDefinition)
	{
		if(microplateDefinition == null)
			microplateDefinition = new CustomRectangularMicroplateDefinition();
		this.microplateDefinition = microplateDefinition;
		microplateUI = new MicroplateWellSelectionUI(client, server);
		microplateUI.setSelectionMode(SelectionMode.NONE);
		try {
			addFill(microplateUI.toPanel(frame));
		} catch (AddonException e2) {
			client.sendError("Could not add visualization of microplate.", e2);
		}
		add(new JLabel("Name of microplate type:"));
		add(microplateNameField);
		add(new JLabel("Number of wells horizontal/vertical:"));
        JPanel numberPanel = new JPanel(new GridLayout(1, 2, 0, 3));
        numXField.setMinimalValue(1);
        numberPanel.add(numXField);
        numYField.setMinimalValue(1);
        numberPanel.add(numYField);
        add(numberPanel);
        add(new JLabel("Distance wells horizontal/vertical (in um):"));
        JPanel distancePanel = new JPanel(new GridLayout(1, 2, 0, 3));
        widthField.setMinimalValue(Double.MIN_NORMAL*10);
        distancePanel.add(widthField);
        heightField.setMinimalValue(Double.MIN_NORMAL*10);
        distancePanel.add(heightField);
        add(distancePanel);
        
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
                	CustomRectangularMicroplateDefinition newDefinition = new CustomRectangularMicroplateDefinition();
                	newDefinition.setNumWellsX(numXField.getValue());
                	newDefinition.setNumWellsY(numYField.getValue());
                	newDefinition.setWellWidth(widthField.getValue());
                	newDefinition.setWellHeight(heightField.getValue());
                	newDefinition.setCustomMicroplateName(microplateName);
                	if(CustomRectangularMicroplatesConfigurationPanel.this.microplateDefinition != null)
                	{
                		String typeIdentifier = CustomMicroplateManager.getCustomMicroplateTypeIdentifier(CustomRectangularMicroplatesConfigurationPanel.this.microplateDefinition.getCustomMicroplateName());
                		if(!CustomMicroplateManager.deleteCustomMicroplate(typeIdentifier))
                			client.sendError("Could not delete old microplate type definition.", null);
                	}
                	CustomRectangularMicroplatesConfigurationPanel.this.microplateDefinition = newDefinition;
               	
                	try
					{
						CustomMicroplateManager.saveCustomMicroplate(newDefinition);
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
        widthField.setValue(microplateDefinition.getWellWidth());
        heightField.setValue(microplateDefinition.getWellHeight());
        numXField.setValue(microplateDefinition.getNumWellsX());
        numYField.setValue(microplateDefinition.getNumWellsY());
        updateMicroplateUI();
        
        ActionListener dynamicChangeListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				updateMicroplateUI();
			}
		};
		widthField.addActionListener(dynamicChangeListener);
        heightField.addActionListener(dynamicChangeListener);
        numXField.addActionListener(dynamicChangeListener);
        numYField.addActionListener(dynamicChangeListener);
	}
	private void updateMicroplateUI()
	{
		microplateUI.setMicroplateLayout(new RectangularMicroplateLayout(numXField.getValue(), numYField.getValue(), widthField.getValue(), heightField.getValue()));
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
