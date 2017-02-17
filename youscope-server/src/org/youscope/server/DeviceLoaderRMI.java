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

import org.youscope.addon.microscopeaccess.AvailableDeviceDriverInternal;
import org.youscope.addon.microscopeaccess.DeviceLoaderInternal;
import org.youscope.common.microscope.AvailableDeviceDriver;
import org.youscope.common.microscope.DeviceLoader;
import org.youscope.common.microscope.MicroscopeDriverException;
import org.youscope.common.microscope.MicroscopeLockedException;

/**
 * @author langmo
 */
class DeviceLoaderRMI extends UnicastRemoteObject implements DeviceLoader
{

	/**
	 * Serial Version UID.
	 */
	private static final long		serialVersionUID	= -975901235633383293L;

	protected DeviceLoaderInternal	deviceManager;

	protected int					accessID;

	/**
	 * Constructor.
	 * 
	 * @throws RemoteException
	 */
	DeviceLoaderRMI(DeviceLoaderInternal deviceManager, int accessID) throws RemoteException
	{
		super();
		this.accessID = accessID;
		this.deviceManager = deviceManager;
	}

	@Override
	public AvailableDeviceDriver[] getAvailableDeviceDrivers() throws RemoteException, MicroscopeDriverException
	{
		AvailableDeviceDriverInternal[] driversInternal = deviceManager.getAvailableDeviceDrivers();
		AvailableDeviceDriver[] drivers = new AvailableDeviceDriver[driversInternal.length];
		for(int i = 0; i < driversInternal.length; i++)
		{
			drivers[i] = new AvailableDeviceDriverRMI(driversInternal[i], accessID);
		}
		return drivers;
	}

	@Override
	public void removeDevice(String name) throws MicroscopeDriverException, MicroscopeLockedException
	{
		deviceManager.removeDevice(name, accessID);
	}
}
