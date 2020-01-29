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
package org.youscope.common;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Class representing a well of a microplate.
 * The class is immutable.
 * @author Moritz Lang
 * 
 */
@XStreamAlias("well")
public final class Well implements Serializable, Comparable<Well>
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -8352497094146421375L;

	@Override
	public int hashCode()
	{
		final int prime = 1000;
		int result = 100 + wellX;
		result = prime * result + wellY;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		Well other = (Well)obj;
		if(wellX != other.wellX)
			return false;
		if(wellY != other.wellY)
			return false;
		return true;
	}

	/**
	 * X-position of well, starting at 0.
	 */
	@XStreamAlias("well-x")
	@XStreamAsAttribute
	private final int	wellX;
	/**
	 * Y-position of well, starting at 0.
	 */
	@XStreamAlias("well-y")
	@XStreamAsAttribute
	private final int	wellY;

	/**
	 * Constructor.
	 * @param wellX X-position of well, starting at 0.
	 * @param wellY Y-position of well, starting at 0.
	 */
	public Well(int wellY, int wellX)
	{
		this.wellX = wellX;
		this.wellY = wellY;
	}

	/**
	 * Clone constructor.
	 * @param parent The well which should be cloned.
	 */
	public Well(Well parent)
	{
		this.wellX = parent.wellX;
		this.wellY = parent.wellY;
	}

	/**
	 * Returns the X-position of the well, starting at 0.
	 * @return x-position of well.
	 */
	public int getWellX()
	{
		return wellX;
	}

	/**
	 * Returns the Y-position of the well, starting at 0.
	 * @return y-position of well.
	 */
	public int getWellY()
	{
		return wellY;
	}

	/**
	 * Returns the name of a well in the x-direction.
	 * @return Name of the x-position of the well. The name is identical to wellX + 1.
	 */
	public String getXWellName()
	{
		return getXWellName(wellX);
	}

	/**
	 * Returns the name of a well in the x-direction.
	 * @param wellX The x-position of the well, starting at 0.
	 * @return Name of the x-position of the well. The name is identical to wellX + 1.
	 */
	public static String getXWellName(int wellX)
	{
		return Integer.toString(wellX + 1);
	}

	/**
	 * Returns the name of a well in the y-direction. The well with the wellY value of 0 corresponds to "A", with 1 to "B", and so on.
	 * After the name "Z", the next well is named "AA", then "AB", and so on.
	 * @return Name of y-position of the well.
	 */
	public String getYWellName()
	{
		return getYWellName(wellY);
	}

	/**
	 * Returns the name of a well in the y-direction. The well with the wellY value of 0 corresponds to "A", with 1 to "B", and so on.
	 * After the name "Z", the next well is named "AA", then "AB", and so on.
	 * @param wellY The y-position of the well, starting at 0.
	 * @return Name of y-position of the well.
	 */
	public static String getYWellName(int wellY)
	{
		char AChar = 'A';
		char ZChar = 'Z';
		double numChars = ZChar - AChar + 1;
		String returnVal = "";
		int wellYTemp = wellY;
		while(true)
		{
			returnVal = (char)(wellYTemp % numChars + AChar) + returnVal;
			wellYTemp = (int)Math.floor(wellYTemp / numChars - 1);
			if(wellYTemp < 0)
				break;
		}

		return returnVal;
	}

	/**
	 * Returns the common name for a well, e.g. "B3" for the well with wellY=1 and wellX=4 (remark: internally, the numbering of the wells starts with 0.).
	 * Same as getYWellName() + getXWellName().
	 * @return Common name of the well.
	 */
	public String getWellName()
	{
		return getYWellName() + getXWellName();
	}
	
	@Override
	public String toString()
	{
		return getWellName();
	}

	/**
	 * Returns the common name for a well, e.g. "B3" for the well with wellY=1 and wellX=4 (remark: internally, the numbering of the wells starts with 0.).
	 * Same as getYWellName(wellY) + getXWellName(wellX).
	 * @param wellY The y-position of the well, starting at 0.
	 * @param wellX The x-position of the well, starting at 0.
	 * @return Common name of the well.
	 */
	public static String getWellName(int wellY, int wellX)
	{
		return getYWellName(wellY) + getXWellName(wellX);
	}

	@Override
	public int compareTo(Well arg0)
	{
		if(arg0.getWellY() > getWellY())
			return -1;
		else if(arg0.getWellY() < getWellY())
			return 1;
		else if(arg0.getWellX() > getWellX())
			return -1;
		else if(arg0.getWellX() < getWellX())
			return 1;
		else
			return 0;
	}
}
