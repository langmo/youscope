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
package org.youscope.plugin.imagingjob;

import org.youscope.common.configuration.CameraConfiguration;
import org.youscope.common.configuration.ChannelConfiguration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.image.ImageProducerConfiguration;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.basicjobs.ImagingJob;
import org.youscope.common.util.ConfigurationTools;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;

/**
 * This job/task makes images in a certain channel in regular intervals.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("imaging-job")
public class ImagingJobConfiguration implements JobConfiguration, ImageProducerConfiguration
{
	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= 7944732041177941146L;

	/**
	 * The channel configuration where the images should be made.
	 */
	@XStreamAlias("channel-configuration")
	private ChannelConfiguration				channelConfiguration				= new ChannelConfiguration();
	
	@XStreamAlias("camera-configuration")
	private CameraConfiguration				cameraConfiguration				= new CameraConfiguration();

	/**
	 * The exposure time for the imaging.
	 */
	@XStreamAlias("exposure-ms")
	@XStreamAsAttribute
	private double				exposure			= 5.0;

	/**
	 * Whether images should be saved to disk or not.
	 */
	@XStreamAlias("save-images")
	@XStreamAsAttribute
	@XStreamConverter(value = BooleanConverter.class, booleans = {false}, strings = {"yes", "no"})
	private boolean				saveImages			= true;

	/**
	 * The name under which the image is saved.
	 */
	@XStreamAlias("image-save-name")
	@XStreamAsAttribute
	private String				imageSaveName		= "";

	@Override
	public String getDescription()
	{
		return "<p>" + getImageSaveName() + " = snapImage(channel = " + getChannel() + ", exposure = " + Double.toString(getExposure()) + "ms)</p>";
	}

	/**
	 * Sets the channel and channel group in which should be imaged.
	 * @param channelGroup The group of the channel.
	 * @param channel The channel.
	 */
	public void setChannel(String channelGroup, String channel)
	{
		this.channelConfiguration = new ChannelConfiguration(channelGroup, channel);
	}

	/**
	 * Returns the channel in which should be imaged.
	 * @return the channel.
	 */
	public String getChannel()
	{
		return channelConfiguration.getChannel();
	}

	/**
	 * @return the group of the channel
	 */
	public String getChannelGroup()
	{
		return channelConfiguration.getChannelGroup();
	}

	/**
	 * @param exposure
	 *            the exposure to set
	 */
	public void setExposure(double exposure)
	{
		this.exposure = exposure;
	}

	/**
	 * @return the exposure
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
	 * @return the saveImages
	 */
	public boolean isSaveImages()
	{
		return saveImages;
	}

	
	/**
	 * Returns the name under which the images should be saved.
	 * @return Name of the job.
	 */
	public String getImageSaveName()
	{
		return imageSaveName;
	}

	
	/**
	 * The name under which the images should be saved.
	 * @param name Name of the job.
	 */
	public void setImageSaveName(String name)
	{
		this.imageSaveName = name;
	}

	@Override
	public String getTypeIdentifier()
	{
		return ImagingJob.DEFAULT_TYPE_IDENTIFIER;
	}
	
	@Override
	public String[] getImageSaveNames()
	{
		if(saveImages)
			return new String[]{imageSaveName};
		return null;
	}

	@Override
	public int getNumberOfImages()
	{
		return 1;
	}

	/**
	 * Sets the camera with which should be imaged. Set to null to use the default camera.
	 * @param camera ID of camera device, or null.
	 */
	public void setCamera(String camera)
	{
		this.cameraConfiguration.setCameraDevice(camera);
	}

	/**
	 * Returns the ID of the camera with which should be imaged, or null, if imaging with the default camera.
	 * @return ID of camera, or null.
	 */
	public String getCamera()
	{
		return cameraConfiguration.getCameraDevice();
	}

	/**
	 * Returns the channel configuration.
	 * @return Channel configuration.
	 */
	public ChannelConfiguration getChannelConfiguration() 
	{
		try {
			return ConfigurationTools.deepCopy(channelConfiguration, ChannelConfiguration.class);
		} catch (@SuppressWarnings("unused") ConfigurationException e) 
		{
			return channelConfiguration;
		}
	}

	/**
	 * Sets the channel configuration. Set to null to image in currently active channel.
	 * @param channelConfiguration The channel configuration.
	 */
	public void setChannelConfiguration(ChannelConfiguration channelConfiguration) 
	{
		this.channelConfiguration = new ChannelConfiguration(channelConfiguration);
	}

	/**
	 * Returns the camera configuration,.
	 * @return Camera configuration
	 */
	public CameraConfiguration getCameraConfiguration() {
		try {
			return ConfigurationTools.deepCopy(cameraConfiguration, CameraConfiguration.class);
		} catch (@SuppressWarnings("unused") ConfigurationException e) {
			return cameraConfiguration;
		}
	}

	/**
	 * Sets the configuration of the camera. Set to null to use default camera.
	 * @param cameraConfiguration The configuration of the camera.
	 */
	public void setCameraConfiguration(CameraConfiguration cameraConfiguration) {
		this.cameraConfiguration = new CameraConfiguration(cameraConfiguration);
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		if(cameraConfiguration != null)
			cameraConfiguration.checkConfiguration();
		if(channelConfiguration != null)
			channelConfiguration.checkConfiguration();
	}
}
