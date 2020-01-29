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
package org.youscope.plugin.waitforuser;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;

/**
 * @author Moritz Lang
 */
class WaitForUserJobConfigurationAddon extends ComponentAddonUIAdapter<WaitForUserJobConfiguration>
{
	private JTextArea							messageField				= new JTextArea(6, 30);
	
	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws AddonException 
	 */
	public WaitForUserJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(),  client, server);
	}
    
	static ComponentMetadataAdapter<WaitForUserJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<WaitForUserJobConfiguration>(WaitForUserJobConfiguration.TYPE_IDENTIFIER, 
				WaitForUserJobConfiguration.class, 
				WaitForUserJob.class, 
				"Wait for user", 
				new String[]{"misc"}, 
				"Displays a message in form of a dialog to the user, and waits until the user acknowledges the dialog. Useful to schedule manual user interventions during a measurement.",
				"icons/user--exclamation.png");
	}

	@Override
	protected Component createUI(WaitForUserJobConfiguration configuration) throws AddonException
	{
		setTitle("Wait For User");
		setResizable(true);
		setMaximizable(false);
		
		DynamicPanel elementsPanel = new DynamicPanel();
		elementsPanel.add(new JLabel("User message:"));
		elementsPanel.add(new JScrollPane(messageField));

		// Load state
		messageField.setText(configuration.getMessage());

		return elementsPanel;
	}

	@Override
	protected void commitChanges(WaitForUserJobConfiguration configuration) {
		configuration.setMessage(messageField.getText());
	}

	@Override
	protected void initializeDefaultConfiguration(WaitForUserJobConfiguration configuration) throws AddonException {
		// do nothing.
	}
}
