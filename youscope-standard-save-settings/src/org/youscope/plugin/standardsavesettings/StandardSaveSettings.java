package org.youscope.plugin.standardsavesettings;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.measurement.microplate.Well;
import org.youscope.common.resource.ResourceAdapter;
import org.youscope.common.saving.SaveSettings;

/**
 * Standard save settings, e.g. the definition where images should be stored under which name.
 * @author mlang
 *
 */
public class StandardSaveSettings extends ResourceAdapter<StandardSaveSettingsConfiguration> implements SaveSettings
{

	/**
	 * Constructor.
	 * @param positionInformation Position information.
	 * @param configuration configuration of the save settings.
	 * @throws ConfigurationException
	 */
	public StandardSaveSettings(PositionInformation positionInformation, StandardSaveSettingsConfiguration configuration)
					throws ConfigurationException {
		super(positionInformation, configuration, StandardSaveSettingsConfiguration.TYPE_IDENTIFIER, StandardSaveSettingsConfiguration.class, "standard save settings");
	}

	@Override
	public String getScopeSettingsFilePath() {
		return "scope_settings.xml";
	}

	@Override
	public String getMeasurementConfigurationFilePath() {
		return "configuration.csb";
	}

	@Override
	public String getMicroscopeConfigurationFilePath() {
		return "YSConfig_Microscope.cfg";
	}

	@Override
	public String getLogErrFilePath() {
		return "measurement_err.txt";
	}

	@Override
	public String getLogOutFilePath() {
		return "measurement_log.txt";
	}

	@Override
	public String getMeasurementBasePath(String measurementName, long timeMs) {
		DateFormat dateToFileName = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		return getConfiguration().getBaseFolder() + File.separator + measurementName + File.separator + dateToFileName.format(new Date(timeMs));
	}

	@Override
	public String getTableFilePath(String tableName) {
		return tableName+".csv";
	}

	@Override
	public String getImageMetadataTableFilePath() {
		return "images.csv";
	}

	@Override
	public String getImageFilePath(ImageEvent<?> event, String imageName) 
	{
		// Construct file name of image.
		String channel = event.getChannel();
		String camera = event.getCamera();
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(new Date(event.getCreationTime()));
		Well well = event.getPositionInformation().getWell();
		int[] positionInformation = event.getPositionInformation().getPositions();
		String imageFile = NamingMacros.convertFileName(getConfiguration().getImageFileName(), imageName, channel, well, positionInformation, event.getExecutionInformation(), calendar, camera, null);
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
			for(int i = 0; i < positionInformation.length; i++)
			{
				if(imageFolderName.length() > 0)
					imageFolderName += File.separator;
				imageFolderName += "position_" + Integer.toString(positionInformation[i]+1);
			}
		}
		if(type == FolderStructureType.SEPARATE_WELL_AND_CHANNEL || type == FolderStructureType.SEPARATE_WELL_POSITION_AND_CHANNEL)
		{
			if(imageFolderName.length() > 0)
				imageFolderName += File.separator;
			imageFolderName += imageName;
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
	public String getImageExtension(ImageEvent<?> event, String imageName) {
		return getConfiguration().getImageFileType();
	}
	
}
