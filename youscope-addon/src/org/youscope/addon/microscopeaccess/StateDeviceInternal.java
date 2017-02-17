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
package org.youscope.addon.microscopeaccess;


import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;

/**
 * @author langmo
 *
 */
public interface StateDeviceInternal extends DeviceInternal
{
	/**
	 * Returns the number of states.
	 * @return Number of states.
	 */
	int getNumStates();

	/**
	 * Returns the current state.
	 * @return Current state.
	 * @throws MicroscopeException
	 */
	int getState() throws MicroscopeException;

	/**
	 * Returns a list of all labels of the states of this device. The list has the same length as getNumStates().
	 * @return List of state labels.
	 */
	String[] getStateLabels();

	/**
	 * Returns the current state label.
	 * @return Current state label.
	 * @throws MicroscopeException
	 */
	String getStateLabel() throws MicroscopeException;
	
	/**
	 * Returns the label of the given state.
	 * @param state State for which the label should be queried.
	 * @return Label of state.
	 * @throws ArrayIndexOutOfBoundsException
	 */
	String getStateLabel(int state) throws ArrayIndexOutOfBoundsException;

	/**
	 * Sets the label for the given state.
	 * @param state The state for which the label should be set
	 * @param label The label for the state.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws ArrayIndexOutOfBoundsException 
	 * @throws MicroscopeException
	 * @throws MicroscopeLockedException
	 */
	void setStateLabel(int state, String label, int accessID) throws ArrayIndexOutOfBoundsException, MicroscopeException, MicroscopeLockedException;

	/**
	 * Sets all state labels.
	 * The number of labels must be exactly equal to getNumStates().
	 * @param labels New labels for the states.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeException
	 * @throws ArrayIndexOutOfBoundsException Thrown if length of labels is wrong, in one or the other direction.
	 * @throws MicroscopeLockedException
	 */
	void setStateLabels(String[] labels, int accessID) throws MicroscopeException, ArrayIndexOutOfBoundsException, MicroscopeLockedException;

	/**
	 * Sets the current state.
	 * @param state State which should be set.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeException
	 * @throws ArrayIndexOutOfBoundsException 
	 * @throws MicroscopeLockedException
	 */
	void setState(int state, int accessID) throws MicroscopeException, ArrayIndexOutOfBoundsException, MicroscopeLockedException;

	/**
	 * Sets the current state.
	 * @param label Label of the state which should be set.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeException
	 * @throws DeviceException Thrown if label is unknown.
	 * @throws MicroscopeLockedException
	 */
	void setState(String label, int accessID) throws MicroscopeException, DeviceException, MicroscopeLockedException;
}
