/**
 * 
 */
package ch.ethz.csb.youscope.addon.nemesys;

import ch.ethz.csb.youscope.shared.table.ColumnDefinition;
import ch.ethz.csb.youscope.shared.table.TableDefinition;

/**
 * Class to provide information about the layout of the table consumed by the Nemesys controller job.
 * @author Moritz Lang
 *
 */
public class NemesysControlTable
{
	/**
	 * Column specifying the ID (zero based) of the flow unit whose target flow should be changed.
	 */
	public final static ColumnDefinition<Integer> COLUMN_FLOW_UNIT = ColumnDefinition.createIntegerColumnDefinition("Flow unit ID", "Zero based index of the flow unit to change the flow of.", false);
	/**
	 * Column specifying the target flow rate in ul/min the flow unit should be set to.
	 */
	public final static ColumnDefinition<Double> COLUMN_FLOW_RATE = ColumnDefinition.createDoubleColumnDefinition("Flow rate", "Target flow rate in microliter per minute.", false);
 	
	/**
	 * Private constructor. Use static methods.
	 */
	private NemesysControlTable()
	{
		// only static methods.
	}
	
	private static TableDefinition tableDefinition = null;
	
	/**
 	 * Returns the layout of the Nemesys control table.
 	 * @return Nemesys control table layout.
 	 */
 	public static synchronized TableDefinition getTableDefinition()
 	{
 		if(tableDefinition != null)
 			return tableDefinition;
 		
 		tableDefinition = new TableDefinition("Nemesys syringe system control table", "Table containing the target flow rates of several syringe units.\n If a target flow rate for an existing syringe unit is not set, its flow is kept unmodified.\nIf the target flow rate of a syringe is set which does not exist (i.e. Flow Unit ID is invalid), an error is thrown.",
 				COLUMN_FLOW_UNIT,COLUMN_FLOW_RATE);
 		return tableDefinition;
 	}
}
