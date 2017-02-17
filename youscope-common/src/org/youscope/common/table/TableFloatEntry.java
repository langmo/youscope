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
 * A table entry containing a float.
 * @author Moritz Lang
 *
 */
class TableFloatEntry extends TableEntryAdapter<Float> 
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -381739064279371190L;

	public TableFloatEntry(Float value, boolean nullAllowed) throws NullPointerException, TableException 
	{
		super(value, nullAllowed, Float.class);
	}

	@Override
	protected Float cloneValue(Float value) 
	{
		// Floats are immutable
		return value;
	}
	
	@Override
	public String getValueAsString(Float value) 
	{
		return value.toString();
	}

}
