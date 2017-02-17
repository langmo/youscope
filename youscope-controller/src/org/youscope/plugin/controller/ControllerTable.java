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
package org.youscope.plugin.controller;

import org.youscope.common.table.ColumnDefinition;
import org.youscope.common.table.TableDefinition;
import org.youscope.common.table.TableException;

/**
 * @author Moritz Lang
 *
 */
public class ControllerTable
{
	private static final String INPUT_PREFIX = "input.";
	private static final String OUTPUT_PREFIX = "output.";
	
	/**
	 * Returns the controller table layout, for a given set of input and output columns.
	 * @param inputColumns Table columns used as inputs (null if unknown).
	 * @param outputColumns Table columns used as outputs (null if unknown). 
	 * @return Controller table layout.
	 */
	public static TableDefinition getTableDefinition(ColumnDefinition<?>[] inputColumns, ColumnDefinition<?>[] outputColumns)
	{
		if(inputColumns == null)
			inputColumns = new ColumnDefinition<?>[0];
		if(outputColumns == null)
			outputColumns = new ColumnDefinition<?>[0];
		ColumnDefinition<?>[] columns = new ColumnDefinition<?>[inputColumns.length + outputColumns.length];
		
		try
		{
			for(int i=0; i< inputColumns.length; i++)
			{
				columns[i] = ColumnDefinition.createColumnDefinition(INPUT_PREFIX + inputColumns[i].getColumnName(), inputColumns[i].getColumnDescription(),  inputColumns[i].getValueType(), true);
			}
			for(int i=0; i< outputColumns.length; i++)
			{
				columns[i] = ColumnDefinition.createColumnDefinition(OUTPUT_PREFIX + outputColumns[i].getColumnName(), outputColumns[i].getColumnDescription(),  outputColumns[i].getValueType(), true);
			}
		}
		catch(@SuppressWarnings("unused") TableException e)
		{
			// should not happen, because we only construct table columns for which we know that
			// the corresponding value type is supported, because we already got columns with the
			// given value types.
			return new TableDefinition("Controller Input/Output", "Table containing the inputs and outputs of the controller.");
		}
		
		return new TableDefinition("Controller Input/Output", "Table containing the inputs and outputs of the controller.", columns);
	}
}
