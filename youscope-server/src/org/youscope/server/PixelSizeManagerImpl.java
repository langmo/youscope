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
import java.util.Arrays;
import java.util.Hashtable;

import org.youscope.addon.microscopeaccess.MicroscopeInternal;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.microscope.MicroscopeConfigurationListener;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.PixelSize;
import org.youscope.common.microscope.PixelSizeManager;
import org.youscope.common.microscope.SettingException;

/**
 * @author langmo
 * 
 */
class PixelSizeManagerImpl implements MicroscopeConfigurationListener
{

	private final MicroscopeInternal				microscope;

	private final Hashtable<String, PixelSizeImpl>	pixelSizes	= new Hashtable<String, PixelSizeImpl>();

	PixelSizeManagerImpl(MicroscopeInternal microscope)
	{
		this.microscope = microscope;
		microscope.addConfigurationListener(this);
	}
 
	public PixelSizeImpl[] getPixelSizes()
	{
		PixelSizeImpl[] retVal = pixelSizes.values().toArray(new PixelSizeImpl[0]);
		Arrays.sort(retVal);
		return retVal;
	}

	public void removePixelSize(String pixelSizeID, int accessID) throws MicroscopeLockedException, SettingException
	{
		boolean success;
		microscope.lockExclusiveWrite(accessID);
		try
		{
			success = pixelSizes.remove(pixelSizeID) != null;
		}
		finally
		{
			microscope.unlockExclusiveWrite(accessID);
		}
		if(success)
			ServerSystem.out.println("Pixel size setting with ID " + pixelSizeID + " removed.");
		else
			ServerSystem.out.println("Pixel size setting with ID " + pixelSizeID + " could not be found nor be removed.");
	}

	public PixelSizeImpl getPixelSize(String pixelSizeID) throws SettingException
	{
		PixelSizeImpl pixelSize = pixelSizes.get(pixelSizeID);
		if(pixelSize == null)
			throw new SettingException("Pixel size with ID " + pixelSizeID + " not defined.");
		return pixelSize;
	}

	@Override
	public void deviceRemoved(String deviceID)
	{
		// Remove the device settings of all pixel size settings for the respective device.
		for(PixelSizeImpl pixelSize : pixelSizes.values())
		{
			pixelSize.deviceRemoved(deviceID);
		}
	}

	public PixelSizeImpl addPixelSize(String pixelSizeID, int accessID) throws MicroscopeLockedException, SettingException
	{
		PixelSizeImpl pixelSize;
		microscope.lockExclusiveWrite(accessID);
		try
		{
			try
			{
				// return pixel size if it exists.
				return getPixelSize(pixelSizeID);
			}
			catch(@SuppressWarnings("unused") SettingException e)
			{
				// pixel size does not yet exist.
			}
			pixelSize = new PixelSizeImpl(pixelSizeID, microscope);
			pixelSizes.put(pixelSizeID, pixelSize);
		}
		finally
		{
			microscope.unlockExclusiveWrite(accessID);
		}
		ServerSystem.out.println("New pixel size configuration " + pixelSizeID + " created.");
		return pixelSize;
	}

	@Override
	public void microscopeUninitialized()
	{
		for(PixelSizeImpl pixelSize : pixelSizes.values())
		{
			pixelSize.microscopeUninitialized();
		}

		pixelSizes.clear();
	}

	@Override
	public void labelChanged(DeviceSetting oldLabel, DeviceSetting newLabel)
	{
		for(PixelSizeImpl pixelSize : pixelSizes.values())
		{
			pixelSize.labelChanged(oldLabel, newLabel);
		}
	}

	private class RMIInterface extends UnicastRemoteObject implements PixelSizeManager
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= 7993259236258557481L;
		private final int			accessID;

		/**
		 * Constructor.
		 * @param accessID Access ID for the microscope.
		 * @throws RemoteException
		 */
		protected RMIInterface(int accessID) throws RemoteException
		{
			super();
			this.accessID = accessID;
		}

		@Override
		public PixelSize[] getPixelSizes() throws RemoteException
		{
			PixelSizeImpl[] orgVal = PixelSizeManagerImpl.this.getPixelSizes();
			PixelSize[] returnVal = new PixelSize[orgVal.length];
			for(int i = 0; i < orgVal.length; i++)
			{
				returnVal[i] = orgVal[i].getRMIInterface(accessID);
			}

			return returnVal;
		}

		@Override
		public PixelSize getPixelSize(String pixelSizeID) throws SettingException, RemoteException
		{
			return PixelSizeManagerImpl.this.getPixelSize(pixelSizeID).getRMIInterface(accessID);
		}

		@Override
		public PixelSize addPixelSize(String pixelSizeID) throws MicroscopeLockedException, SettingException, RemoteException
		{
			return PixelSizeManagerImpl.this.addPixelSize(pixelSizeID, accessID).getRMIInterface(accessID);
		}

		@Override
		public void removePixelSize(String pixelSizeID) throws MicroscopeLockedException, SettingException, RemoteException
		{
			PixelSizeManagerImpl.this.removePixelSize(pixelSizeID, accessID);
		}
	}

	/**
	 * Returns the RMI interface to this object which can be used by clients.
	 * @param accessID The access ID to the microscope used by the RMI interface.
	 * @return The RMI interface.
	 * @throws RemoteException
	 */
	public PixelSizeManager getRMIInterface(int accessID) throws RemoteException
	{
		return new RMIInterface(accessID);
	}
}
