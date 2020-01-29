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

import org.youscope.addon.microscopeaccess.DeviceInternal;
import org.youscope.addon.microscopeaccess.MicroscopeInternal;
import org.youscope.addon.microscopeaccess.PropertyInternal;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.microscope.PropertyType;
import org.youscope.common.microscope.SettingException;

/**
 * Helper class to validate if given device settings used later for microscope queries or manipulations are valid.
 * @author Moritz Lang
 * 
 */
class SettingsValidator
{
	/**
	 * Checks if the provided setting is valid. If not, a setting exception is thrown.
	 * @param setting Setting to test.
	 * @param onlyAbsolute True if an exception should be thrown if the setting corresponds to relative values.
	 * @param microscope The microscope for which the setting should be checked on.
	 * @param accessID The access ID for the microscope used for checking.
	 * @throws SettingException
	 */
	public static void isSettingValid(DeviceSetting setting, boolean onlyAbsolute, MicroscopeInternal microscope, int accessID) throws SettingException
	{
		if(setting.getDevice() == null || setting.getDevice().length() <= 0)
			throw new SettingException("Device name of device setting is null or empty.");
		if(setting.getProperty() == null || setting.getProperty().length() <= 0)
			throw new SettingException("Device property name of device setting for device " + setting.getDevice() + " is null or empty.");
		if(onlyAbsolute && !setting.isAbsoluteValue())
			throw new SettingException("Device setting for device " + setting.getDevice() + " is a relative setting, but only absolute ones are allowed.");
		if(setting.getDevice().equals("Core"))
			throw new SettingException("The MicroManager core is not a device in YouScope.");
		DeviceInternal device;
		try
		{
			device = microscope.getDevice(setting.getDevice());
		}
		catch(DeviceException e)
		{
			throw new SettingException("Device " + setting.getDevice() + " does not exist.", e);
		}

		PropertyInternal property;
		try
		{
			property = device.getProperty(setting.getProperty());
		}
		catch(DeviceException e)
		{
			throw new SettingException("Property " + setting.getProperty() + " of device " + setting.getDevice() + " does not exist.", e);
		}

		// Check if when setting is relative, it can be relative...
		PropertyType type = property.getType();
		if(setting.isAbsoluteValue() == false)
		{

			if(type != PropertyType.PROPERTY_FLOAT && type != PropertyType.PROPERTY_INTEGER)
			{
				throw new SettingException("Property " + setting.getProperty() + " of device " + setting.getDevice() + " cannot be set relatively, since it has neither integer nor double values.");
			}
		}

		// Check if correct type.
		if(type == PropertyType.PROPERTY_FLOAT)
		{
			try
			{
				setting.getFloatValue();
			}
			catch(NumberFormatException e)
			{
				throw new SettingException("Property " + setting.getProperty() + " of device " + setting.getDevice() + " is not a float value.", e);
			}
		}
		else if(type == PropertyType.PROPERTY_INTEGER)
		{
			try
			{
				setting.getIntegerValue();
			}
			catch(NumberFormatException e)
			{
				throw new SettingException("Property " + setting.getProperty() + " of device " + setting.getDevice() + " is not an integer value.", e);
			}
		}
	}

	/**
	 * Calls isSettingValid() for all settings. An exception is thrown if one of the settings is invalid.
	 * @param settings Settings to check if they are valid.
	 * @param onlyAbsolute True if an exception should be thrown if the setting corresponds to relative values.
	 * @param microscope The microscope for which the settings should be checked on.
	 * @param accessID The access ID for the microscope used for checking.
	 * @throws SettingException
	 */
	public static void areSettingsValid(DeviceSetting[] settings, boolean onlyAbsolute, MicroscopeInternal microscope, int accessID) throws SettingException
	{
		for(DeviceSetting setting : settings)
		{
			isSettingValid(setting, onlyAbsolute, microscope, accessID);
		}
	}
}
