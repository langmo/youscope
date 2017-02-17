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
package org.youscope.server;

import java.util.Vector;

import org.youscope.addon.microscopeaccess.AvailableDeviceDriverInternal;
import org.youscope.addon.microscopeaccess.MicroscopeInternal;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.microscope.MicroscopeDriverException;
import org.youscope.common.microscope.MicroscopeLockedException;

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
	private final Vector<DeviceSetting>	settings				= new Vector<DeviceSetting>();
	private final static String				LIBRARY_SERIAL_MANAGER	= "SerialManager";

	ConfigFileParserDevice(String deviceID, String libraryID, String driverID, MicroscopeInternal microscope)
	{
		this.microscope = microscope;
		this.deviceID = deviceID;
		this.libraryID = libraryID;
		this.driverID = driverID;
	}

	void addPreInitDeviceSetting(DeviceSetting setting)
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
		for(DeviceSetting setting : settings)
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
		driver.initializeDevice(settings.toArray(new DeviceSetting[0]), accessID);
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
