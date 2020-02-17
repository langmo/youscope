package org.youscope.server;

import java.rmi.RemoteException;


import org.youscope.addon.microscopeaccess.AvailableDeviceDriverInternal;
import org.youscope.addon.microscopeaccess.HubDeviceInternal;
import org.youscope.common.microscope.AvailableDeviceDriver;
import org.youscope.common.microscope.HubDevice;
import org.youscope.common.microscope.MicroscopeDriverException;

class HubDeviceRMI extends DeviceRMI implements HubDevice 
{
	/**
     * Serial Version UID.
     */
    private static final long serialVersionUID = 9144637057274529300L;
    private final HubDeviceInternal hubDevice;
    HubDeviceRMI(HubDeviceInternal hubDevice, MicroscopeRMI microscope, int accessID)
            throws RemoteException
    {
        super(hubDevice, microscope, accessID);
        this.hubDevice = hubDevice;
    }
	@Override
	public AvailableDeviceDriver[] getPeripheralDeviceDrivers() throws MicroscopeDriverException, RemoteException 
	{
		AvailableDeviceDriverInternal[] rawDrivers = hubDevice.getPeripheralDeviceDrivers();
		AvailableDeviceDriver[] drivers = new AvailableDeviceDriver[rawDrivers.length];
		for(int i=0; i<rawDrivers.length; i++)
		{
			drivers[i] = new AvailableDeviceDriverRMI(rawDrivers[i], microscope, accessID);
		}
		return drivers;
	}
	@Override
	public AvailableDeviceDriver getPeripheralDeviceDriver(String driverID)
			throws MicroscopeDriverException, RemoteException 
	{
		return new AvailableDeviceDriverRMI(hubDevice.getPeripheralDeviceDriver(driverID), microscope, accessID);
	}
}
