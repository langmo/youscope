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
package org.youscope.plugin.rectangularmicroplates;

import org.youscope.addon.microplate.MicroplateConfiguration;
import org.youscope.common.configuration.YSConfigAlias;
import org.youscope.common.configuration.YSConfigDoubleRange;
import org.youscope.common.configuration.YSConfigIntegerRange;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Configuration of a rectangular microplate.
 * @author Moritz Lang
 *
 */
@YSConfigAlias("Rectangular Microplate")
@XStreamAlias("rectangular-microplate")
public class RectangularMicroplateConfiguration extends MicroplateConfiguration {

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 1375820755467997821L;

	@YSConfigAlias("Number of wells horizontally")
	@YSConfigIntegerRange(minValue=1)
	@XStreamAlias("num-wells-x")
	private int numWellsX = 12;
	
	@YSConfigAlias("Number of wells vertically")
	@YSConfigIntegerRange(minValue=1)
	@XStreamAlias("num-wells-y")
	private int numWellsY = 8;
	
	@YSConfigAlias("Width of/horizontal distance between wells (um)")
	@YSConfigDoubleRange(minValue=Double.MIN_NORMAL*10)
	@XStreamAlias("well-width")
	private double wellWidth = 9000.;
	
	@YSConfigAlias("Height of/vertical distance between wells (um)")
	@YSConfigDoubleRange(minValue=Double.MIN_NORMAL*10)
	@XStreamAlias("well-height")
	private double wellHeight = 9000.;
	/**
	 * Returns the number of wells in the x-direction.
	 * @return the Horizontal well number.
	 */
	public int getNumWellsX()
	{
		return numWellsX;
	}
	
	/**
	 * Sets the number of wells in the x-direction.
	 * @param numWellsX Horizontal well number.
	 */
	public void setNumWellsX(int numWellsX)
	{
		this.numWellsX = numWellsX;
	}

	/**
	 * Returns the number of wells in the y-direction.
	 * @return the Vertical well number.
	 */
	public int getNumWellsY()
	{
		return numWellsY;
	}
	
	/**
	 * Sets the number of wells in the y-direction.
	 * @param numWellsY Vertical well number.
	 */
	public void setNumWellsY(int numWellsY)
	{
		this.numWellsY = numWellsY;
	}

	/**
	 * Returns the width of one well (distance of well centers) in mu;
	 * @return width of one well.
	 */
	public double getWellWidth()
	{
		return wellWidth;
	}
	
	/**
	 * Sets the width of one well (distance of well centers) in mu;
	 * @param wellWidth width of one well.
	 */
	public void setWellWidth(double wellWidth)
	{
		this.wellWidth =  wellWidth;
	}

	/**
	 * Returns the height of one well (distance of well centers) in mu;
	 * @return height of one well.
	 */
	public double getWellHeight()
	{
		return wellHeight;
	}
	
	/**
	 * Sets the height of one well (distance of well centers) in mu;
	 * @param wellHeight height of one well.
	 */
	public void setWellHeight(double wellHeight)
	{
		this.wellHeight =  wellHeight;
	}

	/**
	 * Type identifier.
	 */
	public static final String TYPE_IDENTIFIER = "YouScope.microplate.RectangularMicroplate";
	
	@Override
	public String getTypeIdentifier() {
		return TYPE_IDENTIFIER;
	}
}
