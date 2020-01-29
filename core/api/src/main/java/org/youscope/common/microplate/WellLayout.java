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
package org.youscope.common.microplate;

import java.io.Serializable;
import java.security.InvalidParameterException;

import org.youscope.common.Well;

/**
 * Layout (dimensions, position and logical position) of a well in a microplate. Typically, the microscope's stage is directed to the center of this well.
 * This class is immutable.
 * @author Moritz Lang
 *
 */
public final class WellLayout implements Serializable, Comparable<WellLayout>
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 8252698070781291610L;
	private final double x;
	private final double y;
	private final double width; 
	private final double height;
	private final Well well;
	/**
	 * Constructor.
	 * @param x x-coordinate of the well in um, i.e. the left-most position of the well.
	 * @param y y-coordinate of the well in um, i.e. to top-most position of the well.
	 * @param width width in um of the well, i.e. x+width = right most position of the well.
	 * @param height eight in um of the well, i.e. y+height = bottom most position of the well.
	 * @param well Well for which this is the layout.
	 */
	public WellLayout(double x, double y, double width, double height, Well well)
	{
		if(well == null)
			throw new NullPointerException();
		else if(width < Double.MIN_VALUE)
			throw new InvalidParameterException("The width must be positive and non-zero.");
		else if(height < Double.MIN_VALUE)
			throw new InvalidParameterException("The height must be positive and non-zero.");
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.well = well;
	}
	/**
	 * Returns the x-coordinate of the well in um, i.e. the left-most position of the well.
	 * @return x-coordinate of the well.
	 */
	public double getX()
	{
		return x;
	}
	
	/**
	 * Returns the the y-coordinate of the well in um, i.e. to top-most position of the well.
	 * @return y-coordinate of the well.
	 */
	public double getY()
	{
		return y;
	}
	
	/**
	 * Returns the width in um of the well, i.e. x+width = right most position of the well.
	 * @return width of the well.
	 */
	public double getWidth()
	{
		return width;
	}
	/**
	 * Returns the height in um of the well, i.e. y+height = bottom most position of the well.
	 * @return width of the well.
	 */
	public double getHeight()
	{
		return height;
	}
	/**
	 * Returns the well identifier for which this is the layout.
	 * @return Well identifier of layout.
	 */
	public Well getWell()
	{
		return well;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(height);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((well == null) ? 0 : well.hashCode());
		temp = Double.doubleToLongBits(width);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		WellLayout other = (WellLayout) obj;
		if (Double.doubleToLongBits(height) != Double.doubleToLongBits(other.height))
			return false;
		if (well == null) {
			if (other.well != null)
				return false;
		} else if (!well.equals(other.well))
			return false;
		if (Double.doubleToLongBits(width) != Double.doubleToLongBits(other.width))
			return false;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}
	/**
	 * Returns true if this is the layout of the provided well identifier, false otherwise.
	 * @param well the well identifier.
	 * @return true if this is the layout of the provided well identifier.
	 */
	public boolean isLayoutOfWell(Well well)
	{
		return this.well.equals(well);
	}
	@Override
	public int compareTo(WellLayout o) {
		return well.compareTo(o.getWell());
	}
}
