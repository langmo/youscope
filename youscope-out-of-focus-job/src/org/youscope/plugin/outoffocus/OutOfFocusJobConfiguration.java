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
package org.youscope.plugin.outoffocus;

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.FocusConfiguration;
import org.youscope.common.image.ImageProducerConfiguration;
import org.youscope.common.job.JobConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;

/**
 * @author langmo
 *
 */
@XStreamAlias("out-of-focus-job")
public class OutOfFocusJobConfiguration implements JobConfiguration, ImageProducerConfiguration
{
	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= 7911732041177941146L;
	
	/**
	 * The identifier for this job type.
	 */
	public static final String	TYPE_IDENTIFIER	= "YouScope.OutOfFocusJob";

	/**
	 * Configuration of the focus device used for focussing.
	 */
	@XStreamAlias("focus-configuration")
	private FocusConfiguration	focusConfiguration	= null;

	/**
	 * The new position/offset of the focus/autofocus.
	 */
	@XStreamAlias("relative-focus-position-um")
	private double							position			= 0;
	
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
	 * The exposure time for the imaging.
	 */
	@XStreamAlias("exposure-ms")
	@XStreamAsAttribute
	private double				exposure			= 20.0;

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
	private String				imageSaveName		= "BFout";
	
	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}
	
	/**
	 * @param focusConfiguration
	 *            The configuration of the focus .
	 */
	public void setFocusConfiguration(FocusConfiguration focusConfiguration)
	{
		this.focusConfiguration = focusConfiguration;
	}

	/**
	 * @return The configuration of the focus.
	 */
	public FocusConfiguration getFocusConfiguration()
	{
		return focusConfiguration;
	}
	
	/**
	 * @param position
	 *            the position to set
	 */
	public void setPosition(double position)
	{
		this.position = position;
	}

	/**
	 * @return the position
	 */
	public double getPosition()
	{
		return position;
	}
	
	@Override
	public String getDescription()
	{
		double offset = getPosition();
		boolean isPos;
		if (offset >= 0)
        {
            isPos = true;
        } 
		else
        {
            isPos = false;
            offset = -offset;
        }
        
		if(getFocusConfiguration() != null && getFocusConfiguration().getFocusDevice() != null)
        {
        	String focusDevice = getFocusConfiguration().getFocusDevice();
        	String description =
                "<p>" + focusDevice + ".position = " + focusDevice + ".position" + (isPos ? " + " : " - ")
                        + Double.toString(offset) + "</p>";
	        description += "<p>" + getImageSaveName() + "=snapImage(channel=\"" + getChannel() + "\", exposure=" + Double.toString(getExposure()) + "ms)</p>";
	        description += "<p>" + focusDevice + ".position = " + focusDevice + ".position" + (isPos ? " - " : " + ")
	        			+ Double.toString(offset) + "</p>";
	        return description;
        }
		// else
		String description =
		    "<p>focus.position = focus.position" + (isPos ? " + " : " - ")
		            + Double.toString(offset) + "</p>";
		description += "<p>" + getImageSaveName() + "=snapImage(channel=\"" + getChannel() + "\", exposure=" + Double.toString(getExposure()) + "ms)</p>";
		description += "<p>focus.position = focus.position" + (isPos ? " - " : " + ")
					+ Double.toString(offset) + "</p>";
		return description;
	}
	
	/**
	 * Sets the channel which should be imaged.
	 * @param channelGroup The group of the channel.
	 * @param channel The channel.
	 */
	public void setChannel(String channelGroup, String channel)
	{
		this.channelGroup = channelGroup;
		this.channel = channel;
	}

	/**
	 * @return the channel in which should be imaged.
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
	 * @return Name of imaging job.
	 */
	public String getImageSaveName()
	{
		return imageSaveName;
	}
	
	/**
	 * Sets the name under which the images should be saved.
	 * @param name Name of imaging job.
	 */
	public void setImageSaveName(String name)
	{
		this.imageSaveName = name;
	}
	
	@Override
	public String[] getImageSaveNames()
	{
		if(saveImages)
			return new String[]{imageSaveName};
		return new String[0];
	}

	@Override
	public int getNumberOfImages()
	{
		return 1;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		if(exposure <= 0)
			throw new ConfigurationException("Exposure must be positive.");
		
	}
}
