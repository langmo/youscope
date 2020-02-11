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
	 * Returns all peripheral devices associated to this hub device.
	 * @return peripheral devices.
	 * @throws MicroscopeDriverException
	 * @throws RemoteException
	 */
	AvailableDeviceDriver[] getPeripheralDevices() throws MicroscopeDriverException, RemoteException;
}
