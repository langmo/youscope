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
package org.youscope.plugin.dropletmicrofluidics.flexiblecontroller;

import java.io.Serializable;

/**
 * A row defining when which syringes have which flows, and which are used to correct droplet heights.
 * @author Moritz Lang
 *
 */
class FlexibleSyringeTableRow implements Serializable, Cloneable, Comparable<FlexibleSyringeTableRow>
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 2609521164408226064L;
	
	private long startTimeMS;
	private double[] targetFlowRates;
	private SyringeControlState[] syringeControlStates;
	private double maxDeltaFlowRate;
	protected FlexibleSyringeTableRow()
	{
		this.startTimeMS = 0;
		this.targetFlowRates = new double[0];
		this.syringeControlStates = new SyringeControlState[0];
		this.maxDeltaFlowRate = 0;
	}
	FlexibleSyringeTableRow(long startTimeMS, int numSyringes)
	{
		this.startTimeMS= startTimeMS;
		this.targetFlowRates = new double[numSyringes];
		this.syringeControlStates = new SyringeControlState[numSyringes];
		for(int i=0; i<numSyringes; i++)
		{
			syringeControlStates[i] = SyringeControlState.FIXED;
		}
		this.maxDeltaFlowRate = 0;
	}
	
	public int getNumSyringes()
	{
		return targetFlowRates.length;
	}
	public long getStartTimeMS() {
		return startTimeMS;
	}
	public void setStartTimeMS(long startTimeMS) {
		this.startTimeMS = startTimeMS;
	}
	public SyringeControlState[] getSyringeControlStates() {
		return syringeControlStates;
	}
	public SyringeControlState getSyringeControlState(int syringeID) 
	{
		return syringeControlStates[syringeID];
	}
	public void setSyringeControlState(int syringeID, SyringeControlState newState)
	{
		syringeControlStates[syringeID] = newState;
	}
	
	public double[] getTargetFlowRates() {
		return targetFlowRates;
	}
	public double getTargetFlowRate(int syringeID) 
	{
		return targetFlowRates[syringeID];
	}
	public void setTargetFlowRate(int syringeID, double newRate)
	{
		targetFlowRates[syringeID] = newRate;
	}
	
	@Override
	public FlexibleSyringeTableRow clone()
	{
		FlexibleSyringeTableRow clone;
		try {
			clone = (FlexibleSyringeTableRow) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Clone not supported.", e); // will not happen.
		}
		clone.syringeControlStates = new SyringeControlState[syringeControlStates.length];
		clone.targetFlowRates = new double[targetFlowRates.length];
		for(int i=0; i<syringeControlStates.length; i++)
		{
			clone.syringeControlStates[i] = syringeControlStates[i];
			clone.targetFlowRates[i] = targetFlowRates[i];
		}
		return clone;
	}
	@Override
	public int compareTo(FlexibleSyringeTableRow o) 
	{
		return o==null ? -1 : (int) (startTimeMS-o.startTimeMS);
	}
	public double getMaxDeltaFlowRate() {
		return maxDeltaFlowRate;
	}
	public void setMaxDeltaFlowRate(double maxDeltaFlowRate) {
		this.maxDeltaFlowRate = maxDeltaFlowRate;
	}

}
