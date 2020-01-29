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
