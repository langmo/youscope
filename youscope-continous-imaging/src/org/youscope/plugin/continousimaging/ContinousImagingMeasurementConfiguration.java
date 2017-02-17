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

import org.youscope.common.measurement.MeasurementConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;

/**
 * This class represents the configuration of a user configurable measurement.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("continuous-imaging-measurement")
public class ContinousImagingMeasurementConfiguration extends MeasurementConfiguration
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -6934063013631887402L;

	/**
	 * The identifier for this measurement type.
	 */
	public static final String	TYPE_IDENTIFIER		= "YouScope.ContinuousImagingMeasurement";

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
	
	
	@Override 
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
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
}
