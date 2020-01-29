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
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.common.task.PeriodConfiguration;
import org.youscope.common.util.ConfigurationTools;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;

/**
 * This class represents a measurement which takes images in a two dimensional spatial array, such that
 * the single pictures can be afterwards composed to a single big one.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("composed-imaging-measurement")
public class ComposedImagingMeasurementConfiguration extends MeasurementConfiguration
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -6934063013631887401L;

	/**
	 * The identifier for this measurement type.
	 */
	public static final String	TYPE_IDENTIFIER		= "YouScope.ComposedImagingMeasurement";

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
	 * Name of the pixel size setting defining the pixel size.
	 */
	@XStreamAlias("pixel-size-id")
	private String pixelSizeID = null;

	/**
	 * The number of pixels in an image
	 */
	@XStreamAlias("num-pixels")
	private Dimension			numPixels			= new Dimension(1024, 1344);

	/**
	 * Percentage of overlapping between the images.
	 */
	@XStreamAlias("overlap")
	private double				overlap				= 0.1;
	
	/**
	 * Period in which measurement is repeated. NULL is
	 * interpreted as as fast as possible.
	 */
	@XStreamAlias("period")
	private PeriodConfiguration										period				= null;
	
	/**
	 * The camera device to use. Set to null if standard camera should be used.
	 */
	@XStreamAlias("camera")
	private String cameraDevice = null;
	
	/**
	 * Sets the period length between single repetitions of the imaging process.
	 * @param period Period time between making several composed images.
	 */
	public void setPeriod(PeriodConfiguration period)
	{
		if(period!=null)
		{
			try
			{
				this.period = ConfigurationTools.deepCopy(period, PeriodConfiguration.class);
			}
			catch(ConfigurationException e)
			{
				throw new IllegalArgumentException("Could not copy argument.", e);
			}
		}
	}

	/**
	 * @return the period
	 */
	public PeriodConfiguration getPeriod()
	{
		if(period == null)
			return null;
		try
		{
			return ConfigurationTools.deepCopy(period, PeriodConfiguration.class);
		}
		catch(ConfigurationException e)
		{
			throw new IllegalArgumentException("Could not copy argument.", e);
		}
	}
	
	/**
	 * Sets the channel for imaging.
	 * @param channelGroup
	 * @param channel
	 */
	public void setChannel(String channelGroup, String channel)
	{
		this.channel = channel;
		this.channelGroup = channelGroup;
	}

	
	/**
	 * Returns the channel in which is imaged.
	 * @return Channel.
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
	 * Sets the exposure in ms.
	 * @param exposure Exposure time in ms.
	 */
	public void setExposure(double exposure)
	{
		this.exposure = exposure;
	}

	/**
	 * Returns the exposure time.
	 * @return Exposure time in ms.
	 */
	public double getExposure()
	{
		return exposure;
	}

	/**
	 * Sets if taken images should be saved to disk
	 * @param saveImages true if images should be saved.
	 */
	public void setSaveImages(boolean saveImages)
	{
		this.saveImages = saveImages;
	}

	/**
	 * Returns if taken images should be saved to disk
	 * @return true if images should be saved.
	 */
	public boolean isSaveImages()
	{
		return saveImages;
	}

	/**
	 * Returns the name under which the images are saved.
	 * @return Name of the imaging job.
	 */
	public String getImageSaveName()
	{
		return imageSaveName;
	}

	/**
	 * Sets the name under which the images are saved.
	 * @param name Name of the imaging job.
	 */
	public void setImageSaveName(String name)
	{
		this.imageSaveName = name;
	}

	/**
	 * Sets the number of images which should be made in the x-direction.
	 * @param nx Number of images in the x-direction.
	 */
	public void setNx(int nx)
	{
		this.nx = nx;
	}

	/**
	 * Returns the number of images which should be made in the x-direction.
	 * @return Number of images in the x-direction.
	 */
	public int getNx()
	{
		return nx;
	}

	/**
	 * Sets the number of images which should be made in the y-direction.
	 * @param ny Number of images in the y-direction.
	 */
	public void setNy(int ny)
	{
		this.ny = ny;
	}

	/**
	 * Returns the number of images which should be made in the y-direction.
	 * @return Number of images in the y-direction.
	 */
	public int getNy()
	{
		return ny;
	}

	/**
	 * Sets the size of one pixel in micro meters.
	 * @param pixelSize Size of one pixel.
	 */
	public void setPixelSize(double pixelSize)
	{
		this.pixelSize = pixelSize;
	}

	/**
	 * Returns the size of one pixel in micro meters.
	 * @return Size of one pixel.
	 */
	public double getPixelSize()
	{
		return pixelSize;
	}

	/**
	 * Sets the number of pixels in one image.
	 * @param numPixels Number of pixels in one image.
	 */
	public void setNumPixels(Dimension numPixels)
	{
		if(numPixels == null)
			throw new NullPointerException();
		this.numPixels = (Dimension)numPixels.clone();
	}

	/**
	 * Returns the number of pixels in one image.
	 * @return Number of pixels in one image.
	 */
	public Dimension getNumPixels()
	{
		return (Dimension)numPixels.clone();
	}

	/**
	 * Sets the overlap (0.0-1.0) between neighboring images.
	 * @param overlap Overlap between neighboring images.
	 */
	public void setOverlap(double overlap)
	{
		this.overlap = overlap;
	}

	/**
	 * Returns the overlap (0.0-1.0) of the two neighboring images.
	 * @return Overlap between neighboring images.
	 */
	public double getOverlap()
	{
		return overlap;
	}

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
	public void checkConfiguration() throws ConfigurationException {
		if(pixelSize <= 0)
			throw new ConfigurationException("Pixel size must be positive.");
		if(numPixels == null)
			throw new ConfigurationException("Number of pixels in an image not set.");
		if(overlap < 0)
			throw new ConfigurationException("Overlap must be bigger or equal to zero.");
		
	}
}
