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
package org.youscope.plugin.nemesys;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Callback given to the Nemesys control script such that it can obtain its state, and set the flow rates and similar.
 * @author Moritz Lang 
 *
 */
public interface NemesysScriptCallback extends Remote
{
	
	/**
	 * Returns the number of dosing units/syringes attached to the Nemesys device. 
	 * @return Number of dosing units.
	 * @throws RemoteException
	 * @throws NemesysException
	 * @throws InterruptedException 
	 */
	public int getNumberOfDosingUnits() throws RemoteException, NemesysException, InterruptedException;
	/**
	 * Returns the maximal flow rate of a given dosing unit.
	 * @param dosingUnit The ID (zero based) of the dosing unit.
	 * @return absolute value of the maximal flow rate for the given dosing unit, in the unit given by getFlowUnitShort().
	 * @throws RemoteException
	 * @throws NemesysException
	 * @throws InterruptedException 
	 */
	public double getFlowRateMax(int dosingUnit) throws RemoteException, NemesysException, InterruptedException;
	/**
	 * Sets the flow rate of a given dosing unit.
	 * @param dosingUnit The ID (zero based) of the dosing unit.
	 * @param flowRate The flow rate, in the unit given by getFlowUnitShort(). 
	 * @throws RemoteException
	 * @throws NemesysException
	 * @throws InterruptedException 
	 */
	public void setFlowRate(int dosingUnit, double flowRate) throws RemoteException, NemesysException, InterruptedException;
	/**
	 * Returns the current flow rate of a given dosing unit.
	 * @param dosingUnit The ID (zero based) of the dosing unit.
	 * @return The flow rate, in the unit given by getFlowUnitShort(). 
	 * @throws RemoteException
	 * @throws NemesysException
	 * @throws InterruptedException 
	 */
	public double getFlowRate(int dosingUnit) throws RemoteException, NemesysException, InterruptedException;

	/**
	 * Returns the unit identifier of the flow rate.
	 * @param dosingUnit the ID (zero based) of the dosing unit.
	 * @return string describing the unit of the flow rate.
	 * @throws RemoteException
	 * @throws NemesysException
	 * @throws InterruptedException 
	 */
	public String getFlowUnit(int dosingUnit) throws RemoteException, NemesysException, InterruptedException;
	
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
