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
import java.util.EventListener;


/**
 * Listener which gets notified if a device of the microscope was removed or its configuration modified.
 * @author Moritz Lang
 *
 */
public interface MicroscopeConfigurationListener extends EventListener, Remote
{
	
	/**
	 * Called when the microscope is uninitialized. All stored values about the microscope configuration should be unloaded.
	 * @throws RemoteException 
	 */
	public void microscopeUninitialized() throws RemoteException;
	/**
	 * Called when a previously defined device was removed.
	 * @param deviceID The ID of the removed device.
	 * @throws RemoteException 
	 */
	public void deviceRemoved(String deviceID) throws RemoteException;
	/**
	 * Called when a label of a state device changed its name.
	 * @param oldLabel Setting containing the old label.
	 * @param newLabel Setting containing the new label.
	 * @throws RemoteException 
	 */
	public void labelChanged(DeviceSetting oldLabel, DeviceSetting newLabel) throws RemoteException;
}
