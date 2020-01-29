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
package org.youscope.addon.dropletmicrofluidics;

import java.io.Serializable;

/**
 * Result of a droplet based microfluidics observer.
 * @author Moritz Lang
 *
 */
public class DropletObserverResult implements Serializable, Cloneable {

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 4729191654255897853L;
	private final int currentDroplet;
	private double[] dropletOffsets;
	/**
	 * Constructor.
	 * @param currentDroplet The ID of the current droplet. Must be smaller than dropletOffsets.length.
	 * @param dropletOffsets The estimated current droplet offsets.
	 */
	public DropletObserverResult(int currentDroplet, double[] dropletOffsets) 
	{
		this.currentDroplet = currentDroplet;
		this.dropletOffsets = dropletOffsets;
	}
	
	@Override
	public DropletObserverResult clone()
	{
		DropletObserverResult clone;
		try {
			clone = (DropletObserverResult) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Clone not supported", e); // will not happen.
		}
		clone.dropletOffsets = new double[dropletOffsets.length];
		System.arraycopy(dropletOffsets, 0, clone.dropletOffsets, 0, dropletOffsets.length);
		return clone;
	}

	/**
	 * Returns the estimated current droplet offsets.
	 * @return estimated current offsets.
	 */
	public double[] getDropletOffsets()
	{
		double[] returnVal = new double[dropletOffsets.length];
		System.arraycopy(dropletOffsets, 0, returnVal, 0, dropletOffsets.length);
		return returnVal;
	}
	
	/**
	 * Returns the offset of the droplet with the given ID.
	 * @param dropletID ID of droplet for which the offset should be returned.
	 * @return Offset of droplet.
	 * @throws IndexOutOfBoundsException Thrown if droplet ID small zero or greater equal {@link #getNumDroplets()}
	 */
	public double getDropletOffset(int dropletID) throws IndexOutOfBoundsException
	{
		return dropletOffsets[dropletID];
	}
	
	/**
	 * Returns the number of droplets.
	 * @return Number of droplets.
	 */
	public int getNumDroplets()
	{
		return dropletOffsets.length;
	}
	
	/**
	 * Returns the ID of the currently measured droplet.
	 * @return ID of currently measured droplet.
	 */
	public int getCurrentDroplet()
	{
		return currentDroplet;
	}
	
	/**
	 * Returns the estimated offset of the currently measured droplet.
	 * @return Offset of currently measured droplet.
	 */
	public double getCurrentOffset()
	{
		return dropletOffsets[currentDroplet];
	}
	
	/**
	 * Returns the mean droplet offset.
	 * @return Mean droplet offset.
	 */
	public double getMeanOffset()
	{
		double mean = 0;
		for(double offset : dropletOffsets)
		{
			mean += offset;
		}
		return mean/dropletOffsets.length;
	}
	
	
}
