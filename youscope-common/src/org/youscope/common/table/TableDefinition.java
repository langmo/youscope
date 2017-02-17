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
 * Definition of a table. This definition is immutable.
 * A table consists of a pre-defined array of columns with specific value types, and a various number of rows.
 * @author Moritz Lang
 *
 */
public final class TableDefinition implements Iterable<ColumnDefinition<?>>, Serializable
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 7914283857441553356L;
	private final ColumnDefinition<?>[] columns;
	private final String tableName;
	private final String tableDescription;
	
	/**
	 * Constructor.
	 * Currently, YouScope only supports a limited set of value types for tables. Thus, this function throws a 
	 * TableException if given value type is currently not supported by YouScope. See {@link TableHelper} for supported column types. 
	 * @param tableName The name (title) of the table. Should be a single short line.
	 * @param tableDescription Description of the meaning and intended usage of the table. Might contain multiple lines (separated by line breaks).
	 * @param columns The columns of the table.
	 * @throws NullPointerException Thrown if tableName, or tableDescription is null.
	 */
	public TableDefinition(String tableName, String tableDescription, ColumnDefinition<?>... columns) throws NullPointerException
	{
		if(columns == null)
			columns = new ColumnDefinition<?>[0];
		for(ColumnDefinition<?> column : columns)
		{
			if(column == null)
				throw new NullPointerException("Columns of table must not be null.");
		}
		if(tableName == null)
			throw new NullPointerException("Table name must not be null.");
		if(tableDescription == null)
			throw new NullPointerException("Table description must not be null.");
		this.columns = columns;
		this.tableName = tableName;
		this.tableDescription = tableDescription;
	}
	
	@Override
	public Iterator<ColumnDefinition<?>> iterator() 
	{
		return new Iterator<ColumnDefinition<?>>()
				{
					private int column = -1;
					@Override
					public boolean hasNext() 
					{
						return column+1 < columns.length;
					}

					@Override
					public ColumnDefinition<?> next() 
					{
						if(!hasNext())
							throw new NoSuchElementException("No more columns defined (number of defined columns: " + Integer.toString(columns.length) + ").");
						column++;
						try
						{
							return getColumnDefinition(column);
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
	/**
	 * Returns the name (title) of the table.
	 * The name should usually be a single short line.
	 * @return Title of table.
	 */
	public String getTableName()
	{
		return tableName;
	}
	
	/**
	 * Returns a description of the meaning and intended usage of the table. Might contain multiple lines (separated by line breaks).
	 * @return Description of table.
	 */
	public String getTableDescription()
	{
		return tableDescription;
	}
	/**
	 * Returns the number of defined columns.
	 * Same as {@link #getSize()}.
	 * @return number of columns.
	 */
	public int getNumColumns()
	{
		return columns.length;
	}
	/**
	 * Returns the number of defined columns.
	 * Same as {@link #getNumColumns()}.
	 * @return number of columns.
	 */
	public int getSize()
	{
		return getNumColumns();
	}
	/**
	 * Returns the column definition at the given index.
	 * @param columnIndex Column index.
	 * @return Column definition at the given index.
	 * @throws IndexOutOfBoundsException Thrown if columnIndex is invalid.
	 */
	public ColumnDefinition<?> getColumnDefinition(int columnIndex) throws IndexOutOfBoundsException
	{
		if(columnIndex < 0 || columnIndex >= columns.length)
			throw new IndexOutOfBoundsException("Column " + Integer.toString(columnIndex) + " does not exist (number of defined columns: " + Integer.toString(columns.length) + ").");
		return columns[columnIndex];
	}
	/**
	 * Returns the column definition with the given column name.
	 * @param columnName Column name.
	 * @return Column with the given name. The column name is case sensitive.
	 * @throws TableException Thrown if column definition with given column name does not exist.
	 */
	public ColumnDefinition<?> getColumnDefinition(String columnName) throws TableException
	{
		for(ColumnDefinition<?> column : columns)
		{
			if(column.getColumnName().equals(columnName))
				return column;
		}
		throw new TableException("No column with name " + columnName + " defined. Note that column names are case sensitive.");
	}
	
	/**
	 * Returns the name of the given column.
	 * @param column Index of the column.
	 * @return Name (title) of the column.
	 * @throws IndexOutOfBoundsException Thrown if column index is invalid.
	 */
	public String getColumnName(int column) throws IndexOutOfBoundsException
	{
		return getColumnDefinition(column).getColumnName();
	}
	
	/**
	 * Returns the names (titles) of all columns.
	 * @return Names of all columns in same order as columns are in table.
	 */
	public String[] getColumnNames()
	{
		String[] names = new String[getNumColumns()];
		for(int i=0; i < getNumColumns(); i++)
		{
			names[i] = columns[i].getColumnName();
		}
		return names;
	}
	
	/**
	 * Returns the column definitions of this table as an array.
	 * @return Columns of table.
	 */
	public ColumnDefinition<?>[] getColumnDefinitions()
	{
		// make copy of array, since arrays are not immutable. However, the entries are.
		return columns.clone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(columns);
		result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
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
		TableDefinition other = (TableDefinition) obj;
		if (!Arrays.equals(columns, other.columns))
			return false;
		if (tableName == null) {
			if (other.tableName != null)
				return false;
		} else if (!tableName.equals(other.tableName))
			return false;
		return true;
	}
}
