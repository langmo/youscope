/**
 * 
 */
package ch.ethz.csb.youscope.shared.measurement;

import java.io.Serializable;

import ch.ethz.csb.youscope.shared.configuration.ImageFolderStructure;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author langmo
 * 
 */
@XStreamAlias("save-settings")
public class MeasurementSaveSettings implements Cloneable, Serializable
{
	/**
	 * Serial version UID.
	 */
	private static final long		serialVersionUID		= -5443517330196112267L;

	/**
	 * The folder where the measurement (information and images made during the measurement) should be stored.
	 */
	@XStreamAlias("folder")
	private String					folder					= "";

	/**
	 * The file type in which the images should be stored
	 */
	@XStreamAlias("image-file-type")
	private String					imageFileType			= "tif";

	/**
	 * TRUE, if images of different channels and positions should be saved in
	 * separate folders. FALSE, if all images should be saved in main folder.
	 */
	@XStreamAlias("folder-structure")
	private ImageFolderStructure	imageFolderStructure	= ImageFolderStructure.ALL_IN_ONE_FOLDER;

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
	 * Returns the folder where the measurement should be saved.
	 * @return the folder of the measurement.
	 */
	public String getFolder()
	{
		return folder;
	}

	/**
	 * Sets the folder where the measurement should be saved.
	 * Be aware that an additional folder is created inside this folder when the measurement starts,
	 * which indicates the specific time when the measurement was started. This is done since one
	 * measurement can be started multiple times. To obtain the full path to this subfolder, use the
	 * respective function in MeasurementSaver.
	 * @param folder the folder of the measurement.
	 */
	public void setFolder(String folder)
	{
		this.folder = folder;
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

	/**
	 * @return The folder structure in which the images are saved.
	 */
	public ImageFolderStructure getImageFolderStructure()
	{
		return imageFolderStructure;
	}

	/**
	 * @param imageFolderStructure
	 *            The folder structure in which the images are saved.
	 * 
	 */
	public void setImageFolderStructure(ImageFolderStructure imageFolderStructure)
	{
		this.imageFolderStructure = imageFolderStructure;
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
}
