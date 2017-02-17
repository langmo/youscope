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
package org.youscope.plugin.fluigent;

import org.youscope.common.table.ColumnDefinition;
import org.youscope.common.table.TableDefinition;

/**
 * Class to provide information about the layout of the table produced by the Fluigent device job.
 * @author Moritz Lang
 *
 */
class FluigentStateTable
{
	/**
	 * Column specifying the ID of the flow unit whose target flow should be changed.
	 */
	public final static ColumnDefinition<Integer> COLUMN_FLOW_UNIT = ColumnDefinition.createIntegerColumnDefinition("Flow unit ID", "Zero based index of the flow unit whose actual flow rate was measured.", false);
	/**
	 * Column specifying the current flow rate in ul/min the flow unit.
	 */
	public final static ColumnDefinition<Double> COLUMN_FLOW_RATE = ColumnDefinition.createDoubleColumnDefinition("Flow rate", "Current flow rate in microliter per minute.", false);
 	
	/**
	 * Private constructor. Use static methods.
	 */
	private FluigentStateTable()
	{
		// only static methods.
	}
	
	private static TableDefinition tableDefinition = null;
	
	/**
 	 * Returns the layout of the Fluigent control table.
 	 * @return Fluigent control table layout.
 	 */
 	public static synchronized TableDefinition getTableDefinition()
 	{
 		if(tableDefinition != null)
 			return tableDefinition;
 		
 		tableDefinition = new TableDefinition("Fluigent pump system monitor table", "Table containing the current flow rates of several flow units.",
 				COLUMN_FLOW_UNIT,COLUMN_FLOW_RATE);
 		return tableDefinition;
 	}
}
