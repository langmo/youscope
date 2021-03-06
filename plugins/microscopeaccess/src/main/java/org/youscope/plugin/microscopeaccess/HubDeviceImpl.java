package org.youscope.plugin.microscopeaccess;

import java.util.ArrayList;

import org.youscope.addon.microscopeaccess.AvailableDeviceDriverInternal;
import org.youscope.addon.microscopeaccess.HubDeviceInternal;
import org.youscope.common.microscope.DeviceType;
import org.youscope.common.microscope.MicroscopeDriverException;

import mmcorej.CMMCore;
import mmcorej.StrVector;

class HubDeviceImpl extends DeviceImpl implements HubDeviceInternal 
{
	HubDeviceImpl(MicroscopeImpl microscope, String deviceID, String libraryID, String driverID, HubDeviceImpl hub)
	{
		super(microscope, deviceID, libraryID, driverID, DeviceType.HubDevice, hub);
	}
	@Override
	public AvailableDeviceDriverInternal[] getPeripheralDeviceDrivers() throws MicroscopeDriverException 
	{
		StrVector peripheralsRaw;
		try
		{
			CMMCore core = microscope.startRead();
			peripheralsRaw = core.getInstalledDevices(getDeviceID());

		}
		catch(Exception e)
		{
			throw new MicroscopeDriverException("Could not get peripheral devices of hub \"" + getDeviceID() + "\": " + e.getMessage());
		}
		finally
		{
			microscope.unlockRead();
		}
		ArrayList<AvailableDeviceDriverInternal> peripherals = new ArrayList<AvailableDeviceDriverInternal>((int) peripheralsRaw.size());
		for(String peripheralDriverID : peripheralsRaw)
		{			
			AvailableDeviceDriverImpl impl = microscope.getDeviceLoader().getDeviceDriver(getLibraryID(), peripheralDriverID);
			if(impl == null)
			{
				impl = new AvailableDeviceDriverImpl(microscope, getLibraryID(), peripheralDriverID, "", DeviceType.AnyType);
			}
			impl.setHub(this);
			peripherals.add(impl);
		}
		return peripherals.toArray(new AvailableDeviceDriverInternal[peripherals.size()]);
		
	}
	@Override
	public AvailableDeviceDriverInternal getPeripheralDeviceDriver(String driverID)
			throws MicroscopeDriverException 
	{
		for(AvailableDeviceDriverInternal driver : getPeripheralDeviceDrivers())
		{
			if(driver.getDriverID().equals(driverID))
				return driver;
		}
		return null;
	}

}
