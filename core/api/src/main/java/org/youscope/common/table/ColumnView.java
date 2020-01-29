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
 * Represents a view on a column of a graph. Does itself not contain data, and should not be passed around (pass the graph, or a copy of the graph, instead).
 * @author Moritz Lang
 *
 * @param <T> A value type for which the columns entries are subclasses of. Example: if the {@link #getColumnDefinition()}.{@link ColumnDefinition#getValueType()} is Integer, T can be Integer or Number.
 */
public interface ColumnView<T extends Serializable> extends Iterable<TableEntry<? extends T>>
{
	/**
	 * Returns the class in which the values of this column are encoded.
	 * @return Value type.
	 */
	public Class<? extends T> getValueType();
	
	/**
	 * Returns the name of the column.
	 * @return Name of column.
	 */
	public String getColumnName();
	
	/**
	 * Returns a description of the meaning of the elements in this column.
	 * @return description of column content.
	 */
	public String getColumnDescription();
	
	/**
	 * Returns true if entries in the column might have null values. False otherwise.
	 * @return true if column entries's values might be null.
	 */
	public boolean isNullAllowed();
	
	/**
	 * Returns the definition of this column.
	 * @return column definition.
	 */
	public ColumnDefinition<? extends T> getColumnDefinition();
	
	/**
	 * Returns the number of entries (i.e. rows) in this column.
	 * Same as {@link #getNumRows()}.
	 * @return number of rows.
	 */
	public int getSize();
	/**
	 * Returns the number of entries (i.e. rows) in this column.
	 * Same as {@link #getSize()}.
	 * @return number of rows.
	 */
	public int getNumRows();
	
	/**
	 * Returns the entry in the given row.
	 * @param row The row of the entry.
	 * @return The entry at the given row.
	 * @throws IndexOutOfBoundsException Thrown if row index is invalid.
	 */
	public TableEntry<? extends T> get(int row) throws IndexOutOfBoundsException;
	
	/**
	 * Returns the value of the entry in the given row.
	 * @param row The row of the entry.
	 * @return The value of the entry at the given row.
	 * @throws IndexOutOfBoundsException Thrown if row index is invalid.
	 */
	public T getValue(int row) throws IndexOutOfBoundsException;
}
