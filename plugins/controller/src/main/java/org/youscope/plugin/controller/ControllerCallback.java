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
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Callback given to the control algorithm such that it can obtain its state and the values of the inputs, and set the output.
 * @author Moritz Lang
 *
 */
public interface ControllerCallback extends Remote
{
	/**
	 * Sets the output in the given row of the output data table with the given column header to the given value.
	 * @param rowNumber The number of the row to set. If the row does not exist, it it created. If also smaller row numbers do not exist, also these rows are created..
	 * @param columnName The column header of the output to set.
	 * @param value The value of the output.
	 * @throws RemoteException
	 * @throws ControllerException
	 */
	public void setOutput(int rowNumber, String columnName, String value) throws RemoteException, ControllerException;
	
	/**
	 * Sets the output in the given row of the output data table with the given column header to the given value.
	 * @param rowNumber The number of the row to set. If the row does not exist, it it created. If also smaller row numbers do not exist, also these rows are created..
	 * @param columnName The column header of the output to set.
	 * @param value The value of the output.
	 * @throws RemoteException
	 * @throws ControllerException
	 */
	public void setOutput(int rowNumber, String columnName, int value) throws RemoteException, ControllerException;
	
	/**
	 * Sets the output in the given row of the output data table with the given column header to the given value.
	 * @param rowNumber The number of the row to set. If the row does not exist, it it created. If also smaller row numbers do not exist, also these rows are created..
	 * @param columnName The column header of the output to set.
	 * @param value The value of the output.
	 * @throws RemoteException
	 * @throws ControllerException
	 */
	public void setOutput(int rowNumber, String columnName, float value) throws RemoteException, ControllerException;
	
	/**
	 * Sets the output in the given row of the output data table with the given column header to the given value.
	 * @param rowNumber The number of the row to set. If the row does not exist, it it created. If also smaller row numbers do not exist, also these rows are created..
	 * @param columnName The column header of the output to set.
	 * @param value The value of the output.
	 * @throws RemoteException
	 * @throws ControllerException
	 */
	public void setOutput(int rowNumber, String columnName, boolean value) throws RemoteException, ControllerException;
	
	/**
	 * Sets the output in the given row of the output data table with the given column header to the given value.
	 * @param rowNumber The number of the row to set. If the row does not exist, it it created. If also smaller row numbers do not exist, also these rows are created..
	 * @param columnName The column header of the output to set.
	 * @param value The value of the output.
	 * @throws RemoteException
	 * @throws ControllerException
	 */
	public void setOutput(int rowNumber, String columnName, long value) throws RemoteException, ControllerException;
	
	/**
	 * Sets the output in the given row of the output data table with the given column header to the given value.
	 * @param rowNumber The number of the row to set. If the row does not exist, it it created. If also smaller row numbers do not exist, also these rows are created..
	 * @param columnName The column header of the output to set.
	 * @param value The value of the output.
	 * @throws RemoteException
	 * @throws ControllerException
	 */
	public void setOutput(int rowNumber, String columnName, double value) throws RemoteException, ControllerException;
	
	/**
	 * Sets the output in the given row of the output data table with the given column header to the given value.
	 * @param rowNumber The number of the row to set. If the row does not exist, it it created. If also smaller row numbers do not exist, also these rows are created..
	 * @param columnName The column header of the output to set.
	 * @param value The value of the output.
	 * @throws RemoteException
	 * @throws ControllerException
	 */
	public void setOutput(int rowNumber, String columnName, Serializable value) throws RemoteException, ControllerException;
	
	/**
	 * Sets the output in the given row of the output data table with the given column number to the given value.
	 * @param rowNumber The number of the row to set. If the row does not exist, it it created. If also smaller row numbers do not exist, also these rows are created..
	 * @param columnNumber The number of the column of the output to set.
	 * @param value The value of the output.
	 * @throws RemoteException
	 * @throws ControllerException
	 */
	public void setOutput(int rowNumber, int columnNumber, String value) throws RemoteException, ControllerException;
	
	/**
	 * Sets the output in the given row of the output data table with the given column number to the given value.
	 * @param rowNumber The number of the row to set. If the row does not exist, it it created. If also smaller row numbers do not exist, also these rows are created..
	 * @param columnNumber The number of the column of the output to set.
	 * @param value The value of the output.
	 * @throws RemoteException
	 * @throws ControllerException
	 */
	public void setOutput(int rowNumber, int columnNumber, Serializable value) throws RemoteException, ControllerException;
	
