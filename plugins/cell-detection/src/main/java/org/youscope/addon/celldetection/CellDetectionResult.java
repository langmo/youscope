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
package org.youscope.addon.celldetection;

import java.io.Serializable;

import org.youscope.common.image.ImageEvent;
import org.youscope.common.table.Table;

/**
 * Result of a cell detection algorithm.
 * @author Moritz Lang
 *
 */
public class CellDetectionResult implements Serializable, Cloneable
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 8995560180318058006L;

	private final Table cellTable;
	private final ImageEvent<?> labelImage;
	
	/**
	 * Constructor.
	 * Same as CellDetectionResult(cellTable, null);
	 * @param cellTable Table containing information about segmented cells.
	 */
	public CellDetectionResult(Table cellTable)
	{
		this(cellTable, null);
	}
	
	/**
	 * Constructor.
	 * @param cellTable Table containing information about segmented cells.
	 * @param labelImage The label image. Set to null if not available.
	 */
	public CellDetectionResult(Table cellTable, ImageEvent<?> labelImage)
	{
		if(cellTable == null)
			throw new NullPointerException("Cell table must not be null");
		this.cellTable = cellTable;
		this.labelImage = labelImage;
	}
	
	@Override
	public CellDetectionResult clone()
	{
		Table cellTableCopy = cellTable.clone();
		ImageEvent<?> labelImageCopy = labelImage != null ? labelImage.clone() : null;
		return new CellDetectionResult(cellTableCopy, labelImageCopy);
	}

	/**
	 * Returns the cell detection table which contain information about the detected cells, e.g. their positions.
	 * Each row represents a detected cell, respectively a cell in a fluorescence channel. The columns represent a date about the respective cell, e.g. its x-position.
	 * @return Table of detected cells.
	 */
	public Table getCellTable()
	{
		return cellTable;
	}

	/**
	 * Returns the detection image. The detection image is an image of the same size as the original image,
	 * in which the whole background (each pixel not corresponding to a cell) has a value of 0, and each pixel belonging to
	 * a given cell has the same intensity value as the respective cell number.
	 * @return Label image, or null, if none available/generated..
	 */
	public ImageEvent<?> getLabelImage()
	{
		return labelImage;
	}
}
