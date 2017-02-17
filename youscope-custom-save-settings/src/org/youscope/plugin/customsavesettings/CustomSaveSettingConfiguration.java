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
package org.youscope.plugin.customsavesettings;

import org.youscope.common.saving.MeasurementSaver;
import org.youscope.common.saving.SaveSettingsConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Configuration of a standard folder structure to save measurements.
 * @author mlang
 *
 */
public class CustomSaveSettingConfiguration  extends SaveSettingsConfiguration
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 113104186393312354L;

	/**
	 * Name of save setting type to use.
	 */
	private String customSaveSettingTypeName = "unnamed";
	
	
	/**
	 * The folder where the measurement (information and images made during the measurement) should be stored. Might be ignored by custom save setting type.
	 */
	@XStreamAlias("base-folder")
	private String					baseFolder					= "";

	/**
	 * Returns the folder where all measurements should be saved.
	 * Might be ignored by custom save setting type.
	 * @return the folder of the measurement.
	 */
	public String getBaseFolder()
	{
		return baseFolder;
	}

	/**
	 * Sets the folder where all measurements should be saved.
	 * Additional folders might be created inside this folder when the measurement starts (depending on the save setting type),
	 * which indicate the specific time when the measurement was started. This is done since one
	 * measurement can be started multiple times. To obtain the full path to this subfolder, use the
	 * respective function in {@link MeasurementSaver}. Might be ignored by custom save setting type.
	 * @param folder the folder of the measurement.
	 */
	public void setBaseFolder(String folder)
	{
		this.baseFolder = folder;
	}

	@Override
	public String getTypeIdentifier() {
		return CustomSaveSettingManager.getCustomSaveSettingTypeIdentifier(customSaveSettingTypeName);
	}

	/**
	 * Returns the name of the custom save setting type this configuration refers to.
	 * Must be one of the names returned by {@link CustomSaveSettingManager#getCustomSaveSettingNames()}.
	 * @return Custom save setting type identifier.
	 */
	public String getCustomSaveSettingTypeName() {
		return customSaveSettingTypeName;
	}


	/**
	 * Sets the name of the custom save setting type this configuration refers to.
	 * Must be one of the names returned by {@link CustomSaveSettingManager#getCustomSaveSettingNames()}.
	 * @param customSaveSettingTypeName name of the custom save setting type.
	 */
	public void setCustomSaveSettingTypeName(String customSaveSettingTypeName) {
		this.customSaveSettingTypeName = customSaveSettingTypeName;
	}
}
