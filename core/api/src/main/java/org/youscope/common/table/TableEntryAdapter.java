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
 * Adapter and super-class for all table entry implementations.
 * All sub-classes must have a public constructor of the form <code>Foo(T value, boolean nullAllowed) throws TableException</code>.
 * @author Moritz Lang
 *
 * @param <T>
 */
abstract class TableEntryAdapter<T extends Serializable> implements TableEntry<T>, Serializable, Cloneable 
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 7255882266426675000L;
	private T value;
	private final boolean nullAllowed;
	private final Class<T> valueType;
	public TableEntryAdapter(T value, boolean nullAllowed, Class<T> valueType) throws TableException, NullPointerException
	{
		if(valueType == null)
			throw new NullPointerException();
		if(value == null && !nullAllowed)
			throw new TableException("Table entry does not allow for null values.");
		this.value = value;
		this.nullAllowed = nullAllowed;
		this.valueType = valueType;
	}
	/**
	 * Clones the entry. Throwing a CloneNotSupportedException is not permitted, thus, not part of the interface declaration.
	 * @return A copy of the entry.
	 */
	@Override
	public TableEntryAdapter<T> clone() 
	{
		try {
			@SuppressWarnings("unchecked")
			TableEntryAdapter<T> clone = (TableEntryAdapter<T>) super.clone();
			clone.value = cloneValue(value);
			return clone;
		} 
		catch (CloneNotSupportedException e) {
			throw new RuntimeException("Clone not supported", e); // won't happen.
		}
	}
	
	@Override
	public boolean isNull() 
	{
		return value == null;
	}
	
	/**
	 * Helper function for the implementation of {@link #getValueAsString()}.
	 * Only called for non-null values.
	 * @param value Value to be converted to a String.
	 * @return String representation of value.
	 */
	abstract String getValueAsString(T value);
	@Override
	public String getValueAsString() 
	{
		if(value == null)
			return null;
		return getValueAsString(value);
	}
	
	@Override
	public String toString()
	{
		if(value == null)
			return "unset";
		return getValueAsString(value);
	}
	/**
	 * Should create a clone of the value. If value is immutable, can return the value directly.
	 * Must not be called if value is null.
	 * @param value Value to clone.
	 * @return Clone of value, or value if value is immutable. 
	 */
	protected abstract T cloneValue(T value);
	
	@Override
	public boolean isNullAllowed()
	{
		return nullAllowed;
	}
	@Override
	public T getValue() 
	{
		return cloneValue(value);
	}
	@Override
	public void setValue(Serializable value) throws TableException 
	{
		if(value == null && !nullAllowed)
			throw new TableException("Table entry does not allow for null values.");
		else if(value == null)
			this.value = null;
		else if(valueType.isInstance(value))
			this.value = cloneValue(valueType.cast(value));
		else
			throw new TableException("Value is of type " + value.getClass().getName()+", which is not compatible to table entry which requires values of type "+valueType.getName()+".");
	}
	@Override
	public void setValue(TableEntry<? extends Serializable> tableEntry) throws TableException {
		setValue(tableEntry.getValue());
	}
	@Override
	public Class<T> getValueType()
	{
		return valueType;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (nullAllowed ? 1231 : 1237);
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		result = prime * result + ((valueType == null) ? 0 : valueType.getName().hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TableEntryAdapter<?> other = (TableEntryAdapter<?>) obj;
		if (nullAllowed != other.nullAllowed)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		if (valueType == null) {
			if (other.valueType != null)
				return false;
		} else if (!valueType.equals(other.valueType))
			return false;
		return true;
	}
}
