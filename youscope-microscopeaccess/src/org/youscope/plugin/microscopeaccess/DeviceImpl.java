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
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import org.youscope.addon.microscopeaccess.DeviceInternal;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.DeviceType;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;

import mmcorej.CMMCore;
import mmcorej.PropertyType;
import mmcorej.StrVector;

/**
 * @author langmo
 * 
 */
class DeviceImpl implements DeviceInternal, Comparable<DeviceImpl>, PropertyActionListener
{

	protected final MicroscopeImpl	microscope;
	private final String			deviceID;
	private final DeviceType			deviceType;
	private final String[] invalidParams;
	
	private final String libraryID;
	private final String driverID;
		
	private double delay = 0;
	
	private final Date initializationTime = new Date();
	
	/**
	 * Time (in ms) of last device action. Used for explicit delay.
	 */
	volatile long lastDeviceActionTime = 0;
	
	protected final Hashtable<String, PropertyImpl> properties = new Hashtable<String, PropertyImpl>();
	
	/**
	 * The order of device initialization might important when saving the configuration to a file and later on reading it.
	 * We thus have to know in which order the devices were initialized...
	 */
	private static int nextDeviceInitID = 0;
	private final int deviceInitID;
	/**
	 * Constructor.
	 * @param microscope Microscope device belongs to.
	 * @param deviceName Name of the device.
	 * @param libraryID ID of the driver library where this device's driver is defined.
	 * @param driverID ID of the driver.
	 * @param deviceType The type of this device.
	 */
	public DeviceImpl(MicroscopeImpl microscope, String deviceName, String libraryID, String driverID, DeviceType deviceType)
	{
		this.microscope = microscope;
		this.deviceID = deviceName;
		this.deviceType = deviceType;
		this.libraryID = libraryID;
		this.driverID = driverID;
		this.invalidParams = new String[0];
		
		this.deviceInitID = nextDeviceInitID++;
	}
	protected DeviceImpl(MicroscopeImpl microscope, String deviceName, String libraryID, String driverID, DeviceType deviceType, String[] invalidParams)
	{
		this.microscope = microscope;
		this.deviceID = deviceName;
		this.deviceType = deviceType;
		this.libraryID = libraryID;
		this.driverID = driverID;
		this.invalidParams = invalidParams;
		
		this.deviceInitID = nextDeviceInitID++;
	}
	
	int getDeviceInitID()
	{
		return deviceInitID;
	}
	
	/**
	 * Initializes device
	 * @param accessID  
	 */
	protected void initializeDevice(int accessID) throws MicroscopeException
	{
		// Initialize properties
		StrVector devicesPropertyNames;
		try
		{
			devicesPropertyNames = microscope.startRead().getDevicePropertyNames(deviceID);
		}
		catch(Exception e)
		{
			throw new  MicroscopeException("Could not get property names of device " + deviceID + ".", e);
		}
		finally
		{
			microscope.unlockRead();
		}

		if(devicesPropertyNames == null)
		{
			devicesPropertyNames = new StrVector();
		}

		for(String propertyID : devicesPropertyNames)
		{
			// Sort out invalid ones
			if(isInvalidParameter(propertyID))
				continue;
			
			properties.put(propertyID, initializeProperty(propertyID));
		}
		
		// Initialize delay
		try
		{
			CMMCore core = microscope.startRead();
			boolean delayable = core.usesDeviceDelay(getDeviceID());
			if(delayable)
				delay = core.getDeviceDelayMs(getDeviceID());
				
		}
		catch(Exception e)
		{
			throw new  MicroscopeException("Could not detect if device " + deviceID + " has an explicit delay.", e);
		}
		finally
		{
			microscope.unlockRead();
		}
	}
	
	protected PropertyImpl initializeProperty(String propertyID) throws MicroscopeException
	{
		// Detect if it is a pre-init property.
		boolean preInit;
		try
		{
			preInit = microscope.startRead().isPropertyPreInit(deviceID, propertyID);
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not get information if property " + deviceID + "." + propertyID + " is pre-init.", e);
		}
		finally
		{
			microscope.unlockRead();
		}
		
		// Detect if it is a read only property.
		try
		{
			if(microscope.startRead().isPropertyReadOnly(deviceID, propertyID))
			{
				return new ReadOnlyPropertyImpl(microscope, deviceID, propertyID, preInit, this);
			}
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not get information if property " + deviceID + "." + propertyID + " is editable.", e);
		}
		finally
		{
			microscope.unlockRead();
		}
		
		// Detect if it is a selectable property
		StrVector devicesPropertyValues;
		try
		{
			devicesPropertyValues = microscope.startRead().getAllowedPropertyValues(deviceID, propertyID);
			if(devicesPropertyValues != null && devicesPropertyValues.size() != 0)
			{
				return new SelectablePropertyImpl(microscope, deviceID, propertyID, devicesPropertyValues.toArray(), preInit, this);
			}
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not detect allowed possible values of property " + propertyID + " of device " + deviceID + ".", e);
		}
		finally
		{
			microscope.unlockRead();
		}
		
		// Detect if it is an integer, float or a string property.
		PropertyType type;
		
		try
		{
			type = microscope.startRead().getPropertyType(deviceID, propertyID);
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not detect type of property " + deviceID + "." + propertyID + ".", e);
		}
		finally
		{
			microscope.unlockRead();
		}
		if(type == PropertyType.Float)
		{
			// Get limits
			float lowerLimit = Float.MIN_VALUE;
			float upperLimit = Float.MAX_VALUE;
			try
			{
				CMMCore core = microscope.startRead();
				boolean hasLimits = core.hasPropertyLimits(deviceID, propertyID);
				if(hasLimits)
				{
					lowerLimit = (float)core.getPropertyLowerLimit(deviceID, propertyID);
					upperLimit = (float)core.getPropertyUpperLimit(deviceID, propertyID);
				}
			}
			catch(Exception e)
			{
				throw new MicroscopeException("Could not detect limits of integer property " + deviceID + "." + propertyID + ".", e);
			}
			finally
			{
				microscope.unlockRead();
			}
			
			// Create property
			return new FloatPropertyImpl(microscope, deviceID, propertyID, lowerLimit, upperLimit, preInit, this);
		}
		else if(type == PropertyType.Integer)
		{
			// Get limits
			int lowerLimit = Integer.MIN_VALUE;
			int upperLimit = Integer.MAX_VALUE;
			try
			{
				CMMCore core = microscope.startRead();
				boolean hasLimits = core.hasPropertyLimits(deviceID, propertyID);
				if(hasLimits)
				{
					lowerLimit = (int)core.getPropertyLowerLimit(deviceID, propertyID);
					upperLimit = (int)core.getPropertyUpperLimit(deviceID, propertyID);
				}
			}
			catch(Exception e)
			{
				throw new MicroscopeException("Could not detect limits of integer property " + deviceID + "." + propertyID + ".", e);
			}
			finally
			{
				microscope.unlockRead();
			}
			
			// Create property
			return new IntegerPropertyImpl(microscope, deviceID, propertyID, lowerLimit, upperLimit, preInit, this);
		}
		else 
			return new StringPropertyImpl(microscope, deviceID, propertyID, preInit, this);
	}
	
