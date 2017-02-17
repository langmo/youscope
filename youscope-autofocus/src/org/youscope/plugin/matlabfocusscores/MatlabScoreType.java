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
package org.youscope.plugin.matlabfocusscores;

/**
 * @author Moritz Lang
 *
 */
public enum MatlabScoreType
{
	/**
	 * Histogram range algorithm.
	 */
	HISTOGRAM_RANGE("Histogram Range"),
	/**
	 * Sobel edge detection.
	 */
	SOBEL3("Sobel 3x3"),
	/**
	 * Sobel edge detection.
	 */
	SOBEL5("Sobel 5x5"),
	/**
	 * Sobel edge detection.
	 */
	SOBEL7("Sobel 7x7"),
	/**
	 * Normalized variances.
	 */
	NORMALIZED_VARIANCES("Normalized Variances");
	
	
	private final String description;
	MatlabScoreType(String description)
	{
		this.description = description;
	}
	
	/**
	 * Returns a human readable short description of the algorithm.
	 * @return Short algorithm description.
	 */
	public String getDescription()
	{
		return description;
	}
	
	@Override
	public String toString()
	{
		return description;
	}
}
