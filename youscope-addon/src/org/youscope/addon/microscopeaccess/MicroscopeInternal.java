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

import org.youscope.common.MessageListener;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.microscope.DeviceType;
import org.youscope.common.microscope.MicroscopeConfigurationListener;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.SettingException;

/**
 * Internal interface of the microscope. Non-remote, and all functions changing the microscope state have to be provided with the access ID.
 * @author langmo
 */
public interface MicroscopeInternal
{
	/**
	 * Returns a class with which the device driver settings of the microscope can be modified.
	 * @return Interface to modify device driver settings.
	 * @throws UnsupportedOperationException If modifying device driver settings is not supported.
	 */
	public DeviceLoaderInternal getDeviceLoader() throws UnsupportedOperationException;
	
	/**
	 * Returns an interface with which general configuration settings of the microscope can be changed.
	 * @return General configuration settings.
	 */
	MicroscopeConfigurationInternal getMicroscopeConfiguration();
	
	/**
	 * Returns a list of all devices of this microscope, or an empty list if this microscope does not have any devices.
	 * @return List of all devices.
	 */
	public DeviceInternal[] getDevices();

	/**
	 * Returns a list of all devices of the given type, or an empty list if no such device is configured.
	 * @param type Type of the devices to query for.
	 * @return List of all devices of the given type.
	 */
	public DeviceInternal[] getDevices(DeviceType type);

	/**
	 * Returns the device of this microscope with the given name.
	 * @param name Name of the device.
	 * @return Device with the given name.
	 * @throws DeviceException Thrown if the given device could not be found.
	 */
	public DeviceInternal getDevice(String name) throws DeviceException;

	/**
	 * Returns the current default focus device.
	 * 
	 * @return Focus device.
	 * @throws DeviceException
	 */
	public FocusDeviceInternal getFocusDevice() throws DeviceException;
	
	/**
	 * Returns a list of all installed focus devices.
	 * 
	 * @return List of all installed focus devices.
	 */
	public FocusDeviceInternal[] getFocusDevices();

	/**
	 * Returns the focus device with the given name.
	 * @param deviceID Name of focus device.
	 * 
	 * @return Focus device.
	 * @throws DeviceException
	 */
	public FocusDeviceInternal getFocusDevice(String deviceID) throws DeviceException;
	
	/**
	 * Returns the current default auto-focus device.
	 * 
	 * @return Current auto-focus device.
	 * @throws DeviceException
	 */
	public AutoFocusDeviceInternal getAutoFocusDevice() throws DeviceException;
	
	/**
	 * Returns a list of all installed auto-focus devices.
	 * 
	 * @return List of all installed auto-focus devices.
	 */
	public AutoFocusDeviceInternal[] getAutoFocusDevices();

	/**
	 * Returns the auto-focus device with the given name.
	 * @param deviceID ID of the auto-focus device.
	 * 
	 * @return The auto-focus device with the given ID.
	 * @throws DeviceException
	 */
	public AutoFocusDeviceInternal getAutoFocusDevice(String deviceID) throws DeviceException;
	
	
	/**
	 * Sets the default focus device.
	 * @param deviceID Name of focus device.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * 
	 * @throws DeviceException
	 * @throws MicroscopeLockedException 
	 */
	public void setFocusDevice(String deviceID, int accessID) throws DeviceException, MicroscopeLockedException;
	
	/**
	 * Sets the default auto-focus device.
	 * @param deviceID Name of auto-focus device.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * 
	 * @throws DeviceException
	 * @throws MicroscopeLockedException 
	 */
	public void setAutoFocusDevice(String deviceID, int accessID) throws DeviceException, MicroscopeLockedException;
	
	/**
	 * Sets the default shutter device.
	 * @param deviceID Name of shutter device.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * 
	 * @throws DeviceException
	 * @throws MicroscopeLockedException 
	 */
	public void setShutterDevice(String deviceID, int accessID) throws DeviceException, MicroscopeLockedException;
	
	/**
	 * Returns the current default shutter device.
	 * 
	 * @return shutter device.
	 * @throws DeviceException
	 */
	public ShutterDeviceInternal getShutterDevice() throws DeviceException;
	
	/**
	 * Returns a list of all installed shutter devices.
	 * 
	 * @return List of all installed shutter devices.
	 */
	public ShutterDeviceInternal[] getShutterDevices();

	/**
	 * Returns the shutter device with the given name.
	 * @param deviceID Name of shutter device.
	 * 
	 * @return shutter device.
	 * @throws DeviceException
	 */
	public ShutterDeviceInternal getShutterDevice(String deviceID) throws DeviceException;

	/**
	 * Returns the current default stage device.
	 * 
	 * @return Stage device.
	 * @throws DeviceException
	 */
	public StageDeviceInternal getStageDevice() throws DeviceException;
	
	/**
	 * Returns all installed stage devices.
	 * 
	 * @return List of all stage devices.
	 */
	public StageDeviceInternal[] getStageDevices();

	/**
	 * Returns the stage device with the given name.
	 * @param deviceID Name of stage device.
	 * 
	 * @return Stage device.
	 * @throws DeviceException
	 */
	public StageDeviceInternal getStageDevice(String deviceID) throws DeviceException;
	
	/**
	 * Sets the default stage device.
	 * @param deviceID Name of stage device.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * 
	 * @throws DeviceException
	 * @throws MicroscopeLockedException 
	 */
	public void setStageDevice(String deviceID, int accessID) throws DeviceException, MicroscopeLockedException;

	/**
	 * Returns the current default camera device.
	 * 
	 * @return Device of the camera.
	 * @throws DeviceException
	 */
	CameraDeviceInternal getCameraDevice() throws DeviceException;
	
