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
package org.youscope.common.microscope;

import java.rmi.RemoteException;

/**
 * @author langmo
 * 
 */
public interface StateDevice extends Device
{
	/**
	 * Returns the number of states.
	 * @return Number of states.
	 * @throws RemoteException
	 */
	int getNumStates() throws RemoteException;

	/**
	 * Returns the current state.
	 * @return Current state.
	 * @throws MicroscopeException
	 * @throws RemoteException
	 */
	int getState() throws MicroscopeException, RemoteException;

	/**
	 * Returns a list of all labels of the states of this device. The list has the same length as getNumStates().
	 * @return List of state labels.
	 * @throws RemoteException
	 */
	String[] getStateLabels() throws RemoteException;

	/**
	 * Returns the current state label.
	 * @return Current state label.
	 * @throws MicroscopeException
	 * @throws RemoteException
	 */
	String getStateLabel() throws MicroscopeException, RemoteException;

	/**
	 * Returns the label of the given state.
	 * @param state State for which the label should be queried.
	 * @return Label of state.
	 * @throws ArrayIndexOutOfBoundsException
	 * @throws RemoteException
	 */
	String getStateLabel(int state) throws ArrayIndexOutOfBoundsException, RemoteException;

	/**
	 * Sets the label for the given state.
	 * @param state The state for which the label should be set
	 * @param label The label for the state.
	 * @throws MicroscopeException
	 * @throws ArrayIndexOutOfBoundsException
	 * @throws RemoteException
	 * @throws MicroscopeLockedException
	 */
	void setStateLabel(int state, String label) throws MicroscopeException, ArrayIndexOutOfBoundsException, RemoteException, MicroscopeLockedException;

	/**
	 * Sets all state labels.
	 * The number of labels must be exactly equal to getNumStates().
	 * @param labels New labels for the states.
	 * @throws MicroscopeException
	 * @throws RemoteException
	 * @throws ArrayIndexOutOfBoundsException Thrown if length of labels is wrong, in one or the other direction.
	 * @throws MicroscopeLockedException
	 */
	void setStateLabels(String[] labels) throws MicroscopeException, ArrayIndexOutOfBoundsException, RemoteException, MicroscopeLockedException;

	/**
	 * Sets the current state.
	 * @param state State which should be set.
	 * @throws MicroscopeException
	 * @throws ArrayIndexOutOfBoundsException
	 * @throws RemoteException
	 * @throws MicroscopeLockedException
	 */
	void setState(int state) throws MicroscopeException, ArrayIndexOutOfBoundsException, RemoteException, MicroscopeLockedException;

	/**
	 * Sets the current state.
	 * @param label Label of the state which should be set.
	 * @throws MicroscopeException
	 * @throws RemoteException
	 * @throws DeviceException Thrown if label is unknown.
	 * @throws MicroscopeLockedException
	 */
	void setState(String label) throws MicroscopeException, DeviceException, RemoteException, MicroscopeLockedException;
}
