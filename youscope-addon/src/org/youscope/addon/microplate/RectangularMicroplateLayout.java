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
package org.youscope.addon.microplate;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.youscope.common.Well;
import org.youscope.common.microplate.MicroplateLayout;
import org.youscope.common.microplate.WellLayout;

/**
 * Layout for rectangular microplates, i.e. microplates consisting of a given number of rows and columns, where each
 * well's position only depends on its row and column.
 * @author Moritz Lang
 *
 */
public class RectangularMicroplateLayout implements MicroplateLayout
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -4368249923838529705L;
	private final int numWellsX;
	private final int numWellsY;
	private final double wellWidth;
	private final double wellHeight;
	/**
	 * Constructor.
	 * @param numWellsX number of wells in x-direction/columns.
	 * @param numWellsY number of wells in y-direction/rows.
	 * @param wellWidth width of wells in um. Also serves to determine distance between wells.
	 * @param wellHeight height of wells in um. Also serves to determine distance between wells.
	 */
	public RectangularMicroplateLayout(int numWellsX, int numWellsY, double wellWidth, double wellHeight)
	{
		this.numWellsX = numWellsX;
		this.numWellsY = numWellsY;
		this.wellHeight = wellHeight;
		this.wellWidth = wellWidth;
	}

	@Override
	public int getNumWells() {
		return numWellsY * numWellsX;
	}

	@Override
	public WellLayout getWell(int index) {
		if(index < 0 || index >= getNumWells())
			throw new NoSuchElementException();
		int row = index/numWellsX;
		int column = index % numWellsX;
		return new WellLayout(column * wellWidth, row * wellHeight, wellWidth, wellHeight, new Well(row, column));
	}

	@Override
	public Iterator<WellLayout> iterator() {
		return new Iterator<WellLayout>()
				{
					int index = 0;
					@Override
					public boolean hasNext() {
						return index < getNumWells();
					}

					@Override
					public WellLayout next() {
						return getWell(index++);
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
			
				};
	}

	@Override
	public WellLayout getWell(Well well) {
		if(well == null)
			return null;
		int y = well.getWellY();
		int x = well.getWellX();
		if(y < 0 || x < 0 || y >= numWellsY || x>=numWellsX)
			return null;
		return new WellLayout(x * wellWidth, y * wellHeight, wellWidth, wellHeight, well);
	}

}
