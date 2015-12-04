package org.youscope.plugin.dropletmicrofluidics.tablecontroller;

/**
 * State of a syringe, i.e. if it is inactive, or used to set inflow or outflow.
 * @author Moritz Lang
 *
 */
public enum SyringeState 
{
	/**
	 * inactive (flow rate = 0).
	 */
	INACTIVE("inactive"),
	/**
	 * used for inflow.
	 */
	INFLOW("inflow"),
	/**
	 * used for outflow
	 */
	OUTFLOW("outflow");
	
	private final String description;
	SyringeState(String description)
	{
		this.description = description;
	}
	@Override
	public String toString() 
	{
		return description;
	}
}
