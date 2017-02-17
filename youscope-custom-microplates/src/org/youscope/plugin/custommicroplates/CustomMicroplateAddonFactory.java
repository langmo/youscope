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

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactory;
import org.youscope.addon.component.ComponentAddonUI;
import org.youscope.addon.component.ComponentMetadata;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.Component;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.serverinterfaces.ConstructionContext;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 *
 */
public class CustomMicroplateAddonFactory implements ComponentAddonFactory
{
	@Override
	public ComponentAddonUI<?> createComponentUI(String typeIdentifier, YouScopeClient client,
			YouScopeServer server) throws AddonException 
	{
		if(!isSupportingTypeIdentifier(typeIdentifier))
				throw new AddonException("Factory does not support creation of components with ID " + typeIdentifier+".");
		
		CustomMicroplateConfiguration microplateConfiguration = new CustomMicroplateConfiguration();
		String name = CustomMicroplateManager.getCustomMicroplateName(typeIdentifier);
		microplateConfiguration.setCustomMicroplateName(name);
		CustomMicroplateConfigurationAddon addon = new CustomMicroplateConfigurationAddon(client, server, typeIdentifier);
		try
		{
			addon.setConfiguration(microplateConfiguration);
		}
		catch(ConfigurationException e)
		{
			throw new AddonException("Could not initialize custom microplate configuration.", e);
		}
		return addon;
	}

	@Override
	public String[] getSupportedTypeIdentifiers() {
		return CustomMicroplateManager.getCustomMicroplateTypeIdentifiers();
	}

	@Override
	public boolean isSupportingTypeIdentifier(String typeIdentifier) {
		for(String addonID : getSupportedTypeIdentifiers())
		{
			if(addonID.equals(typeIdentifier))
				return true;
		}
		return false;
	}

	@Override
	public ComponentMetadata<?> getComponentMetadata(String typeIdentifier) throws AddonException {
		if(!isSupportingTypeIdentifier(typeIdentifier))
		{
			throw new AddonException("Factory does not support creation of measurement components with ID " + typeIdentifier+".");
		}
		return CustomMicroplateManager.getMetadata(typeIdentifier);
	}

	@Override
	public Component createComponent(PositionInformation positionInformation, Configuration configuration,
			ConstructionContext constructionContext) throws ConfigurationException, AddonException 
	{
		if(positionInformation == null)
			throw new AddonException("Position information is null");
		else if(configuration == null)
			throw new AddonException("Configuration is null");
		else if(constructionContext == null)
			throw new AddonException("Construction context is null");
		else if(!isSupportingTypeIdentifier(configuration.getTypeIdentifier()))
			throw new AddonException("Configuration with type identifier " + configuration.getTypeIdentifier() + " not supported by this factory.");
		else if(!(configuration instanceof CustomMicroplateConfiguration))
			throw new AddonException("Configuration with type identifier " + configuration.getTypeIdentifier() + " has class " + configuration.getClass().getName()+", which is not of class " + CustomMicroplateConfiguration.class.getName() + " which is expected.");
		
		try {
			return new CustomRectangularMicroplateResource(positionInformation, configuration.getTypeIdentifier());
		} catch (RemoteException e) {
			throw new AddonException("Remote Exception while creating resource.", e);
		}
	}
}
