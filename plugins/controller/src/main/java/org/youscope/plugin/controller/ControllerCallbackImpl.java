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
/**
 * 
 */
package org.youscope.plugin.controller;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

import org.youscope.common.table.Table;
import org.youscope.common.table.TableEntry;
import org.youscope.common.table.TableException;
import org.youscope.common.table.TemporaryRow;

/**
 * @author Moritz Lang
 *
 */
class ControllerCallbackImpl extends UnicastRemoteObject implements ControllerCallback
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 7043433263726596570L;
	private final Table outputTable;
	private final Table inputTable;
	private final ArrayList<TemporaryRow> outputRows = new ArrayList<TemporaryRow>();
	private final HashMap<String, String> states;
	private final static int MAX_OUTPUT_ROWS = 100;
	ControllerCallbackImpl(Table inputTable, Table outputTable, HashMap<String, String> states) throws RemoteException
	{
		super();
		this.outputTable = outputTable;
		this.inputTable = inputTable;
		this.states = states;
	}
	public Table getOutputTable() throws TableException
	{
		for(TemporaryRow row : outputRows)
		{
			outputTable.addRow(row);
		}
		return outputTable;
	}
	
	HashMap<String, String> getStates()
	{
		return states;
	}
	
	private TemporaryRow getOutputRow(int rowNumber) throws ControllerException
	{
		if(rowNumber < 0)
			throw new ControllerException("Row number most be bigger or equal to zero.");
		else if(rowNumber >= MAX_OUTPUT_ROWS)
			throw new ControllerException("Maximal amount of rows to add to the output table is " + Integer.toString(MAX_OUTPUT_ROWS)+".");
		while(rowNumber >= outputRows.size())
		{
			try {
				outputRows.add(outputTable.createTemporaryRow());
			} catch (TableException e) {
				throw new ControllerException("Could not create new row.", e);
			}
		}
		return outputRows.get(rowNumber);
	}
	
	private TableEntry<?> getOutputEntry(int rowNumber, int columnNumber) throws ControllerException
	{
		TemporaryRow row = getOutputRow(rowNumber);
		if(columnNumber < 0)
			throw new ControllerException("Column number most be bigger or equal to zero.");
		else if(columnNumber >= row.getNumColumns())
			throw new ControllerException("Table only has "+Integer.toString(row.getNumColumns())+" columns. Requested column number was "+Integer.toString(columnNumber)+".");
		try
		{
			return row.get(columnNumber);
		}
		catch(IndexOutOfBoundsException e)
		{
			throw new ControllerException("Could not get table entry.",e);
		}
	}
	
	private int getOutputColumnNumber(String columnName) throws ControllerException
	{
		for(int i=0; i<outputTable.getNumColumns();i++)
		{
			if(outputTable.getColumnName(i).equals(columnName))
				return i;
		}
		
		String existingColumns = "";
		for(int i=0; i< outputTable.getNumColumns(); i++)
		{
			if(i>0)
				existingColumns += ", ";
			existingColumns += outputTable.getColumnName(i);
		}
		
		throw new ControllerException("An output column with the name \"" + columnName + "\" does not exist in the output table. Existing columns: " + existingColumns + ".");
	}
	
	@Override
	public void setOutput(int rowNumber, String columnName, String value) throws RemoteException, ControllerException
	{
		setOutput(rowNumber, getOutputColumnNumber(columnName), value);
	}

	@Override
	public void setOutput(int rowNumber, String columnName, int value) throws RemoteException, ControllerException
	{
		setOutput(rowNumber, getOutputColumnNumber(columnName), value);
	}

	@Override
	public void setOutput(int rowNumber, String columnName, double value) throws RemoteException, ControllerException
	{
		setOutput(rowNumber, getOutputColumnNumber(columnName), value);
	}

	@Override
	public void setOutput(int rowNumber, int columnNumber, String value) throws RemoteException, ControllerException
	{
		TableEntry<?> entry = getOutputEntry(rowNumber, columnNumber);
		if(entry.getValueType().isAssignableFrom(String.class))
		{
			try {
				entry.setValue(value);
			} catch (TableException e) {
				throw new ControllerException("Could not set table entry to value "+value+".", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Integer.class))
		{
			try
			{
				try {
					entry.setValue(new Integer(value));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+value+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Integer.", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Double.class))
		{
			try
			{
				try {
					entry.setValue(new Double(value));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+value+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Double.", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Float.class))
		{
			try
			{
				try {
					entry.setValue(new Float(value));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+value+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Float.", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Long.class))
		{
			try
			{
				try {
					entry.setValue(new Long(value));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+value+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Long.", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Boolean.class))
		{
			try
			{
				try {
					entry.setValue(new Boolean(value));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+value+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Boolean.", e);
			}
		}
		else
		{
			throw new ControllerException("Required entry type is "+entry.getClass().toString()+".");
		}
	}

	@Override
	public void setOutput(int rowNumber, int columnNumber, int value) throws RemoteException, ControllerException
	{
		TableEntry<?> entry = getOutputEntry(rowNumber, columnNumber);
		if(entry.getValueType().isAssignableFrom(String.class))
		{
			try {
				entry.setValue(Integer.toString(value));
			} catch (TableException e) {
				throw new ControllerException("Could not set table entry to value "+Integer.toString(value)+".", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Integer.class))
		{
			try
			{
				try {
					entry.setValue(new Integer(value));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+Integer.toString(value)+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Integer.", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Double.class))
		{
			try
			{
				try {
					entry.setValue(new Double(value));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+Integer.toString(value)+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Double.", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Float.class))
		{
			try
			{
				try {
					entry.setValue(new Float(value));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+Integer.toString(value)+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Float.", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Long.class))
		{
			try
			{
				try {
					entry.setValue(new Long(value));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+Integer.toString(value)+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Long.", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Boolean.class))
		{
			try
			{
				try {
					entry.setValue(new Boolean(value!=0));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+Integer.toString(value)+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Boolean.", e);
			}
		}
		else
		{
			throw new ControllerException("Required entry type is "+entry.getClass().toString()+".");
		}
	}

	@Override
	public void setOutput(int rowNumber, int columnNumber, double value) throws RemoteException, ControllerException
	{
		TableEntry<?> entry = getOutputEntry(rowNumber, columnNumber);
		if(entry.getValueType().isAssignableFrom(String.class))
		{
			try {
				entry.setValue(Double.toString(value));
			} catch (TableException e) {
				throw new ControllerException("Could not set table entry to value "+Double.toString(value)+".", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Integer.class))
		{
			try
			{
				try {
					entry.setValue(new Integer((int)value));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+Double.toString(value)+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Integer.", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Double.class))
		{
			try
			{
				try {
					entry.setValue(new Double(value));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+Double.toString(value)+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Double.", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Float.class))
		{
			try
			{
				try {
					entry.setValue(new Float(value));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+Double.toString(value)+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Float.", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Long.class))
		{
			try
			{
				try {
					entry.setValue(new Long((long) value));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+Double.toString(value)+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Long.", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Boolean.class))
		{
			try
			{
				try {
					entry.setValue(new Boolean(value!=0));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+Double.toString(value)+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Boolean.", e);
			}
		}
		else
		{
			throw new ControllerException("Required entry type is "+entry.getClass().toString()+".");
		}
	}

	private int getInputColumnNumber(String columnName) throws ControllerException
	{
		for(int i=0; i<inputTable.getNumColumns();i++)
		{
			if(inputTable.getColumnName(i).equals(columnName))
				return i;
		}
		
		String existingColumns = "";
		for(int i=0; i< inputTable.getNumColumns(); i++)
		{
			if(i>0)
				existingColumns += ", ";
			existingColumns += inputTable.getColumnName(i);
		}
		
		throw new ControllerException("An input column with the name \"" + columnName + "\" does not exist in the input table. Existing columns: " + existingColumns + ".");
	}
	
	private TableEntry<?> getInputEntry(int rowNumber, int columnNumber) throws ControllerException
	{
		if(columnNumber < 0)
			throw new ControllerException("Column number most be bigger or equal to zero.");
		else if(columnNumber >= inputTable.getNumColumns())
			throw new ControllerException("Table only has "+Integer.toString(inputTable.getNumColumns())+" columns. Requested column number was "+Integer.toString(columnNumber)+".");
		if(rowNumber < 0)
			throw new ControllerException("Row number most be bigger or equal to zero.");
		else if(rowNumber >= inputTable.getNumRows())
			throw new ControllerException("Table only has "+Integer.toString(inputTable.getNumRows())+" rows. Requested row number was "+Integer.toString(rowNumber)+".");
		try
		{
			return inputTable.getEntry(rowNumber, columnNumber);
		}
		catch(IndexOutOfBoundsException e)
		{
			throw new ControllerException("Could not get table entry.", e);
		}
	}
	
	@Override
	public String getInputAsString(int rowNumber, String columnName) throws RemoteException, ControllerException
	{
		return getInputAsString(rowNumber, getInputColumnNumber(columnName));
	}

	@Override
	public String getInputAsString(int rowNumber, int columnNumber) throws RemoteException, ControllerException
	{
		return getInputEntry(rowNumber, columnNumber).getValueAsString();
	}

	@Override
	public double getInputAsDouble(int rowNumber, String columnName) throws RemoteException, ControllerException, NumberFormatException
	{
		return getInputAsDouble(rowNumber, getInputColumnNumber(columnName));
	}

	@Override
	public double getInputAsDouble(int rowNumber, int columnNumber) throws RemoteException, ControllerException, NumberFormatException
	{
		TableEntry<?> entry = getInputEntry(rowNumber, columnNumber);
		if(Number.class.isAssignableFrom(entry.getValueType()))
			return ((Number)entry.getValue()).doubleValue();
		return Double.parseDouble(entry.getValueAsString());
	}

	@Override
	public int getInputAsInteger(int rowNumber,String columnName) throws RemoteException, ControllerException, NumberFormatException
	{
		return getInputAsInteger(rowNumber, getInputColumnNumber(columnName));
	}

	@Override
	public int getInputAsInteger(int rowNumber, int columnNumber) throws RemoteException, ControllerException, NumberFormatException
	{
		TableEntry<?> entry = getInputEntry(rowNumber, columnNumber);
		if(Number.class.isAssignableFrom(entry.getValueType()))
			return ((Number)entry.getValue()).intValue();
		return Integer.parseInt(entry.getValueAsString());
	}

	@Override
	public int getNumInputRows() throws RemoteException
	{
		return inputTable.getNumRows();
	}

	@Override
	public int getNumInputColumns() throws RemoteException
	{
		return inputTable.getNumColumns();
	}

	@Override
	public int getNumOutputColumns() throws RemoteException
	{
		return outputTable.getNumColumns();
	}

	@Override
	public String getStateAsString(String state, String defaultValue) throws RemoteException
	{
		String value = states.get(state);
		if(value == null)
			return defaultValue;
		return value;
	}

	@Override
	public double getStateAsDouble(String state, double defaultValue) throws RemoteException, NumberFormatException
	{
		return Double.parseDouble(getStateAsString(state, Double.toString(defaultValue)));
	}

	@Override
	public int getStateAsInteger(String state, int defaultValue) throws RemoteException, NumberFormatException
	{
		double val = getStateAsDouble(state, defaultValue);
		if(Math.abs(val - ((int)val)) < 0.000000001)
			return (int)val;

		throw new NumberFormatException("State " + state + " has value " + Double.toString(val) + ", which is not an integer.");
	}

	@Override
	public void setState(String state, String value) throws RemoteException
	{
		states.put(state, value);
	}

	@Override
	public void setState(String state, int value) throws RemoteException
	{
		states.put(state, Integer.toString(value));
	}

	@Override
	public void setState(String state, double value) throws RemoteException
	{
		states.put(state, Double.toString(value));
	}
	@Override
	public void setOutput(String columnName, String value) throws RemoteException, ControllerException
	{
		setOutput(0, columnName, value);
		
	}
	@Override
	public void setOutput(String columnName, int value) throws RemoteException, ControllerException
	{
		setOutput(0, columnName, value);
	}
	@Override
	public void setOutput(String columnName, double value) throws RemoteException, ControllerException
	{
		setOutput(0, columnName, value);
	}
	@Override
	public void setOutput(int columnNumber, String value) throws RemoteException, ControllerException
	{
		setOutput(0, columnNumber, value);
	}
	@Override
	public void setOutput(int columnNumber, int value) throws RemoteException, ControllerException
	{
		setOutput(0, columnNumber, value);
	}
	@Override
	public void setOutput(int columnNumber, double value) throws RemoteException, ControllerException
	{
		setOutput(0, columnNumber, value);
	}
	@Override
	public void setOutput(int rowNumber, String columnName, float value) throws RemoteException, ControllerException {
		setOutput(rowNumber, getOutputColumnNumber(columnName), value);
	}
	@Override
	public void setOutput(int rowNumber, String columnName, boolean value) throws RemoteException, ControllerException {
		setOutput(rowNumber, getOutputColumnNumber(columnName), value);
	}
	@Override
	public void setOutput(int rowNumber, String columnName, long value) throws RemoteException, ControllerException {
		setOutput(rowNumber, getOutputColumnNumber(columnName), value);
	}
	@Override
	public void setOutput(int rowNumber, int columnNumber, float value) throws RemoteException, ControllerException {
		TableEntry<?> entry = getOutputEntry(rowNumber, columnNumber);
		if(entry.getValueType().isAssignableFrom(String.class))
		{
			try {
				entry.setValue(Float.toString(value));
			} catch (TableException e) {
				throw new ControllerException("Could not set table entry to value "+Float.toString(value)+".", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Integer.class))
		{
			try
			{
				try {
					entry.setValue(new Integer((int) value));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+Float.toString(value)+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Integer.", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Double.class))
		{
			try
			{
				try {
					entry.setValue(new Double(value));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+Float.toString(value)+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Double.", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Float.class))
		{
			try
			{
				try {
					entry.setValue(new Float(value));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+Float.toString(value)+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Float.", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Long.class))
		{
			try
			{
				try {
					entry.setValue(new Long((long) value));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+Float.toString(value)+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Long.", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Boolean.class))
		{
			try
			{
				try {
					entry.setValue(new Boolean(value!=0));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+Float.toString(value)+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Boolean.", e);
			}
		}
		else
		{
			throw new ControllerException("Required entry type is "+entry.getClass().toString()+".");
		}
	}
	@Override
	public void setOutput(int rowNumber, int columnNumber, boolean value) throws RemoteException, ControllerException {
		TableEntry<?> entry = getOutputEntry(rowNumber, columnNumber);
		if(entry.getValueType().isAssignableFrom(String.class))
		{
			try {
				entry.setValue(Boolean.toString(value));
			} catch (TableException e) {
				throw new ControllerException("Could not set table entry to value "+Boolean.toString(value)+".", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Integer.class))
		{
			try
			{
				try {
					entry.setValue(new Integer(value?1:0));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+Boolean.toString(value)+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Integer.", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Double.class))
		{
			try
			{
				try {
					entry.setValue(new Double(value?1:0));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+Boolean.toString(value)+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Double.", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Float.class))
		{
			try
			{
				try {
					entry.setValue(new Float(value?1:0));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+Boolean.toString(value)+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Float.", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Long.class))
		{
			try
			{
				try {
					entry.setValue(new Long(value?1:0));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+Boolean.toString(value)+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Long.", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Boolean.class))
		{
			try
			{
				try {
					entry.setValue(new Boolean(value));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+Boolean.toString(value)+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Boolean.", e);
			}
		}
		else
		{
			throw new ControllerException("Required entry type is "+entry.getClass().toString()+".");
		}
	}
	@Override
	public void setOutput(int rowNumber, int columnNumber, long value) throws RemoteException, ControllerException {
		TableEntry<?> entry = getOutputEntry(rowNumber, columnNumber);
		if(entry.getValueType().isAssignableFrom(String.class))
		{
			try {
				entry.setValue(Long.toString(value));
			} catch (TableException e) {
				throw new ControllerException("Could not set table entry to value "+Long.toString(value)+".", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Integer.class))
		{
			try
			{
				try {
					entry.setValue(new Integer((int) value));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+Long.toString(value)+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Integer.", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Double.class))
		{
			try
			{
				try {
					entry.setValue(new Double(value));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+Long.toString(value)+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Double.", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Float.class))
		{
			try
			{
				try {
					entry.setValue(new Float(value));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+Long.toString(value)+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Float.", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Long.class))
		{
			try
			{
				try {
					entry.setValue(new Long(value));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+Long.toString(value)+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Long.", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(Boolean.class))
		{
			try
			{
				try {
					entry.setValue(new Boolean(value!=0));
				} catch (TableException e) {
					throw new ControllerException("Could not set table entry to value "+Long.toString(value)+".", e);
				}
			}
			catch(NumberFormatException e)
			{
				throw new ControllerException("Required entry type is Boolean.", e);
			}
		}
		else
		{
			throw new ControllerException("Required entry type is "+entry.getClass().toString()+".");
		}
		
	}
	@Override
	public float getInputAsFloat(int rowNumber, String columnName) throws RemoteException, ControllerException {
		return getInputAsFloat(rowNumber, getInputColumnNumber(columnName));
	}
	@Override
	public long getInputAsLong(int rowNumber, String columnName) throws RemoteException, ControllerException {
		return getInputAsLong(rowNumber, getInputColumnNumber(columnName));
	}
	@Override
	public boolean getInputAsBoolean(int rowNumber, String columnName) throws RemoteException, ControllerException {
		return getInputAsBoolean(rowNumber, getInputColumnNumber(columnName));
	}
	@Override
	public boolean getInputAsBoolean(int rowNumber, int columnNumber) throws RemoteException, ControllerException {
		TableEntry<?> entry = getInputEntry(rowNumber, columnNumber);
		if(Boolean.class.isAssignableFrom(entry.getValueType()))
			return ((Boolean)entry.getValue()).booleanValue();
		return Boolean.parseBoolean(entry.getValueAsString());
	}
	@Override
	public float getInputAsFloat(int rowNumber, int columnNumber) throws RemoteException, ControllerException, NumberFormatException {
		TableEntry<?> entry = getInputEntry(rowNumber, columnNumber);
		if(Number.class.isAssignableFrom(entry.getValueType()))
			return ((Number)entry.getValue()).floatValue();
		return Float.parseFloat(entry.getValueAsString());
	}
	@Override
	public long getInputAsLong(int rowNumber, int columnNumber) throws RemoteException, ControllerException, NumberFormatException {
		TableEntry<?> entry = getInputEntry(rowNumber, columnNumber);
		if(Number.class.isAssignableFrom(entry.getValueType()))
			return ((Number)entry.getValue()).longValue();
		return Long.parseLong(entry.getValueAsString());
	}
	@Override
	public void setOutput(int rowNumber, String columnName, Serializable value)
			throws RemoteException, ControllerException {
		setOutput(rowNumber, getOutputColumnNumber(columnName), value);
		
	}
	@Override
	public void setOutput(int rowNumber, int columnNumber, Serializable value)
			throws RemoteException, ControllerException {
		TableEntry<?> entry = getOutputEntry(rowNumber, columnNumber);
		if(entry.getValueType().isInstance(value))
		{
			try {
				entry.setValue(value);
			} catch (TableException e) {
				throw new ControllerException("Could not set table entry to value "+value.toString()+".", e);
			}
		}
		else if(entry.getValueType().isAssignableFrom(String.class))
		{
			try {
				entry.setValue(value.toString());
			} catch (TableException e) {
				throw new ControllerException("Could not set table entry to value "+value.toString()+".", e);
			}
		}
		else
		{
			throw new ControllerException("Required entry type is "+entry.getClass().toString()+".");
		}
		
	}
	@Override
	public Serializable getInput(int rowNumber, String columnName) throws RemoteException, ControllerException {
		return getInput(rowNumber, getInputColumnNumber(columnName));
	}
	@Override
	public Serializable getInput(int rowNumber, int columnNumber) throws RemoteException, ControllerException {
		TableEntry<?> entry = getInputEntry(rowNumber, columnNumber);
		return entry.getValue();
	}

}
