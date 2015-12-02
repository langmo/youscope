package ch.ethz.csb.youscope.addon.quickdetect;

import ch.ethz.csb.youscope.shared.addon.celldetection.CellDetectionTableColumns;
import ch.ethz.csb.youscope.shared.table.TableDefinition;

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
