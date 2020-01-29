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
package org.youscope.common.microscope;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author langmo
 * 
 */
public interface DeviceLoader extends Remote
{
	/**
	 * Returns a list of all available device drivers.
	 * @return List of all available device drivers.
	 * @throws RemoteException
	 * @throws MicroscopeDriverException
	 */
	public AvailableDeviceDriver[] getAvailableDeviceDrivers() throws RemoteException, MicroscopeDriverException;

	/**
	 * Removes a previously added device.
	 * @param name Name of the device.
	 * @throws RemoteException
	 * @throws MicroscopeDriverException
	 * @throws MicroscopeLockedException
	 */
	public void removeDevice(String name) throws RemoteException, MicroscopeDriverException, MicroscopeLockedException;
}
