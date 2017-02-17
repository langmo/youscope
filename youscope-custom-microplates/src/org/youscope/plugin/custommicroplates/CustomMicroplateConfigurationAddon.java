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

import java.awt.Component;

import javax.swing.JPanel;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 */
class CustomMicroplateConfigurationAddon extends ComponentAddonUIAdapter<CustomMicroplateConfiguration>
{
	private final String typeIdentifier;
	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @param typeIdentifier Type identifier of custom microplate.
	 * @throws AddonException 
	 */
	public CustomMicroplateConfigurationAddon(YouScopeClient client, YouScopeServer server, String typeIdentifier) throws AddonException
	{
		super(CustomMicroplateManager.getMetadata(typeIdentifier),  client, server);
		this.typeIdentifier = typeIdentifier;
	}
        
    @Override
	protected Component createUI(CustomMicroplateConfiguration configuration) throws AddonException
	{
		setTitle(CustomMicroplateManager.getCustomMicroplateName(typeIdentifier));
		setResizable(true);
		setMaximizable(false);
        JPanel panel = new JPanel();
        panel.setOpaque(false);
		return panel;
    }

	@Override
	protected void commitChanges(CustomMicroplateConfiguration configuration) {
		// do nothing.
	}

	@Override
	protected void initializeDefaultConfiguration(CustomMicroplateConfiguration configuration) throws AddonException {
		// do nothing.
	}
}