	/**
	 * Sets the output in the given row of the output data table with the given column number to the given value.
	 * @param rowNumber The number of the row to set. If the row does not exist, it it created. If also smaller row numbers do not exist, also these rows are created..
	 * @param columnNumber The number of the column of the output to set.
	 * @param value The value of the output.
	 * @throws RemoteException
	 * @throws ControllerException
	 */
	public void setOutput(int rowNumber, int columnNumber, float value) throws RemoteException, ControllerException;
	
	/**
	 * Sets the output in the given row of the output data table with the given column number to the given value.
	 * @param rowNumber The number of the row to set. If the row does not exist, it it created. If also smaller row numbers do not exist, also these rows are created..
	 * @param columnNumber The number of the column of the output to set.
	 * @param value The value of the output.
	 * @throws RemoteException
	 * @throws ControllerException
	 */
	public void setOutput(int rowNumber, int columnNumber, boolean value) throws RemoteException, ControllerException;
	
	/**
	 * Sets the output in the given row of the output data table with the given column number to the given value.
	 * @param rowNumber The number of the row to set. If the row does not exist, it it created. If also smaller row numbers do not exist, also these rows are created..
	 * @param columnNumber The number of the column of the output to set.
	 * @param value The value of the output.
	 * @throws RemoteException
	 * @throws ControllerException
	 */
	public void setOutput(int rowNumber, int columnNumber, long value) throws RemoteException, ControllerException;
	
	/**
	 * Sets the output in the given row of the output data table with the given column number to the given value.
	 * @param rowNumber The number of the row to set. If the row does not exist, it it created. If also smaller row numbers do not exist, also these rows are created..
	 * @param columnNumber The number of the column of the output to set.
	 * @param value The value of the output.
	 * @throws RemoteException
	 * @throws ControllerException
	 */
	public void setOutput(int rowNumber, int columnNumber, int value) throws RemoteException, ControllerException;
	
	/**
	 * Sets the output in the given row of the output data table with the given column number to the given value.
	 * @param rowNumber The number of the row to set. If the row does not exist, it it created. If also smaller row numbers do not exist, also these rows are created..
	 * @param columnNumber The number of the column of the output to set.
	 * @param value The value of the output.
	 * @throws RemoteException
	 * @throws ControllerException
	 */
	public void setOutput(int rowNumber, int columnNumber, double value) throws RemoteException, ControllerException;
	
	
	
	/**
	 * Sets the output in the first row of the output data table with the given column header to the given value.
	 * @param columnName The column header of the output to set.
	 * @param value The value of the output.
	 * @throws RemoteException
	 * @throws ControllerException
	 */
	public void setOutput(String columnName, String value) throws RemoteException, ControllerException;
	
	/**
	 * Sets the output in the first row of the output data table with the given column header to the given value.
	 * @param columnName The column header of the output to set.
	 * @param value The value of the output.
	 * @throws RemoteException
	 * @throws ControllerException
	 */
	public void setOutput(String columnName, int value) throws RemoteException, ControllerException;
	
	/**
	 * Sets the output in the first row of the output data table with the given column header to the given value.
	 * @param columnName The column header of the output to set.
	 * @param value The value of the output.
	 * @throws RemoteException
	 * @throws ControllerException
	 */
	public void setOutput(String columnName, double value) throws RemoteException, ControllerException;
	
	/**
	 * Sets the output in the first row of the output data table with the given column number to the given value.
	 * @param columnNumber The number of the column of the output to set.
	 * @param value The value of the output.
	 * @throws RemoteException
	 * @throws ControllerException
	 */
	public void setOutput(int columnNumber, String value) throws RemoteException, ControllerException;
	
	/**
	 * Sets the output in the first row of the output data table with the given column number to the given value.
	 * @param columnNumber The number of the column of the output to set.
	 * @param value The value of the output.
	 * @throws RemoteException
	 * @throws ControllerException
	 */
	public void setOutput(int columnNumber, int value) throws RemoteException, ControllerException;
	
	/**
	 * Sets the output in the first row of the output data table with the given column number to the given value.
	 * @param columnNumber The number of the column of the output to set.
	 * @param value The value of the output.
	 * @throws RemoteException
	 * @throws ControllerException
	 */
	public void setOutput(int columnNumber, double value) throws RemoteException, ControllerException;
	
