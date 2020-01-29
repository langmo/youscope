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
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A temporary row is a row not yet added to the table. Different to normal table
 * rows, a temporary row allows for null values for each of column. A temporary row
 * can, thus, be used to comfortably initialize one by one the values in a table row.
 * Afterwards, a temporary row can be added to the table, which is the moment when
 * the checking for null values for columns not allowing null entries is performed. 
 * @author Moritz Lang
 *
 */
public class TemporaryRow implements Serializable, Cloneable, RowView 
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -8904425978875939424L;
	private final ColumnDefinition<?>[] columnDefinitions;
	private final TableEntryAdapter<?>[] entries;
	/**
	 * Creates a temporary row with columns in agreement with the given column definitions.
	 * All column values are initialized to null.
	 * @param columnDefinitions The definition of the columns of this row.
	 * @throws NullPointerException Thrown if columnDefinitions are null.
	 * @throws TableException Thrown if temporary table could not be constructed.
	 */
	public TemporaryRow(ColumnDefinition<?>... columnDefinitions) throws NullPointerException, TableException
	{
		if(columnDefinitions == null)
			throw new NullPointerException("Column definitions are null.");
		this.columnDefinitions = columnDefinitions;
		entries = new TableEntryAdapter<?>[columnDefinitions.length];
		for(int i=0; i< columnDefinitions.length; i++)
		{
			if(columnDefinitions[i] == null)
				throw new NullPointerException("Column definition number "+Integer.toString(i)+" is null.");
			entries[i] = TableHelper.createEntry(columnDefinitions[i].getValueType(), null, true);
		}
	}
	@Override
	protected TemporaryRow clone()
	{
		TemporaryRow clone;
		try {
			clone = (TemporaryRow) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Clone not supported.", e); // will not happen.
		}
		for(int i=0; i< entries.length;i++)
		{
			clone.entries[i] = entries[i].clone();
		}
		return clone;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(columnDefinitions);
		result = prime * result + Arrays.hashCode(entries);
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
		TemporaryRow other = (TemporaryRow) obj;
		if (!Arrays.equals(columnDefinitions, other.columnDefinitions))
			return false;
		if (!Arrays.equals(entries, other.entries))
			return false;
		return true;
	}
	@Override
	public Iterator<TableEntry<?>> iterator() 
	{
		return new Iterator<TableEntry<?>>()
				{
					private int column = -1;
					@Override
					public boolean hasNext() {
						return column+1 < entries.length;
					}

					@Override
					public TableEntry<?> next() 
					{
						try
						{
							column++;
							return entries[column];
						}
						catch(IndexOutOfBoundsException e)
						{
							throw new NoSuchElementException(e.getMessage());
						}
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
			
				};
	}
	@Override
	public Class<? extends Serializable> getValueType(int column)
			throws IndexOutOfBoundsException 
	{
		return columnDefinitions[column].getValueType();
	}
	@Override
	public String getColumnName(int column) throws IndexOutOfBoundsException {
		return columnDefinitions[column].getColumnName();
	}
	@Override
	public ColumnDefinition<?> getColumnDefinition(int column)
			throws IndexOutOfBoundsException {
		return columnDefinitions[column];
	}
	@Override
	public int getSize() {
		return columnDefinitions.length;
	}
	@Override
	public int getNumColumns() {
		return getSize();
	}
	@Override
	public TableEntry<?> get(int column) throws IndexOutOfBoundsException {
		return entries[column];
	}
	@Override
	public <T extends Serializable> TableEntry<? extends T> get(int column,
			Class<T> valueType) throws IndexOutOfBoundsException,
			TableException {
		TableEntry<?> entry = get(column);
		if(valueType.isAssignableFrom(entry.getValueType()))
		{
			@SuppressWarnings("unchecked")
			TableEntry<? extends T> returnValue = (TableEntry<? extends T>) entry;
			return returnValue;
		}
		throw new TableException("Entry has value type "+entry.getValueType().getName()+", which is not a sub-class of "+valueType.getName()+".");
	}
	@Override
	public TableEntry<?> get(String columnName) throws TableException {
		for(int i=0; i<columnDefinitions.length;i++)
		{
			if(columnDefinitions[i].getColumnName().equals(columnName))
				return get(i);
		}
		throw new TableException("No column with name "+columnName+" defined in table. Note that column names are case sensitive.");
	}
	@Override
	public <T extends Serializable> TableEntry<? extends T> get(
			String columnName, Class<T> valueType) throws TableException {
		TableEntry<?> entry = get(columnName);
		if(valueType.isAssignableFrom(entry.getValueType()))
		{
			@SuppressWarnings("unchecked")
			TableEntry<? extends T> returnValue = (TableEntry<? extends T>) entry;
			return returnValue;
		}
		throw new TableException("Entry has value type "+entry.getValueType().getName()+", which is not a sub-class of "+valueType.getName()+".");
	}
	@Override
	public Serializable getValue(int column) throws IndexOutOfBoundsException {
		return get(column).getValue();
	}
	@Override
	public Serializable getValue(String columnName) throws TableException {
		return get(columnName).getValue();
	}
	@Override
	public <T extends Serializable> T getValue(int column, Class<T> valueType)
			throws IndexOutOfBoundsException, TableException {
		return get(column, valueType).getValue();
	}
	@Override
	public <T extends Serializable> T getValue(String columnName,
			Class<T> valueType) throws TableException {
		return get(columnName, valueType).getValue();
	}
	@Override
	public <T extends Serializable> TableEntry<T> get(
			ColumnDefinition<T> columnDefinition) throws TableException,
			NullPointerException {
		if(columnDefinition == null)
			throw new NullPointerException();
		TableEntry<?> entry = get(columnDefinition.getColumnName());
		if(entry.getValueType().equals(columnDefinition.getValueType()))
		{
			@SuppressWarnings("unchecked")
			TableEntry<T> returnValue = (TableEntry<T>) entry;
			return returnValue;
		}
		throw new TableException("Entry has value type "+entry.getValueType().getName()+", which is not a sub-class of "+columnDefinition.getValueType().getName()+".");
	}
	@Override
	public <T extends Serializable> T getValue(
			ColumnDefinition<T> columnDefinition) throws TableException,
			NullPointerException {
		return get(columnDefinition).getValue();
	}
	
}
