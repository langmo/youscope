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
package org.youscope.plugin.customsavesettings;


import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
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
 * Factory for standard save settings.
 * @author Moritz Lang
 */
public class CustomSaveSettingFactory extends ComponentAddonFactoryAdapter
{
	
	private CustomSaveSettingType getType(String typeIdentifier) throws AddonException 
	{
		String name = CustomSaveSettingManager.getCustomSaveSettingName(typeIdentifier);
		try {
			return CustomSaveSettingManager.getCustomSaveSettingType(name);
		} catch (CustomSaveSettingException e) {
			throw new AddonException("Custom save setting with type identifier "+typeIdentifier+" not supported by this addon.", e);
		}
	}
	
	@Override
	public ComponentAddonUI<?> createComponentUI(String typeIdentifier, YouScopeClient client,
			YouScopeServer server) throws AddonException 
	{
		CustomSaveSettingType type = getType(typeIdentifier);
		CustomSaveSettingConfigurationUI addon = new CustomSaveSettingConfigurationUI(client, server, type);
		return addon;
	}

	@Override
	public String[] getSupportedTypeIdentifiers() {
		String[] customTypeNames = CustomSaveSettingManager.getCustomSaveSettingNames();
		String[] returnVal = new String[customTypeNames.length];
		for(int i=0; i<customTypeNames.length; i++)
		{
			returnVal[i] = CustomSaveSettingManager.getCustomSaveSettingTypeIdentifier(customTypeNames[i]);
		}
		
		return returnVal;
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
		return CustomSaveSettingConfigurationUI.getMetadata(CustomSaveSettingManager.getCustomSaveSettingName(typeIdentifier));
	}

	@Override
	public Component createComponent(PositionInformation positionInformation, Configuration configuration,
			ConstructionContext constructionContext) throws ConfigurationException, AddonException 
	{
		if(configuration == null)
			throw new AddonException("Configuration is null");
		else if(constructionContext == null)
			throw new AddonException("Construction context is null");
		else if(!(configuration instanceof CustomSaveSettingConfiguration))
			throw new AddonException("Configuration with type identifier " + configuration.getTypeIdentifier() + " has class " + configuration.getClass().getName()+", which is not of class " + CustomSaveSettingConfiguration.class.getName() + " which is expected.");
		CustomSaveSettingType type = getType(configuration.getTypeIdentifier());
		CustomSaveSetting saveSetting;
		try {
			saveSetting = new CustomSaveSetting(positionInformation, (CustomSaveSettingConfiguration)configuration);
			saveSetting.setSaveSettingType(type);
		} catch (Exception e) {
			throw new AddonException("Could not set custom save setting type.", e);
		} 
		
		return saveSetting;
	}
}