	/**
	 * Returns the input from the input data table with the given row, and from the column with the given header.
	 * @param columnName The header of the column which should be returned.
	 * @param rowNumber The number of the row which should be returned.
	 * @return The input value as a string.
	 * @throws RemoteException
	 * @throws ControllerException
	 */
	public String getInputAsString(int rowNumber, String columnName) throws RemoteException, ControllerException;
	
	/**
	 * Returns the input from the input data table with the given row, and from the column with the given header.
	 * @param columnName The header of the column which should be returned.
	 * @param rowNumber The number of the row which should be returned.
	 * @return The input value.
	 * @throws RemoteException
	 * @throws ControllerException
	 */
	public Serializable getInput(int rowNumber, String columnName) throws RemoteException, ControllerException;
	
	/**
	 * Returns the input from the input data table with the given row, and from the column with the given header.
	 * @param columnName The header of the column which should be returned.
	 * @param rowNumber The number of the row which should be returned.
	 * @return The input value as a float.
	 * @throws RemoteException
	 * @throws ControllerException
	 * @throws NumberFormatException 
	 */
	public float getInputAsFloat(int rowNumber, String columnName) throws RemoteException, ControllerException, NumberFormatException;
	/**
	 * Returns the input from the input data table with the given row, and from the column with the given header.
	 * @param columnName The header of the column which should be returned.
	 * @param rowNumber The number of the row which should be returned.
	 * @return The input value as a long.
	 * @throws RemoteException
	 * @throws ControllerException
	 * @throws NumberFormatException 
	 */
	public long getInputAsLong(int rowNumber, String columnName) throws RemoteException, ControllerException, NumberFormatException;
	/**
	 * Returns the input from the input data table with the given row, and from the column with the given header.
	 * @param columnName The header of the column which should be returned.
	 * @param rowNumber The number of the row which should be returned.
	 * @return The input value as a boolean.
	 * @throws RemoteException
	 * @throws ControllerException
	 */
	public boolean getInputAsBoolean(int rowNumber, String columnName) throws RemoteException, ControllerException;

	/**
	 * Returns the input from the input data table with the given row, and the given column.
	 * @param columnNumber The number of the column which should be returned.
	 * @param rowNumber The number of the row which should be returned.
	 * @return The input value as a string.
	 * @throws RemoteException
	 * @throws ControllerException
	 */
	public String getInputAsString(int rowNumber, int columnNumber) throws RemoteException, ControllerException;

	/**
	 * Returns the input from the input data table with the given row, and the given column.
	 * @param columnNumber The number of the column which should be returned.
	 * @param rowNumber The number of the row which should be returned.
	 * @return The input value.
	 * @throws RemoteException
	 * @throws ControllerException
	 */
	public Serializable getInput(int rowNumber, int columnNumber) throws RemoteException, ControllerException;
	
	/**
	 * Returns the input from the input data table with the given row, and the given column.
	 * @param columnNumber The number of the column which should be returned.
	 * @param rowNumber The number of the row which should be returned.
	 * @return The input value as a boolean.
	 * @throws RemoteException
	 * @throws ControllerException
	 */
	public boolean getInputAsBoolean(int rowNumber, int columnNumber) throws RemoteException, ControllerException;
	/**
	 * Returns the input from the input data table with the given row, and the given column.
	 * @param columnNumber The number of the column which should be returned.
	 * @param rowNumber The number of the row which should be returned.
	 * @return The input value as a float.
	 * @throws RemoteException
	 * @throws ControllerException
	 * @throws NumberFormatException 
	 */
	public float getInputAsFloat(int rowNumber, int columnNumber) throws RemoteException, ControllerException, NumberFormatException;
	/**
	 * Returns the input from the input data table with the given row, and the given column.
	 * @param columnNumber The number of the column which should be returned.
	 * @param rowNumber The number of the row which should be returned.
	 * @return The input value as a long.
	 * @throws RemoteException
	 * @throws ControllerException
	 * @throws NumberFormatException 
	 */
	public long getInputAsLong(int rowNumber, int columnNumber) throws RemoteException, ControllerException, NumberFormatException;
	
	
	/**
	 * Returns the input from the input data table with the given row, and from the column with the given header.
	 * @param columnName The header of the column which should be returned.
	 * @param rowNumber The number of the row which should be returned.
	 * @return The input value as a double.
	 * @throws RemoteException
	 * @throws ControllerException
	 * @throws NumberFormatException 
	 */
	public double getInputAsDouble(int rowNumber, String columnName) throws RemoteException, ControllerException, NumberFormatException;
	/**
	 * Returns the input from the input data table with the given row, and the given column.
	 * @param columnNumber The number of the column which should be returned.
	 * @param rowNumber The number of the row which should be returned.
	 * @return The input value as a double.
	 * @throws RemoteException
	 * @throws ControllerException
	 * @throws NumberFormatException 
	 */
	public double getInputAsDouble(int rowNumber, int columnNumber) throws RemoteException, ControllerException, NumberFormatException;
	
