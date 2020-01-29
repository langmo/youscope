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
class RowViewImpl implements RowView 
{
	private final ColumnDefinition<?>[] columnDefinitions;
	private final Table table;
	private final int row;
	
	public RowViewImpl(Table table, int row, ColumnDefinition<?>[] columnDefinitions) 
	{
		this.table = table;
		this.row = row;
		this.columnDefinitions = columnDefinitions;
	}

	@Override
	public Iterator<TableEntry<?>> iterator() 
	{
		return new Iterator<TableEntry<?>>()
		{
			int column = -1;
			@Override
			public boolean hasNext() 
			{
				return column+1 < table.getNumColumns();
			}

			@Override
			public TableEntry<?> next() 
			{
				column++;
				try
				{
					return table.getEntry(row, column);
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
	public Class<? extends Serializable> getValueType(int column) throws IndexOutOfBoundsException 
	{
		return columnDefinitions[column].getValueType();
	}

	@Override
	public String getColumnName(int column) throws IndexOutOfBoundsException 
	{
		return columnDefinitions[column].getColumnName();
	}

	@Override
	public ColumnDefinition<?> getColumnDefinition(int column) throws IndexOutOfBoundsException 
	{
		return columnDefinitions[column];
	}

	@Override
	public int getSize() 
	{
		return table.getNumColumns();
	}

	@Override
	public TableEntry<?> get(int column) throws IndexOutOfBoundsException 
	{
		return table.getEntry(row, column);
	}
	
	@Override
	public <T extends Serializable> TableEntry<? extends T> get(int column, Class<T> valueType) throws IndexOutOfBoundsException, TableException 
	{
		return table.getEntry(row, column, valueType);
	}

	@Override
	public Serializable getValue(int column) throws IndexOutOfBoundsException 
	{
		return table.getEntry(row, column).getValue();
	}

	@Override
	public int getNumColumns() {
		return getSize();
	}

	@Override
	public TableEntry<?> get(String columnName) throws TableException 
	{
		return table.getEntry(row, columnName);
	}

	@Override
	public <T extends Serializable> TableEntry<? extends T> get(String columnName, Class<T> valueType)
			throws TableException {
		return table.getEntry(row, columnName, valueType);
	}

	@Override
	public <T extends Serializable> TableEntry<T> get(
			ColumnDefinition<T> columnDefinition) throws TableException,
			NullPointerException {
		return table.getEntry(row, columnDefinition);
		
	}

	@Override
	public <T extends Serializable> T getValue(
			ColumnDefinition<T> columnDefinition) throws TableException,
			NullPointerException {
		return table.getValue(row, columnDefinition);
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
}
