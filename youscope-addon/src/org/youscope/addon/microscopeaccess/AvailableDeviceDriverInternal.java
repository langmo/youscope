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



import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.microscope.DeviceType;
import org.youscope.common.microscope.MicroscopeDriverException;
import org.youscope.common.microscope.MicroscopeLockedException;

/**
 * @author langmo
 *
 */
public interface AvailableDeviceDriverInternal
{
	/**
	 * Returns the identifier of the device driver.
	 * @return Identifier of device.
	 * @throws MicroscopeDriverException 
	 */
	public String getDriverID() throws MicroscopeDriverException;

	/**
	 * Returns the type of this device driver.
	 * @return Type of the device.
	 * @throws MicroscopeDriverException 
	 */
	public DeviceType getType() throws MicroscopeDriverException;
	
	/**
	 * Returns the description of the device driver.
	 * @return Description of the driver.
	 * @throws MicroscopeDriverException 
	 */
	public String getDescription() throws MicroscopeDriverException;
	
	/**
	 * Returns the name of the library where this device is implemented.
	 * @return The library name of this device.
	 * @throws MicroscopeDriverException 
	 */
	public String getLibraryID() throws MicroscopeDriverException;
	
	/**
	 * Loads the specified device driver, but yet does not initialize it.
	 * Returns a (possibly empty) list of properties for this device driver which have to be set when initializing the driver.
	 * Should be followed by a call to initializeDevice or unloadDevice.
	 * @param deviceID The ID under which the device should be initialized.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @return List of properties which have to be pre-initialized.
	 * @throws MicroscopeDriverException 
	 * @throws MicroscopeLockedException 
	 */
	public PreInitDevicePropertyInternal[] loadDevice(String deviceID, int accessID) throws MicroscopeDriverException, MicroscopeLockedException;
	
	/**
	 * Initializes a previously loaded device. The device can then be used.
	 * The device settings should correspond (in the same order) to the PreInitDevicePropertyInternal device settings returned by the previous call to
	 * loadDevice. Throws an error if called before loadDevice was called.
	 * Load device has to be called each time before calling this function.
	 * @param preInitSettings Device properties necessary to be set prior to the initialization of the device. Can be null or an empty array if no settings are necessary.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeDriverException
	 * @throws MicroscopeLockedException 
	 */
	public void initializeDevice(DeviceSetting[] preInitSettings, int accessID) throws MicroscopeDriverException, MicroscopeLockedException;
	
	/**
	 * Unloads a previously loaded, but yet not initialized device. Should be called to clean up if a device was loaded, but it was decided to not initialize it.
	 * Does nothing if currently no device is loaded.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeLockedException
	 * @throws MicroscopeDriverException 
	 */
	public void unloadDevice(int accessID) throws MicroscopeLockedException, MicroscopeDriverException;
	
	/**
	 * Returns true if this driver communicates over a serial port.
	 * Should only be called when the respective device was already loaded. If device is not loaded, throws a driver exception.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @return True if driver uses a serial port, false otherwise.
	 * @throws MicroscopeLockedException
	 * @throws MicroscopeDriverException
	 */
	public boolean isSerialPortDriver(int accessID) throws MicroscopeLockedException, MicroscopeDriverException;
}
