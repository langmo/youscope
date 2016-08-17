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
