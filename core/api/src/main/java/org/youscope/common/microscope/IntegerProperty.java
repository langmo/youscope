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
 * @author Moritz Lang
 * 
 */
public interface IntegerProperty extends Property
{
	/**
	 * Returns the current value of the device property as an integer.
	 * @return Value of property.
	 * @throws MicroscopeException
	 * @throws NumberFormatException Thrown when value is not an integer.
	 * @throws InterruptedException
	 * @throws RemoteException
	 */
	public int getIntegerValue() throws MicroscopeException, NumberFormatException, InterruptedException, RemoteException;

	/**
	 * Sets the current value of the property.
	 * @param value Value to set the property to.
	 * @throws MicroscopeException
	 * @throws MicroscopeLockedException
	 * @throws InterruptedException
	 * @throws RemoteException
	 */
	public void setValue(int value) throws MicroscopeException, MicroscopeLockedException, InterruptedException, RemoteException;

	/**
	 * Sets the value of the microscope relative to its current value. Throws a NumberFormatException if the current value does not correspond to an integer value.
	 * @param offset Amount for which the current value should be changed.
	 * @throws MicroscopeException
	 * @throws MicroscopeLockedException
	 * @throws NumberFormatException Thrown when current value is not an integer.
	 * @throws InterruptedException
	 * @throws RemoteException
	 */
	public void setValueRelative(int offset) throws MicroscopeException, MicroscopeLockedException, NumberFormatException, InterruptedException, RemoteException;

	/**
	 * Returns the lower limit of this value.
	 * Returns Integer.MIN_VALUE if this property does not have limits.
	 * @return Minimal value this property can have.
	 * @throws RemoteException
	 */
	int getLowerLimit() throws RemoteException;

	/**
	 * Returns the upper limit of this value.
	 * Returns Integer.MAX_VALUE if this property does not have limits.
	 * @return Maximal value this property can have.
	 * @throws RemoteException
	 */
	int getUpperLimit() throws RemoteException;
}
