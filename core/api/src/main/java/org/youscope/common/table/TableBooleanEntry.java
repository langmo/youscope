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
 * A table entry containing a boolean.
 * @author Moritz Lang
 *
 */
class TableBooleanEntry extends TableEntryAdapter<Boolean> 
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -381739064279374990L;

	public TableBooleanEntry(Boolean value, boolean nullAllowed) throws NullPointerException, TableException 
	{
		super(value, nullAllowed, Boolean.class);
	}

	@Override
	protected Boolean cloneValue(Boolean value) 
	{
		// Booleans are immutable
		return value;
	}
	
	@Override
	public String getValueAsString(Boolean value) 
	{
		return value.toString();
	}

}
