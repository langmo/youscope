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

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Represents a property of a device.
 * @author langmo
 */
public interface Property extends Remote
{
	/**
	 * Returns the name of the device where this property belongs to.
	 * @return Name of device.
	 * @throws RemoteException
	 */
	public String getDeviceID() throws RemoteException;

	/**
	 * Returns the name of the device property.
	 * @return Name of property.
	 * @throws RemoteException
	 */
	public String getPropertyID() throws RemoteException;

	/**
	 * Returns the type of this device property.
	 * @return Type of property.
	 * @throws RemoteException
	 */
	public PropertyType getType() throws RemoteException;

	/**
	 * Returns the current value of the device property as a string.
	 * @return Value of property.
	 * @throws RemoteException
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 */
	public String getValue() throws RemoteException, MicroscopeException, InterruptedException;

	/**
	 * Sets the current value of the property. Throws a device exception if the value is not valid (i.e. if the property is an integer property and the value is not an integer),
	 * or if the property is not editable.
	 * @param value Value to set the property to.
	 * @throws RemoteException
	 * @throws MicroscopeException
	 * @throws MicroscopeLockedException
	 * @throws InterruptedException
	 * @throws DeviceException Thrown if value does not correspond to property type.
	 */
	public void setValue(String value) throws RemoteException, MicroscopeException, MicroscopeLockedException, InterruptedException, DeviceException;

	/**
	 * Returns true if property is editable and false if not.
	 * @return True if editable, otherwise false.
	 * @throws RemoteException
	 */
	public boolean isEditable() throws RemoteException;
}
