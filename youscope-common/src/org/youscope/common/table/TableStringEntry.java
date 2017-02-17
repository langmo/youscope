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
 * A table entry containing a string.
 * @author Moritz Lang
 *
 */
class TableStringEntry extends TableEntryAdapter<String> 
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -6259177334525937339L;

	public TableStringEntry(String value, boolean nullAllowed) throws NullPointerException, TableException 
	{
		super(value, nullAllowed, String.class);
	}

	@Override
	protected String cloneValue(String value) 
	{
		// Strings are immutable.
		return value;
	}

	@Override
	String getValueAsString(String value) 
	{
		return value;
	}
}
