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
package org.youscope.plugin.lifecelldetection;

import org.youscope.addon.celldetection.CellDetectionConfiguration;
import org.youscope.addon.celldetection.CellVisualizationConfiguration;
import org.youscope.common.measurement.MeasurementConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;

/**
 * This class represents the configuration of a continuous measurement, for which the cells in the images are life detected.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("cell-detection-measurement")
public class CellDetectionMeasurementConfiguration extends MeasurementConfiguration
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -6934063013699987401L;

	/**
	 * The identifier for this measurement type.
	 */
	public static final String	TYPE_IDENTIFIER		= "YouScope.ContinuousLifeCellDetectionMeasurement";

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
	 * The exposure time in ms.
	 */
	@XStreamAlias("exposure")
	private double			exposure = 20;
	
	@XStreamAlias("detection-algorithm-configuration")
	private CellDetectionConfiguration detectionAlgorithmConfiguration = null;
	
	@XStreamAlias("detection-visualization-configuration")
	private CellVisualizationConfiguration visualizationAlgorithmConfiguration = null;
	
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
	private int imagingPeriod = -1;
	
	
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

	/**
	 * Sets the configuration of the cell detection algorithm to use.
	 * @param detectionAlgorithmConfiguration Configuration of the algorithm.
	 */
	public void setDetectionAlgorithmConfiguration(CellDetectionConfiguration detectionAlgorithmConfiguration)
	{
		this.detectionAlgorithmConfiguration = detectionAlgorithmConfiguration;
	}

	/**
	 * Return the configuration of the cell detection algorithm to use.
	 * @return Configuration of the algorithm.
	 */
	public CellDetectionConfiguration getDetectionAlgorithmConfiguration()
	{
		return detectionAlgorithmConfiguration;
	}
	
	/**
	 * Returns the configuration of the cell visualization algorithm to use.
	 * Returns null if not generating a cell detection visualization image.
	 * @return Cell visualization configuration, or null.
	 */
	public CellVisualizationConfiguration getVisualizationAlgorithmConfiguration()
	{
		return visualizationAlgorithmConfiguration;
	}

	/**
	 * Set the configuration of the cell visualization algorithm to use.
	 * Set to null to not generate a cell detection visualization image.
	 * @param visualizationAlgorithmConfiguration Cell visualization configuration, or null.
	 */
	public void setVisualizationAlgorithmConfiguration(CellVisualizationConfiguration visualizationAlgorithmConfiguration)
	{
		this.visualizationAlgorithmConfiguration = visualizationAlgorithmConfiguration;
	}
}
