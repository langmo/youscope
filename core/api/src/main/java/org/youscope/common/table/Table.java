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
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;

/**
 * A table, containing entries of specific {@link TableEntry} types.
 * Tables are used in YouScope to e.g. communicate between jobs, or to log states of jobs.
 * @author Moritz Lang
 *
 */
public class Table implements Cloneable, Serializable, Iterable<RowView>
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -611965457300954038L;
	
	private final TableDefinition tableDefinition;
	private final ColumnDataStorage<?>[] columnDataStorages;
	private final ExecutionInformation	executionInformation;
	private final PositionInformation		positionInformation;
	private final long creationTime;
	private final long creationRuntime;
	
	/**
	 * Only used for tables without columns, i.e. empty tables.
	 */
	private int numRows = 0;
	
	/**
	 * Protected Copy constructor. Use {@link #clone()} from outside, and copy constructor from sub-classes.
	 * @param other object to copy.
	 */
	protected Table(Table other)
	{
		tableDefinition = other.tableDefinition;
		columnDataStorages = new ColumnDataStorage<?>[other.columnDataStorages.length];
		for(int i=0; i<columnDataStorages.length; i++)
		{
			columnDataStorages[i] = other.columnDataStorages[i].clone();
		}
		executionInformation = other.executionInformation;
		positionInformation = other.positionInformation;
		creationTime = other.creationTime;
		creationRuntime = other.creationRuntime;
		numRows = other.numRows;
	}
	
	/**
	 * Constructor using an already available {@link TableDefinition}.
	 * @param tableDefinition The table definition, defining the number, names and types of the columns.
	 * @param executionInformation The execution of the measurement where this table was created.
	 * @param positionInformation The logical position in the measurement where this table was created.
	 * @param creationTime The time (in milliseconds since January 1, 1970, 00:00:00 GMT) when the content of this table was created (see {@link Date#getTime()}). 
	 * @param creationRuntime The measurement runtime in ms when the content of this table was created (see {@link MeasurementContext#getMeasurementRuntime()}.
	 * @throws NullPointerException Thrown if the table definition is null.
	 */
	public Table(TableDefinition tableDefinition, long creationTime, long creationRuntime, PositionInformation positionInformation, ExecutionInformation executionInformation) throws NullPointerException
	{
		if(tableDefinition == null)
			throw new NullPointerException();
		this.tableDefinition = tableDefinition;
		this.columnDataStorages = createColumns();
		this.positionInformation = positionInformation;
		this.executionInformation = executionInformation;
		this.creationTime = creationTime;
		this.creationRuntime = creationRuntime;
	}
	
	/**
	 * Constructor using an already available {@link TableDefinition}.
	 * The creation time of the table is set to the current time.
	 * @param tableDefinition The table definition, defining the number, names and types of the columns.
	 * @param creationRuntime The measurement runtime in ms when the content of this table was created (see {@link MeasurementContext#getMeasurementRuntime()}.
	 * @param executionInformation The execution of the measurement where this table was created.
	 * @param positionInformation The logical position in the measurement where this table was created.
	 * @throws NullPointerException Thrown if the table definition is null.
	 */
	public Table(TableDefinition tableDefinition, long creationRuntime, PositionInformation positionInformation, ExecutionInformation executionInformation) throws NullPointerException
	{
		this(tableDefinition, System.currentTimeMillis(), creationRuntime, positionInformation, executionInformation);
	}
	
	/**
	 * Constructor using information on the column names and types.
	 * Currently, YouScope only supports a limited set of value types for tables. Thus, this function throws a 
	 * TableException if given value type is currently not supported by YouScope. See {@link TableHelper} for supported column types. 
	 * @param tableName The name (title) of the table. Should be a single short line.
	 * @param tableDescription Description of the meaning and intended usage of the table. Might contain multiple lines (separated by line breaks).
	 * @param creationTime The time (in milliseconds since January 1, 1970, 00:00:00 GMT) when the content of this table was created (see {@link Date#getTime()}).
	 * @param creationRuntime The measurement runtime in ms when the content of this table was created (see {@link MeasurementContext#getMeasurementRuntime()}.
	 * @param executionInformation The execution of the measurement where this table was created.
	 * @param positionInformation The logical position in the measurement where this table was created.
	 * @param columnDefinitions The columns of the table.
	 * @throws TableException Thrown if given column definitions are currently not supported by YouScope.
	 * @throws NullPointerException Thrown if one of the columns is null, or the title is null.
	 */
	public Table(String tableName, String tableDescription, long creationTime, long creationRuntime, PositionInformation positionInformation, ExecutionInformation executionInformation, ColumnDefinition<?>... columnDefinitions) throws TableException, NullPointerException
	{
		this(new TableDefinition(tableName, tableDescription, columnDefinitions), creationTime, creationRuntime, positionInformation, executionInformation);
	}
	
	/**
	 * Constructor using information on the column names and types.
	 * Currently, YouScope only supports a limited set of value types for tables. Thus, this function throws a 
	 * TableException if given value type is currently not supported by YouScope. See {@link TableHelper} for supported column types. 
	 * The creation time of the table is set to the current time.
	 * @param tableName The name (title) of the table. Should be a single short line.
	 * @param tableDescription Description of the meaning and intended usage of the table. Might contain multiple lines (separated by line breaks).
	 * @param creationRuntime The measurement runtime in ms when the content of this table was created (see {@link MeasurementContext#getMeasurementRuntime()}.
	 * @param executionInformation The execution of the measurement where this table was created.
	 * @param positionInformation The logical position in the measurement where this table was created.
	 * @param columnDefinitions The columns of the table.
	 * @throws TableException Thrown if given column definitions are currently not supported by YouScope.
	 * @throws NullPointerException Thrown if one of the columns is null, or the title is null.
	 */
	public Table(String tableName, String tableDescription, long creationRuntime, PositionInformation positionInformation, ExecutionInformation executionInformation, ColumnDefinition<?>... columnDefinitions) throws TableException, NullPointerException
	{
		this(new TableDefinition(tableName, tableDescription, columnDefinitions),System.currentTimeMillis(), creationRuntime, positionInformation, executionInformation);
	}
	
	/**
	 * Creates a table containing conforming to the provided table definition.
	 * For every column (identified by column name) contained in both tables, the new table contains the entries of the current table.
	 * If a column with a given name does only exist in the new table, its entries are set to null if this is allowed, or a TableExcecption is thrown.
	 * Columns only existing in current table but not in new table are ignored.
	 * If columns exist in both tables, but their value types are different, a TableException is thrown.
	 * If a column in the new table does not allow for null values, but the corresponding column in the old table does contain null entries, a TableException is thrown.
	 * @param tableDefinition The table definition of the new table.
	 * @return The newly created table conforming to the given table definition.
	 * @throws TableException Thrown if the new table could not be constructed due to the reasons stated in the text.
	 * @throws NullPointerException Thrown if tableDefinition is null. 
	 */
	public Table toTable(TableDefinition tableDefinition) throws TableException, NullPointerException
	{
		if(tableDefinition == null)
			throw new NullPointerException();
		Table table = new Table(tableDefinition, getCreationTime(), getPositionInformation(), getExecutionInformation());
		try
		{
			for(RowView rowView : this)
			{
				Serializable[] entries = new Serializable[table.getNumColumns()];
				for(int column = 0; column < table.getNumColumns(); column++)
				{
					try
					{
						entries[column] = rowView.getValue(table.getColumnName(column));
					}
					catch(TableException e)
					{
						if(tableDefinition.getColumnDefinition(column).isNullAllowed())
							entries[column] = null;
						else
							throw new TableException("Table does not contain a column with name " + table.getColumnName(column)+ " which is not allowed to have null entries.", e);
					}
				}
				table.addRow(entries);
			}
		}
		catch(Exception e)
		{
			throw new TableException("Table does not conform to provided table definition.", e);
		}
		return table;
	}
	
	/**
	* Helper function to create the internal data storage structure..
	* @return internal data storage structure.
	*/
	private ColumnDataStorage<?>[] createColumns()
	{
		ColumnDefinition<?>[] columnDefinitions = tableDefinition.getColumnDefinitions();
		ColumnDataStorage<?>[] columns = new ColumnDataStorage<?>[columnDefinitions.length];
		for(int i=0;i<columns.length; i++)
		{
			columns[i] = ColumnDataStorage.createColumn(columnDefinitions[i]);
		}
		return columns;
	}
	
	/**
	 * Adds a row to the table. The number of values must be equal to the number of columns in this table.
	 * Each value either has to be null, or of the value type or a sub-class of the value type of the respective column.
	 * @param values The values of the row.
	 * @throws TableException Thrown if number or types of values is wrong, or if a value corresponding to a column not allowing null entries is null.
	 * 
	 */
	public synchronized void addRow(Serializable... values) throws TableException
	{
		if(values.length != columnDataStorages.length)
			throw new TableException("The table has " + Integer.toString(columnDataStorages.length) + " columns, while " + Integer.toString(values.length) + " entries were provided for the new row.");
		for(int i=0; i<columnDataStorages.length; i++)
		{
			try
			{
				columnDataStorages[i].addRow(values[i]);
			}
			catch(TableException e)
			{
				// remove all already added entries.
				for(int j=0; j<i;j++)
				{
					columnDataStorages[j].removeRow(columnDataStorages[j].getSize()-1);
				}
				throw e;
			}
		}
		numRows++;
	}
	
	/**
	 * Creates a temporary row. The entries of a temporary row do not (yet) belong
	 * to the table. Different to table rows, all entries of a temporary row can be null.
	 * Thus, a temporary row can be used to set the row values one by one.
	 * Afterwards, a temporary row can be added to the table using {@link #addRow(TemporaryRow)} or {@link #insertRow(int, TemporaryRow)}.
	 * @return a temporary row.
	 * @throws TableException Thrown if temporary row could not be constructed.
	 */
	public TemporaryRow createTemporaryRow() throws TableException
	{
		return new TemporaryRow(tableDefinition.getColumnDefinitions());
	}
	
	/**
	 * Adds a temporary row to this table. The row is checked if it contains the
	 * right amount of entries with the right value types.
	 * Furthermore, for all columns which must not contain null values, it is checked
	 * if the corresponding entry in the temporary row is non-null.
	 * @param temporaryRow The temporary row containing the values to add.
	 * @throws TableException thrown if any of the conditions is violated.
	 * @throws NullPointerException Thrown if temporaryRow is null.
	 */
	public void addRow(TemporaryRow temporaryRow) throws TableException, NullPointerException
	{
		if(temporaryRow == null)
			throw new NullPointerException();
		Serializable[] values = new Serializable[temporaryRow.getSize()];
		for(int i=0; i<values.length; i++)
		{
			values[i] = temporaryRow.getValue(i);
		}
		addRow(values);
	}
	
	/**
	 * Adds all rows of the table at the end of the rows of this table. Table definitions have to match.
	 * @param table Table whose rows should be copied.
	 * @throws TableException Thrown if table definitions do not match.
	 * @throws NullPointerException Thrown if table is null.
	 */
	public void addRows(Table table) throws TableException, NullPointerException
	{
		if(table == null)
			throw new NullPointerException();
		if(!getTableDefinition().equals(table.getTableDefinition()))
			throw new TableException("Layout of table which rows should be added to this table is different from this table layout.");
		Serializable[] values = new Serializable[table.getNumColumns()];
		for(RowView row : table)
		{
			for(int i = 0; i<values.length; i++)
			{
				values[i] = row.getValue(i);
			}
			addRow(values);
		}
	}
	/**
	 * Inserts a temporary row at the given row index to this table. The index of the row currently having the given index, and of all subsequent rows, are increased by one.
	 * The row is checked if it contains the
	 * right amount of entries with the right value types.
	 * Furthermore, for all columns which must not contain null values, it is checked
	 * if the corresponding entry in the temporary row is non-null.
	 * @param row The index where to add this row. Must be bigger or equal to 0, and smaller than {@link #getNumRows()}.
	 * @param temporaryRow The temporary row containing the values to insert.
	 * @throws TableException thrown if any of the conditions is violated.
	 * @throws NullPointerException Thrown if temporaryRow is null.
	 * @throws IndexOutOfBoundsException thrown if row index is invalid. 
	 */
	public void insertRow(int row, TemporaryRow temporaryRow) throws TableException, NullPointerException, IndexOutOfBoundsException 
	{
		if(temporaryRow == null)
			throw new NullPointerException();
		Serializable[] values = new Serializable[temporaryRow.getSize()];
		for(int i=0; i<values.length; i++)
		{
			values[i] = temporaryRow.getValue(i);
		}
		insertRow(row, values);
	}
	/**
	 * Inserts a row to the table at the given row. The index of the row currently having the given index, and of all subsequent rows, are increased by one.
	 * The number of values must be equal to the number of columns in this table.
	 * Each value either has to be null, or of the value type or a sub-class of the value type of the respective column.
	 * @param row The index where to add this row. Must be bigger or equal to 0, and smaller than {@link #getNumRows()}.
	 * @param values The values of the row.
	 * @throws TableException Thrown if number or types of values is wrong, or if a value corresponding to a column not allowing null entries is null.
	 * @throws IndexOutOfBoundsException thrown if row index is invalid.
	 * 
	 */
	public synchronized void insertRow(int row, Serializable... values) throws TableException, IndexOutOfBoundsException 
	{
		if(values.length != columnDataStorages.length)
			throw new TableException("The table has " + Integer.toString(columnDataStorages.length) + " columns, while " + Integer.toString(values.length) + " entries were provided for the new row.");
		if(row < 0 || row >= getNumRows())
			throw new IndexOutOfBoundsException("Table has " + Integer.toString(getNumRows()) + " rows. Tried to insert row with index " + Integer.toString(row)+".");
		for(int i=0; i<columnDataStorages.length; i++)
		{
			try
			{
				columnDataStorages[i].insertRow(row, values[i]);
			}
			catch(TableException e)
			{
				// remove all already added entries.
				for(int j=0; j<i;j++)
				{
					columnDataStorages[j].removeRow(row);
				}
				throw e;
			}
		}
		numRows++;
	}
	
	/**
	 * Removes the row at the given index.
	 * @param row The index of the row to remove. Must be bigger or equal to 0, and smaller than {@link #getNumRows()}.
	 * @throws IndexOutOfBoundsException
	 */
	public void removeRow(int row) throws IndexOutOfBoundsException
	{
		if(row < 0 || row >= getNumRows())
			throw new IndexOutOfBoundsException("Table has " + Integer.toString(getNumRows()) + " rows. Tried to remove row with index " + Integer.toString(row)+".");
		for(int i=0; i<columnDataStorages.length; i++)
		{
			columnDataStorages[i].removeRow(row);
		}
		numRows--;
	}
	
	/**
	 * Returns the definition (number, type and names of columns) of this table.
	 * @return Table definition.
	 */
	public TableDefinition getTableDefinition()
	{
		return tableDefinition;
	}
	
	/**
	 * Returns the number of columns in this table. This number can not change.
	 * @return Number of columns.
	 */
	public int getNumColumns()
	{
		return columnDataStorages.length;
	}
	
	/**
	 * Returns the number of rows in this table. This number might change when adding or removing rows.
	 * @return Number of rows.
	 */
	public int getNumRows()
	{
		if(getNumColumns() == 0)
			return numRows;
		return columnDataStorages[0].getSize();
	}
	
	/**
	 * Returns a row view for the given row index.
	 * @param row row index.
	 * @return Row at given index.
	 * @throws IndexOutOfBoundsException Thrown if row index is invalid.
	 */
	public RowView getRowView(final int row) throws IndexOutOfBoundsException
	{
		if(row < 0 || row >= getNumRows())
			throw new IndexOutOfBoundsException("Table has " + Integer.toString(getNumRows())+" rows. Requested row index was " + Integer.toString(row)+".");
		return new RowViewImpl(this, row, tableDefinition.getColumnDefinitions());
	}
	
	/**
	 * Returns the entry at the given row and column.
	 * @param row Row of the entry.
	 * @param column Column of the entry.
	 * @return Table entry at given row and column.
	 * @throws IndexOutOfBoundsException thrown, if row or column index is invalid.
	 */
	public TableEntry<?> getEntry(int row, int column) throws IndexOutOfBoundsException
	{
		if(column < 0 || column >= getNumColumns())
			throw new IndexOutOfBoundsException("Table has " + Integer.toString(getNumColumns())+" columns. Requested column index was " + Integer.toString(column)+".");
		if(row < 0 || row >= getNumRows())
			throw new IndexOutOfBoundsException("Table has " + Integer.toString(getNumRows())+" rows. Requested row index was " + Integer.toString(row)+".");
		
		return columnDataStorages[column].get(row);
		
	}
	
	/**
	 * Returns the table entry's value at the given row and column.
	 * @param row Row of the entry.
	 * @param column Column of the entry.
	 * @return Table entry's value at given row and column.
	 * @throws IndexOutOfBoundsException thrown, if row or column index is invalid.
	 */
	public Serializable getValue(int row, int column) throws IndexOutOfBoundsException
	{
		return getEntry(row, column).getValue();		
	}
	
	/**
	 * Returns the entry at the given row and column.
	 * @param row Row of the entry.
	 * @param columnName Name of the column.
	 * @return Table entry at given row and column.
	 * @throws IndexOutOfBoundsException thrown, if row index is invalid.
	 * @throws TableException Thrown if table does not contain column with given name.
	 * @throws NullPointerException Thrown if columnName is null.
	 */
	public TableEntry<?> getEntry(int row, String columnName) throws TableException, NullPointerException, IndexOutOfBoundsException
	{
		if(columnName == null)
			throw new NullPointerException();
		if(row < 0 || row >= getNumRows())
			throw new IndexOutOfBoundsException("Table has " + Integer.toString(getNumRows())+" rows. Requested row index was " + Integer.toString(row)+".");
		
		for(ColumnDataStorage<?> columnData : columnDataStorages)
		{
			if(columnData.getColumnName().equals(columnName))
				return columnData.get(row);
		}
		throw new TableException("Table does not contain a column with name "+columnName+". Note, that names are case sensitive.");	
	}
	
	/**
	 * Returns the entry's value at the given row and column.
	 * @param row Row of the entry.
	 * @param columnName Name of the column.
	 * @return Table entry's value at given row and column.
	 * @throws IndexOutOfBoundsException thrown, if row index is invalid.
	 * @throws TableException Thrown if table does not contain column with given name.
	 * @throws NullPointerException Thrown if columnName is null.
	 */
	public Serializable getValue(int row, String columnName) throws TableException, NullPointerException, IndexOutOfBoundsException
	{
		return getEntry(row, columnName).getValue();
	}
	
	/**
	 * Returns the entry at the given row and column, having the given value type.
	 * @param row Row of the entry.
	 * @param columnName Name of the column.
	 * @param valueType value type of entry. Must be a super-class of the actual value type.
	 * @return Table entry at given row and column.
	 * @throws IndexOutOfBoundsException thrown, if row index is invalid.
	 * @throws TableException Thrown if table does not contain column with given name.
	 * @throws NullPointerException Thrown if columnName or valueType is null.
	 */
	public <T extends Serializable> TableEntry<? extends T> getEntry(int row, String columnName, Class<T> valueType) throws TableException, NullPointerException, IndexOutOfBoundsException
	{
		if(columnName == null || valueType == null)
			throw new NullPointerException();
		TableEntry<?> entryRaw = getEntry(row, columnName);
		if(!valueType.isAssignableFrom(entryRaw.getValueType()))
			throw new TableException("Entries in column with name "+columnName+" have value type " + entryRaw.getValueType().getName()+", whereas value type "+ valueType.getName()+" was requested.");
		@SuppressWarnings("unchecked")
		TableEntry<? extends T> returnVal = (TableEntry<? extends T>) entryRaw;
		return returnVal;	
	}
	
	/**
	 * Returns the entry at the given row corresponding to the given columnDefinition.
	 * @param row Row of the entry.
	 * @param columnDefinition Definition of the column whose entry at the given row should be returned.
	 * @return Table entry at given row and column.
	 * @throws TableException Thrown if table does not contain column corresponding to the given definition, or if column has different value types.
	 * @throws NullPointerException Thrown if columnDefinition is null.
	 * @throws IndexOutOfBoundsException Thrown if row is invalid.
	 */
	public <T extends Serializable> TableEntry<T> getEntry(int row, ColumnDefinition<T> columnDefinition) throws TableException, NullPointerException, IndexOutOfBoundsException
	{
		if(columnDefinition == null)
			throw new NullPointerException();
		TableEntry<?> entryRaw = getEntry(row, columnDefinition.getColumnName());
		if(!columnDefinition.getValueType().equals(entryRaw.getValueType()))
			throw new TableException("Entries in column with name "+columnDefinition.getColumnName()+" have value type " + entryRaw.getValueType().getName()+", whereas value type "+ columnDefinition.getValueType().getName()+" was requested.");
		@SuppressWarnings("unchecked")
		TableEntry<T> returnVal = (TableEntry<T>) entryRaw;
		return returnVal;	
	}
	
	/**
	 * Returns the value of the entry at the given row corresponding to the given columnDefinition.
	 * @param row Row of the entry.
	 * @param columnDefinition Definition of the column whose entry's value at the given row should be returned.
	 * @return Table entry's value at given row and column.
	 * @throws TableException Thrown if table does not contain column corresponding to the given definition, or if column has different value types.
	 * @throws NullPointerException Thrown if columnDefinition is null.
	 * @throws IndexOutOfBoundsException thrown if row is invalid.
	 */
	public <T extends Serializable> T getValue(int row, ColumnDefinition<T> columnDefinition) throws TableException, NullPointerException, IndexOutOfBoundsException
	{
		return getEntry(row, columnDefinition).getValue();
	}
	
	
	/**
	 * Returns the entry's value at the given row and column, having the given value type.
	 * @param row Row of the entry.
	 * @param columnName Name of the column.
	 * @param valueType value type of entry. Must be a super-class of the actual value type.
	 * @return Table entry's value at given row and column.
	 * @throws IndexOutOfBoundsException thrown, if row index is invalid.
	 * @throws TableException Thrown if table does not contain column with given name.
	 * @throws NullPointerException Thrown if columnName or valueType is null.
	 */
	public <T extends Serializable> T getValue(int row, String columnName, Class<T> valueType) throws TableException, NullPointerException, IndexOutOfBoundsException
	{
		return getEntry(row, columnName, valueType).getValue();
	}
	
	/**
	 * Returns the entry at the given row and column, having the given value type.
	 * @param row Row of the entry.
	 * @param column Column of the entry.
	 * @param valueType value type of entry. Must be a super-class of the actual value type.
	 * @return Table entry at given row and column.
	 * @throws IndexOutOfBoundsException thrown, if row or column index is invalid.
	 * @throws TableException Thrown if the entry's value type is incompatible with given valueType.
	 * @throws NullPointerException Thrown if valueType is null.
	 */
	public <T extends Serializable> TableEntry<? extends T> getEntry(int row, int column, Class<T> valueType) throws IndexOutOfBoundsException, TableException, NullPointerException
	{
		if(valueType==null)
			throw new NullPointerException();
		TableEntry<?> entryRaw = getEntry(row, column);
		if(!valueType.isAssignableFrom(entryRaw.getValueType()))
			throw new TableException("Entry has value type " + entryRaw.getValueType().getName()+", whereas value type "+ valueType.getName()+" was requested.");
		@SuppressWarnings("unchecked")
		TableEntry<? extends T> returnVal = (TableEntry<? extends T>) entryRaw;
		return returnVal;
		
	}
	
	/**
	 * Returns the entry's value at the given row and column, having the given value type.
	 * @param row Row of the entry.
	 * @param column Column of the entry.
	 * @param valueType value type of entry. Must be a super-class of the actual value type.
	 * @return Table entry's value at given row and column.
	 * @throws IndexOutOfBoundsException thrown, if row or column index is invalid.
	 * @throws TableException Thrown if the entry's value type is incompatible with given valueType.
	 * @throws NullPointerException Thrown if valueType is null.
	 */
	public <T extends Serializable> T getValue(int row, int column, Class<T> valueType) throws IndexOutOfBoundsException, TableException, NullPointerException
	{
		return getEntry(row, column, valueType).getValue();
	}
	
	/**
	 * Returns a view on the column with the given index.
	 * @param column index of column.
	 * @return The column at the index.
	 * @throws IndexOutOfBoundsException Thrown if index is invalid.
	 */
	public ColumnView<?> getColumnView(int column) throws IndexOutOfBoundsException
	{
		if(column < 0 || column >= getNumColumns())
			throw new IndexOutOfBoundsException("Table has " + Integer.toString(getNumColumns())+" columns. Requested column index was " + Integer.toString(column)+".");
		return ColumnViewImpl.createColumnView(this, column, tableDefinition.getColumnDefinition(column));
	}
	
	/**
	 * Returns the name (title) of the column with the given index.
	 * @param column index of column.
	 * @return Name of column.
	 * @throws IndexOutOfBoundsException Thrown if index is invalid.
	 */
	public String getColumnName(int column) throws IndexOutOfBoundsException
	{
		if(column < 0 || column >= getNumColumns())
			throw new IndexOutOfBoundsException("Table has " + Integer.toString(getNumColumns())+" columns. Requested column index was " + Integer.toString(column)+".");
		return columnDataStorages[column].getColumnName();
	}
	
	/**
	 * Returns the names (titles) of all columns.
	 * @return Names of all columns in same order as columns are in table.
	 */
	public String[] getColumnNames()
	{
		String[] names = new String[columnDataStorages.length];
		for(int i=0; i < columnDataStorages.length; i++)
		{
			names[i] = columnDataStorages[i].getColumnName();
		}
		return names;
	}
	
	/**
	 * Returns a view on the column with the given name.
	 * @param columnName Name of the column (case sensitive).
	 * @return The column with the given name.
	 * @throws NullPointerException Thrown if columnName is null.
	 * @throws TableException Thrown if table does not contain column with given name.
	 */
	public ColumnView<?> getColumnView(String columnName) throws NullPointerException, TableException
	{
		if(columnName == null)
			throw new NullPointerException("Provided column name is null.");
		for(int column=0; column<getNumColumns(); column++)
		{
			if(!columnDataStorages[column].getColumnName().equals(columnName))
				continue;
			return ColumnViewImpl.createColumnView(this, column, tableDefinition.getColumnDefinition(column));				
		}
		throw new TableException("Table does not contain a column with name "+columnName+". Note, that names are case sensitive.");
	}
	
	/**
	 * Returns a view on the column with the given index, having the given value type.
	 * @param column index of column.
	 * @param valueType value type of column. Must be a super-class of the actual value type.
	 * @return The column at the index.
	 * @throws IndexOutOfBoundsException Thrown if index is invalid.
	 * @throws TableException Thrown if column at given index does not contain entries with value type valueType.
	 * @throws NullPointerException Thrown if valueType is null.
	 */
	public <T extends Serializable> ColumnView<? extends T> getColumnView(int column, Class<T> valueType) throws IndexOutOfBoundsException, TableException, NullPointerException
	{
		if(valueType == null)
			throw new NullPointerException();
		ColumnView<?> columnRaw = getColumnView(column);
		if(!valueType.isAssignableFrom(columnRaw.getValueType()))
			throw new TableException("Column contains entries with value type " + columnRaw.getValueType().getName()+", whereas column value type "+ valueType.getName()+" was requested.");
		@SuppressWarnings("unchecked")
		ColumnView<? extends T> returnVal = (ColumnView<? extends T>) columnRaw;
		return returnVal;
	}
	
	/**
	 * Returns a view on the column with the given name, having the given value type.
	 * @param columnName  Name of the column (case sensitive).
	 * @param valueType value type of column.
	 * @return The column with the given name.
	 * @throws TableException Thrown if table does not contain a column with given name, or if column with the given name does not contain entries with value type valueType.
	 * @throws NullPointerException Thrown if columnName or valueType is null.
	 */
	public <T extends Serializable> ColumnView<? extends T> getColumnView(String columnName, Class<T> valueType) throws TableException, NullPointerException
	{
		if(columnName== null || valueType == null)
			throw new NullPointerException();
		ColumnView<?> columnRaw = getColumnView(columnName);
		if(!valueType.isAssignableFrom(columnRaw.getValueType()))
			throw new TableException("Column contains entries with value type " + columnRaw.getValueType().getName()+", whereas column value type "+ valueType.getName()+" was requested.");
		@SuppressWarnings("unchecked")
		ColumnView<? extends T> returnVal = (ColumnView<? extends T>) columnRaw;
		return returnVal;
	}
	
	/**
	 * Returns a view on the column which equals (only name and value type) a given, already known, column definition.
	 * @param columnDefinition  The definition of the column.
	 * @return The column corresponding to the given column definition.
	 * @throws TableException Thrown if table does not contain corresponding column, or if corresponding column has different valueType.
	 * @throws NullPointerException Thrown if columnDefinition is null.
	 */
	public <T extends Serializable> ColumnView<T> getColumnView(ColumnDefinition<T> columnDefinition) throws TableException, NullPointerException
	{
		if(columnDefinition == null)
			throw new NullPointerException();
		ColumnView<?> columnRaw = getColumnView(columnDefinition.getColumnName());
		if(!columnDefinition.getValueType().equals(columnRaw.getValueType()))
			throw new TableException("Column contains entries with value type " + columnRaw.getValueType().getName()+", whereas column value type "+ columnDefinition.getValueType().getName()+" was defined in column definition.");
		@SuppressWarnings("unchecked")
		ColumnView<T> returnVal = (ColumnView<T>) columnRaw;
		return returnVal;
	}
	
	@Override
	public Table clone()
	{
		return new Table(this);
	}

	/**
	 * Returns the logical position information in the measurement where this table was created.
	 * Returns null if unknown, or if not created as part of a measurement.
	 * @return position information where this table was created.
	 */
	public PositionInformation getPositionInformation() {
		return positionInformation;
	}

	/**
	 * Returns logical information of when during the measurement execution the table was created.
	 * Returns null if unknown, or if not created as part of a measurement.
	 * @return logical execution information when the table was created.
	 */
	public ExecutionInformation getExecutionInformation() {
		return executionInformation;
	}

	/**
	 * Returns the time in milliseconds after January 1, 1970 00:00:00 GMT, when the content of this table was created.
	 * See {@link Date#Date(long)}.
	 * @return Time in ms when content was created.
	 */
	public long getCreationTime() {
		return creationTime;
	}
	
	/**
	 * Returns the runtime of the measurement, in ms, when the content of this table was created.
	 * See {@link MeasurementContext#getMeasurementRuntime()}.
	 * @return Time in ms when content was created.
	 */
	public long getCreationRuntime() {
		return creationRuntime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(columnDataStorages);
		result = prime * result + (int) (creationTime ^ (creationTime >>> 32));
		result = prime
				* result
				+ ((executionInformation == null) ? 0 : executionInformation
						.hashCode());
		result = prime * result + numRows;
		result = prime
				* result
				+ ((positionInformation == null) ? 0 : positionInformation
						.hashCode());
		result = prime * result
				+ ((tableDefinition == null) ? 0 : tableDefinition.hashCode());
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
		Table other = (Table) obj;
		if (!Arrays.equals(columnDataStorages, other.columnDataStorages))
			return false;
		if (creationTime != other.creationTime)
			return false;
		if (executionInformation == null) {
			if (other.executionInformation != null)
				return false;
		} else if (!executionInformation.equals(other.executionInformation))
			return false;
		if (numRows != other.numRows)
			return false;
		if (positionInformation == null) {
			if (other.positionInformation != null)
				return false;
		} else if (!positionInformation.equals(other.positionInformation))
			return false;
		if (tableDefinition == null) {
			if (other.tableDefinition != null)
				return false;
		} else if (!tableDefinition.equals(other.tableDefinition))
			return false;
		return true;
	}

	@Override
	public Iterator<RowView> iterator()
	{
		return new Iterator<RowView>()
		{
			private int row = -1;
			@Override
			public boolean hasNext() {
				return row+1<getNumRows();
			}

			@Override
			public RowView next() {
				row++;
				try
				{
					return getRowView(row);
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
}
