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
 * @author Moritz Lang
 * 
 */
public interface Channel extends Remote
{
	/**
	 * Returns all device settings which get set if this channel is activated.
	 * @return Set of device settings which get set if this channel is activated.
	 * @throws RemoteException
	 */
	DeviceSetting[] getChannelOnSettings() throws RemoteException;

	/**
	 * Returns all device settings which get set if this channel is deactivated.
	 * @return Set of device settings which get set if this channel is activated.
	 * @throws RemoteException
	 */
	DeviceSetting[] getChannelOffSettings() throws RemoteException;

	/**
	 * Sets all device settings which get set if this channel is activated. All previous settings get deleted.
	 * @param settings New settings for the channel.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeLockedException
	 * @throws SettingException
	 * @throws RemoteException
	 */
	void setChannelOnSettings(DeviceSetting[] settings) throws MicroscopeLockedException, SettingException, RemoteException;

	/**
	 * Sets all device settings which get set if this channel is deactivated. All previous settings get deleted.
	 * @param settings New settings for the channel.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeLockedException
	 * @throws SettingException
	 * @throws RemoteException
	 */
	void setChannelOffSettings(DeviceSetting[] settings) throws MicroscopeLockedException, SettingException, RemoteException;

	/**
	 * Adds a setting to the list of settings which get activated if this channel gets activated.
	 * @param setting Setting to add.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeLockedException
	 * @throws SettingException
	 * @throws RemoteException
	 */
	void addChannelOnSetting(DeviceSetting setting) throws MicroscopeLockedException, SettingException, RemoteException;

	/**
	 * Adds a setting to the list of settings which get deactivated if this channel gets deactivated.
	 * @param setting Setting to add.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeLockedException
	 * @throws SettingException
	 * @throws RemoteException
	 */
	void addChannelOffSetting(DeviceSetting setting) throws MicroscopeLockedException, SettingException, RemoteException;

	/**
	 * Returns the ID of the channel group where this channel belongs to.
	 * @return ID of channel group.
	 * @throws RemoteException
	 */
	String getChannelGroupID() throws RemoteException;

	/**
	 * Returns the ID of this channel. Together with the channel group ID, this ID identifies this channel.
	 * @return ID of channel.
	 * @throws RemoteException
	 */
	String getChannelID() throws RemoteException;

	/**
	 * Sets a timeout when changing the channel for synchronization
	 * purposes. After activating the channel on setting, and before imaging the microscope will be blocked for the given time
	 * 
	 * @param timeOutInMillis The timeout (wait) in ms.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeLockedException
	 * @throws RemoteException
	 */
	void setChannelTimeout(int timeOutInMillis) throws MicroscopeLockedException, RemoteException;

	/**
	 * Returns the current timeout when changing a channel.
	 * 
	 * @return Current timeout (default = 0).
	 * @throws RemoteException
	 */
	int getChannelTimeout() throws RemoteException;

	/**
	 * Sets the shutter device of this channel. The shutter is automatically opened shortly before the image process and closed afterwards.
	 * Set to null if the shutter should not be opened and closed automatically.
	 * @param deviceID The device ID of the shutter.
	 * @throws SettingException
	 * @throws MicroscopeLockedException
	 * @throws RemoteException
	 */
	void setShutter(String deviceID) throws SettingException, MicroscopeLockedException, RemoteException;

	/**
	 * Returns the device ID of the shutter of this channel. The shutter is automatically opened shortly before the image process and closed afterwards.
	 * Returns null if the shutter is not be opened and closed automatically.
	 * @return The device ID of the shutter associated to this device.
	 * @throws RemoteException
	 */
	String getShutter() throws RemoteException;
}
