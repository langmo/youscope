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
package org.youscope.plugin.autofocus;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.youscope.addon.component.ComponentAddonUI;
import org.youscope.addon.component.ComponentMetadata;
import org.youscope.addon.focussearch.FocusSearchConfiguration;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.plugin.exhaustivefocussearch.ExhaustiveFocusSearchConfiguration;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.ComponentComboBox;
import org.youscope.uielements.DynamicPanel;

class FocusSearchAlgorithmPanel extends JPanel
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -7471369551694828424L;
	
	private final ComponentComboBox<FocusSearchConfiguration>	focusSearchAlgorithmField;
	private Component addonConfigurationPanel = null;
	private final YouScopeClient client; 
	private final YouScopeFrame frame;
	private final FocusSearchConfiguration lastConfiguration;
	private ComponentAddonUI<? extends FocusSearchConfiguration> currentAddon = null;
	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @param frame The YouScope frame this panel is in.
	 * @param focusSearchConfiguration 
	 */
	public FocusSearchAlgorithmPanel(YouScopeClient client, YouScopeServer server, YouScopeFrame frame, FocusSearchConfiguration focusSearchConfiguration)
	{
		super(new BorderLayout());
		this.client = client;
		this.frame = frame;
		this.lastConfiguration = 		focusSearchConfiguration;
		
		setOpaque(false);
		
		String selectedConfigurationID;
		if(focusSearchConfiguration != null)
			selectedConfigurationID = focusSearchConfiguration.getTypeIdentifier();
		else
			selectedConfigurationID = ExhaustiveFocusSearchConfiguration.CONFIGURATION_ID;
		focusSearchAlgorithmField = new ComponentComboBox<FocusSearchConfiguration>(client, FocusSearchConfiguration.class);
		focusSearchAlgorithmField.setSelectedElement(selectedConfigurationID);			
		
		DynamicPanel topPanel = new DynamicPanel();
		topPanel.add(new JLabel("Focus Search Algorithm:"));
		topPanel.add(focusSearchAlgorithmField);
		add(topPanel, BorderLayout.NORTH);
		
		displayAddonConfiguration(focusSearchAlgorithmField.getSelectedElement());
		focusSearchAlgorithmField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) {
				displayAddonConfiguration(focusSearchAlgorithmField.getSelectedElement());
			}
		});
	}
	
	private Component createErrorUI(String message, Exception exception)
	{
		if(exception != null)
			message += "\n\n"+exception.getMessage();
		JTextArea textArea = new JTextArea(message);
		textArea.setEditable(false);
		JPanel errorPane = new JPanel(new BorderLayout());
		errorPane.add(new JLabel("An error occured:"), BorderLayout.NORTH);
		errorPane.add(new JScrollPane(textArea), BorderLayout.CENTER);
		return errorPane;
	}
	public FocusSearchConfiguration getConfiguration()
	{
		if(currentAddon == null)
			return null;
		return currentAddon.getConfiguration();
	}
	private void displayAddonConfiguration(ComponentMetadata<? extends FocusSearchConfiguration> metadata)
	{
		if(currentAddon != null)
		{
			ComponentMetadata<?> currentMetadata = currentAddon.getAddonMetadata();
			if(currentMetadata!= null && metadata != null &&currentMetadata.getTypeIdentifier().equals(metadata.getTypeIdentifier()))
				return;
		}
		if(addonConfigurationPanel != null)
			remove(addonConfigurationPanel);
		if(metadata == null)
		{
			addonConfigurationPanel = createErrorUI("No focus search algorithms available.", null);
			add(addonConfigurationPanel, BorderLayout.CENTER);
			revalidate();
			if(frame.isVisible())
				frame.pack();
			return;
		}
		try 
		{
			currentAddon = client.getAddonProvider().createComponentUI(metadata);
			if(lastConfiguration != null && lastConfiguration.getTypeIdentifier().equals(metadata.getTypeIdentifier()))
			{
				currentAddon.setConfiguration(lastConfiguration);
			}
			
			addonConfigurationPanel = currentAddon.toPanel(frame);
		} 
		catch (Exception e) 
		{
			addonConfigurationPanel = createErrorUI("Error loading focus search configuration UI.", e);
			add(addonConfigurationPanel, BorderLayout.CENTER);
			client.sendError("Error loading focus search configuration UI.",  e);
			revalidate();
			if(frame.isVisible())
				frame.pack();
			return;
		}
		
		add(addonConfigurationPanel, BorderLayout.CENTER);
		revalidate();
		if(frame.isVisible())
			frame.pack();
	}

}
