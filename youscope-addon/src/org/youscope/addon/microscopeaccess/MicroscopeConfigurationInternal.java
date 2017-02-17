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
package org.youscope.addon.microscopeaccess;


import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.SettingException;

/**
 * @author Moritz Lang
 *
 */
public interface MicroscopeConfigurationInternal
{
	/**
	 * Sets the devices for which the microscope waits before an image is made.
	 * If devices is null, the microscope will not wait for any device.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @param devices Devices to which the imaging process should synchronize, or null.
	 * @throws SettingException
	 * @throws MicroscopeLockedException 
	 */
	public void setImageSynchronizationDevices(String[] devices, int accessID) throws SettingException, MicroscopeLockedException;
	
	/**
	 * Adds the given device to the list of image synchronization devices.
	 * @param device Device to add to the image synchronization list.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws SettingException
	 * @throws MicroscopeLockedException
	 */
	public void addImageSynchronizationDevice(String device, int accessID) throws SettingException, MicroscopeLockedException;
	/**
	 * Removes the given device to the list of image synchronization devices.
	 * @param device Device to remove from the image synchronization list.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws SettingException
	 * @throws MicroscopeLockedException
	 */
	public void removeImageSynchronizationDevice(String device, int accessID) throws SettingException, MicroscopeLockedException;
	/**
	 * Returns the devices to which the microscope synchronizes when imaging.
	 * @return List of synchronizing devices.
	 */
	public String[] getImageSynchronizationDevices();
	
	/**
	 * Returns the settings which will be applied when the system starts up.
	 * @return Settings applied at startup.
	 */
	public DeviceSetting[] getSystemStartupSettings();
	/**
	 * Set the setting which will be applied at startup. If settings == null, no settings will be applied.
	 * @param settings Settings which will be applied at startup.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws SettingException 
	 * @throws MicroscopeLockedException 
	 */
	public void setSystemStartupSettings(DeviceSetting[] settings, int accessID) throws SettingException, MicroscopeLockedException;
	/**
	 * Adds a setting to the list of settings which will be applied at startup. If the value of the device property
	 * is already defined by another setting, this overwrites the old setting.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @param setting A setting which should be applied at startup.
	 * @throws SettingException 
	 * @throws MicroscopeLockedException 
	 */
	public void addSystemStartupSetting(DeviceSetting setting, int accessID) throws SettingException, MicroscopeLockedException;
	
	/**
	 * Returns the settings which will be applied when the system shuts down.
	 * @return Settings applied at shutdown.
	 */
	public DeviceSetting[] getSystemShutdownSettings();
	/**
	 * Set the setting which will be applied at shutdown. If settings == null, no settings will be applied.
	 * @param settings Settings which will be applied at shutdown.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws SettingException 
	 * @throws MicroscopeLockedException 
	 */
	public void setSystemShutdownSettings(DeviceSetting[] settings, int accessID) throws SettingException, MicroscopeLockedException;
	/**
	 * Adds a setting to the list of settings which will be applied at shutdown. If the value of the device property
	 * is already defined by another setting, this overwrites the old setting.
	 * @param setting A setting which should be applied at shutdown.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws SettingException 
	 * @throws MicroscopeLockedException 
	 */
	public void addSystemShutdownSetting(DeviceSetting setting, int accessID) throws SettingException, MicroscopeLockedException;
	
	/**
	 * Gets the timeout between in the communication with the devices.
	 * @return Timeout in ms.
	 */
	public int getCommunicationTimeout();
	
	/**
	 * Sets the timeout between in the communication with the devices.
	 * @param timeout Timeout in ms.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws SettingException
	 * @throws MicroscopeLockedException 
	 */
	public void setCommunicationTimeout(int timeout, int accessID) throws SettingException, MicroscopeLockedException;
	
	/**
	 * Sets the size of an internally used image buffer.
	 * Note that this function might not be used for any implementation.
	 * @param sizeMB Size of the image buffer, in MB.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws SettingException
	 * @throws MicroscopeLockedException
	 * @throws UnsupportedOperationException Thrown if no image buffer is supported by this implementation.
	 */
	public void setImageBufferSize(int sizeMB, int accessID) throws SettingException, MicroscopeLockedException, UnsupportedOperationException;
	
	/**
	 * Returns the size of the internally used image buffer.
	 * Note that this function might not be used for any implementation.
	 * Returns -1 if the image buffer size was not set, yet, or if a default value is used.
	 * @return Size in MB.
	 * @throws UnsupportedOperationException Thrown if no image buffer is supported by this implementation.
	 */
	public int getImageBufferSize() throws UnsupportedOperationException;
	
	/**
	 * Gets the wait time used when pinging a device if a certain action is finished.
	 * @return Ping period in ms.
	 */
	public int getCommunicationPingPeriod();
	
	/**
	 * Sets the wait time used when pinging a device if a certain action is finished.
	 * @param pingPeriod Ping period in ms.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws SettingException
	 * @throws MicroscopeLockedException 
	 */
	public void setCommunicationPingPeriod(int pingPeriod, int accessID) throws SettingException, MicroscopeLockedException;
	
}
