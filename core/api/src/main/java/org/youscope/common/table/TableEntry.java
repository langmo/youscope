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

import java.io.Serializable;

/**
 * Interface all Table entry types have to implement.
 * @author Moritz Lang
 * @param <T> Type encoding the value.
 *
 */
public interface TableEntry<T extends Serializable> extends Serializable, Cloneable 
{
	/**
	 * Returns true if entry is null, i.e. not set.
	 * @return True if entry is null.
	 */
	public boolean isNull();
	
	/**
	 * Returns a string representation of the table entry. The return value of this function is different from the return value of {@link Object#toString()} function, since
	 * null will be returned when the value of this entry is not set, whereas the toString() function should return a non-null value.
	 * @return String representation of table entry, or null if table entry is null.
	 */
	public String getValueAsString();
	
	/**
	 * Returns the value of the entry, or null if the entry is null.
	 * @return Value of entry.
	 */
	public T getValue();
	
	/**
	 * Sets the table value to the given argument. If the value type is not assignable to this entry, throws a TableException.
	 * @param value Value to set table entry to.
	 * @throws TableException Thrown if value type is not assignable to this entry, or value is null and null values are not allowed.
	 */
	public void setValue(Serializable value) throws TableException;
	
	/**
	 * Sets the table value to the given tableEntry value. If the tableEntry value type is not assignable to this entry, throws a TableException.
	 * @param tableEntry Entry to copy the value from.
	 * @throws TableException Thrown if value type is not assignable to this entry, or if value of entry is null and null values are not allowed.
	 */
	public void setValue(TableEntry<? extends Serializable> tableEntry) throws TableException;
	
	/**
	 * Returns the class in which the value is encoded.
	 * @return Value class.
	 */
	public Class<T> getValueType();
	
	/**
	 * Returns true if this entry allows values to be set to null. False otherwise.
	 * @return true if null values are allowed for this entry.
	 */
	public boolean isNullAllowed();
}
