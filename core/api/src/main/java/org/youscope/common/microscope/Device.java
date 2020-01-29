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
package org.youscope.common.microscope;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface represents a physical microscope device and allows setting and getting values of it.
 * @author langmo
 */
public interface Device extends Remote
{
	/**
	 * Returns the name of the device.
	 * @return Name of device.
	 * @throws RemoteException
	 */
	public String getDeviceID() throws RemoteException;

	/**
	 * Returns the library name in which this device's driver is defined.
	 * @return Name of device driver library.
	 * @throws RemoteException
	 */
	public String getLibraryID() throws RemoteException;

	/**
	 * Returns the Identifier under which this device's driver is defined in the library.
	 * @return Device's driver identifier.
	 * @throws RemoteException
	 */
	public String getDriverID() throws RemoteException;

	/**
	 * Returns the type of this device.
	 * @return Type of the device.
	 * @throws RemoteException
	 */
	public DeviceType getType() throws RemoteException;

	/**
	 * Returns a list of all properties of this device.
	 * @return List of properties.
	 * @throws RemoteException
	 */
	public Property[] getProperties() throws RemoteException;

	/**
	 * Returns a list of all editable properties of this device.
	 * @return List of editable properties.
	 * @throws RemoteException
	 */
	public Property[] getEditableProperties() throws RemoteException;

	/**
	 * Returns the property of this device with the given name, or null if this device does not have a property with this name.
	 * @param propertyID Name of the property.
	 * @return the property of this device with the given name, or null.
	 * @throws DeviceException
	 * @throws RemoteException
	 */
	public Property getProperty(String propertyID) throws DeviceException, RemoteException;

	/**
	 * Waits for the device to have finished its latest actions before returning.
	 * Some devices give a notification if they have finished their actions, then it is waited for this notification.
	 * Devices which do not support this handshaking mechanism return immediately, even if they still complete an action, except an explicit delay greater 0 is set.
	 * 
	 * For all devices with explicit delays greater zero up to two mechanisms may become active:
	 * 1) Some devices support explicit delays intrinsically. Then the set explicit delay is submitted to them and they handle this explicit delay in a driver specific way.
	 * 2) For all devices, including devices supporting the handshaking mechanism and those supporting explicit delays on their own,
	 * YouScope guarantees that at least the given time is waited since the last transmission of a command intended to change the state of the device.
	 * A changing command is a command intended to change the state of the device (e.g. setPosition(), setState(), ...), but not command intended to only
	 * read out the state of the device (e.g. getPosition(), getDeviceID(), ...), although also the latter ones might trigger some device actions for badly programmed device drivers
	 * (but usually don't). Furthermore, this mechanism only works if a device action was triggered by YouScopeClient (e.g. changes by directly manipulating the hardware or by
	 * driver-driver communication are not included).
	 * @throws MicroscopeException
	 * @throws RemoteException
	 * @throws InterruptedException
	 */
	public void waitForDevice() throws MicroscopeException, RemoteException, InterruptedException;

	/**
	 * Returns the explicit device delay in ms.
	 * See waitForDevice() for more information.
	 * @return Device delay in ms.
	 * @throws RemoteException
	 */
	public double getExplicitDelay() throws RemoteException;

	/**
	 * Sets the explicit delay in ms.
	 * See waitForDevice() for more information.
	 * @param delay Delay iin ms.
	 * @throws MicroscopeException
	 * @throws RemoteException
	 * @throws MicroscopeLockedException
	 */
	public void setExplicitDelay(double delay) throws MicroscopeException, RemoteException, MicroscopeLockedException;
}
