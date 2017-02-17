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

import org.youscope.addon.microscopeaccess.ShutterDeviceInternal;
import org.youscope.common.microscope.DeviceType;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;

/**
 * @author Moritz Lang
 *
 */
class ShutterDeviceImpl extends DeviceImpl implements ShutterDeviceInternal
{
	ShutterDeviceImpl(MicroscopeImpl microscope, String deviceName, String libraryID, String driverID)
	{
		super(microscope, deviceName, libraryID, driverID, DeviceType.ShutterDevice);
	}

	@Override
	public void setOpen(boolean open, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException
	{
		if(Thread.interrupted())
			throw new InterruptedException();
		try
		{
			CMMCore core = microscope.startWrite(accessID);
			core.setShutterDevice(getDeviceID());
			core.setShutterOpen(open);
		}
		catch(MicroscopeLockedException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not open/close shutter device " + getDeviceID() + ".", e);
		}
		finally
		{
			deviceStateModified();
			microscope.unlockWrite();
		}
	}

	@Override
	public boolean isOpen() throws MicroscopeException, InterruptedException
	{
		if(Thread.interrupted())
			throw new InterruptedException();
		try
		{
			CMMCore core = microscope.startRead();
			core.setShutterDevice(getDeviceID());
			return core.getShutterOpen();
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not detect if shutter device " + getDeviceID() + " is open.", e);
		}
		finally
		{
			microscope.unlockRead();
		}
	}
}
