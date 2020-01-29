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
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Internal implementation of a column. Do not make public, since the complete implementation of a Table might change in future releases.
 * @author Moritz Lang
 *
 * @param <T> The value type of the entries in the column.
 */
class ColumnDataStorage<T extends Serializable> implements Serializable, Cloneable, Iterable<TableEntry<? extends T>>
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 4457909174489646893L;
	private final ArrayList<TableEntryAdapter<? extends T>> rows = new ArrayList<TableEntryAdapter<? extends T>>();
	private final ColumnDefinition<T> columnDefinition;
	
	/**
	 * Creates a new column data storage element in agreement with the given column definition.
	 * @param columnDefinition The definition of the column type and name.
	 * @throws NullPointerException Thrown if columnDefinition is null.
	 */
	public ColumnDataStorage(ColumnDefinition<T> columnDefinition) throws NullPointerException
	{
		if(columnDefinition == null)
			throw new NullPointerException();
		this.columnDefinition = columnDefinition;
	}
	
	/**
	 * Private copy constructor. Use {@link #clone()}.
	 * @param other Object to create copy of.
	 */
	protected ColumnDataStorage(ColumnDataStorage<T> other)
	{
		this.columnDefinition = other.columnDefinition;
		for(TableEntryAdapter<? extends T> row : other.rows)
		{
			rows.add(row.clone());
		}
	}
	
	/**
	 * Helper function to create a new column data storage element in agreement with the given column definition.
	 * Same as {@link ColumnDataStorage#ColumnDataStorage(ColumnDefinition)}.
	 * @param columnDefinition The definition of the column type and name.
	 * @return The newly created column data storage element.
	 * @throws NullPointerException Thrown if columnDefinition is null.
	 */
	static <T extends Serializable> ColumnDataStorage<T> createColumn(ColumnDefinition<T> columnDefinition) throws NullPointerException
	{
		return new ColumnDataStorage<T>(columnDefinition);
	}
	
	void addRow(Serializable value) throws TableException
	{
		if(value == null && !columnDefinition.isNullAllowed())
			throw new TableException("Column with name " + columnDefinition.getColumnName() + " does not allow entries to have null values.");
		else if(value == null)
			rows.add(TableHelper.createEntry(columnDefinition.getValueType(), null, columnDefinition.isNullAllowed()));
		else if(!columnDefinition.getValueType().isAssignableFrom(value.getClass()))
		{
			throw new TableException("Column with name "+columnDefinition.getColumnName()+" requires values of type " + columnDefinition.getValueType().getName()+". Provided value was of type "+value.getClass().getName()+".");
		}
		else
		{
			rows.add(TableHelper.createEntry(columnDefinition.getValueType(), columnDefinition.getValueType().cast(value), columnDefinition.isNullAllowed()));
		}
	}
	
	void insertRow(int row, Serializable value) throws TableException, IndexOutOfBoundsException 
	{
		if(value == null && !columnDefinition.isNullAllowed())
			throw new TableException("Column with name " + columnDefinition.getColumnName() + " does not allow entries to have null values.");
		else if(value == null)
			rows.add(row, TableHelper.createEntry(columnDefinition.getValueType(), null, columnDefinition.isNullAllowed()));
		else if(!columnDefinition.getValueType().isAssignableFrom(value.getClass()))
		{
			throw new TableException("Column with name "+columnDefinition.getColumnName()+" requires values of type " + columnDefinition.getValueType().getName()+". Provided value was of type "+value.getClass().getName()+".");
		}
		else
		{
			rows.add(row, TableHelper.createEntry(columnDefinition.getValueType(), columnDefinition.getValueType().cast(value), columnDefinition.isNullAllowed()));
		}
	}
	void removeRow(int row) throws IndexOutOfBoundsException
	{
		rows.remove(row);
	}

	@Override
	public Iterator<TableEntry<? extends T>> iterator() 
	{
		// we have to map TableEntryAdapter to TableEntry.
		return new Iterator<TableEntry<? extends T>>()
				{
					private final Iterator<TableEntryAdapter<? extends T>> iterator = rows.iterator();
					@Override
					public boolean hasNext() {
						return iterator.hasNext();
					}

					@Override
					public TableEntry<? extends T> next() {
						return iterator.next();
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
			
				};
	}

	@Override
	public ColumnDataStorage<T> clone()
	{
		return new ColumnDataStorage<T>(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((rows == null) ? 0 : rows.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ColumnDataStorage<?> other = (ColumnDataStorage<?>) obj;
		if (rows == null) {
			if (other.rows != null)
				return false;
		} else if (!rows.equals(other.rows))
			return false;
		return true;
	}

	public int getSize() {
		return rows.size();
	}

	public TableEntry<? extends T> get(int row) throws IndexOutOfBoundsException {
		return rows.get(row);
	}

	public T getValue(int row) throws IndexOutOfBoundsException {
		return rows.get(row).getValue();
	}

	public Class<T> getValueType() {
		return columnDefinition.getValueType();
	}

	public String getColumnName() {
		return columnDefinition.getColumnName();
	}

	public ColumnDefinition<T> getColumnDefinition() {
		return columnDefinition;
	}
}