	/**
	 * Returns the input from the input data table with the given row, and from the column with the given header.
	 * @param columnName The header of the column which should be returned.
	 * @param rowNumber The number of the row which should be returned.
	 * @return The input value as an integer.
	 * @throws RemoteException
	 * @throws ControllerException
	 * @throws NumberFormatException 
	 */
	public int getInputAsInteger(int rowNumber, String columnName) throws RemoteException, ControllerException, NumberFormatException;
	/**
	 * Returns the input from the input data table with the given row, and the given column.
	 * @param columnNumber The number of the column which should be returned.
	 * @param rowNumber The number of the row which should be returned.
	 * @return The input value as an integer.
	 * @throws RemoteException
	 * @throws ControllerException
	 * @throws NumberFormatException 
	 */
	public int getInputAsInteger(int rowNumber, int columnNumber) throws RemoteException, ControllerException, NumberFormatException;
	
	/**
	 * Returns the number of rows in the input data table.
	 * @return Number of rows.
	 * @throws RemoteException
	 */
	public int getNumInputRows() throws RemoteException;
	
	/**
	 * Returns the number of columns in the input data table.
	 * @return Number of columns.
	 * @throws RemoteException
	 */
	public int getNumInputColumns() throws RemoteException;
	
	/**
	 * Returns the number of columns in the output data table.
	 * @return Number of columns.
	 * @throws RemoteException
	 */
	public int getNumOutputColumns() throws RemoteException;
	
	/**
	 * Returns the value of the state with the given name. If the state was not set before, the default value (i.e. the initial value) is returned.
	 * @param state Name of the state.
	 * @param defaultValue Default value of the state. Typically used to set an initial value for the state when the controller is executed the first time.
	 * @return State value as a string.
	 * @throws RemoteException
	 */
	public String getStateAsString(String state, String defaultValue) throws RemoteException;
	
	/**
	 * Returns the value of the state with the given name. If the state was not set before, the default value (i.e. the initial value) is returned.
	 * @param state Name of the state.
	 * @param defaultValue Default value of the state. Typically used to set an initial value for the state when the controller is executed the first time.
	 * @return State value as a double.
	 * @throws RemoteException
	 * @throws NumberFormatException 
	 */
	public double getStateAsDouble(String state, double defaultValue) throws RemoteException, NumberFormatException;
	
	/**
	 * Returns the value of the state with the given name. If the state was not set before, the default value (i.e. the initial value) is returned.
	 * @param state Name of the state.
	 * @param defaultValue Default value of the state. Typically used to set an initial value for the state when the controller is executed the first time.
	 * @return State value as an integer.
	 * @throws RemoteException
	 * @throws NumberFormatException 
	 */
	public int getStateAsInteger(String state, int defaultValue) throws RemoteException, NumberFormatException;
	
	/**
	 * Sets the state with the given name to the given value. A state which is set in one evaluation can be read out in the next.
	 * @param state Name of the state.
	 * @param value Value to set the state to
	 * @throws RemoteException
	 */
	public void setState(String state, String value) throws RemoteException;
	
	/**
	 * Sets the state with the given name to the given value. A state which is set in one evaluation can be read out in the next.
	 * @param state Name of the state.
	 * @param value Value to set the state to
	 * @throws RemoteException
	 */
	public void setState(String state, int value) throws RemoteException;
	
	/**
	 * Sets the state with the given name to the given value. A state which is set in one evaluation can be read out in the next.
	 * @param state Name of the state.
	 * @param value Value to set the state to
	 * @throws RemoteException
	 */
	public void setState(String state, double value) throws RemoteException;
}