	/**
	 * Sets the current default camera device.
	 * 
	 * @param cameraDevice Name of the camera device which should be the default device.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws DeviceException
	 * @throws MicroscopeLockedException 
	 */
	void setCameraDevice(String cameraDevice, int accessID) throws DeviceException, MicroscopeLockedException;
	
	/**
	 * Returns all currently installed camera devices.
	 * 
	 * @return All installed camera devices.
	 */
	CameraDeviceInternal[] getCameraDevices();

	/**
	 * Returns the camera device with the given name.
	 * @param name Name of the camera device.
	 * 
	 * @return Device of the camera.
	 * @throws DeviceException
	 */
	CameraDeviceInternal getCameraDevice(String name) throws DeviceException;

	/**
	 * Sets the given device property to the given value.
	 * Does synchronize to the devices afterwards.
	 * 
	 * @param setting Structure containing device, property and value configuration.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws SettingException 
	 * @throws MicroscopeLockedException
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 */
	void applyDeviceSetting(DeviceSetting setting, int accessID) throws SettingException, MicroscopeLockedException, MicroscopeException, InterruptedException;

	/**
	 * Sets the given device properties to the given values. Same as calling
	 * setDeviceSetting for every element in the array, except that the
	 * microscope is locked during the whole procedure, thus either all or none
	 * of the settings is set.
	 * Does synchronize to the devices afterwards.
	 * 
	 * @param settings
	 *            Array of structures containing device, property and value
	 *            configurations.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws SettingException
	 * @throws MicroscopeLockedException
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 */
	void applyDeviceSettings(DeviceSetting[] settings, int accessID) throws SettingException, MicroscopeLockedException, MicroscopeException, InterruptedException;
	
	/**
	 * Sets the given device property to the given value.
	 * Does not synchronize to the devices afterwards (i.e. does not wait for the actions to be finished).
	 * 
	 * @param setting Structure containing device, property and value configuration.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws SettingException 
	 * @throws MicroscopeLockedException
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 */
	void applyDeviceSettingAsync(DeviceSetting setting, int accessID) throws SettingException, MicroscopeLockedException, MicroscopeException, InterruptedException;

	/**
	 * Sets the given device properties to the given values. Same as calling
	 * setDeviceSetting for every element in the array, except that the
	 * microscope is locked during the whole procedure, thus either all or none
	 * of the settings is set.
	 * Does not synchronize to the devices afterwards (i.e. does not wait for the actions to be finished).
	 * 
	 * @param settings
	 *            Array of structures containing device, property and value
	 *            configurations.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws SettingException
	 * @throws MicroscopeLockedException
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 */
	void applyDeviceSettingsAsync(DeviceSetting[] settings, int accessID) throws SettingException, MicroscopeLockedException, MicroscopeException, InterruptedException;

	/**
	 * Locks the microscope for a longer time (e.g. for the duration of a
	 * measurement), such that only the locker may acquire the write lock to the
	 * microscope. However, read locks may be acquired, as long as the thread is
	 * not actually acquiring the write-lock... Must be surrounded by a
	 * try-finally block with unlockExclusiveWrite in the finally statement.
	 * 
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeLockedException
	 *             Thrown if microscope is already locked for a longer time by
	 *             somebody else.
	 */
	void lockExclusiveWrite(int accessID) throws MicroscopeLockedException;

	/**
	 * Unlocks the previously acquired lock.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID or not locked at all, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeLockedException Thrown if this microscope object is not the owner of the exclusive write lock.
	 */
	void unlockExclusiveWrite(int accessID) throws MicroscopeLockedException;
	
	/**
	 * Initializes the microscope.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeException
	 * @throws MicroscopeLockedException
	 * @throws InterruptedException
	 */
	void initializeMicroscope(int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException;
	
	/**
	 * Uninitializes the microscope. All loaded devices are unload.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * 
	 * @throws MicroscopeException
	 * @throws MicroscopeLockedException
	 * @throws InterruptedException 
	 */
	void uninitializeMicroscope(int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException;

	/**
	 * Stops the XY stage of the microscope and prevents ASAP any other thread
	 * to obtain access to the microscope.
	 */
	void emergencyStop();

	/**
	 * Resets the emergency-stop state, such that microscope can be accessed
	 * again.
	 */
	void resetEmergencyStop();

	/**
	 * Returns true if microscope is currently in the emergency-stop state.
	 * 
	 * @return TRUE if emergency-stopped.
	 */
	boolean isEmergencyStopped();

	/**
	 * Adds a listener which gets informed when the state of the microscope
	 * changes.
	 * 
	 * @param listener
	 *            The listener.
	 */
	void addMessageListener(MessageListener listener);

	/**
	 * Removes a previously added listener.
	 * 
	 * @param listener
	 *            The listener.
	 */
	void removeMessageListener(MessageListener listener);
	
	/**
	 * Returns the state device with the given name.
	 * @param name Name of state device.
	 * 
	 * @return State device.
	 * @throws DeviceException
	 */
	public StateDeviceInternal getStateDevice(String name) throws DeviceException;

	/**
	 * Returns a list of all installed state devices.
	 * 
	 * @return List of all installed state devices.
	 */
	public StateDeviceInternal[] getStateDevices();
	
	/**
	 * Adds a listener which gets informed if the configuration of the microscope changes.
	 * @param listener Listener to add.
	 */
	public void addConfigurationListener(MicroscopeConfigurationListener listener);

	/**
	 * Removes a previously added listener
	 * @param listener Listener to remove.
	 */
	public void removeConfigurationListener(MicroscopeConfigurationListener listener);
}
