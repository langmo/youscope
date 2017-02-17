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
import java.util.ArrayList;

import org.youscope.addon.microscopeaccess.ChannelInternal;
import org.youscope.addon.microscopeaccess.MicroscopeInternal;
import org.youscope.addon.microscopeaccess.ShutterDeviceInternal;
import org.youscope.common.microscope.Channel;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.microscope.MicroscopeConfigurationListener;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.SettingException;

/**
 * Implementation of the channel interface.
 * Use getRMIInterface to provide RMI interface for clients.
 * @author Moritz Lang
 * 
 */
class ChannelImpl implements ChannelInternal, MicroscopeConfigurationListener, Comparable<ChannelImpl>
{
	private MicroscopeInternal					microscope;
	private final ArrayList<DeviceSetting>	settingsOn		= new ArrayList<DeviceSetting>();
	private final ArrayList<DeviceSetting>	settingsOff		= new ArrayList<DeviceSetting>();
	private final String						channelGroupID;
	private final String						channelID;
	private int									channelTimeout	= 0;
	private String								shutterDeviceID;

	ChannelImpl(String channelGroupID, String channelID, MicroscopeInternal microscope)
	{
		this.channelGroupID = channelGroupID;
		this.channelID = channelID;
		this.microscope = microscope;
		try
		{
			shutterDeviceID = microscope.getShutterDevice().getDeviceID();
		}
		catch(@SuppressWarnings("unused") DeviceException e)
		{
			// No standard shutter device set. Inactivate auto-shutter.
			shutterDeviceID = null;
		}
	}

	@Override
	public void deviceRemoved(String deviceID)
	{
		for(int i = 0; i < settingsOn.size(); i++)
		{
			if(settingsOn.get(i).getDevice().equals(deviceID))
			{
				settingsOn.remove(i);
				i--;
			}
		}
		for(int i = 0; i < settingsOff.size(); i++)
		{
			if(settingsOff.get(i).getDevice().equals(deviceID))
			{
				settingsOff.remove(i);
				i--;
			}
		}
		if(getShutter() != null && getShutter().equals(deviceID))
			shutterDeviceID = null;
	}

	DeviceSetting[] getChannelOnSettings()
	{
		// clone values.
		DeviceSetting[] returnVal = new DeviceSetting[settingsOn.size()];
		for(int i = 0; i < returnVal.length; i++)
		{
			returnVal[i] = new DeviceSetting(settingsOn.get(i));
		}
		return returnVal;
	}

	DeviceSetting[] getChannelOffSettings()
	{
		// clone values.
		DeviceSetting[] returnVal = new DeviceSetting[settingsOff.size()];
		for(int i = 0; i < returnVal.length; i++)
		{
			returnVal[i] = new DeviceSetting(settingsOff.get(i));
		}
		return returnVal;
	}

	private void setChannelOnSettings(DeviceSetting[] settings, int accessID) throws MicroscopeLockedException, SettingException
	{
		microscope.lockExclusiveWrite(accessID);
		try
		{
			SettingsValidator.areSettingsValid(settings, true, microscope, accessID);
			this.settingsOn.clear();
			for(DeviceSetting setting : settings)
			{
				this.settingsOn.add(new DeviceSetting(setting));
			}
		}
		finally
		{
			microscope.unlockExclusiveWrite(accessID);
		}

		ServerSystem.out.println("Settings for activation of channel " + this.toString() + " updated.");
	}

	private void setChannelOffSettings(DeviceSetting[] settings, int accessID) throws MicroscopeLockedException, SettingException
	{
		microscope.lockExclusiveWrite(accessID);
		try
		{
			SettingsValidator.areSettingsValid(settings, true, microscope, accessID);
			this.settingsOff.clear();
			for(DeviceSetting setting : settings)
			{
				this.settingsOff.add(new DeviceSetting(setting));
			}
		}
		finally
		{
			microscope.unlockExclusiveWrite(accessID);
		}

		ServerSystem.out.println("Settings for deactivation of channel " + this.toString() + " updated.");
	}

	@Override
	public String getChannelGroupID()
	{
		return channelGroupID;
	}

	@Override
	public String getChannelID()
	{
		return channelID;
	}

	@Override
	public void activateChannel(int accessID) throws MicroscopeLockedException, MicroscopeException, InterruptedException, SettingException
	{
		microscope.applyDeviceSettings(getChannelOnSettings(), accessID);
		if(channelTimeout > 0)
		{
			Thread.sleep(channelTimeout);
		}
	}

	@Override
	public void deactivateChannel(int accessID) throws MicroscopeLockedException, MicroscopeException, InterruptedException, SettingException
	{
		microscope.applyDeviceSettings(getChannelOffSettings(), accessID);
	}

	@Override
	public void openShutter(int accessID) throws DeviceException, MicroscopeException, MicroscopeLockedException, InterruptedException
	{
		if(shutterDeviceID == null)
			return;

		ShutterDeviceInternal shutter = microscope.getShutterDevice(shutterDeviceID);
		shutter.setOpen(true, accessID);
		shutter.waitForDevice();
	}

	@Override
	public void closeShutter(int accessID) throws DeviceException, MicroscopeException, MicroscopeLockedException, InterruptedException
	{
		if(shutterDeviceID == null)
			return;

		ShutterDeviceInternal shutter = microscope.getShutterDevice(shutterDeviceID);
		shutter.setOpen(false, accessID);
		shutter.waitForDevice();
	}

