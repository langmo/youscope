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
