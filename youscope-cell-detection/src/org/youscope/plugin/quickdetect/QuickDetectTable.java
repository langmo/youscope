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
package org.youscope.plugin.quickdetect;

import org.youscope.addon.celldetection.CellDetectionTableColumns;
import org.youscope.common.table.TableDefinition;

/**
 * Provides information on the layout of the tables produced by the quick detect algorithm.
 * @author Moritz Lang
 *
 */
public class QuickDetectTable {

	/**
	 * Use static methods.
	 */
	private QuickDetectTable()
	{
		// use static methods.
	}
	
	private static TableDefinition tableDefinition = null;
	
	/**
	 * Returns the table layout for the quick-detect cell-detecktion algorithm.
	 * @return Layout for the cell-detection table.
	 */
	public synchronized static TableDefinition getTableDefinition()
	{
		if(tableDefinition != null)
			return tableDefinition;
		tableDefinition = new TableDefinition("Cell-Detection Results", 
				"Contains information about the cells detected in the images by the Quick-Detect algorithm.",
				CellDetectionTableColumns.TABLE_COLUMN_CELL_ID,
				CellDetectionTableColumns.TABLE_COLUMN_QUANTIFICATION_IMAGE_ID,
				CellDetectionTableColumns.TABLE_COLUMN_XPOSITION_PX,
				CellDetectionTableColumns.TABLE_COLUMN_YPOSITION_PX,
				CellDetectionTableColumns.TABLE_COLUMN_AREA_PX,
				CellDetectionTableColumns.TABLE_COLUMN_FLUORESCENCE);
		return tableDefinition;		
	}
}
