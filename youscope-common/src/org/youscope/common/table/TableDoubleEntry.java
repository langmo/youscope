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
 * A table entry containing a double.
 * @author Moritz Lang
 *
 */
class TableDoubleEntry extends TableEntryAdapter<Double>  
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -5118391925520387931L;
	
	public TableDoubleEntry(Double value, boolean nullAllowed) throws NullPointerException, TableException 
	{
		super(value, nullAllowed, Double.class);
	}

	@Override
	String getValueAsString(Double value) 
	{
		return value.toString();
	}

	@Override
	protected Double cloneValue(Double value) 
	{
		// Double's are immutable
		return value;
	}
	
}
