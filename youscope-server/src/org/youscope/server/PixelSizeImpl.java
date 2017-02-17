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
import java.util.Vector;

import org.youscope.addon.microscopeaccess.MicroscopeInternal;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.microscope.MicroscopeConfigurationListener;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.PixelSize;
import org.youscope.common.microscope.SettingException;

/**
 * For a given set of device settings, this class stores information about the corresponding pixel size of the camera.
 * @author Moritz Lang
 * 
 */
class PixelSizeImpl implements MicroscopeConfigurationListener, Comparable<PixelSizeImpl>
{
	private final String					pixelSizeID;
	private final Vector<DeviceSetting>	pixelSizeSettings	= new Vector<DeviceSetting>();
	private double							pixelSize			= 6.45;
	private final MicroscopeInternal		microscope;

	public PixelSizeImpl(String pixelSizeID, MicroscopeInternal microscope)
	{
		this.pixelSizeID = new String(pixelSizeID);
		this.microscope = microscope;
	}

	public String getPixelSizeID()
	{
		return pixelSizeID;
	}

	public DeviceSetting[] getPixelSizeSettings()
	{
		// Clone every element...
		DeviceSetting[] newArray = new DeviceSetting[pixelSizeSettings.size()];
		for(int i = 0; i < newArray.length; i++)
		{
			newArray[i] = new DeviceSetting(pixelSizeSettings.elementAt(i));
		}

		return newArray;
	}

	public double getPixelSize()
	{
		return pixelSize;
	}

	@Override
	public void deviceRemoved(String deviceID)
	{
		for(int i = 0; i < pixelSizeSettings.size(); i++)
		{
			DeviceSetting setting = pixelSizeSettings.elementAt(i);
			if(setting.getDevice().equals(deviceID))
			{
				pixelSizeSettings.remove(setting);
				i--;
			}
		}
	}

	public void setPixelSizeSettings(DeviceSetting[] newSettings, int accessID) throws MicroscopeLockedException, SettingException
	{
		microscope.lockExclusiveWrite(accessID);
		try
		{

			SettingsValidator.areSettingsValid(newSettings, true, microscope, accessID);
			pixelSizeSettings.clear();
			for(DeviceSetting setting : newSettings)
			{
				pixelSizeSettings.add(new DeviceSetting(setting));
			}
		}
		finally
		{
			microscope.unlockExclusiveWrite(accessID);
		}

		ServerSystem.out.println("Settings of pixel size configuration " + pixelSizeID + " changed.");
	}

	public void addPixelSizeSetting(DeviceSetting setting, int accessID) throws MicroscopeLockedException, SettingException
	{
		microscope.lockExclusiveWrite(accessID);
		try
		{
			SettingsValidator.isSettingValid(setting, true, microscope, accessID);
			pixelSizeSettings.add(setting);
		}
		finally
		{
			microscope.unlockExclusiveWrite(accessID);
		}

		ServerSystem.out.println("Added settings \"" + setting.toString() + "\" to pixel size configuration " + pixelSizeID + ".");
	}

	public void setPixelSize(double pixelSize, int accessID) throws MicroscopeLockedException, SettingException
	{
		microscope.lockExclusiveWrite(accessID);
		try
		{
			this.pixelSize = pixelSize;
		}
		finally
		{
			microscope.unlockExclusiveWrite(accessID);
		}

		ServerSystem.out.println("Pixel size of " + pixelSizeID + " set to " + Double.toString(pixelSize) + " microns.");
	}

	@Override
	public void microscopeUninitialized()
	{
		pixelSizeSettings.clear();
		pixelSize = 6.45;
	}

	@Override
	public void labelChanged(DeviceSetting oldLabel, DeviceSetting newLabel)
	{
		for(int i = 0; i < pixelSizeSettings.size(); i++)
		{
			if(pixelSizeSettings.elementAt(i).equals(oldLabel))
				pixelSizeSettings.setElementAt(newLabel, i);
		}
	}

	@Override
	public int compareTo(PixelSizeImpl o)
	{
		if(o == null)
			return -1;
		return getPixelSizeID().compareToIgnoreCase(o.getPixelSizeID());
	}

	private class RMIInterface extends UnicastRemoteObject implements PixelSize
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -297042545568227183L;
		private final int			accessID;

		/**
		 * Constructor.
		 * @param accessID The microscope access ID used by this RMI interface.
		 * @throws RemoteException
		 */
		RMIInterface(int accessID) throws RemoteException
		{
			super();
			this.accessID = accessID;
		}

		@Override
		public String getPixelSizeID() throws RemoteException
		{
			return PixelSizeImpl.this.getPixelSizeID();
		}

		@Override
		public DeviceSetting[] getPixelSizeSettings() throws RemoteException
		{
			return PixelSizeImpl.this.getPixelSizeSettings();
		}

		@Override
		public void setPixelSizeSettings(DeviceSetting[] newSettings) throws MicroscopeLockedException, SettingException, RemoteException
		{
			PixelSizeImpl.this.setPixelSizeSettings(newSettings, accessID);
		}

		@Override
		public void addPixelSizeSetting(DeviceSetting setting) throws MicroscopeLockedException, SettingException, RemoteException
		{
			PixelSizeImpl.this.addPixelSizeSetting(setting, accessID);
		}

		@Override
		public double getPixelSize() throws RemoteException
		{
			return PixelSizeImpl.this.getPixelSize();
		}

		@Override
		public void setPixelSize(double pixelSize) throws MicroscopeLockedException, SettingException, RemoteException
		{
			PixelSizeImpl.this.setPixelSize(pixelSize, accessID);
		}
	}

	/**
	 * Returns the RMI interface of this class used by client applications.
	 * @param accessID The access ID to the microscope used by the RMI interface.
	 * @throws RemoteException
	 */
	PixelSize getRMIInterface(int accessID) throws RemoteException
	{
		return new RMIInterface(accessID);
	}
}
