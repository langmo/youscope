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

import org.youscope.addon.microscopeaccess.SerialDeviceInternal;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.SerialDevice;

/**
 * @author Moritz Lang
 * 
 */
class SerialDeviceRMI extends DeviceRMI implements SerialDevice
{

	/**
	 * Serial Version UID.
	 */
	private static final long		serialVersionUID	= -7624821113540201850L;

	private SerialDeviceInternal	serialDevice;

	SerialDeviceRMI(SerialDeviceInternal serialDevice, int accessID) throws RemoteException
	{
		super(serialDevice, accessID);
		this.serialDevice = serialDevice;
	}

	@Override
	public void sendCommand(String command) throws MicroscopeLockedException, MicroscopeException, InterruptedException, RemoteException
	{
		serialDevice.sendCommand(command, accessID);
	}

}
