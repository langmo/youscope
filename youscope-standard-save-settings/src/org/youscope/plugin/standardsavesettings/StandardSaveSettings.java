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
package org.youscope.plugin.standardsavesettings;

import java.io.File;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.Well;
import org.youscope.common.resource.ResourceAdapter;
import org.youscope.common.resource.ResourceException;
import org.youscope.common.saving.FileNameMacroConverter;
import org.youscope.common.saving.SaveInformation;
import org.youscope.common.saving.SaveSettings;
import org.youscope.common.util.TextTools;

/**
 * Standard save settings, e.g. the definition where images should be stored under which name.
 * @author mlang
 *
 */
public class StandardSaveSettings extends ResourceAdapter<StandardSaveSettingsConfiguration> implements SaveSettings
{

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -3600016559165429159L;
	private static final String FILE_NAME_MACRO = "%N_position%4w%2p_time%4n";
	
	/**
	 * Constructor.
	 * @param positionInformation Position information.
	 * @param configuration configuration of the save settings.
	 * @throws ConfigurationException
	 * @throws RemoteException 
	 */
	public StandardSaveSettings(PositionInformation positionInformation, StandardSaveSettingsConfiguration configuration)
					throws ConfigurationException, RemoteException {
		super(positionInformation, configuration, StandardSaveSettingsConfiguration.TYPE_IDENTIFIER, StandardSaveSettingsConfiguration.class, "standard save settings");
	}

	@Override
	public String getMeasurementConfigurationFilePath(SaveInformation saveInformation) {
		return "configuration.csb";
	}

	@Override
	public String getMicroscopeConfigurationFilePath(SaveInformation saveInformation) {
		return "YSConfig_Microscope.cfg";
	}

	@Override
	public String getLogErrFilePath(SaveInformation saveInformation) {
		return "measurement_err.txt";
	}

	@Override
	public String getLogOutFilePath(SaveInformation saveInformation) {
		return "measurement_log.txt";
	}

	@Override
	public String getMeasurementBasePath(SaveInformation saveInformation) {
		DateFormat dateToFileName = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		return getConfiguration().getBaseFolder() + File.separator +  TextTools.convertToFileName(saveInformation.getMeasurementName()) + File.separator + dateToFileName.format(new Date(saveInformation.getMeasurementStartTime()));
	}

	@Override
	public String getTableFilePath(SaveInformation saveInformation, String tableName) {
		return TextTools.convertToFileName(tableName)+".csv";
	}

	@Override
	public String getImageMetadataTableFilePath(SaveInformation saveInformation) {
		return "images.csv";
	}

	@Override
	public String getImageFilePath(SaveInformation saveInformation, ImageEvent<?> event, String imageName) 
	{
		// Construct file name of image.
		Well well = event.getPositionInformation().getWell();
		int[] positions=event.getPositionInformation().getPositions();
		String imageFile = FileNameMacroConverter.convertImagePath(FILE_NAME_MACRO, imageName, event, saveInformation);
		String imageFileType = getConfiguration().getImageFileType();
		String imageFolderName = "";
		FolderStructureType type = getConfiguration().getFolderStructureType();
		if(type == FolderStructureType.SEPARATE_WELL_AND_CHANNEL || type == FolderStructureType.SEPARATE_WELL_AND_POSITION || type == FolderStructureType.SEPARATE_WELL_POSITION_AND_CHANNEL)
		{
			if(well != null)
			{
				if(imageFolderName.length() > 0)
					imageFolderName += File.separator;
				imageFolderName += "well_" + well.getWellName();
			}
		}
		if(type == FolderStructureType.SEPARATE_WELL_AND_POSITION || type == FolderStructureType.SEPARATE_WELL_POSITION_AND_CHANNEL)
		{
			// Add position information if necessary (only for
			// multi-position jobs
			for(int i = 0; i < positions.length; i++)
			{
				if(imageFolderName.length() > 0)
					imageFolderName += File.separator;
				imageFolderName += "position_" + Integer.toString(positions[i]+1);
			}
		}
		if(type == FolderStructureType.SEPARATE_WELL_AND_CHANNEL || type == FolderStructureType.SEPARATE_WELL_POSITION_AND_CHANNEL)
		{
			if(imageFolderName.length() > 0)
				imageFolderName += File.separator;
			imageFolderName += TextTools.convertToFileName(imageName);
		}

		// Combine both to obtain full path
		String imagePath = imageFile + "." + imageFileType;
		if(imageFolderName.length() > 0)
		{
			imagePath = imageFolderName + File.separator + imagePath;

		}
		return imagePath;
	}

	@Override
	public String getImageExtension(SaveInformation saveInformation, ImageEvent<?> event, String imageName) {
		return getConfiguration().getImageFileType();
	}

	@Override
	public String getXMLInformationFilePath(SaveInformation saveInformation) throws ResourceException, RemoteException {
		return "information.xml";
	}

	@Override
	public String getHTMLInformationFilePath(SaveInformation saveInformation)
			throws ResourceException, RemoteException {
		return "information.html";
	}
	
}
