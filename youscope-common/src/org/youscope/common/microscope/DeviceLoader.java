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
	 * Removes a previously added device.
	 * @param name Name of the device.
	 * @throws RemoteException
	 * @throws MicroscopeDriverException
	 * @throws MicroscopeLockedException
	 */
	public void removeDevice(String name) throws RemoteException, MicroscopeDriverException, MicroscopeLockedException;
}