	private boolean isInvalidParameter(String parameter)
	{
		for(String invalidParameter : invalidParams)
		{
			if(invalidParameter.equals(parameter))
				return true;
		}
		return false;
	}
	
	@Override
	public Date getInitializationTime()
	{
		return (Date)initializationTime.clone();
	}
	
	@Override
	public String getDeviceID()
	{
		return deviceID;
	}

	@Override
	public DeviceType getType()
	{
		return deviceType;
	}

	@Override
	public PropertyImpl[] getEditableProperties()
	{
		Vector<PropertyImpl> editableProperties = new Vector<PropertyImpl>();
		for(PropertyImpl property : properties.values())
		{
			if(property.isEditable())
				editableProperties.addElement(property);
		}
		Collections.sort(editableProperties);
		return editableProperties.toArray(new PropertyImpl[editableProperties.size()]);
	}
	@Override
	public PropertyImpl[] getProperties()
	{
		PropertyImpl[] propertyArray = properties.values().toArray(new PropertyImpl[0]);
		Arrays.sort(propertyArray);
		return propertyArray;
	}

	@Override
	public PropertyImpl getProperty(String propertyID) throws DeviceException
	{
		PropertyImpl property = properties.get(propertyID);
		if(property == null)
			throw new DeviceException("Device " + getDeviceID() + " does not have a property named " + propertyID);
		return property;
	}

	@Override
	public String getLibraryID()
	{
		return libraryID;
	}

	@Override
	public String getDriverID()
	{
		return driverID;
	}

	@Override
	public double getExplicitDelay()
	{
		return delay;
	}

	@Override
	public void setExplicitDelay(double delay, int accessID) throws MicroscopeException, MicroscopeLockedException
	{
		try
		{
			microscope.startWrite(accessID).setDeviceDelayMs(getDeviceID(), delay);
			this.delay = delay;
		}
		catch(MicroscopeLockedException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not set delay of device " + deviceID + " to " + Double.toString(delay) + ": " + e.getMessage());
		}
		finally
		{
			microscope.unlockWrite();
		}
		microscope.stateChanged("Delay of device " + deviceID + " set to "+Double.toString(delay)+"ms.");
	}
	@Override
	public int compareTo(DeviceImpl o)
	{
		if(o==null)
			return -1;
		return getDeviceID().compareToIgnoreCase(o.getDeviceID());
	}
	@Override
	public void waitForDevice() throws MicroscopeException, InterruptedException
	{
		// First, let the device decide if it thinks it is ready...
		try
		{
			CMMCore core = microscope.startRead();
			
			int timeout = microscope.getMicroscopeConfiguration().getCommunicationTimeout();
			int pingPeriod = microscope.getMicroscopeConfiguration().getCommunicationPingPeriod();
			
			//core.waitForDevice(getDeviceID());
			boolean finished = false;
			for(int time = 0; time < timeout; time += pingPeriod)
			{
				try
				{
					finished = !core.deviceBusy(getDeviceID());
				}
				catch(Exception e)
				{
					throw new MicroscopeException("Error in waiting for device " + getDeviceID() + ".", e);
				}	
				if(finished)
					break;
				Thread.sleep(pingPeriod);
			}
			if(!finished)
			{
				throw new MicroscopeException("Waiting for device " + getDeviceID() + " timed out (total waiting time "+Integer.toString(timeout)+"ms).");
			}
		}
		finally
		{
			microscope.unlockRead();
		}
		
		// Now, let's do the own waiting algorithm on top to guarantee a given waiting period.
		if(delay <=0)
			return;
		long deltaT = (long)(delay - (System.currentTimeMillis() - lastDeviceActionTime));
		if(deltaT <= 0)
			return;
		Thread.sleep(deltaT);
	}
	@Override
	public void deviceStateModified()
	{
		lastDeviceActionTime = System.currentTimeMillis();
	}
}
