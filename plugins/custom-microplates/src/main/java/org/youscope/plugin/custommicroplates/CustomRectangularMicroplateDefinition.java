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
package org.youscope.plugin.custommicroplates;

class CustomRectangularMicroplateDefinition implements CustomMicroplateDefinition
{
	private int numWellsX = 12;
	private int numWellsY = 8;
	private double wellWidth = 9000.;
	private double wellHeight = 9000.;
	private String customMicroplateName = "unnamed";
	public int getNumWellsX()
	{
		return numWellsX;
	}
	
	public void setNumWellsX(int numWellsX)
	{
		this.numWellsX = numWellsX;
	}

	public int getNumWellsY()
	{
		return numWellsY;
	}
	
	public void setNumWellsY(int numWellsY)
	{
		this.numWellsY = numWellsY;
	}

	public double getWellWidth()
	{
		return wellWidth;
	}
	
	public void setWellWidth(double wellWidth)
	{
		this.wellWidth =  wellWidth;
	}

	public double getWellHeight()
	{
		return wellHeight;
	}
	
	public void setWellHeight(double wellHeight)
	{
		this.wellHeight =  wellHeight;
	}

	@Override
	public void setCustomMicroplateName(String customMicroplateName)
	{
		this.customMicroplateName = customMicroplateName;
	}

	@Override
	public String getCustomMicroplateName()
	{
		return customMicroplateName;
	}

}
