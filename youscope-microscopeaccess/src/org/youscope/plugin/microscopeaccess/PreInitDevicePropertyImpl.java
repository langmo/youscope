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
package org.youscope.plugin.microscopeaccess;

import java.util.Arrays;

import org.youscope.addon.microscopeaccess.DeviceInternal;
import org.youscope.addon.microscopeaccess.PreInitDevicePropertyInternal;
import org.youscope.common.microscope.DeviceType;
import org.youscope.common.microscope.MicroscopeDriverException;
import org.youscope.common.microscope.PropertyType;

/**
 * @author langmo
 *
 */
class PreInitDevicePropertyImpl implements PreInitDevicePropertyInternal
{
	private final String propertyID;
	private final PropertyType type;
	private final String[] allowedValues;
	private final String  defaultValue;
	private final MicroscopeImpl microscope;
	PreInitDevicePropertyImpl(MicroscopeImpl microscope, String name, PropertyType type, String[] allowedValues, String defaultValue)
	{
		this.microscope = microscope;
		this.propertyID = name;
		this.type = type;
		this.allowedValues = allowedValues;
		this.defaultValue = defaultValue;
	}
	@Override
	public String getPropertyID()
	{
		return propertyID;
	}
	
	@Override
	public String getDefaultValue()
	{
		return defaultValue;
	}

	@Override
	public PropertyType getType()
	{
		return type;
	}
	
	
	private String[] getSerialPortNames() throws MicroscopeDriverException
	{
		DeviceInternal[] devices = microscope.getDevices(DeviceType.SerialDevice);
		String[] deviceNames = new String[devices.length];
		for(int i = 0; i < deviceNames.length; i++)
		{
			deviceNames[i] = devices[i].getDeviceID();
		}
		return deviceNames;
	}
	@Override
	public String[] getAllowedPropertyValues() throws MicroscopeDriverException
	{
		if(getPropertyID().equals("Port"))
		{
			// microManager does not automatically set the available ports property.
			return getSerialPortNames();
		}
		else if(allowedValues == null)
			return null;
		else
			return Arrays.copyOf(allowedValues, allowedValues.length);
	}
}
