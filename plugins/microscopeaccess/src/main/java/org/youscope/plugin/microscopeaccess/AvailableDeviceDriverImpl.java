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

import java.util.Vector;

import org.youscope.addon.microscopeaccess.AvailableDeviceDriverInternal;
import org.youscope.addon.microscopeaccess.PreInitDevicePropertyInternal;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.microscope.DeviceType;
import org.youscope.common.microscope.MicroscopeDriverException;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.PropertyType;

import mmcorej.CMMCore;
import mmcorej.StrVector;
 
/**
 * @author langmo
 *
 */
class AvailableDeviceDriverImpl implements AvailableDeviceDriverInternal
{
	private final String identifier;
	private final String description;
	private final String library;
	private final DeviceType deviceType;
	private final MicroscopeImpl microscope;
	private volatile String currentlyLoadedDeviceID = null;
	private volatile boolean serialPort = false;
	AvailableDeviceDriverImpl(MicroscopeImpl microscope, String library, String identifier, String description, DeviceType deviceType)
	{
		this.library = library;
		this.identifier = identifier;
		this.description = description;
		this.deviceType = deviceType;
		this.microscope = microscope;
	}
	
	@Override
	public DeviceType getType()
	{
		return deviceType;
	}

	@Override
	public String getDriverID()
	{
		return identifier;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public String getLibraryID()
	{
		return library;
	}


	@Override
	public boolean isSerialPortDriver(int accessID) throws MicroscopeLockedException, MicroscopeDriverException
	{
		if(currentlyLoadedDeviceID == null)
			throw new MicroscopeDriverException("Device driver must be loaded before properties of the device can be queried.");
		return serialPort;
	}

	@Override
	public PreInitDevicePropertyInternal[] loadDevice(String deviceID, int accessID) throws MicroscopeDriverException, MicroscopeLockedException
	{
		if(deviceID == null || deviceID.length() < 1)
			throw new MicroscopeDriverException("The intended ID of the device is null or empty.");
		if(currentlyLoadedDeviceID != null)
			throw new MicroscopeDriverException("Device already loaded. Initialize device or unload it.");
		try
		{
			// Get access to microManager
			CMMCore core = microscope.startWrite(accessID);
			
			// Construct a device
			core.loadDevice(deviceID, library, identifier);
			currentlyLoadedDeviceID = deviceID;
			PreInitDevicePropertyInternal[] properties = loadProperties(accessID);
			return properties;
			
		}
		catch(MicroscopeLockedException e)
		{
			throw new MicroscopeDriverException("Could not load driver for device since microscope is locked.", e);
		}
		catch(MicroscopeException e)
		{
			throw new MicroscopeDriverException("Could not load driver for device.", e);
		}
		catch(Exception e)
		{
			throw new MicroscopeDriverException("Could not load driver for device: " + e.getMessage());
		}
		finally
		{
			microscope.unlockWrite();
		}
	}

	@Override
	public synchronized void initializeDevice(DeviceSetting[] preInitSettings, int accessID) throws MicroscopeDriverException, MicroscopeLockedException
	{
		if(currentlyLoadedDeviceID == null)
			throw new MicroscopeDriverException("Device driver must be loaded before device is initialized.");
		if(preInitSettings == null)
			preInitSettings = new DeviceSetting[0];
		
		try
		{
			// Get access to microManager
			CMMCore core = microscope.startWrite(accessID);
			
			// Set pre-init settings
			for(DeviceSetting setting : preInitSettings)
			{
				if(setting.isAbsoluteValue() == false)
					throw new MicroscopeDriverException("Relative values are not allowed for properties when initializing a device.");
				if(!setting.getDevice().equals(currentlyLoadedDeviceID))
					throw new MicroscopeDriverException("Provided pre-initialization device property has a different deviceID then the loaded device.");
				core.setProperty(setting.getDevice(), setting.getProperty(), setting.getStringValue());
			}
			
			// Initialize device.
			core.initializeDevice(currentlyLoadedDeviceID);
			microscope.initializeDevice(currentlyLoadedDeviceID, library, identifier, accessID);
			currentlyLoadedDeviceID = null;
		}
		catch(MicroscopeException e)
		{
			try
			{
				unloadDevice(accessID);
			}
			catch(Exception e1)
			{
				throw new MicroscopeDriverException("Could not initialize driver. Automatic driver unloading failed. Configuration might got corrupted. Restarting YouScope is recommended.", e1);
			}
			throw new MicroscopeDriverException("Could not initilize driver. Check if device is correctly connected, or retry with different pre-initialization settings.", e);
		}
		catch(Exception e)
		{
			try
			{
				unloadDevice(accessID);
			}
			catch(Exception e1)
			{
				throw new MicroscopeDriverException("Could not initialize driver. Automatic driver unloading failed. Configuration might got corrupted. Restarting YouScope is recommended.", e1);
			}
			throw new MicroscopeDriverException("Could not initilize driver. Check if device is correctly connected, or retry with different pre-initialization settings. ", e);
		}
		finally
		{
			microscope.unlockWrite();
		}
		
	}

	@Override
	public synchronized void unloadDevice(int accessID) throws MicroscopeLockedException, MicroscopeDriverException
	{
		if(currentlyLoadedDeviceID == null)
			return;
		try
		{
			// Get access to microManager
			CMMCore core = microscope.startWrite(accessID);
			// unload device
			core.unloadDevice(currentlyLoadedDeviceID);
			
			currentlyLoadedDeviceID = null;
		}
		catch(MicroscopeLockedException e)
		{
			throw new MicroscopeDriverException("Could not unload device driver with ID " + currentlyLoadedDeviceID + " since microscope is locked.", e);
		}
		catch(Exception e)
		{
			throw new MicroscopeDriverException("Could not unload device driver with ID " + currentlyLoadedDeviceID + " since microscope is locked.", e);
		}
		finally
		{
			microscope.unlockWrite();
		}
	}
	
	
	private synchronized PreInitDevicePropertyInternal[] loadProperties(int accessID) throws MicroscopeDriverException
	{
		if(currentlyLoadedDeviceID == null)
			throw new MicroscopeDriverException("No device driver loaded.");
	
		// Now, get the properties.
		Vector<PreInitDevicePropertyInternal> preInitProps;
		CMMCore core = null;
		serialPort = false;
		try
		{
			// Get access to microManager
			core = microscope.startWrite(accessID);
					
			// Get properties which must be pre-initialized
			StrVector propertyNames = core.getDevicePropertyNames(currentlyLoadedDeviceID);
			preInitProps = new Vector<PreInitDevicePropertyInternal>();
			for(String propertyName : propertyNames)
			{
				if(core.isPropertyPreInit(currentlyLoadedDeviceID, propertyName))
				{
					// Get property type.
					PropertyType propertyType;
					mmcorej.PropertyType mmPropertyType = core.getPropertyType(currentlyLoadedDeviceID, propertyName);
					if(mmPropertyType == mmcorej.PropertyType.Float)
						propertyType = PropertyType.PROPERTY_FLOAT;
					else if(mmPropertyType == mmcorej.PropertyType.Integer)
						propertyType = PropertyType.PROPERTY_INTEGER;
					else
						propertyType = PropertyType.PROPERTY_STRING;
					
					// Get allowed values.
					String currentValue = core.getProperty(currentlyLoadedDeviceID, propertyName);
					String[] propertyValues;
					StrVector mmPropertyValues = core.getAllowedPropertyValues(currentlyLoadedDeviceID, propertyName);
					if(mmPropertyValues == null || mmPropertyValues.size() == 0)
					{
						propertyValues = null;
					}
					else
					{
						propertyValues = new String[(int)mmPropertyValues.size()];
						for(int i = 0; i < propertyValues.length; i++)
						{
							propertyValues[i] = mmPropertyValues.get(i);
						}
						propertyType = PropertyType.PROPERTY_SELECTABLE;
					}
					if(propertyName.equals("Port"))
						serialPort = true;
					preInitProps.add(new PreInitDevicePropertyImpl(microscope, propertyName, propertyType, propertyValues, currentValue));
				}
			} 
		}
		catch(MicroscopeLockedException e)
		{
			throw new MicroscopeDriverException("Could not get driver information since microscope is locked.", e);
		}
		catch(Exception e)
		{
			throw new MicroscopeDriverException("Could not load driver information. ", e);
		}
		finally
		{
			microscope.unlockWrite();
		}
		
		return preInitProps.toArray(new PreInitDevicePropertyInternal[preInitProps.size()]);
	}
	
	
	
}
