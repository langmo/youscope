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
 * Definition of a table column. This definition is immutable. 
 * A table column definition is composed of a column name, and a value type, i.e. the type (class) column entries must implement.
 * @author Moritz Lang
 * @param <T> type of column entries.
 * 
 */
public final class ColumnDefinition<T extends Serializable> implements Serializable 
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 8467621679897389458L;
	private final String columnName;
	private final String columnDescription;
	private final Class<T> valueType;
	private final boolean nullAllowed;
	
	/**
	 * Creates a new Column definition with the given column name and value type.
	 * Currently, YouScope only supports a limited set of value types for tables. Thus, this function throws a 
	 * TableException if given value type is currently not supported by YouScope. See {@link TableHelper} for supported column types. 
	 * See also {@link #createDoubleColumnDefinition(String, String, boolean)}, {@link #createIntegerColumnDefinition(String, String, boolean)}, 
	 * {@link #createStringColumnDefinition(String, String, boolean)}, {@link #createBooleanColumnDefinition(String, String, boolean)}, {@link #createFloatColumnDefinition(String, String, boolean)},
	 * {@link #createLongColumnDefinition(String, String, boolean)} to create column definitions for value types by default supported by YouScope.
	 * Therefore, these functions do not throw TableExceptions.
	 * @param columnName Name (header) of this column. Should be a single and short line.
	 * @param columnDescription Description of the meaning of the elements in this column. Might contain multiple lines (separated by line breaks).
	 * @param valueType Type of entries in the column.
	 * @param nullAllowed True if the values of entries in this column might be null, false if null is forbidden.
	 * @throws TableException Thrown if value type is currently not supported by YouScope.
	 * @throws NullPointerException Thrown if either columnName, columnDescription or valueType is null.
	 */
	public ColumnDefinition(String columnName, String columnDescription, Class<T> valueType, boolean nullAllowed) throws TableException, NullPointerException
	{
		if(columnName == null || valueType == null || columnDescription == null)
			throw new NullPointerException();
		TableHelper.assertSupported(valueType);
		this.columnName = columnName;
		this.valueType = valueType;
		this.nullAllowed = nullAllowed;
		this.columnDescription = columnDescription;
	}
	
	/**
	 * Returns the class in which the values of this column are encoded.
	 * @return Value type.
	 */
	public Class<T> getValueType()
	{
		return valueType;
	}
	
	/**
	 * Creates a new Column definition with the given column name and value type.
	 * Currently, YouScope only supports a limited set of value types for tables. Thus, this function throws a 
	 * TableException if given value type is currently not supported by YouScope. See {@link TableHelper} for supported column types. 
	 * See also {@link #createDoubleColumnDefinition(String, String, boolean)}, {@link #createIntegerColumnDefinition(String, String, boolean)} 
	 * and {@link #createStringColumnDefinition(String, String, boolean)} to create column definitions for value types by default supported by YouScope
	 * Therefore, these functions do not throw TableExceptions.
	 * @param columnName Name (header) of this column. Should be a single and short line.
	 * @param columnDescription Description of the meaning of the elements in this column. Might contain multiple lines (separated by line breaks).
	 * @param valueType Type of entries in the column.
	 * @param nullAllowed True if the values of entries in this column might be null, false if null is forbidden.
	 * @return newly created column definition. 
	 * @throws TableException Thrown if value type is currently not supported by YouScope.
	 * @throws NullPointerException Thrown if either columnName, columnDescription or valueType is null.
	 */
	public static <T extends Serializable> ColumnDefinition<T> createColumnDefinition(String columnName, String columnDescription, Class<T> valueType, boolean nullAllowed) throws TableException, NullPointerException
	{
		return new ColumnDefinition<T>(columnName, columnDescription, valueType, nullAllowed);
	}
	/**
	 * Creates a new Column definition for String value types.
	 * @param columnName Name (header) of this column. Should be a single and short line.
	 * @param columnDescription Description of the meaning of the elements in this column. Might contain multiple lines (separated by line breaks).
	 * @param nullAllowed True if the values of entries in this column might be null, false if null is forbidden.
	 * @return newly created column definition.
	 * @throws NullPointerException  Thrown if either columnName, columnDescription or valueType is null.
	 */
	public static ColumnDefinition<String> createStringColumnDefinition(String columnName, String columnDescription, boolean nullAllowed) throws NullPointerException
	{
		try {
			return new ColumnDefinition<String>(columnName, columnDescription, String.class, nullAllowed);
		} 
		catch (TableException e) {
			throw new RuntimeException("Could not create String column definition.", e);
		}
	}
	
	/**
	 * Creates a new Column definition for Integer value types.
	 * @param columnName Name (header) of this column. Should be a single and short line.
	 * @param columnDescription Description of the meaning of the elements in this column. Might contain multiple lines (separated by line breaks).
	 * @param nullAllowed True if the values of entries in this column might be null, false if null is forbidden.
	 * @return newly created column definition.
	 * @throws NullPointerException  Thrown if either columnName, columnDescription or valueType is null.
	 */
	public static ColumnDefinition<Integer> createIntegerColumnDefinition(String columnName, String columnDescription, boolean nullAllowed) throws NullPointerException
	{
		try {
			return new ColumnDefinition<Integer>(columnName, columnDescription, Integer.class, nullAllowed);
		} 
		catch (TableException e) {
			throw new RuntimeException("Could not create Integer column definition.", e);
		}
	}
	
	/**
	 * Creates a new Column definition for Double value types.
	 * @param columnName Name (header) of this column. Should be a single and short line.
	 * @param columnDescription Description of the meaning of the elements in this column. Might contain multiple lines (separated by line breaks).
	 * @param nullAllowed True if the values of entries in this column might be null, false if null is forbidden.
	 * @return newly created column definition.
	 * @throws NullPointerException  Thrown if either columnName, columnDescription or valueType is null.
	 */
	public static ColumnDefinition<Double> createDoubleColumnDefinition(String columnName, String columnDescription, boolean nullAllowed) throws NullPointerException
	{
		try {
			return new ColumnDefinition<Double>(columnName, columnDescription, Double.class, nullAllowed);
		} 
		catch (TableException e) {
			throw new RuntimeException("Could not create Double column definition.", e);
		}
	}
	
	/**
	 * Creates a new Column definition for Float value types.
	 * @param columnName Name (header) of this column. Should be a single and short line.
	 * @param columnDescription Description of the meaning of the elements in this column. Might contain multiple lines (separated by line breaks).
	 * @param nullAllowed True if the values of entries in this column might be null, false if null is forbidden.
	 * @return newly created column definition.
	 * @throws NullPointerException  Thrown if either columnName, columnDescription or valueType is null.
	 */
	public static ColumnDefinition<Float> createFloatColumnDefinition(String columnName, String columnDescription, boolean nullAllowed) throws NullPointerException
	{
		try {
			return new ColumnDefinition<Float>(columnName, columnDescription, Float.class, nullAllowed);
		} 
		catch (TableException e) {
			throw new RuntimeException("Could not create Float column definition.", e);
		}
	}
	
	/**
	 * Creates a new Column definition for Long value types.
	 * @param columnName Name (header) of this column. Should be a single and short line.
	 * @param columnDescription Description of the meaning of the elements in this column. Might contain multiple lines (separated by line breaks).
	 * @param nullAllowed True if the values of entries in this column might be null, false if null is forbidden.
	 * @return newly created column definition.
	 * @throws NullPointerException  Thrown if either columnName, columnDescription or valueType is null.
	 */
	public static ColumnDefinition<Long> createLongColumnDefinition(String columnName, String columnDescription, boolean nullAllowed) throws NullPointerException
	{
		try {
			return new ColumnDefinition<Long>(columnName, columnDescription, Long.class, nullAllowed);
		} 
		catch (TableException e) {
			throw new RuntimeException("Could not create Long column definition.", e);
		}
	}
	
	/**
	 * Creates a new Column definition for Boolean value types.
	 * @param columnName Name (header) of this column. Should be a single and short line.
	 * @param columnDescription Description of the meaning of the elements in this column. Might contain multiple lines (separated by line breaks).
	 * @param nullAllowed True if the values of entries in this column might be null, false if null is forbidden.
	 * @return newly created column definition.
	 * @throws NullPointerException  Thrown if either columnName, columnDescription or valueType is null.
	 */
	public static ColumnDefinition<Boolean> createBooleanColumnDefinition(String columnName, String columnDescription, boolean nullAllowed) throws NullPointerException
	{
		try {
			return new ColumnDefinition<Boolean>(columnName, columnDescription, Boolean.class, nullAllowed);
		} 
		catch (TableException e) {
			throw new RuntimeException("Could not create Boolean column definition.", e);
		}
	}
	
	
	/**
	 * Returns the name (header) of the column.
	 * @return Name of column.
	 */
	public String getColumnName()
	{
		return columnName;
	}
	
	/**
	 * Returns a description of the meaning of the elements in this column.
	 * @return description of column content.
	 */
	public String getColumnDescription()
	{
		return columnDescription;
	}
	
	/**
	 * Returns true if entries in the column might have null values. False otherwise.
	 * @return true if column entries's values might be null.
	 */
	public boolean isNullAllowed()
	{
		return nullAllowed;
	}

	@Override
	public String toString() {
		return columnName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((columnName == null) ? 0 : columnName.hashCode());
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
		ColumnDefinition<?> other = (ColumnDefinition<?>) obj;
		if (columnName == null) {
			if (other.columnName != null)
				return false;
		} else if (!columnName.equals(other.columnName))
			return false;
		if (valueType == null) {
			if (other.valueType != null)
				return false;
		} else if (!valueType.equals(other.valueType))
			return false;
		return true;
	}
}
