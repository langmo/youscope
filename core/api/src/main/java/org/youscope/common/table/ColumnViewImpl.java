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
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implementation of a row view.
 * @author Moritz Lang
 *
 */
class ColumnViewImpl<T extends Serializable> implements ColumnView<T> 
{
	private final ColumnDefinition<T> columnDefinition;
	private final Table table;
	private final int column;
	
	public ColumnViewImpl(Table table, int column, ColumnDefinition<T> columnDefinition) 
	{
		this.table = table;
		this.column = column;
		this.columnDefinition = columnDefinition;
	}
	
	/**
	 * Helper function to create column views.
	 * @param table Table where this is a column view.
	 * @param column Column index of this view.
	 * @param columnDefinition Definition of this view.
	 * @return Column view.
	 */
	public static <T extends Serializable> ColumnViewImpl<T> createColumnView(Table table, int column, ColumnDefinition<T> columnDefinition)
	{
		return new ColumnViewImpl<T>(table, column, columnDefinition);
	}

	@Override
	public Iterator<TableEntry<? extends T>> iterator() 
	{
		return new Iterator<TableEntry<? extends T>>()
		{
			int row = -1;
			@Override
			public boolean hasNext() 
			{
				return row+1<table.getNumRows();
			}

			@Override
			public TableEntry<? extends T> next() 
			{
				row++;
				try
				{
					return table.getEntry(row, column, columnDefinition.getValueType());
				}
				catch(IndexOutOfBoundsException e)
				{
					throw new NoSuchElementException(e.getMessage());
				} 
				catch (TableException e) {
					// Should not happen, since we know the type of the column.
					throw new RuntimeException("Table element at row " + Integer.toString(row)+" and column "+ Integer.toString(column)+" is not of type " + columnDefinition.getValueType().getName()+".", e);
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}		
		};
	}

	@Override
	public Class<T> getValueType()
	{
		return columnDefinition.getValueType();
	}

	@Override
	public String getColumnName()
	{
		return columnDefinition.getColumnName();
	}

	@Override
	public ColumnDefinition<T> getColumnDefinition()
	{
		return columnDefinition;
	}

	@Override
	public int getSize() 
	{
		return table.getNumRows();
	}

	@Override
	public TableEntry<? extends T> get(int row) throws IndexOutOfBoundsException 
	{
		try {
			return table.getEntry(row, column, columnDefinition.getValueType());
		} catch (TableException e) {
			// Should not happen, since we know the column type.
			throw new RuntimeException("Table element at row " + Integer.toString(row)+" and column "+Integer.toString(column)+" is of value type " + table.getEntry(row, column).getValueType().getName()+", however, value type "+columnDefinition.getValueType().getName()+" was expected.", e);
		}
	}

	@Override
	public T getValue(int row) throws IndexOutOfBoundsException 
	{
		try {
			return table.getEntry(row, column, columnDefinition.getValueType()).getValue();
		} catch (TableException e) {
			// Should not happen, since we know the column type.
			throw new RuntimeException("Table element at row " + Integer.toString(row)+" and column "+Integer.toString(column)+" is of value type " + table.getEntry(row, column).getValueType().getName()+", however, value type "+columnDefinition.getValueType().getName()+" was expected.", e);
		}
	}

	@Override
	public int getNumRows() {
		return getSize();
	}

	@Override
	public String getColumnDescription() {
		return columnDefinition.getColumnDescription();
	}

	@Override
	public boolean isNullAllowed() {
		return columnDefinition.isNullAllowed();
	}
}
