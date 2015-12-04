package org.youscope.plugin.dropletmicrofluidics.flexiblecontroller;

/**
 * Defines if syringe should have a fixed flow rate, or if its flow rate should be varied either positively or negatively to correct for droplet heights.
 * @author Moritz Lang
 *
 */
public enum SyringeControlState 
{
	/**
	 * fixed flow rate.
	 */
	FIXED("keep fixed"),
	/**
	 * flow rate is kept over zero to correct for droplet height.
	 */
	POSITIVE("keep positive"),
	/**
	 * flow rate is kept below zero to correct for droplet height.
	 */
	NEGATIVE("keep negative");
	
	private final String description;
	SyringeControlState(String description)
	{
		this.description = description;
	}
	@Override
	public String toString() 
	{
		return description;
	}
}
