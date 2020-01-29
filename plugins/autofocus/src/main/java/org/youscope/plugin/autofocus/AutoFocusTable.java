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
package org.youscope.plugin.autofocus;

import org.youscope.common.table.ColumnDefinition;
import org.youscope.common.table.TableDefinition;

/**
 * Provide information about the layout of the table filled with the autofocus scores.
 * @author Moritz Lang 
 *
 */
public class AutoFocusTable
{
	/**
	 * Column containing index (one based) of focus search step.
	 */
	public final static ColumnDefinition<Integer> COLUMN_FOCUS_STEP = ColumnDefinition.createIntegerColumnDefinition("Focus Step", "Index of focus step, starting at one.", false);
	/**
	 * Column containing focus position in um relative to initial focus position of the focus search step.
	 */
	public final static ColumnDefinition<Double> COLUMN_RELATIVE_FOCUS = ColumnDefinition.createDoubleColumnDefinition("Relative Focus", "Focus position relative to the position where the focus algorithm is started.", false);
	/**
	 * Column containing absolute focus position in um of the focus search step.
	 */
	public final static ColumnDefinition<Double> COLUMN_ABSOLUTE_FOCUS = ColumnDefinition.createDoubleColumnDefinition("Absolute Focus", "Absolute focus position as defined by the focus device.", false);
	/**
	 * Column containing score of the focus position of the focus search step.
	 */
	public final static ColumnDefinition<Double> COLUMN_FOCUS_SCORE = ColumnDefinition.createDoubleColumnDefinition("Focus Score", "Score of the focal plane.\nThe higher the score, the more in focus are the images taken at the given focus position.", false);
	/**
	 * Column containing the index (one based) of focus search step which turned out to have the highest score.
	 */
	public final static ColumnDefinition<Integer> COLUMN_FOCAL_PLANE_FOCUS_STEP = ColumnDefinition.createIntegerColumnDefinition("Focal Plane: Focus Step",    "Information about the optimal focal plane.\nIndex of focus step which turned out to be optimal, starting at one.", true);
	/**
	 * Column containing focus position in um relative to initial focus position of the focus search step which turned out to have the highest score.
	 */
	public final static ColumnDefinition<Double> COLUMN_FOCAL_PLANE_RELATIVE_FOCUS = ColumnDefinition.createDoubleColumnDefinition("Focal Plane: Relative Focus", "Information about the optimal focal plane.\nFocus position relative to the position where the focus algorithm is started.",  true);
	/**
	 * Column containing absolute focus position in um of the focus search step which turned out to have the highest score.
	 */
	public final static ColumnDefinition<Double> COLUMN_FOCAL_PLANE_ABSOLUTE_FOCUS = ColumnDefinition.createDoubleColumnDefinition("Focal Plane: Absolute Focus", "Information about the optimal focal plane.\nAbsolute focus position as defined by the focus device.", true);
	/**
	 * Column containing score of the focus position of the focus search step which turned out to have the highest score.
	 */
	public final static ColumnDefinition<Double> COLUMN_FOCAL_PLANE_FOCUS_SCORE = ColumnDefinition.createDoubleColumnDefinition("Focal Plane: Focus Score", "Information about the optimal focal plane.\nScore of the focal plane.\nThe higher the score, the more in focus are the images taken at the given focus position.", true);
	
	
	/**
	 * Use static methods.
	 */
	private AutoFocusTable()
	{
		// use static methods.
	}
	
	private static TableDefinition tableDefinition = null;
	
	/**
	 * Returns the table layout for the autofocus table.
	 * @return Layout for the autofocus table.
	 */
	public synchronized static TableDefinition getTableDefinition()
	{
		if(tableDefinition != null)
			return tableDefinition;
		tableDefinition = new TableDefinition("Autofocus Results", 
				"Contains information about the focus score of every single autofocus search step in separate rows, as well as of the found (optimal) focal plane in the first row only.",
				COLUMN_FOCUS_STEP,
				COLUMN_RELATIVE_FOCUS,
				COLUMN_ABSOLUTE_FOCUS,
				COLUMN_FOCUS_SCORE,
				COLUMN_FOCAL_PLANE_FOCUS_STEP,
				COLUMN_FOCAL_PLANE_RELATIVE_FOCUS,
				COLUMN_FOCAL_PLANE_ABSOLUTE_FOCUS,
				COLUMN_FOCAL_PLANE_FOCUS_SCORE
				);
		return tableDefinition;
	}
}
