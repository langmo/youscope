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
package org.youscope.plugin.continousimaging;

import org.youscope.common.configuration.CameraConfiguration;
import org.youscope.common.configuration.ChannelConfiguration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.YSConfigAlias;
import org.youscope.common.configuration.YSConfigClassification;
import org.youscope.common.configuration.YSConfigConditional;
import org.youscope.common.configuration.YSConfigIcon;
import org.youscope.common.image.ImageProducerConfiguration;
import org.youscope.common.job.JobConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;

/**
 * @author Moritz Lang
 *
 */
@XStreamAlias("short-continuous-imaging-job")
@YSConfigAlias("short continuous imaging")
@YSConfigClassification("imaging")
@YSConfigIcon("icons/camcorder.png")
public class ShortContinuousImagingJobConfiguration implements JobConfiguration, ImageProducerConfiguration
{
	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= 7911732042177921444L;
	
	/**
	 * The identifier for this job type.
	 */
	public static final String	TYPE_IDENTIFIER	= "YouScope.ShortContinuousImagingJob";
	
	@XStreamAlias("camera")
	@YSConfigAlias("camera device")
	private CameraConfiguration				camera				= null;
	
	/**
	 * The channel where the images should be made. Set to an empty string if
	 * the current channel should be taken.
	 */
	@XStreamAlias("channel-configuration")
	@YSConfigAlias("channel")
	private ChannelConfiguration				channel				= null;

	/**
	 * The exposure time for the imaging.
	 */
	@XStreamAlias("exposure-ms")
	@YSConfigAlias("exposure time (ms)")
	private double				exposure			= 20.0;

	/**
	 * Whether images should be saved to disk or not.
	 */
	@XStreamAlias("save-images")
	@YSConfigAlias("save images")
	@XStreamConverter(value = BooleanConverter.class, booleans = {false}, strings = {"yes", "no"})
	private boolean				saveImages			= true; 

	/**
	 * The name under which the image is saved.
	 */
	@XStreamAlias("image-save-name")
	@YSConfigAlias("name of saved images")
	@YSConfigConditional("isSaveImages")
	private String				imageSaveName		= "continuous";
	
	/**
	 * The time between two successive images.
	 */
	@YSConfigAlias("imaging period (ms)")
	@XStreamAlias("period-ms")
	private int imagingPeriod = 0;
	
	/**
	 * The time between two successive images.
	 */
	@YSConfigAlias("number of images")
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
	 * @param camera device, or null.
	 */
	public void setCamera(CameraConfiguration camera)
	{
		this.camera = camera;
	}

	/**
	 * Returns the camera with which should be imaged, or null, if imaging with the default camera.
	 * @return camera device, or null.
	 */
	public CameraConfiguration getCamera()
	{
		return camera;
	}
	
	/**
	 * The channel where the images should be made. Set to null if no device settings should be set for imaging.
	 * @param channel The channel.
	 */
	public void setChannel(ChannelConfiguration channel)
	{
		this.channel = channel;
	}

	/**
	 * The channel where the images should be made. 
	 * @return The channel.
	 */
	public ChannelConfiguration getChannel()
	{
		return channel;
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
	public boolean isSaveImages()
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
	
	@Override
	public void checkConfiguration() throws ConfigurationException {
		if(exposure <= 0)
			throw new ConfigurationException("Exposure must be bigger than zero.");
		
	}
}
