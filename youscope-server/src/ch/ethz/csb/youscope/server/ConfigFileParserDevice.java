/**
 * 
 */
package ch.ethz.csb.youscope.server;

import java.util.Vector;

import ch.ethz.csb.youscope.server.microscopeaccess.AvailableDeviceDriverInternal;
import ch.ethz.csb.youscope.server.microscopeaccess.MicroscopeInternal;
import ch.ethz.csb.youscope.shared.microscope.DeviceException;
import ch.ethz.csb.youscope.shared.microscope.DeviceSettingDTO;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeDriverException;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeLockedException;

/**
 * Represents a device defined in the config file prior to its initialization.
 * @author Moritz Lang
 * 
 */
class ConfigFileParserDevice extends ConfigFileManipulator implements Comparable<ConfigFileParserDevice>
{
	private final String					deviceID;
	private final String					libraryID;
	private final String					driverID;
	private final MicroscopeInternal		microscope;
	private final Vector<DeviceSettingDTO>	settings				= new Vector<DeviceSettingDTO>();
	private final static String				LIBRARY_SERIAL_MANAGER	= "SerialManager";

	ConfigFileParserDevice(String deviceID, String libraryID, String driverID, MicroscopeInternal microscope)
	{
		this.microscope = microscope;
		this.deviceID = deviceID;
		this.libraryID = libraryID;
		this.driverID = driverID;
	}

	void addPreInitDeviceSetting(DeviceSettingDTO setting)
	{
		settings.addElement(setting);
	}

	String getDeviceID()
	{
		return deviceID;
	}

	String getLibraryID()
	{
		return libraryID;
	}

	String getDriverID()
	{
		return driverID;
	}

	void initializeDevice(int accessID) throws MicroscopeLockedException, MicroscopeDriverException, DeviceException
	{
		// check settings
		for(DeviceSettingDTO setting : settings)
		{
			if(setting.isAbsoluteValue() == false)
				throw new MicroscopeDriverException("Relative values are not allowed for properties when initializing a device.");
		}

		// Load driver
		AvailableDeviceDriverInternal driver = microscope.getDeviceLoader().getAvailableDeviceDriver(libraryID, driverID);
		if(driver == null)
			throw new MicroscopeDriverException("Could not find driver with ID " + driverID + " in library " + libraryID + ".");

		// initialize device
		driver.loadDevice(deviceID, accessID);
		driver.initializeDevice(settings.toArray(new DeviceSettingDTO[0]), accessID);
	}

	@Override
	public int compareTo(ConfigFileParserDevice otherDevice)
	{
		// Used to sort elements such that the serial ports are always created first.
		if(LIBRARY_SERIAL_MANAGER.equals(libraryID))
		{
			if(LIBRARY_SERIAL_MANAGER.equals(otherDevice.getLibraryID()))
				return 0;
			return -1;
		}
		if(LIBRARY_SERIAL_MANAGER.equals(otherDevice.getLibraryID()))
			return 1;
		return 0;
	}
}
