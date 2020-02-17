package org.youscope.addon.microscopeaccess;

import org.youscope.common.microscope.MicroscopeDriverException;

public interface HubDeviceInternal  extends DeviceInternal
{
	/**
	 * Returns all peripheral device drivers associated to this hub device.
	 * @return peripheral devices.
	 * @throws MicroscopeDriverException
	 */
	AvailableDeviceDriverInternal[] getPeripheralDeviceDrivers() throws MicroscopeDriverException;
	
	/**
	 * Returns the peripheral device driver with the given library and driver id. If this hub
	 * doesn't have a corresponding peripheral device driver, null is returned.
	 * @param driverID The ID of the peripheral driver. The library is assumed to be the same as the one of this hub.
	 * @return The peripheral driver.
	 * @throws MicroscopeDriverException
	 */
	AvailableDeviceDriverInternal getPeripheralDeviceDriver(String driverID) throws MicroscopeDriverException;
}
