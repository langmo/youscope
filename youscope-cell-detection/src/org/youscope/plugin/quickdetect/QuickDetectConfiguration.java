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
package org.youscope.plugin.quickdetect;

import org.youscope.addon.celldetection.CellDetectionConfiguration;
import org.youscope.common.configuration.YSConfigAlias;
import org.youscope.common.configuration.YSConfigClassification;
import org.youscope.common.configuration.YSConfigDescription;
import org.youscope.common.configuration.YSConfigNotVisible;
import org.youscope.common.table.TableDefinition;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;

/**
 * Configuration of the QuickDetect cell detection algorithm.
 * @author Moritz Lang
 *
 */
@YSConfigDescription("This detection algorithm is optimized for speed, rather than for quality. It only detects the center of the cells and their areas.\n"
			+ "The <i>internal binning</i> parameter defines for which factor the image gets resized (inverse of binning) before cell detection. Since the complexity of the algorithm roughly scales with the number of pixels in the image, this can highly speed up processing time.\n"
			+ "Internally, the gradient of the image is computed. After that, all pixels with values larger than <i>threshold</i> are considered as being edges. "
			+ "The edges are then connected and the holes inside the edges (i.e. the interior of the cells) is filled up.\n"
			+ "Finally, all connected areas with an area larger or equal to (<i>Min. Cell Diameter (px)</i>)<sup>2</sup> and smaller or equal to (<i>Max. Cell Diameter (px)</i>)<sup>2</sup> are detected as being cells.")
@YSConfigAlias("Quick Detect")
@YSConfigClassification("cell detection")
@XStreamAlias("quick-detect-configuration")
public class QuickDetectConfiguration extends CellDetectionConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -8172201676160652226L;
	
	/**
	 * Constructor.
	 */
	public QuickDetectConfiguration()
	{
		// do nothing.
	}

	@YSConfigAlias("Internal Binning")
	@XStreamAlias("internal-binning")
	private int internalBinning = 1;

	@YSConfigAlias("Detection Threshold (0-1)")
	@XStreamAlias("detection-threshold")
	private double detectionThreshold = 0.01;
	
	@YSConfigAlias("Min. Cell Diameter (px)")
	@XStreamAlias("min-cell-diameter-px")
	private int minCellDiameter = 6;
	
	@YSConfigAlias("Max. Cell Diameter (px)")
	@XStreamAlias("max-cell-diameter-px")
	private int maxCellDiameter = 45;
	
	@YSConfigNotVisible
	@XStreamAlias("generate-detection-image")
	@XStreamAsAttribute
	@XStreamConverter(value = BooleanConverter.class, booleans = {false}, strings = {"yes", "no"})
	private boolean generateLabelImage = true;
	
	/**
	 * Sets the binning internally used by the algorithm. 
	 * @param internalBinning
	 */
	public void setInternalBinning(int internalBinning)
	{
		this.internalBinning = internalBinning;
	}

	/**
	 * Returns the binning internally used by the algorithm. 
	 * @return internal binning.
	 */
	public int getInternalBinning()
	{
		return internalBinning;
	}

	/**
	 * Sets the detection threshold (0-1) of the algorithm. Default = 0.01;
	 * @param detectionThreshold
	 */
	public void setDetectionThreshold(double detectionThreshold)
	{
		this.detectionThreshold = detectionThreshold;
	}

	/**
	 * Returns the detection threshold (0-1) of the algorithm. Default = 0.01;
	 * @return detection threshold.
	 */
	public double getDetectionThreshold()
	{
		return detectionThreshold;
	}

	/**
	 * Sets the minimal diameter in pixels a cell must have.
	 * @param minCellDiameter
	 */
	public void setMinCellDiameter(int minCellDiameter)
	{
		this.minCellDiameter = minCellDiameter;
	}

	/**
	 * Returns the minimal diameter in pixels a cell must have.
	 * @return minimal diameter in pixels
	 */
	public int getMinCellDiameter()
	{
		return minCellDiameter;
	}

	/**
	 * Sets the maximal diameter in pixels a cell must have.
	 * @param maxCellDiameter
	 */
	public void setMaxCellDiameter(int maxCellDiameter)
	{
		this.maxCellDiameter = maxCellDiameter;
	}

	/**
	 * Returns the maximal diameter in pixels a cell must have.
	 * @return maximal diameter in pixels.
	 */
	public int getMaxCellDiameter()
	{
		return maxCellDiameter;
	}

	/**
	 * The identifier for this configuration.
	 */
	public static final String	TYPE_IDENTIFIER	= "YouScope.QuickDetect";
	
	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(detectionThreshold);
		result = prime * result + (int)(temp ^ (temp >>> 32));
		result = prime * result + internalBinning;
		result = prime * result + maxCellDiameter;
		result = prime * result + minCellDiameter;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(!super.equals(obj))
			return false;
		if(getClass() != obj.getClass())
			return false;
		QuickDetectConfiguration other = (QuickDetectConfiguration)obj;
		if(Double.doubleToLongBits(detectionThreshold) != Double.doubleToLongBits(other.detectionThreshold))
			return false;
		if(internalBinning != other.internalBinning)
			return false;
		if(maxCellDiameter != other.maxCellDiameter)
			return false;
		if(minCellDiameter != other.minCellDiameter)
			return false;
		return true;
	}

	/**
	 * Set to true if a label image should be generated.
	 * @param generateDetectionImage True if detection image should be generated, otherwise false.
	 */
	public void setGenerateLabelImage(boolean generateDetectionImage)
	{
		this.generateLabelImage = generateDetectionImage;
	}

	@Override
	public boolean isGenerateLabelImage()
	{
		return generateLabelImage;
	}

	@Override
	public TableDefinition getProducedTableDefinition() 
	{
		return QuickDetectTable.getTableDefinition();
	}

}
