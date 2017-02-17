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

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.youscope.addon.microscopeaccess.DeviceInternal;
import org.youscope.addon.microscopeaccess.FloatPropertyInternal;
import org.youscope.addon.microscopeaccess.IntegerPropertyInternal;
import org.youscope.addon.microscopeaccess.PropertyInternal;
import org.youscope.addon.microscopeaccess.ReadOnlyPropertyInternal;
import org.youscope.addon.microscopeaccess.SelectablePropertyInternal;
import org.youscope.addon.microscopeaccess.StringPropertyInternal;
import org.youscope.common.microscope.Device;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.DeviceType;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.Property;

/**
 * @author langmo
 */
class DeviceRMI extends UnicastRemoteObject implements Device
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 8821464564099326373L;

	protected DeviceInternal	device;

	protected int				accessID;

	/**
	 * Constructor.
	 * 
	 * @throws RemoteException
	 */
	DeviceRMI(DeviceInternal device, int accessID) throws RemoteException
	{
		super();
		this.accessID = accessID;
		this.device = device;
	}

	@Override
	public String getDeviceID()
	{
		return device.getDeviceID();
	}

	@Override
	public DeviceType getType()
	{
		return device.getType();
	}

	@Override
	public Property[] getProperties() throws RemoteException
	{
		PropertyInternal[] orgProperties = device.getProperties();
		Property[] newProperties = new Property[orgProperties.length];
		for(int i = 0; i < orgProperties.length; i++)
		{
			newProperties[i] = toProperty(orgProperties[i]);
		}
		return newProperties;
	}

	private Property toProperty(PropertyInternal property) throws RemoteException
	{
		if(property instanceof StringPropertyInternal)
			return new StringPropertyRMI((StringPropertyInternal)property, accessID);
		else if(property instanceof IntegerPropertyInternal)
			return new IntegerPropertyRMI((IntegerPropertyInternal)property, accessID);
		else if(property instanceof FloatPropertyInternal)
			return new FloatPropertyRMI((FloatPropertyInternal)property, accessID);
		else if(property instanceof SelectablePropertyInternal)
			return new SelectablePropertyRMI((SelectablePropertyInternal)property, accessID);
		else if(property instanceof ReadOnlyPropertyInternal)
			return new ReadOnlyPropertyRMI((ReadOnlyPropertyInternal)property, accessID);
		else
			throw new RemoteException("Property type of remote property invalid.");
	}

	@Override
	public Property[] getEditableProperties() throws RemoteException
	{
		PropertyInternal[] orgProperties = device.getEditableProperties();
		Property[] newProperties = new Property[orgProperties.length];
		for(int i = 0; i < orgProperties.length; i++)
		{
			newProperties[i] = toProperty(orgProperties[i]);
		}
		return newProperties;
	}

	@Override
	public Property getProperty(String name) throws DeviceException, RemoteException
	{
		return toProperty(device.getProperty(name));
	}

	@Override
	public String getLibraryID()
	{
		return device.getLibraryID();
	}

	@Override
	public String getDriverID()
	{
		return device.getDriverID();
	}

	@Override
	public double getExplicitDelay()
	{
		return device.getExplicitDelay();

	}

	@Override
	public void setExplicitDelay(double delay) throws MicroscopeException, MicroscopeLockedException
	{
		device.setExplicitDelay(delay, accessID);
	}

	@Override
	public void waitForDevice() throws MicroscopeException, RemoteException, InterruptedException
	{
		device.waitForDevice();
	}
}
