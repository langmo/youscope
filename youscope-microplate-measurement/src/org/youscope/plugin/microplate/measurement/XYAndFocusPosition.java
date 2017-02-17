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
package org.youscope.plugin.microplate.measurement;

import java.io.Serializable;


/**
 * The exact position (x, y, and focus position) of a well or tile.
 * @author Moritz Lang
 * 
 */
public class XYAndFocusPosition extends java.awt.geom.Point2D.Double implements Cloneable, Serializable, Comparable<XYAndFocusPosition>
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 2594269668636226236L;
	/**
	 * The focus position.
	 */
	private double				focus				= 0;

	/**
	 * Constructor.
	 * Sets the x, y position to the given values and focus to {@link java.lang.Double#NaN}.
	 * @param x The x position.
	 * @param y The y position
	 */
	public XYAndFocusPosition(double x, double y)
	{
		this(x, y, java.lang.Double.NaN);
	}

	/**
	 * Constructor.
	 * Sets the x, y, and focus position to the given values.
	 * @param x The x position in um.
	 * @param y The y position in um.
	 * @param focus The focus position. Typically in um, but some focus devices measure in different units. Set to {@link java.lang.Double#NaN} to not store the focus position.
	 */
	public XYAndFocusPosition(double x, double y, double focus)
	{
		this.x = x;
		this.y = y;
		this.focus = focus;
	}

	/**
	 * Sets the focus position. Typically in um, but some focus devices measure in different units. Set to {@link java.lang.Double#NaN} to not store the focus position.
	 * @param focus the focus position.
	 */
	public void setFocus(double focus)
	{
		this.focus = focus;
	}

	/**
	 * Returns the focus position. Typically in um, but some focus devices measure in different units. Returns {@link java.lang.Double#NaN} if focus position is not stored.
	 * @return the focus position.
	 */
	public double getFocus()
	{
		return focus;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = java.lang.Double.doubleToLongBits(focus);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		XYAndFocusPosition other = (XYAndFocusPosition) obj;
		if (java.lang.Double.doubleToLongBits(focus) != java.lang.Double.doubleToLongBits(other.focus))
			return false;
		return true;
	}

	@Override
	public Object clone()
	{
		return super.clone();
	}

	@Override
	public int compareTo(XYAndFocusPosition o) {
		if(getY() != o.getY())
			return getY() < o.getY() ? -1 : 1;
		else if(getX() != o.getX())
			return getX() < o.getX() ? -1 : 1;
		else if(getFocus() != o.getFocus())
			return getFocus() < o.getFocus() ? -1 : 1;
		else
			return 0;
	}
}
