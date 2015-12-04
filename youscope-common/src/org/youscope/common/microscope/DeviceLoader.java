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
public interface DeviceLoader extends Remote
{
	/**
	 * Returns a list of all available device drivers.
	 * @return List of all available device drivers.
	 * @throws RemoteException
	 * @throws MicroscopeDriverException
	 */
	public AvailableDeviceDriver[] getAvailableDeviceDrivers() throws RemoteException, MicroscopeDriverException;

	/**
	 * Adds a new device with the given name. To get information about the library, the identifier and the pre-initialization settings
	 * necessary to add the driver, see getAvailableDeviceDrivers().
	 * @param name Name the device should have.
	 * @param library Library The library where the device driver is specified.
	 * @param identifier Identifier of the device driver.
	 * @param preInitSettings Device properties necessary to be set prior to the initialization of the device. Can be null if no settings are necessary.
	 * @throws RemoteException
	 * @throws MicroscopeDriverException
	 * @throws MicroscopeLockedException
	 */
	// public void addDevice(String name, String library, String identifier, DeviceSettingDTO[] preInitSettings) throws RemoteException, MicroscopeDriverException, MicroscopeLockedException;

	/**
	 * Removes a previously added device.
	 * @param name Name of the device.
	 * @throws RemoteException
	 * @throws MicroscopeDriverException
	 * @throws MicroscopeLockedException
	 */
	public void removeDevice(String name) throws RemoteException, MicroscopeDriverException, MicroscopeLockedException;
}
