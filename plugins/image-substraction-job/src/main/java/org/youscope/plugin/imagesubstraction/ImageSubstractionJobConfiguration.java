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
package org.youscope.plugin.imagesubstraction;

import org.youscope.common.configuration.CameraConfiguration;
import org.youscope.common.configuration.ChannelConfiguration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.FocusConfiguration;
import org.youscope.common.image.ImageProducerConfiguration;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.util.ConfigurationTools;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;

/**
 * @author Moritz Lang
 *
 */
@XStreamAlias("image-substraction-job")
public class ImageSubstractionJobConfiguration implements JobConfiguration, ImageProducerConfiguration
{
	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= 7911732041177941141L;
	
	/**
	 * The identifier for this job type.
	 */
	public static final String	TYPE_IDENTIFIER	= "YouScope.ImageSubstractionJob";

	/**
	 * Configuration of the focus device used for focussing.
	 */
	@XStreamAlias("focus-configuration")
	private FocusConfiguration	focusConfiguration	= null;
	
	@XStreamAlias("camera-configuration")
	private CameraConfiguration				cameraConfiguration				= new CameraConfiguration();

	/**
	 * The focus offset for the first image.
	 */
	@XStreamAlias("relative-focus-position1-um")
	private double							position1			= 5;
	
	/**
	 * The focus offset for the second image.
	 */
	@XStreamAlias("relative-focus-position2-um")
	private double							position2			= -5;
	
	/**
	 * The channel configuration where the images should be made.
	 */
	@XStreamAlias("channel-configuration")
	private ChannelConfiguration				channelConfiguration				= new ChannelConfiguration();
	
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
	private String				imageSaveName		= "Sub";
	
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
	 * @param position1
	 *            the position to set
	 */
	public void setPosition1(double position1)
	{
		this.position1 = position1;
	}

	/**
	 * @return the position
	 */
	public double getPosition1()
	{
		return position1;
	}
	
	/**
	 * @param position2
	 *            the position to set
	 */
	public void setPosition2(double position2)
	{
		this.position2 = position2;
	}

	/**
	 * @return the position
	 */
	public double getPosition2()
	{
		return position2;
	}
	
	@Override
	public String getDescription()
	{
		double offset1 = getPosition1();
		boolean isPos1;
		if (offset1 >= 0)
        {
            isPos1 = true;
        } 
		else
        {
            isPos1 = false;
            offset1 = -offset1;
        }
		
		double offset2 = -getPosition1()+getPosition2();
		boolean isPos2;
		if (offset2 >= 0)
        {
            isPos2 = true;
        } 
		else
        {
            isPos2 = false;
            offset2 = -offset2;
        }
		
		double offset3 = -getPosition2();
		boolean isPos3;
		if (offset3 >= 0)
        {
            isPos3 = true;
        } 
		else
        {
            isPos3 = false;
            offset3 = -offset3;
        }
        
		if(getFocusConfiguration() != null && getFocusConfiguration().getFocusDevice() != null)
        {
        	String focusDevice = getFocusConfiguration().getFocusDevice();
        	String description =
                "<p>" + focusDevice + ".position = " + focusDevice + ".position" + (isPos1 ? " + " : " - ")
                        + Double.toString(offset1) + "</p>";
        	description += "<p>snapImage(channel=\"" + getChannel() + "\", exposure=" + Double.toString(getExposure()) + "ms)</p>";
        	description +=
                    "<p>" + focusDevice + ".position = " + focusDevice + ".position" + (isPos2 ? " + " : " - ")
                            + Double.toString(offset2) + "</p>";
            	description += "<p>snapImage(channel=\"" + getChannel() + "\", exposure=" + Double.toString(getExposure()) + "ms)</p>";
        	description +=
                    "<p>" + focusDevice + ".position = " + focusDevice + ".position" + (isPos3 ? " + " : " - ")
                            + Double.toString(offset3) + "</p>";
            description += "<p>" + getImageSaveName() + "=divideImages()</p>";
	        return description;
        }
		// else
		String description =
		    "<p>focus.position = focus.position" + (isPos1 ? " + " : " - ")
		            + Double.toString(offset1) + "</p>";
		description += "<p>snapImage(channel=\"" + getChannel() + "\", exposure=" + Double.toString(getExposure()) + "ms)</p>";
    	description +=
    			"<p>focus.position = focus.position" + (isPos2 ? " + " : " - ")
                        + Double.toString(offset2) + "</p>";
        	description += "<p>snapImage(channel=\"" + getChannel() + "\", exposure=" + Double.toString(getExposure()) + "ms)</p>";
    	description +=
    			"<p>focus.position = focus.position" + (isPos3 ? " + " : " - ")
                        + Double.toString(offset3) + "</p>";
        description += "<p>" + getImageSaveName() + "=divideImages()</p>";
		return description;
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
	/**
	 * Sets the camera with which should be imaged. Set to null to use the default camera.
	 * @param camera ID of camera device, or null.
	 */
	public void setCamera(String camera)
	{
		this.cameraConfiguration.setCameraDevice(camera);
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
		if(exposure <= 0)
			throw new ConfigurationException("Exposure must be positive.");
		if(cameraConfiguration != null)
			cameraConfiguration.checkConfiguration();
		if(channelConfiguration != null)
			channelConfiguration.checkConfiguration();
		if(focusConfiguration != null)
			focusConfiguration.checkConfiguration();
		
	}
}
