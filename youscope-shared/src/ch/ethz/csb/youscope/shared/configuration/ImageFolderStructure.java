/**
 * 
 */
package ch.ethz.csb.youscope.shared.configuration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * List of the different possibilities which folder structure to use to save the
 * images.
 * 
 * @author langmo
 * 
 */
@XStreamAlias("image-folder-structure")
public enum ImageFolderStructure implements Configuration
{
	/**
	 * A folder is created for every well, then every position and then every
	 * channel.
	 */
	SEPARATE_WELL_POSITION_AND_CHANNEL("Separate folder for each well, position, and channel."),
	/**
	 * A folder is created for every well and then every channel.
	 */
	SEPARATE_WELL_AND_CHANNEL("Separate folder for each well, and channel."),
	/**
	 * A folder is created for every well and then every position.
	 */
	SEPARATE_WELL_AND_POSITION("Separate folder for each well, and position."),
	/**
	 * All images are saved in one folder.
	 */
	ALL_IN_ONE_FOLDER("Store all images in the same folder.");

	private final String	description;

	ImageFolderStructure(String description)
	{
		this.description = description;
	}

	@Override
	public String toString()
	{
		return description;
	}

	@Override
	public String getTypeIdentifier() 
	{
		return "CSB::ImageFolderStructure";
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		// nothing to check.
		
	}
}
