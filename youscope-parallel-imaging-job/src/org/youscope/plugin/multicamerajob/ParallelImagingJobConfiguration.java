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
package org.youscope.plugin.multicamerajob;

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.image.ImageProducerConfiguration;
import org.youscope.common.job.JobConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;

/**
 * @author Moritz Lang
 * 
 */
@XStreamAlias("parallel-imaging-job")
public class ParallelImagingJobConfiguration implements JobConfiguration, ImageProducerConfiguration
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 6725397060734501074L;

	/**
	 * The channel where the images should be made. Set to an empty string if
	 * the current channel should be taken.
	 */
	@XStreamAlias("channel")
	@XStreamAsAttribute
	private String				channel				= "";

	/**
	 * The config group where the channel is defined.
	 */
	@XStreamAlias("channel-group")
	@XStreamAsAttribute
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

	@Override
	public String getDescription()
	{
		String returnVal = "<p>begin parallelize</p><ul style=\"margin-top:0px;margin-bottom:0px;margin-left:12px;list-style-type:none\">";
		for(int i = 0; i < cameras.length; i++)
		{
			returnVal += "<li>" + imageSaveName + "["+Integer.toString(i+1) +"] = snapImage(camera = " + cameras[i] + ", exposure = " + Double.toString(exposures[i]) + "ms)</li>";
		}
		returnVal += "</ul><p>end</p>";
		return returnVal;
	}

	/**
	 * Whether images should be saved to disk or not.
	 */
	@XStreamAlias("save-images")
	@XStreamAsAttribute
	@XStreamConverter(value = BooleanConverter.class, booleans = {false}, strings = {"yes", "no"})
	private boolean	saveImages			= true;

	/**
	 * The name under which the image is saved.
	 */
	@XStreamAlias("image-save-name")
	@XStreamAsAttribute
	private String	imageSaveName		= "";

	/**
	 * Returns the name under which the images are saved.
	 * @return Name of the imaging job.
	 */
	public String getImageSaveName()
	{
		return imageSaveName;
	}

	/**
	 * Sets the name under which the image is saved.
	 * @param name Name of the imaging job.
	 */
	public void setImageSaveName(String name)
	{
		this.imageSaveName = name;
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
	 * @return the saveImages
	 */
	public boolean isSaveImages()
	{
		return saveImages;
	}

	/**
	 * Sets the channel which should be imaged.
	 * @param channelGroup The group of the channel.
	 * @param channel  The channel.
	 */
	public void setChannel(String channelGroup, String channel)
	{
		this.channelGroup = channelGroup;
		this.channel = channel;
	}

	/**
	 * Returns the channel in which should be imaged.
	 * @return the channel
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
	 * The identifier for this job type.
	 */
	public static final String	TYPE_IDENTIFIER	= "YouScope.ParallelImagingJob";

	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
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
		return cameras == null ? 0 : cameras.length;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		// do nothing, too complicated.
		
	}
}
