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
package org.youscope.uielements;

/**
 * Enumeration of typical units used to measure time in YouScope, and their conversion to milliseconds.
 * @author Moritz Lang
 *
 */
public enum TimeUnit
{
	/**
	 * A milli second.
	 */
	MILLI_SECOND("ms", 1),
	/**
	 * A second = 1000 ms.
	 */
	SECOND("s", 1000),
	/**
	 * A minute = 60 * 1000 ms.
	 */
	MINUTE("min", 60 * 1000),
	/**
	 * An hour = 60 * 60 * 1000 ms.
	 */
	HOUR("h", 60*60*1000),
	/**
	 * A day = 24 * 60 * 60 * 1000 ms.
	 */
	DAY("d", 24*60*60*1000);
	
	private final String name;
	private final int unitToMs;
	private TimeUnit(String name, int unitToMs)
	{
		this.name = name;
		this.unitToMs = unitToMs;
	}
	
	@Override
	public String toString()
	{
		return name;
	}
	
	/**
	 * Returns the SI name of the unit.
	 * @return SI name of the unit.
	 */
	public String getSIUnit()
	{
		return name;
	}
	
	/**
	 * Returns the amount of milli seconds one "piece" of unit has.
	 * @return Amount of millisecond of one unit.
	 */
	public int getUnitToMs()
	{
		return unitToMs;
	}
	
	/**
	 * Returns the amount of time in the given unit.
	 * @param timeInMs The time in ms for which the time in the given unit should be calculated.
	 * @return Time in the given unit.
	 */
	public double toUnit(long timeInMs)
	{
		return ((double)timeInMs) / getUnitToMs();
	}
	
	/**
	 * Returns the time in ms a given quantity of time in this unit corresponds to.
	 * @param timeInUnit Time in the given unit.
	 * @return Time in ms.
	 */
	public long toMs(double timeInUnit)
	{
		return (long)(timeInUnit * getUnitToMs());
	}
	
	/**
	 * Returns true if the given time in ms is an integer value in the given unit.
	 * @param timeInMs Time in ms.
	 * @return True, if the given time is an integer value in the given unit.
	 */
	public boolean isIntegerInUnit(long timeInMs)
	{
		return timeInMs % getUnitToMs() == 0;
	}
}
