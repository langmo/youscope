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

import java.io.File;
import java.rmi.RemoteException;

import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.resource.ResourceAdapter;
import org.youscope.common.resource.ResourceException;
import org.youscope.common.saving.FileNameMacroConverter;
import org.youscope.common.saving.SaveInformation;
import org.youscope.common.saving.SaveSettings;

/**
 * Standard save settings, e.g. the definition where images should be stored under which name.
 * @author mlang
 *
 */
public class CustomSaveSetting extends ResourceAdapter<CustomSaveSettingConfiguration> implements SaveSettings
{
	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 3381618438101487879L;
	private CustomSaveSettingType saveSettingType = null;
	/**
	 * Constructor.
	 * @param positionInformation Position information.
	 * @param configuration configuration of the save settings.
	 * @throws ConfigurationException
	 * @throws RemoteException 
	 */
	public CustomSaveSetting(PositionInformation positionInformation, CustomSaveSettingConfiguration configuration)
					throws ConfigurationException, RemoteException {
		super(positionInformation, configuration, configuration.getTypeIdentifier(), CustomSaveSettingConfiguration.class, "custom save settings");
	}
	
	@Override
	public void initialize(MeasurementContext measurementContext) throws ResourceException, RemoteException
	{
		if(saveSettingType == null)
			throw new ResourceException("No save setting type set for custom save setting "+getName()+".");
		super.initialize(measurementContext);
	}

	@Override
	public String getMeasurementConfigurationFilePath(SaveInformation saveInformation) throws ResourceException {
		assertInitialized();
		return FileNameMacroConverter.convertGeneralPath(saveSettingType.getMeasurementConfigurationFilePath(), saveInformation);
	}

	@Override
	public String getMicroscopeConfigurationFilePath(SaveInformation saveInformation) throws ResourceException {
		assertInitialized();
		return FileNameMacroConverter.convertGeneralPath(saveSettingType.getMicroscopeConfigurationFilePath(), saveInformation);
	}

	@Override
	public String getLogErrFilePath(SaveInformation saveInformation) throws ResourceException {
		assertInitialized();
		return FileNameMacroConverter.convertGeneralPath(saveSettingType.getLogErrFilePath(), saveInformation);
	}

	@Override
	public String getLogOutFilePath(SaveInformation saveInformation) throws ResourceException {
		assertInitialized();
		return FileNameMacroConverter.convertGeneralPath(saveSettingType.getLogOutFilePath(), saveInformation);
	}

	@Override
	public String getMeasurementBasePath(SaveInformation saveInformation) throws ResourceException {
		assertInitialized();
		String baseFolder = saveSettingType.getBaseFolder();
		if(baseFolder == null)
			baseFolder = getConfiguration().getBaseFolder();
		return baseFolder + File.separator + FileNameMacroConverter.convertGeneralPath(saveSettingType.getBaseFolderExtension(), saveInformation);
	}

	@Override
	public String getTableFilePath(SaveInformation saveInformation, String tableName) throws ResourceException {
		assertInitialized();
		return FileNameMacroConverter.convertTablePath(saveSettingType.getTableFilePath(), tableName, saveInformation);
	}

	@Override
	public String getImageMetadataTableFilePath(SaveInformation saveInformation) throws ResourceException {
		assertInitialized();
		return FileNameMacroConverter.convertGeneralPath(saveSettingType.getImageMetadataTableFilePath(), saveInformation);
	}

	@Override
	public String getImageFilePath(SaveInformation saveInformation, ImageEvent<?> event, String imageName)  throws ResourceException
	{
		assertInitialized();
		return FileNameMacroConverter.convertImagePath(saveSettingType.getImageFilePath(), imageName, event, saveInformation);
	}

	@Override
	public String getImageExtension(SaveInformation saveInformation, ImageEvent<?> event, String imageName) throws ResourceException {
		assertInitialized();
		return saveSettingType.getImageExtension();
	}

	/**
	 * Returns the save setting type of this save setting configuration.
	 * @return Save setting type.
	 */
	public CustomSaveSettingType getSaveSettingType() {
		return saveSettingType;
	}

	/**
	 * Sets the save setting type of this save setting configuration
	 * @param saveSettingType Save setting type of this configuration.
	 * @throws ResourceException 
	 * @throws RemoteException 
	 */
	public void setSaveSettingType(CustomSaveSettingType saveSettingType) throws ResourceException, RemoteException {
		if(isInitialized())
			throw new ResourceException("Uninitialize resource before modifying it.");
		this.saveSettingType = saveSettingType;
	}

	@Override
	public String getXMLInformationFilePath(SaveInformation saveInformation) throws ResourceException, RemoteException {
		assertInitialized();
		return FileNameMacroConverter.convertGeneralPath(saveSettingType.getXMLInformationFilePath(), saveInformation);
	}

	@Override
	public String getHTMLInformationFilePath(SaveInformation saveInformation)
			throws ResourceException, RemoteException {
		assertInitialized();
		return FileNameMacroConverter.convertGeneralPath(saveSettingType.getHTMLInformationFilePath(), saveInformation);
	}
	
}
