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

import mmcorej.CMMCore;

import org.youscope.addon.microscopeaccess.PropertyInternal;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.PropertyType;

/**
 * @author langmo
 * 
 */
public abstract class PropertyImpl implements PropertyInternal, Comparable<PropertyInternal>
{
	protected final MicroscopeImpl	microscope;
	private final String			device;
	private final String			property;
	private final PropertyType type;
	
	private final boolean editable;
	private final boolean preInit;
	
	private final PropertyActionListener actionListener;

	PropertyImpl(MicroscopeImpl microscope, String deviceID, String propertyID, PropertyType type, boolean editable, boolean preInit, PropertyActionListener actionListener)
	{
		this.microscope = microscope;
		this.device = deviceID;
		this.property = propertyID;
		this.type = type;
		this.editable = editable;
		this.preInit = preInit;
		this.actionListener = actionListener;
	}
	
	protected void deviceStateModified()
	{
		actionListener.deviceStateModified();
	}
	
	@Override
	public boolean isPreInitializationProperty()
	{
		return preInit;
	}
	@Override
	public String getPropertyID()
	{
		return property;
	}
	
	@Override
	public String getDeviceID()
	{
		return device;
	}

	@Override
	public PropertyType getType()
	{
		return type;
	}

	@Override
	public String getValue() throws MicroscopeException, InterruptedException
	{
		if(Thread.interrupted())
			throw new InterruptedException();
		if(device == null || property == null)
			return null;
		try
		{
			return microscope.startRead().getProperty(device, property);
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not get property " + property + " of device " + device + ".", e);
		}
		finally
		{
			microscope.unlockRead();
		}
	}

	@Override
	public abstract void setValue(String value, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException, DeviceException;
	
	protected void setStringValue(String value, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException
	{
		if(Thread.interrupted())
			throw new InterruptedException();
		try
		{
			CMMCore core = microscope.startWrite(accessID);
			core.setProperty(device, property, value);
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Couldn't set property " + device + "." + property + " to " + value + ".", e);
		}
		finally
		{
			deviceStateModified();
			microscope.unlockWrite();
		}
		microscope.stateChanged("Property " + device + "." + property + " set to " + value + ".");
	}

	@Override
	public boolean isEditable()
	{
		return editable;
	}

	@Override
	public int compareTo(PropertyInternal o)
	{
		if(o == null)
			return -1;
		return getPropertyID().compareToIgnoreCase(o.getPropertyID());
		
	}
}
