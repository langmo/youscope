/**
 * 
 */
package org.youscope.common.table;

/**
 * Interface each measurement component configuration should implement if its corresponding component consumes table data.
 * By implementing this interface one accepts the contract that each component created as an artifact of this configuration also implements {@link TableConsumer}, returning the same information as returned here.
 * 
 * @author Moritz Lang
 * 
 */
public interface TableConsumerConfiguration
{
	/**
	 * Returns a information about the layout of the tables which are consumed by the TableConsumer, e.g. the number and types of columns.
	 * 
	 * @return Information about of the layout of the consumed tables.
	 */
	public TableDefinition getConsumedTableDefinition();
}
