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
 * Class to provide information about the layout of the table consumed by the Fluigent table consumer.
 * @author Moritz Lang
 *
 */
public class FluigentControlTable
{
	/**
	 * Column specifying the ID of the flow unit whose target flow should be changed.
	 */
	public final static ColumnDefinition<Integer> COLUMN_FLOW_UNIT = ColumnDefinition.createIntegerColumnDefinition("Flow unit ID", "Zero based index of the flow unit to change the flow of.", false);
	/**
	 * Column specifying the target flow rate in ul/min the flow unit should be set to.
	 */
	public final static ColumnDefinition<Double> COLUMN_FLOW_RATE = ColumnDefinition.createDoubleColumnDefinition("Flow rate", "Target flow rate in microliter per minute.", false);
 	
	/**
	 * Private constructor. Use static methods.
	 */
	private FluigentControlTable() 
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
 		
 		tableDefinition = new TableDefinition("Fluigent pump system control table", "Table containing the target flow rates of several flow units.\n If a target flow rate for an existing flow unit is not set, its flow is kept unmodified.\nIf the target flow rate of a flow unit is set which does not exist (i.e. Flow Unit ID is invalid), an error is thrown.",
 				COLUMN_FLOW_UNIT,COLUMN_FLOW_RATE);
 		return tableDefinition;
 	}
}
