/**
 * 
 */
package ch.ethz.csb.youscope.addon.continousimaging;

import ch.ethz.csb.youscope.shared.configuration.ImageProducerConfiguration;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;

/**
 * @author Moritz Lang
 *
 */
@XStreamAlias("short-continuous-imaging-job")
public class ShortContinuousImagingJobConfiguration extends JobConfiguration implements ImageProducerConfiguration
{
	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= 7911732042177921444L;
	
	/**
	 * The identifier for this job type.
	 */
	public static final String	TYPE_IDENTIFIER	= "CSB::ShortContinuousImagingJob";
	
	@XStreamAlias("camera")
	private String				camera				= null;
	
	/**
	 * The channel where the images should be made. Set to an empty string if
	 * the current channel should be taken.
	 */
	@XStreamAlias("channel")
	private String				channel				= "";

	/**
	 * The config group where the channel is defined.
	 */
	@XStreamAlias("channel-group")
	private String				channelGroup			= "";
	/**
	 * The exposure time for the imaging.
	 */
	@XStreamAlias("exposure-ms")
	private double				exposure			= 20.0;

	/**
	 * Whether images should be saved to disk or not.
	 */
	@XStreamAlias("save-images")
	@XStreamConverter(value = BooleanConverter.class, booleans = {false}, strings = {"yes", "no"})
	private boolean				saveImages			= true;

	/**
	 * The name under which the image is saved.
	 */
	@XStreamAlias("image-save-name")
	private String				imageSaveName		= "";
	
	/**
	 * The time between two successive images.
	 */
	@XStreamAlias("period-ms")
	private int imagingPeriod = 0;
	
	/**
	 * The time between two successive images.
	 */
	@XStreamAlias("num-images")
	private int numImages = 10;
	
	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}
	
	@Override
	public String getDescription()
	{
		String description = "<p>for i=1:"+Integer.toString(numImages);
		description += "<ul style=\"margin-top:0px;margin-bottom:0px;margin-left:12px;list-style-type:none\"><li>";
		description+= getImageSaveName() + " = snapImage(channel = " + getChannel() + ", exposure = " + Double.toString(getExposure()) + "ms)";
		description += "</li></ul>end</p>";
		return description;
	}
	
	/**
	 * Sets the camera with which should be imaged. Set to null to use the default camera.
	 * @param camera ID of camera device, or null.
	 */
	public void setCamera(String camera)
	{
		this.camera = camera;
	}

	/**
	 * Returns the ID of the camera with which should be imaged, or null, if imaging with the default camera.
	 * @return ID of camera, or null.
	 */
	public String getCamera()
	{
		return camera;
	}
	
	/**
	 * The channel where the images should be made. Set to an empty string if
	 * the current channel should be taken.
	 * @param channelGroup The group of the channel.
	 * @param channel The channel.
	 */
	public void setChannel(String channelGroup, String channel)
	{
		this.channel = channel;
		this.channelGroup = channelGroup;
	}

	/**
	 * The channel where the images should be made. 
	 * @return The channel.
	 */
	public String getChannel()
	{
		return channel;
	}

	/**
	 * @return the group of the channel
	 */
	public String getChannelGroup()
	{
		return channelGroup;
	}

	/**
	 * Sets the exposure time in ms.
	 * @param exposure Exposure time in ms.
	 */
	public void setExposure(double exposure)
	{
		this.exposure = exposure;
	}

	
	/**
	 * @return Exposure time in ms.
	 */
	public double getExposure()
	{
		return exposure;
	}

	/**
	 * @param saveImages
	 *            the saveImages to set
	 */
	public void setSaveImages(boolean saveImages)
	{
		this.saveImages = saveImages;
	}

	/**
	 * Returns the name under which the images should be saved.
	 * @return The name of the job.
	 */
	public String getImageSaveName()
	{
		return imageSaveName;
	}
	
	/**
	 * Sets the name under which the images should be saved.
	 * @param name The name of the job.
	 */
	public void setImageSaveName(String name)
	{
		this.imageSaveName = name;
	}

	/**
	 * @param imagingPeriod The time between two successive images.
	 */
	public void setImagingPeriod(int imagingPeriod)
	{
		this.imagingPeriod = imagingPeriod;
	}

	/**
	 * @return The time between two successive images.
	 */
	public int getImagingPeriod()
	{
		return imagingPeriod;
	}

	/**
	 * Returns if the images made by this job should be saved to disk.
	 * @return True if images should be saved.
	 */
	public boolean getSaveImages()
	{
		return saveImages;
	}
	
	/**
	 * Sets the number of images which should be taken.
	 * @param numImages Number of images to be taken.
	 */
	public void setNumImages(int numImages)
	{
		this.numImages = numImages;
	}
	
	/**
	 * Returns the number of images which should be taken.
	 * @return numImages Number of images to be taken.
	 */
	public int getNumImages()
	{
		return numImages;
	}
	
	@Override
	public String[] getImageSaveNames()
	{
		if(saveImages)
		{
			return new String[]{imageSaveName};
		}
		return null;
	}

	@Override
	public int getNumberOfImages()
	{
		return numImages > 0 ? numImages:0;
	}
}
