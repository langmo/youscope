package org.youscope.addon.microscopeaccess;

import org.youscope.common.microscope.MicroscopeDriverException;

public interface HubDeviceInternal  extends DeviceInternal
{
	/**
	 * Returns all peripheral devices associated to this hub device.
	 * @return peripheral devices.
	 * @throws MicroscopeDriverException
	 */
	AvailableDeviceDriverInternal[] getPeripheralDevices() throws MicroscopeDriverException;
}
