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
import org.youscope.addon.focusscore.FocusScoreConfiguration;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.plugin.simplefocusscores.VarianceFocusScoreConfiguration;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.ComponentComboBox;
import org.youscope.uielements.DynamicPanel;

class FocusScoreAlgorithmPanel extends JPanel
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -7771369551694828424L;
	
	private final ComponentComboBox<FocusScoreConfiguration>	focusScoreAlgorithmField;
	private Component addonConfigurationPanel = null;
	private final YouScopeClient client; 
	private final YouScopeFrame frame;
	private ComponentAddonUI<? extends FocusScoreConfiguration> currentAddon = null;
	private final FocusScoreConfiguration lastConfiguration;
	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @param frame 
	 * @param focusScoreConfiguration 
	 */
	public FocusScoreAlgorithmPanel(YouScopeClient client, YouScopeServer server, YouScopeFrame frame, FocusScoreConfiguration focusScoreConfiguration)
	{
		super(new BorderLayout());
		this.client = client;
		this.frame = frame;
		this.lastConfiguration = 		focusScoreConfiguration;
		
		setOpaque(false);
		String selectedConfigurationID;
		if(focusScoreConfiguration != null)
			selectedConfigurationID = focusScoreConfiguration.getTypeIdentifier();
		else
			selectedConfigurationID = VarianceFocusScoreConfiguration.CONFIGURATION_ID;
		focusScoreAlgorithmField = new ComponentComboBox<FocusScoreConfiguration>(client, FocusScoreConfiguration.class);
		focusScoreAlgorithmField.setSelectedElement(selectedConfigurationID);
		
		DynamicPanel topPanel = new DynamicPanel();
		topPanel.add(new JLabel("Focus Score Algorithm:"));
		topPanel.add(focusScoreAlgorithmField);
		add(topPanel, BorderLayout.NORTH);
		
		displayAddonConfiguration(focusScoreAlgorithmField.getSelectedElement());
		focusScoreAlgorithmField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) {
				displayAddonConfiguration(focusScoreAlgorithmField.getSelectedElement());
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
	public FocusScoreConfiguration getConfiguration()
	{
		if(currentAddon == null)
			return null;
		return currentAddon.getConfiguration();
	}
	private void displayAddonConfiguration(ComponentMetadata<? extends FocusScoreConfiguration> metadata)
	{
		if(currentAddon != null)
		{
			ComponentMetadata<?> currentmetadata = currentAddon.getAddonMetadata();
			if(currentmetadata!= null && metadata != null && currentmetadata.getTypeIdentifier().equals(metadata.getTypeIdentifier()))
				return;
		}
		if(addonConfigurationPanel != null)
			remove(addonConfigurationPanel);
		if(metadata == null)
		{
			addonConfigurationPanel = createErrorUI("No focus score algorithms available.", null);
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
			addonConfigurationPanel = createErrorUI("Error loading focus score configuration UI.", e);
			add(addonConfigurationPanel, BorderLayout.CENTER);
			client.sendError("Error loading focus score configuration UI.",  e);
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
