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
 * @author langmo
 * 
 */
public interface AvailableDeviceDriver extends Remote
{
	/**
	 * Returns the identifier of the device driver.
	 * @return Identifier of device.
	 * @throws MicroscopeDriverException
	 * @throws RemoteException
	 */
	public String getDriverID() throws MicroscopeDriverException, RemoteException;

	/**
	 * Returns the type of this device driver.
	 * @return Type of the device.
	 * @throws MicroscopeDriverException
	 * @throws RemoteException
	 */
	public DeviceType getType() throws MicroscopeDriverException, RemoteException;

	/**
	 * Returns the description of the device driver.
	 * @return Description of the driver.
	 * @throws MicroscopeDriverException
	 * @throws RemoteException
	 */
	public String getDescription() throws MicroscopeDriverException, RemoteException;

	/**
	 * Returns the name of the library where this device is implemented.
	 * @return The library name of this device.
	 * @throws MicroscopeDriverException
	 * @throws RemoteException
	 */
	public String getLibraryID() throws MicroscopeDriverException, RemoteException;

	/**
	 * Returns a list of properties for this device driver which have to be set before initialization of the driver.
	 * @return List of properties which have to be pre-initialized.
	 * @throws MicroscopeDriverException
	 * @throws MicroscopeLockedException
	 * @throws RemoteException
	 */
	// public PreInitDeviceProperty[] getPreInitDeviceProperties() throws MicroscopeDriverException, MicroscopeLockedException, RemoteException;

	/**
	 * Loads the specified device driver, but yet does not initialize it.
	 * Returns a (possibly empty) list of properties for this device driver which have to be set when initializing the driver.
	 * Should be followed by a call to initializeDevice or unloadDevice.
	 * @param deviceID The ID under which the device should be initialized.
	 * @return List of properties which have to be pre-initialized.
	 * @throws MicroscopeDriverException
	 * @throws MicroscopeLockedException
	 * @throws RemoteException
	 */
	public PreInitDeviceProperty[] loadDevice(String deviceID) throws MicroscopeDriverException, MicroscopeLockedException, RemoteException;

	/**
	 * Initializes a previously loaded device. The device can then be used.
	 * The device settings should correspond (in the same order) to the PreInitDevicePropertyInternal device settings returned by the previous call to
	 * loadDevice. Throws an error if called before loadDevice was called.
	 * Load device has to be called each time before calling this function.
	 * @param preInitSettings Device properties necessary to be set prior to the initialization of the device. Can be null or an empty array if no settings are necessary.
	 * @throws MicroscopeDriverException
	 * @throws MicroscopeLockedException
	 * @throws RemoteException
	 */
	public void initializeDevice(DeviceSetting[] preInitSettings) throws MicroscopeDriverException, MicroscopeLockedException, RemoteException;

	/**
	 * Unloads a previously loaded, but yet not initialized device. Should be called to clean up if a device was loaded, but it was decided to not initialize it.
	 * Does nothing if currently no device is loaded.
	 * @throws MicroscopeLockedException
	 * @throws MicroscopeDriverException
	 * @throws RemoteException
	 */
	public void unloadDevice() throws MicroscopeLockedException, MicroscopeDriverException, RemoteException;

	/**
	 * Returns true if this driver communicates over a serial port.
	 * @return True if driver uses a serial port, false otherwise.
	 * @throws MicroscopeLockedException
	 * @throws MicroscopeDriverException
	 * @throws RemoteException
	 */
	public boolean isSerialPortDriver() throws MicroscopeLockedException, MicroscopeDriverException, RemoteException;
}
