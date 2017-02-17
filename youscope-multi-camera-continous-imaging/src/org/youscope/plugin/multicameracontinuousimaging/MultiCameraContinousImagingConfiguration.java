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
package org.youscope.plugin.multicameracontinuousimaging;

import org.youscope.common.measurement.MeasurementConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;

/**
 * This class represents the configuration of a user configurable measurement.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("multi-camera-continuous-imaging-measurement")
public class MultiCameraContinousImagingConfiguration extends MeasurementConfiguration
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -6934063013631887401L;

	/**
	 * The identifier for this measurement type.
	 */
	public static final String	TYPE_IDENTIFIER		= "YouScope.MultiCameraContinuousImagingMeasurement";

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
	 * The exposure times for the cameras.
	 */
	@XStreamImplicit(itemFieldName = "exposure-ms")
	private double[]			exposures			= new double[0];

	/**
	 * The cameras which should image.
	 */
	@XStreamImplicit(itemFieldName = "camera-device")
	private String[]			cameras				= new String[0];

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
	@XStreamAlias("imaging-period-ms")
	private int imagingPeriod = 100;
	
	
	@Override 
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}

	/**
	 * The channel where the images should be made. Set to an empty string if
	 * the current channel should be taken.
	 * @param channelGroup The group in which the channel is defined.
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
	 * @param cameras
	 *            The cameras which should image
	 */
	public void setCameras(String[] cameras)
	{
		this.cameras = cameras;
	}

	/**
	 * @return the cameras which should image
	 */
	public String[] getCameras()
	{
		return cameras;
	}

	/**
	 * @param exposures
	 *            the exposures to set
	 */
	public void setExposures(double[] exposures)
	{
		this.exposures = exposures;
	}

	/**
	 * @return the exposures
	 */
	public double[] getExposures()
	{
		return exposures;
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
	 * Returns the name under which the image should be saved.
	 * @return name of job.
	 */
	public String getImageSaveName()
	{
		return imageSaveName;
	}
	
	/**
	 * Sets the name under which the image should be saved.
	 * @param name
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
	 * Returns true if the images should be saved to the disk.
	 * @return True if images should be saved.
	 */
	public boolean isSaveImages()
	{
		return saveImages;
	}
	
}
