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
package org.youscope.plugin.nemesys;

/**
 * @author Moritz Lang
 *
 */
class SyringeTableRow implements Comparable<SyringeTableRow>
{
	public long time;
	public final double[] flowRates;
	public SyringeTableRow(long time, int numDosingUnits)
	{
		this.time = time;
		this.flowRates = new double[numDosingUnits];
		for(int i=0; i<flowRates.length; i++)
		{
			flowRates[i] = 0;
		}
	}
	@Override
	public int compareTo(SyringeTableRow arg0)
	{
		if(arg0 == null)
			return 1;
		else if(arg0.time > time)
			return -1;
		else if(arg0.time < time)
			return 1;
		else
			return 0;
	}
}
