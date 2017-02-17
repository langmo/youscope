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
package org.youscope.plugin.composedimaging;

import java.awt.Dimension;

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.image.ImageProducerConfiguration;
import org.youscope.common.job.JobConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;

/**
 * @author langmo
 */
@XStreamAlias("composed-imaging-job")
public class ComposedImagingJobConfiguration implements JobConfiguration,ImageProducerConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 4548542891764517275L;
	
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
	@XStreamConverter(value = BooleanConverter.class, booleans = {false}, strings = {"yes", "no"})
	@XStreamAsAttribute
	private boolean				saveImages			= true;

	/**
	 * The name under which the image is saved.
	 */
	@XStreamAlias("image-save-name")
	@XStreamAsAttribute
	private String				imageSaveName		= "";

	/**
	 * Number of images in the x-direction.
	 */
	@XStreamAlias("num-tiles-x")
	private int					nx					= 3;

	/**
	 * Number of images in the y-direction
	 */
	@XStreamAlias("num-tiles-y")
	private int					ny					= 3;

	/**
	 * Size of a pixel in mu m
	 */
	@XStreamAlias("pixel-size-um")
	private double				pixelSize			= 6.45;

	/**
	 * The number of pixels in an image
	 */
	@XStreamAlias("num-pixels")
	private Dimension			numPixels			= new Dimension(1024, 1344);

	/**
	 * Percentage of overlapping between the images.
	 */
	@XStreamAlias("overlap")
	private double				overlap				= 0.05;
	
	/**
	 * The camera device to use. Set to null if standard camera should be used.
	 */
	private String cameraDevice = null;
	
	/**
	 * Name of the pixel size setting defining the pixel size.
	 */
	private String pixelSizeID = null;

	@Override
	public String getDescription()
	{
		
		String description;
		description = "<p>for i = 0 : " + Integer.toString(nx-1) + ", j = 0 : " + Integer.toString(ny-1) + "</p>";
		description += "<ul style=\"margin-top:0px;margin-bottom:0px;margin-left:12px;list-style-type:none\">";
		description += "<li>[x, y] += [i &times; " + Double.toString(numPixels.getWidth() * pixelSize * (1-overlap)) + ", j &times; " + Double.toString(numPixels.getHeight() * pixelSize * (1-overlap)) + "]</li>";
		description += "<li>" + getImageSaveName() + "[x, y] = snapImage(channel = " + getChannel() + ", exposure = " + Double.toString(getExposure()) + "ms)</li>";
		description += "<li>[x, y] -= [i &times; " + Double.toString(numPixels.getWidth() * pixelSize * (1-overlap)) + ", j &times; " + Double.toString(numPixels.getHeight() * pixelSize * (1-overlap)) + "]</li>";
		description += "</ul><p>end</p>";
		return description;
	}
	
	/**
	 * Sets the channel in which the images should be made.
	 * @param channelGroup The group of the channel.
	 * @param channel The channel.
	 */
	public void setChannel(String channelGroup, String channel)
	{
		this.channelGroup = channelGroup;
		this.channel = channel;
	}

	
	/**
	 * @return The channel in which is imaged.
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
	 * Returns the name under which images of this job should be saved.
	 * @return Name for images.
	 */
	public String getImageSaveName()
	{
		return imageSaveName;
	}

	/**
	 * Sets the name under which images of this job should be saved.
	 * @param name Name for images.
	 */
	public void setImageSaveName(String name)
	{
		this.imageSaveName = name;
	}

	/**
	 * @param nx the nx to set
	 */
	public void setNx(int nx)
	{
		this.nx = nx;
	}

	/**
	 * @return the nx
	 */
	public int getNx()
	{
		return nx;
	}

	/**
	 * @param ny the ny to set
	 */
	public void setNy(int ny)
	{
		this.ny = ny;
	}

	/**
	 * @return the ny
	 */
	public int getNy()
	{
		return ny;
	}

	/**
	 * @param pixelSize the pixelSize to set
	 */
	public void setPixelSize(double pixelSize)
	{
		this.pixelSize = pixelSize;
	}

	/**
	 * @return the pixelSize
	 */
	public double getPixelSize()
	{
		return pixelSize;
	}

	/**
	 * @param numPixels the numPixels to set
	 */
	public void setNumPixels(Dimension numPixels)
	{
		if(numPixels == null)
			throw new NullPointerException();
		this.numPixels = (Dimension)numPixels.clone();
	}

	/**
	 * @return the numPixels
	 */
	public Dimension getNumPixels()
	{
		return (Dimension)numPixels.clone();
	}

	/**
	 * @param overlap the overlap to set
	 */
	public void setOverlap(double overlap)
	{
		this.overlap = overlap;
	}

	/**
	 * @return the overlap
	 */
	public double getOverlap()
	{
		return overlap;
	}

	/**
	 * The identifier for this job type.
	 */
	public static final String	TYPE_IDENTIFIER	= "YouScope.ComposedImagingJob";

	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}
	
	/**
	 * Sets the ID of the pixel size setting, or null if the pixel size does not correspond to a predefined pixel size setting.
	 * @param pixelSizeID ID of the pixel size setting.
	 */
	public void setPixelSizeID(String pixelSizeID)
	{
		this.pixelSizeID = pixelSizeID;
	}

	/**
	 * Returns the ID of the pixel size setting, or null if the pixel size does not correspond to a predefined pixel size setting.
	 * @return ID of the pixel size setting.
	 */
	public String getPixelSizeID()
	{
		return pixelSizeID;
	}

	/**
	 * Sets the camera device used for imaging.
	 * @param cameraDevice Name of the camera device, or null if standard camera should be used.
	 */
	public void setCameraDevice(String cameraDevice)
	{
		this.cameraDevice = cameraDevice;
	}

	/**
	 * Returns the camera device used for imaging.
	 * @return Name of the camera device, or null if standard camera should be used.
	 */
	public String getCameraDevice()
	{
		return cameraDevice;
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
		return nx * ny;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		// nothing to check.
		
	}
}
