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
package org.youscope.common.table;

/**
 * A table entry containing a long.
 * @author Moritz Lang
 *
 */
class TableLongEntry extends TableEntryAdapter<Long> 
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -311739064279371190L;

	public TableLongEntry(Long value, boolean nullAllowed) throws NullPointerException, TableException 
	{
		super(value, nullAllowed, Long.class);
	}

	@Override
	protected Long cloneValue(Long value) 
	{
		// Longs are immutable
		return value;
	}
	
	@Override
	public String getValueAsString(Long value) 
	{
		return value.toString();
	}

}
