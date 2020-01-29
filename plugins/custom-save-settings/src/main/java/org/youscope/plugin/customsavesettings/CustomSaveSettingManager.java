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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.youscope.addon.ConfigurationManagement;

/**
 * Helper class to load and unload custom save setting types.
 * @author Moritz Lang
 *
 */
public class CustomSaveSettingManager
{
	private static final String CUSTOM_SAVE_SETTING_FOLDER_NAME = "configuration/custom_save_settings";
	private static final String CUSTOM_SAVE_SETTING_TYPE_IDENTIFIER_PREFIX = "YouScope.CustomSaveSetting.";
	private static final String CUSTOM_SAVE_SETTING_FILE_ENDING = ".xml";
	private static String[] customSaveSettingNames = null;
	
	static String getCustomSaveSettingTypeIdentifier(String customTypeName)
	{
		return CUSTOM_SAVE_SETTING_TYPE_IDENTIFIER_PREFIX + customTypeName;
	}
	static String getCustomSaveSettingName(String typeIdentifier)
	{
		return typeIdentifier.substring(CUSTOM_SAVE_SETTING_TYPE_IDENTIFIER_PREFIX.length());
	}
	static String getCustomSaveSettingFileName(String customTypeName)
	{
		return customTypeName+CUSTOM_SAVE_SETTING_FILE_ENDING;
	}
	
	/**
	 * Returns the names of all available custom save setting types.
	 * @return Names of custom save setting types.
	 */
	public static synchronized String[] getCustomSaveSettingNames()
	{
		if(customSaveSettingNames != null)
			return customSaveSettingNames;
		File folder = new File(CUSTOM_SAVE_SETTING_FOLDER_NAME);
		if(!folder.exists() || !folder.isDirectory())
		{
			return new String[0];
		}
		File[] xmlFiles = folder.listFiles(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String name) {
		        return (name.endsWith(CUSTOM_SAVE_SETTING_FILE_ENDING));
		    }

		});
		customSaveSettingNames = new String[xmlFiles.length];
		for(int i=0; i<xmlFiles.length; i++)
		{
			customSaveSettingNames[i] = xmlFiles[i].getName();
			customSaveSettingNames[i] = customSaveSettingNames[i].substring(0, customSaveSettingNames[i].length()-CUSTOM_SAVE_SETTING_FILE_ENDING.length());
		}
		return customSaveSettingNames;
	}
	/**
	 * Deletes a custom save setting type.
	 * @param saveSettingName Name of type to delete.
	 * @return true if successful.
	 */
	public static synchronized boolean deleteCustomSaveSettingType(String saveSettingName)
	{
		File folder = new File(CUSTOM_SAVE_SETTING_FOLDER_NAME);
		if(!folder.exists() || !folder.isDirectory())
		{
			return true;
		}
		File file = new File(folder, getCustomSaveSettingFileName(saveSettingName));
		if(!file.exists())
		{
			return true;
		}		
		boolean success = file.delete();
		customSaveSettingNames = null; // enforce reloading.
		return success;
	}
	/**
	 * Deletes the custom save settings type.
	 * @param customSaveSettingType Type to delete.
	 * @return true if successful.
	 */
	public static boolean deleteCustomSaveSettingType(CustomSaveSettingType customSaveSettingType)
	{
		return deleteCustomSaveSettingType(customSaveSettingType.getSaveSettingName());
	}
	/**
	 * Saves the custom save setting type
	 * @param customSaveSettingType Type to save.
	 * @return true if successful.
	 * @throws CustomSaveSettingException
	 */
	public static synchronized boolean saveCustomSaveSettingType(CustomSaveSettingType customSaveSettingType) throws CustomSaveSettingException
	{	
		File folder = new File(CUSTOM_SAVE_SETTING_FOLDER_NAME);
		if(!folder.exists() || !folder.isDirectory())
		{
			boolean result = folder.mkdirs();
			if(!result)
			{
				throw new CustomSaveSettingException("Custom save setting type folder could not be created. Check if YouScope has sufficients rights to create sub-folders in the YouScope directory.");
			}
		}
		
		try {
			ConfigurationManagement.saveConfiguration(new File(folder, getCustomSaveSettingFileName(customSaveSettingType.getSaveSettingName())).toString(), customSaveSettingType);
		} catch(IOException e)
		{
			throw new CustomSaveSettingException("Could not save custom save setting to file system.", e);
		}

		customSaveSettingNames = null; // enforce reloading.
		return true;
	}
	/**
	 * Returns the custom save setting type with the given name.
	 * @param saveSettingName Name of save setting type to load.
	 * @return Loaded save setting type with given name.
	 * @throws CustomSaveSettingException
	 */
	public static synchronized CustomSaveSettingType getCustomSaveSettingType(String saveSettingName) throws CustomSaveSettingException
	{
		File folder = new File(CUSTOM_SAVE_SETTING_FOLDER_NAME);
		if(!folder.exists() || !folder.isDirectory())
		{
			throw new CustomSaveSettingException("Custom save setting folder does not exist, thus, custom save setting could not be localized.");
		}
		File xmlFile = new File(folder, getCustomSaveSettingFileName(saveSettingName));
		if(!xmlFile.exists())
		{
			throw new CustomSaveSettingException("Custom save setting with name "+saveSettingName+" does not exist.");
		}
		try
		{
			return (CustomSaveSettingType) ConfigurationManagement.loadConfiguration(xmlFile.toString());
		}
		catch(Throwable e)
		{
			throw new CustomSaveSettingException("Could not load custom save setting.", e);
		}
	}
}
