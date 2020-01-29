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
package org.youscope.plugin.travelingsalesman;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * The metric to use in the path optimization.
 * @author Moritz Lang
 *
 */
public enum MetricType 
{
	/**
	 * Manhatten metric or L1 norm.
	 */
	@XStreamAlias("manhatten")
	MANHATTEN("L1 (Manhatten)"),
	/**
	 * Euclidean metric or L2 norm
	 */
	@XStreamAlias("euclidean")
	EUCLIDEAN("L2 (Euclidean)"),
	/**
	 * Maximum metric or Linf norm.
	 */
	@XStreamAlias("maximum")
	MAXIMUM("Linf (Maximum)");
	
	private final String description;
	MetricType(String description)
	{
		this.description = description;
	}
	
	@Override
	public String toString()
	{
		return description;
	}
}
