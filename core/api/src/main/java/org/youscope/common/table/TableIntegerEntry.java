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
 * A table entry containing an integer.
 * @author Moritz Lang
 *
 */
class TableIntegerEntry extends TableEntryAdapter<Integer> 
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -5198391925520387931L;

	public TableIntegerEntry(Integer value, boolean nullAllowed) throws NullPointerException, TableException 
	{
		super(value, nullAllowed, Integer.class);
	}

	@Override
	protected Integer cloneValue(Integer value) 
	{
		// Integer's are immutable
		return value;
	}
	
	@Override
	public String getValueAsString(Integer value) 
	{
		return value.toString();
	}
	
}
