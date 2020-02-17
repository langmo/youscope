package org.youscope.common.microscope;

import java.rmi.RemoteException;



/**
 * A pseudo-device with the main target to host other, peripheral devices
 * @author Moritz Lang
 *
 */
public interface HubDevice extends Device 
{
	/**
	 * Returns all peripheral device drivers associated to this hub device.
	 * @return peripheral device drivers.
	 * @throws MicroscopeDriverException
	 * @throws RemoteException
	 */
	AvailableDeviceDriver[] getPeripheralDeviceDrivers() throws MicroscopeDriverException, RemoteException;

	/**
	 * Retruns the peripheral device driver with the given library and driver id. If this hub
	 * doesn't have a corresponding peripheral device driver, null is returned.
	 * @param driverID The ID of the peripheral driver. The library ID is assumed to be the same as the one of this hub.
	 * @return The peripheral driver.
	 * @throws MicroscopeDriverException
	 * @throws RemoteException 
	 */
	AvailableDeviceDriver getPeripheralDeviceDriver(String driverID) throws MicroscopeDriverException, RemoteException;
}