	void addChannelOnSetting(DeviceSetting setting, int accessID) throws MicroscopeLockedException, SettingException
	{
		microscope.lockExclusiveWrite(accessID);
		try
		{
			SettingsValidator.isSettingValid(setting, true, microscope, accessID);
			this.settingsOn.add(setting);
		}
		finally
		{
			microscope.unlockExclusiveWrite(accessID);
		}
	}

	void addChannelOffSetting(DeviceSetting setting, int accessID) throws MicroscopeLockedException, SettingException
	{
		microscope.lockExclusiveWrite(accessID);
		try
		{
			SettingsValidator.isSettingValid(setting, true, microscope, accessID);
			this.settingsOff.add(setting);
		}
		finally
		{
			microscope.unlockExclusiveWrite(accessID);
		}
	}

	@Override
	public void microscopeUninitialized()
	{
		settingsOn.clear();
		settingsOff.clear();
		shutterDeviceID = null;
	}

	void setChannelTimeout(int timeOutInMillis, int accessID) throws MicroscopeLockedException
	{
		if(timeOutInMillis < 0)
			timeOutInMillis = 0;
		microscope.lockExclusiveWrite(accessID);
		try
		{
			channelTimeout = timeOutInMillis;
		}
		finally
		{
			microscope.unlockExclusiveWrite(accessID);
		}
		ServerSystem.out.println("Timeout of channel " + this.toString() + " set to " + Integer.toString(timeOutInMillis) + "ms.");
	}

	int getChannelTimeout()
	{
		return channelTimeout;
	}

	@Override
	public void labelChanged(DeviceSetting oldLabel, DeviceSetting newLabel)
	{
		for(int i = 0; i < settingsOn.size(); i++)
		{
			if(settingsOn.get(i).equals(oldLabel))
				settingsOn.set(i, newLabel);
		}
		for(int i = 0; i < settingsOff.size(); i++)
		{
			if(settingsOff.get(i).equals(oldLabel))
				settingsOff.set(i, newLabel);
		}
	}

	void setShutter(String deviceID, int accessID) throws SettingException, MicroscopeLockedException
	{
		microscope.lockExclusiveWrite(accessID);
		try
		{
			if(deviceID != null)
				microscope.getShutterDevice(deviceID);
			shutterDeviceID = deviceID;
		}
		catch(DeviceException e)
		{
			throw new SettingException("Shutter of channel " + this.toString() + " is invalid.", e);
		}
		finally
		{
			microscope.unlockExclusiveWrite(accessID);
		}

		if(deviceID == null)
			ServerSystem.out.println("Automatic shutter of channel " + this.toString() + " deactivated.");
		else
			ServerSystem.out.println("Automatic shutter of channel " + this.toString() + " set to device " + deviceID + ".");
	}

	String getShutter()
	{
		return shutterDeviceID;
	}

	@Override
	public int compareTo(ChannelImpl o)
	{
		if(o == null)
			return -1;
		int res = getChannelGroupID().compareToIgnoreCase(o.getChannelGroupID());
		if(res != 0)
			return res;
		return getChannelID().compareToIgnoreCase(o.getChannelID());
	}

	private class ChannelRMI extends UnicastRemoteObject implements Channel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -4267997515192022958L;

		private final int			accessID;

		ChannelRMI(int accessID) throws RemoteException
		{
			super();
			this.accessID = accessID;
		}

		@Override
		public DeviceSetting[] getChannelOnSettings() throws RemoteException
		{
			return ChannelImpl.this.getChannelOnSettings();
		}

		@Override
		public DeviceSetting[] getChannelOffSettings() throws RemoteException
		{
			return ChannelImpl.this.getChannelOffSettings();
		}

		@Override
		public void setChannelOnSettings(DeviceSetting[] settings) throws MicroscopeLockedException, SettingException, RemoteException
		{
			ChannelImpl.this.setChannelOnSettings(settings, accessID);
		}

		@Override
		public void setChannelOffSettings(DeviceSetting[] settings) throws MicroscopeLockedException, SettingException, RemoteException
		{
			ChannelImpl.this.setChannelOffSettings(settings, accessID);
		}

		@Override
		public void addChannelOnSetting(DeviceSetting setting) throws MicroscopeLockedException, SettingException, RemoteException
		{
			ChannelImpl.this.addChannelOnSetting(setting, accessID);
		}

		@Override
		public void addChannelOffSetting(DeviceSetting setting) throws MicroscopeLockedException, SettingException, RemoteException
		{
			ChannelImpl.this.addChannelOffSetting(setting, accessID);
		}

		@Override
		public String getChannelGroupID() throws RemoteException
		{
			return ChannelImpl.this.getChannelGroupID();
		}

		@Override
		public String getChannelID() throws RemoteException
		{
			return ChannelImpl.this.getChannelID();
		}

		@Override
		public void setChannelTimeout(int timeOutInMillis) throws MicroscopeLockedException, RemoteException
		{
			ChannelImpl.this.setChannelTimeout(timeOutInMillis, accessID);
		}

		@Override
		public int getChannelTimeout() throws RemoteException
		{
			return ChannelImpl.this.getChannelTimeout();
		}

		@Override
		public void setShutter(String deviceID) throws SettingException, MicroscopeLockedException, RemoteException
		{
			ChannelImpl.this.setShutter(deviceID, accessID);
		}

		@Override
		public String getShutter() throws RemoteException
		{
			return ChannelImpl.this.getShutter();
		}
	}

	Channel getRMIInterface(int accessID) throws RemoteException
	{
		return new ChannelRMI(accessID);
	}

	@Override
	public String toString()
	{
		return channelGroupID + "." + channelID;
	}
}
