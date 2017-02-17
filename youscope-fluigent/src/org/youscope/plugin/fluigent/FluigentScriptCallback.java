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
package org.youscope.plugin.fluigent;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Callback given to the Fluigent control script such that it can obtain its state, and set the flow rates and similar.
 * @author Moritz Lang
 *
 */
public interface FluigentScriptCallback extends Remote
{
	
	/**
	 * Returns the number of flow units attached to the Fluigent device. 
	 * @return Number of flow units.
	 * @throws RemoteException
	 * @throws ResourceException
	 * @throws InterruptedException 
	 */
	public int getNumberOfFlowUnits() throws RemoteException, ResourceException, InterruptedException;
	/**
	 * Sets the flow rate of a given flow unit.
	 * @param flowUnit The ID (zero based) of the flow unit.
	 * @param flowRate The flow rate, in the unit given by getFlowUnitShort(). 
	 * @throws RemoteException
	 * @throws ResourceException
	 * @throws InterruptedException 
	 */
	public void setFlowRate(int flowUnit, double flowRate) throws RemoteException, ResourceException, InterruptedException;
	/**
	 * Returns the current flow rate of a given flow unit.
	 * @param flowUnit The ID (zero based) of the flow unit.
	 * @return The flow rate, in the unit given by getFlowUnitShort(). 
	 * @throws RemoteException
	 * @throws ResourceException
	 * @throws InterruptedException 
	 */
	public double getFlowRate(int flowUnit) throws RemoteException, ResourceException, InterruptedException;

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
