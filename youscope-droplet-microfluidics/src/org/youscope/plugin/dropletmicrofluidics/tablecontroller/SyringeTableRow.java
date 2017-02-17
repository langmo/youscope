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

import java.io.Serializable;
import java.util.Arrays;

/**
 * A row defining when which syringes are active.
 * @author Moritz Lang
 *
 */
class SyringeTableRow implements Serializable, Cloneable, Comparable<SyringeTableRow>
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 2609521164408226064L;
	
	private long startTimeMS;
	private SyringeState[] syringeStates;
	SyringeTableRow()
	{
		this.startTimeMS = 0;
		this.syringeStates = new SyringeState[0];
	}
	SyringeTableRow(long startTimeMS, SyringeState[] syringeStates)
	{
		this.startTimeMS= startTimeMS;
		this.syringeStates = syringeStates;
	}
	
	public int getNumSyringes()
	{
		return syringeStates.length;
	}
	public long getStartTimeMS() {
		return startTimeMS;
	}
	public void setStartTimeMS(long startTimeMS) {
		this.startTimeMS = startTimeMS;
	}
	public SyringeState[] getSyringeStates() {
		return syringeStates;
	}
	public SyringeState getSyringeState(int syringeID) 
	{
		return syringeStates[syringeID];
	}
	public void setSyringeState(int syringeID, SyringeState newState)
	{
		syringeStates[syringeID] = newState;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (int) (prime * result + startTimeMS);
		result = prime * result + Arrays.hashCode(syringeStates);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SyringeTableRow other = (SyringeTableRow) obj;
		if (startTimeMS != other.startTimeMS)
			return false;
		if (!Arrays.equals(syringeStates, other.syringeStates))
			return false;
		return true;
	}
	public void setSyringeStates(SyringeState[] syringeStates) {
		this.syringeStates = syringeStates;
	}
	
	@Override
	public SyringeTableRow clone()
	{
		SyringeTableRow clone;
		try {
			clone = (SyringeTableRow) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Clone not supported.", e); // will not happen.
		}
		clone.syringeStates = new SyringeState[syringeStates.length];
		for(int i=0; i<syringeStates.length; i++)
		{
			clone.syringeStates[i] = syringeStates[i];
		}
		return clone;
	}
	@Override
	public int compareTo(SyringeTableRow o) 
	{
		
		return o==null ? -1 : (int) (startTimeMS-o.startTimeMS);
	}

}
