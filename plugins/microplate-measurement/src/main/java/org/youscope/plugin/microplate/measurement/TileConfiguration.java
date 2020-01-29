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
package org.youscope.plugin.microplate.measurement;


import org.youscope.addon.microplate.MicroplateConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Configuration of tiles of a measurement.
 * @author Moritz Lang
 *
 */
@XStreamAlias("tile-layout")
public class TileConfiguration extends MicroplateConfiguration {

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 48209026777271327L;

	@XStreamAlias("num-tiles-x")
	private int numTilesX = 12;
	
	@XStreamAlias("num-tiles-y")
	private int numTilesY = 8;
	
	/**
	 * Returns the number of tiles in the x-direction.
	 * @return the Horizontal tile number.
	 */
	public int getNumTilesX()
	{
		return numTilesX;
	}
	
	/**
	 * Sets the number of tiles in the x-direction.
	 * @param numTilesX Horizontal tile number.
	 */
	public void setNumTilesX(int numTilesX)
	{
		this.numTilesX = numTilesX;
	}

	/**
	 * Returns the number of tiles in the y-direction.
	 * @return the Vertical tile number.
	 */
	public int getNumTilesY()
	{
		return numTilesY;
	}
	
	/**
	 * Sets the number of tiles in the y-direction.
	 * @param numTilesY Vertical tile number.
	 */
	public void setNumTilesY(int numTilesY)
	{
		this.numTilesY = numTilesY;
	}

	@Override
	public String getTypeIdentifier() {
		return "YouScope.microplate.tile";
	}
}
