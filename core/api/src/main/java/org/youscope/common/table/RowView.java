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
 * Represents a view on a row of a graph. Does itself not contain data, and should not be passed around (pass the graph, or a copy of the graph, instead).
 * @author Moritz Lang
 *
 */
public interface RowView extends Iterable<TableEntry<?>>
{
	/**
	 * Returns the class in which the values of the given column is encoded.
	 * @param column The column index of the entry.
	 * @return Value type.
	 * @throws IndexOutOfBoundsException Thrown if column index is invalid.
	 */
	public Class<? extends Serializable> getValueType(int column) throws IndexOutOfBoundsException;
	
	/**
	 * Returns the name of the column with the given index.
	 * @param column The column index of the entry.
	 * @return Name of column.
	 * @throws IndexOutOfBoundsException Thrown if column index is invalid.
	 */
	public String getColumnName(int column) throws IndexOutOfBoundsException;
	
	/**
	 * Returns the definition of the column with the given index.
	 * @param column The column index of the entry. 
	 * @return column definition.
	 * @throws IndexOutOfBoundsException Thrown if column index is invalid.
	 */
	public ColumnDefinition<?> getColumnDefinition(int column) throws IndexOutOfBoundsException;
	
	/**
	 * Returns the number of entries (i.e. columns) in this row.
	 * Same as {@link #getNumColumns()}.
	 * @return number of columns.
	 */
	public int getSize();
	/**
	 * Returns the number of entries (i.e. columns) in this row.
	 * Same as {@link #getSize()}.
	 * @return number of columns.
	 */
	public int getNumColumns();
	
	/**
	 * Returns the entry in the given column.
	 * @param column The column of the entry.
	 * @return The entry at the given column.
	 * @throws IndexOutOfBoundsException Thrown if column index is invalid.
	 */
	public TableEntry<?> get(int column) throws IndexOutOfBoundsException;
	
	/**
	 * Returns the table entry corresponding to the given column definition.
	 * @param columnDefinition the definition of the column to return the entry of.
	 * @return The entry corresponding to the column definition.
	 * @throws TableException thrown if there is no entry corresponding to the definition, or if entry has different value type.
	 * @throws NullPointerException thrown if columnDefinition is null.
	 */
	public <T extends Serializable> TableEntry<T> get(ColumnDefinition<T> columnDefinition) throws TableException, NullPointerException;
	
	/**
	 * Returns the entry in the given column with the given value type.
	 * @param column The column of the entry.
	 * @param valueType The value type of the entry. Must be a super-class of the actual value type.
	 * @return The entry at the given column.
	 * @throws IndexOutOfBoundsException  Thrown if column index is invalid.
	 * @throws TableException Thrown if entry type is incompatible.
	 */
	public <T extends Serializable> TableEntry<? extends T> get(int column, Class<T> valueType) throws IndexOutOfBoundsException, TableException;
	
	/**
	 * Returns the entry with the given column name.
	 * @param columnName The column name of the entry.
	 * @return The entry at the column with the given column name.
	 * @throws TableException Thrown if column with the given name does not exist.
	 */
	public TableEntry<?> get(String columnName) throws TableException;
	
	/**
	 * Returns the entry in the column with the given column name, having the given value type.
	 * @param columnName The column name of the entry.
	 * @param valueType The value type of the entry. Must be a super-class of the actual value type.
	 * @return The entry at the column with the given column name.
	 * @throws TableException Thrown if entry type is incompatible, or column with the given name does not exist.
	 */
	public <T extends Serializable> TableEntry<? extends T> get(String columnName, Class<T> valueType) throws TableException;
	
	
	/**
	 * Returns the value of the entry in the given column.
	 * @param column The column of the entry.
	 * @return The value of the entry at the given column.
	 * @throws IndexOutOfBoundsException Thrown if column index is invalid.
	 */
	public Serializable getValue(int column) throws IndexOutOfBoundsException;
	
	/**
	 * Returns the value of the table entry corresponding to the given column definition.
	 * @param columnDefinition the definition of the column to return the value of the entry of.
	 * @return The value of the entry corresponding to the column definition.
	 * @throws TableException thrown if there is no entry corresponding to the definition, or if entry has different value type.
	 * @throws NullPointerException thrown if columnDefinition is null.
	 */
	public <T extends Serializable> T getValue(ColumnDefinition<T> columnDefinition) throws TableException, NullPointerException;
	
	
	/**
	 * Returns the value of the entry in the column with the given column name.
	 * @param columnName The column name of the entry.
	 * @return The value of the entry at the column with the given column name.
	 * @throws TableException Thrown if column with the given name does not exist.
	 */
	public Serializable getValue(String columnName) throws TableException;
	
	/**
	 * Returns the value of the entry in the given column, having the given value type.
	 * @param column The column of the entry.
	 * @param valueType The value type of the entry. Must be a super-class of the actual value type.
	 * @return The value of the entry at the given column.
	 * @throws IndexOutOfBoundsException Thrown if column index is invalid.
	 * @throws TableException Thrown if entry type is not of the given valueType.
	 */
	public <T extends Serializable> T getValue(int column, Class<T> valueType) throws IndexOutOfBoundsException, TableException;
	
	/**
	 * Returns the value of the entry in the column with the given column name, having the given value type.
	 * @param columnName The column name of the entry.
	 * @param valueType The value type of the entry. Must be a super-class of the actual value type.
	 * @return The value of the entry at the column with the given column name.
	 * @throws TableException Thrown if entry type is not of the given valueType, or if a column with the given name does not exist.
	 */
	public <T extends Serializable> T getValue(String columnName, Class<T> valueType) throws TableException;
}
