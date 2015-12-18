package org.youscope.plugin.standardsavesettings;

import org.youscope.common.saving.MeasurementSaver;
import org.youscope.common.saving.SaveSettingsConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Configuration of a standard folder structure to save measurements.
 * @author mlang
 *
 */
public class StandardSaveSettingsConfiguration  extends SaveSettingsConfiguration
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 113704186393312354L;

	/**
	 * Returns the type of the standard folder structure.
	 * @return Type of standard folder structure.
	 */
	public FolderStructureType getFolderStructureType() {
		return folderStructureType;
	}

	/**
	 * Sets the type of the standard folder structure.
	 * @param folderStructureType Type of the standard folder structure.
	 */
	public void setFolderStructureType(FolderStructureType folderStructureType) {
		this.folderStructureType = folderStructureType;
	}
	
	/**
	 * Type identifier of the save settings.
	 */
	public static final String TYPE_IDENTIFIER = "YouScope.StandardSaveSettings";
	
	/**
	 * The folder where the measurement (information and images made during the measurement) should be stored.
	 */
	@XStreamAlias("base-folder")
	private String					baseFolder					= "";

	/**
	 * The file type in which the images should be stored
	 */
	@XStreamAlias("image-file-type")
	private String					imageFileType			= "tif";

	@XStreamAlias("folder-structure-type")
	private FolderStructureType	folderStructureType	= FolderStructureType.ALL_IN_ONE_FOLDER;

	/**
	 * The filename of the image, without the file format (everything before the
	 * dot). To distinguish between the images (and to not override an existing
	 * file), please make them unique by using one of the following macros:
	 * %year ... current year %month ... current month %day ... current day
	 * %hour ... current hour %minute ... current minute %second ... current
	 * seconds %millisecond ... current milliseconds %number ... number of the
	 * image in the measurement, starting at 1 %index ... index of the image (=
	 * %number-1) Everything else will be interpreted as a string. Please take
	 * care that the file name is unique and valid.
	 */
	@XStreamAlias("image-file-name-template")
	private String					imageFileName			= "%N_position%4w%2p_time%n";

	/**
	 * Returns the folder where all measurements should be saved.
	 * @return the folder of the measurement.
	 */
	public String getBaseFolder()
	{
		return baseFolder;
	}

	/**
	 * Sets the folder where all measurements should be saved.
	 * Be aware that an additional folder is created inside this folder when the measurement starts,
	 * which indicates the specific time when the measurement was started. This is done since one
	 * measurement can be started multiple times. To obtain the full path to this subfolder, use the
	 * respective function in {@link MeasurementSaver}.
	 * @param folder the folder of the measurement.
	 */
	public void setBaseFolder(String folder)
	{
		this.baseFolder = folder;
	}

	/**
	 * @return the imageFileType
	 */
	public String getImageFileType()
	{
		return imageFileType;
	}

	/**
	 * @param imageFileType
	 *            the imageFileType to set
	 */
	public void setImageFileType(String imageFileType)
	{
		this.imageFileType = imageFileType;
	}

	/**
	 * @return The file name under which the images are saved.
	 */
	public String getImageFileName()
	{
		return imageFileName;
	}

	/**
	 * @param fileName
	 *            The file name the image should be saved under.
	 */
	public void setImageFileName(String fileName)
	{
		this.imageFileName = fileName;
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

	@Override
	public String getTypeIdentifier() {
		return TYPE_IDENTIFIER;
	}
}
