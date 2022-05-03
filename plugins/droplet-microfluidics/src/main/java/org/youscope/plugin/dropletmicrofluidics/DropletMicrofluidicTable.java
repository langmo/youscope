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
package org.youscope.plugin.dropletmicrofluidics;

import org.youscope.common.table.ColumnDefinition;
import org.youscope.common.table.TableDefinition;

/**
 * Provide information about the layout of the table generated by the droplet based microfluidics job.
 * @author Moritz Lang 
 *
 */
public class DropletMicrofluidicTable
{
	/**
	 * Column containing index (zero based) of droplet which was measured.
	 */
	public final static ColumnDefinition<Integer> COLUMN_CURRENT_DROPLET_ID = ColumnDefinition.createIntegerColumnDefinition("Current Droplet ID", "Index (zero based) of the droplet whose height was measured.", true);
	/**
	 * Column containing measured offset (in um) of droplet which was measured.
	 */
	public final static ColumnDefinition<Double> COLUMN_CURRENT_DROPLET_MEASURED_OFFSET = ColumnDefinition.createDoubleColumnDefinition("Current Droplet - Measured Offset (um)", "Delta height of currently measured droplet as compared to reference height (in um) as measured by the autofocus algorithm.", true);
	/**
	 * Column containing estimated offset (in um) of droplet which was measured.
	 */
	public final static ColumnDefinition<Double> COLUMN_CURRENT_DROPLET_ESTIMATED_OFFSET = ColumnDefinition.createDoubleColumnDefinition("Current Droplet - Estimated Offset (um)", "Delta height of currently measured droplet as compared to reference height (in um) as estimated by the observer.\n\nThis estimate is already adjusted by the measured offset.", true);
	/**
	 * Column containing estimated mean offsets (in um) of droplets.
	 */
	public final static ColumnDefinition<Double> COLUMN_DROPLETS_MEAN_OFFSET = ColumnDefinition.createDoubleColumnDefinition("Mean Estimated Offset (um)", "Estimated mean offset in um of all droplets.", true);
	
	/**
	 * Column containing index (zero based) of droplet.
	 */
	public final static ColumnDefinition<Integer> COLUMN_DROPLET_ID = ColumnDefinition.createIntegerColumnDefinition("Droplet ID", "Index (zero based) of droplet.", true);
	/**
	 * Column containing estimated offset (in um) of droplet.
	 */
	public final static ColumnDefinition<Double> COLUMN_DROPLET_ESTIMATED_OFFSET = ColumnDefinition.createDoubleColumnDefinition("Estimated Offset (um)", "Delta height of droplet with given ID as compared to reference height (in um) as estimated by observer.", true);
	
	/**
	 * Column containing delta flow (in ul/min) of all flow units together.
	 */
	public final static ColumnDefinition<Double> COLUMN_DELTA_FLOW = ColumnDefinition.createDoubleColumnDefinition("Delta Flow (ul/min)", "Delta flow (in ul/min) of flow units to correct droplet offset's.", true);
	/**
	 * Column containing index (zero based) of flow unit.
	 */
	public final static ColumnDefinition<Integer> COLUMN_FLOW_UNIT_ID = ColumnDefinition.createIntegerColumnDefinition("Flow Unit ID", "Index (zero based) of the flow unit.", true);
	/**
	 * Column containing actual flow (in ul/min) of flow unit.
	 */
	public final static ColumnDefinition<Double> COLUMN_FLOW_UNIT_FLOW_RATE = ColumnDefinition.createDoubleColumnDefinition("Flow Unit Flow Rate (ul/min)", "Flow rate (in ul/min) of the flow unit.", true);
	
	/**
	 * Use static methods.
	 */
	private DropletMicrofluidicTable()
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
		tableDefinition = new TableDefinition("Droplet-Based Microfluidic Table", 
				"Contains information about the current state of the controller and observer for droplet-based-microfluidics.",
				COLUMN_CURRENT_DROPLET_ID,
				COLUMN_CURRENT_DROPLET_MEASURED_OFFSET,
				COLUMN_CURRENT_DROPLET_ESTIMATED_OFFSET,
				COLUMN_DROPLETS_MEAN_OFFSET,
				COLUMN_DROPLET_ID,
				COLUMN_DROPLET_ESTIMATED_OFFSET,
				COLUMN_DELTA_FLOW,
				COLUMN_FLOW_UNIT_ID,
				COLUMN_FLOW_UNIT_FLOW_RATE
				);
		return tableDefinition;
	}
}