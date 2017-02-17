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

import org.youscope.common.MessageListener;
import org.youscope.common.util.RMIReader;
import org.youscope.common.util.RMIWriter;

/**
 * This interface represents the microscope and allows access to its devices, its configuration and
 * its camera.
 * 
 * @author Moritz Lang
 */
public interface Microscope extends Remote
{
	/**
	 * Returns a class with which the device driver settings of the microscope can be modified.
	 * @return Interface to modify device driver settings.
	 * @throws UnsupportedOperationException If modifying device driver settings is not supported.
	 * @throws RemoteException
	 */
	public DeviceLoader getDeviceLoader() throws UnsupportedOperationException, RemoteException;

	/**
	 * Returns an interface with which general configuration settings of the microscope can be changed.
	 * @return General configuration settings.
	 * @throws RemoteException
	 */
	MicroscopeConfiguration getMicroscopeConfiguration() throws RemoteException;

	/**
	 * Returns a list of all devices of this microscope, or an empty list if this microscope does not have any devices.
	 * @return List of all devices.
	 * @throws RemoteException
	 */
	public Device[] getDevices() throws RemoteException;

	/**
	 * Returns a list of all devices of the given type, or an empty list if no such device is configured.
	 * @param type Type of the devices to query for.
	 * @return List of all devices of the given type.
	 * @throws RemoteException
	 */
	public Device[] getDevices(DeviceType type) throws RemoteException;

	/**
	 * Returns the device of this microscope with the given name.
	 * @param name Name of the device.
	 * @return Device with the given name.
	 * @throws DeviceException Thrown if the given device could not be found.
	 * @throws RemoteException
	 */
	public Device getDevice(String name) throws DeviceException, RemoteException;

	/**
	 * Returns the current default auto-focus device.
	 * 
	 * @return Current auto-focus device.
	 * @throws DeviceException
	 * @throws RemoteException
	 */
	public AutoFocusDevice getAutoFocusDevice() throws DeviceException, RemoteException;

	/**
	 * Returns a list of all installed auto-focus devices.
	 * 
	 * @return List of all installed auto-focus devices.
	 * @throws RemoteException
	 */
	public AutoFocusDevice[] getAutoFocusDevices() throws RemoteException;

	/**
	 * Returns the auto-focus device with the given name.
	 * @param deviceID ID of the auto-focus device.
	 * 
	 * @return The auto-focus device with the given ID.
	 * @throws DeviceException
	 * @throws RemoteException
	 */
	public AutoFocusDevice getAutoFocusDevice(String deviceID) throws DeviceException, RemoteException;

	/**
	 * Returns the current default focus device.
	 * 
	 * @return Focus device.
	 * @throws DeviceException
	 * @throws RemoteException
	 */
	public FocusDevice getFocusDevice() throws DeviceException, RemoteException;

	/**
	 * Returns a list of all installed focus devices.
	 * 
	 * @return List of all installed focus devices.
	 * @throws RemoteException
	 */
	public FocusDevice[] getFocusDevices() throws RemoteException;

	/**
	 * Returns the focus device with the given name.
	 * @param deviceID Name of focus device.
	 * 
	 * @return Focus device.
	 * @throws DeviceException
	 * @throws RemoteException
	 */
	public FocusDevice getFocusDevice(String deviceID) throws DeviceException, RemoteException;

	/**
	 * Sets the default focus device.
	 * @param deviceID Name of focus device.
	 * 
	 * @throws DeviceException
	 * @throws MicroscopeLockedException
	 * @throws RemoteException
	 */
	public void setFocusDevice(String deviceID) throws DeviceException, MicroscopeLockedException, RemoteException;

	/**
	 * Sets the default auto-focus device.
	 * @param deviceID Name of auto-focus device.
	 * 
	 * @throws DeviceException
	 * @throws MicroscopeLockedException
	 * @throws RemoteException
	 */
	public void setAutoFocusDevice(String deviceID) throws DeviceException, MicroscopeLockedException, RemoteException;

	/**
	 * Sets the default shutter device.
	 * @param deviceID Name of shutter device.
	 * 
	 * @throws DeviceException
	 * @throws MicroscopeLockedException
	 * @throws RemoteException
	 */
	public void setShutterDevice(String deviceID) throws DeviceException, MicroscopeLockedException, RemoteException;

	/**
	 * Returns the current default shutter device.
	 * 
	 * @return shutter device.
	 * @throws DeviceException
	 * @throws RemoteException
	 */
	public ShutterDevice getShutterDevice() throws DeviceException, RemoteException;

	/**
	 * Returns a list of all installed shutter devices.
	 * 
	 * @return List of all installed shutter devices.
	 * @throws RemoteException
	 */
	public ShutterDevice[] getShutterDevices() throws RemoteException;

	/**
	 * Returns the shutter device with the given name.
	 * @param deviceID Name of shutter device.
	 * 
	 * @return shutter device.
	 * @throws DeviceException
	 * @throws RemoteException
	 */
	public ShutterDevice getShutterDevice(String deviceID) throws DeviceException, RemoteException;

	/**
	 * Returns the current default stage device.
	 * 
	 * @return Stage device.
	 * @throws DeviceException
	 * @throws RemoteException
	 */
	public StageDevice getStageDevice() throws DeviceException, RemoteException;

	/**
	 * Returns all installed stage devices.
	 * 
	 * @return List of all stage devices.
	 * @throws RemoteException
	 */
	public StageDevice[] getStageDevices() throws RemoteException;

	/**
	 * Returns the stage device with the given name.
	 * @param deviceID Name of stage device.
	 * 
	 * @return Stage device.
	 * @throws DeviceException
	 * @throws RemoteException
	 */
	public StageDevice getStageDevice(String deviceID) throws DeviceException, RemoteException;

	/**
	 * Sets the default stage device.
	 * @param deviceID Name of stage device.
	 * 
	 * @throws DeviceException
	 * @throws MicroscopeLockedException
	 * @throws RemoteException
	 */
	public void setStageDevice(String deviceID) throws DeviceException, MicroscopeLockedException, RemoteException;

	/**
	 * Returns the current default camera device.
	 * 
	 * @return Device of the camera.
	 * @throws DeviceException
	 * @throws RemoteException
	 */
	CameraDevice getCameraDevice() throws DeviceException, RemoteException;

	/**
	 * Sets the current default camera device.
	 * 
	 * @param cameraDevice Name of the camera device which should be the default device.
	 * @throws DeviceException
	 * @throws MicroscopeLockedException
	 * @throws RemoteException
	 */
	void setCameraDevice(String cameraDevice) throws DeviceException, MicroscopeLockedException, RemoteException;

	/**
	 * Returns all currently installed camera devices.
	 * 
	 * @return All installed camera devices.
	 * @throws RemoteException
	 */
	CameraDevice[] getCameraDevices() throws RemoteException;

	/**
	 * Returns the camera device with the given name.
	 * @param name Name of the camera device.
	 * 
	 * @return Device of the camera.
	 * @throws DeviceException
	 * @throws RemoteException
	 */
	CameraDevice getCameraDevice(String name) throws DeviceException, RemoteException;

	/**
	 * Returns an interface with which the current channel settings can be queried and a different channel can be set.
	 * @return the channel manager of the microscope.
	 * @throws RemoteException
	 */
	ChannelManager getChannelManager() throws RemoteException;

	/**
	 * Returns an interface with which the pixel size for different microscope settings can be queried and set.
	 * @return the pixel size manager of the microscope.
	 * @throws RemoteException
	 */
	PixelSizeManager getPixelSizeManager() throws RemoteException;

	/**
	 * Sets the given device property to the given value.
	 * 
	 * @param setting Structure containing device, property and value configuration.
	 * @throws SettingException
	 * @throws MicroscopeLockedException
	 * @throws RemoteException
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 */
	void applyDeviceSetting(DeviceSetting setting) throws SettingException, MicroscopeLockedException, RemoteException, MicroscopeException, InterruptedException;

	/**
	 * Sets the given device properties to the given values. Same as calling
	 * setDeviceSetting for every element in the array, except that the
	 * microscope is locked during the whole procedure, thus either all or none
	 * of the settings is set.
	 * 
	 * @param settings
	 *            Array of structures containing device, property and value
	 *            configurations.
	 * @throws SettingException
	 * @throws RemoteException
	 * @throws MicroscopeLockedException
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 */
	void applyDeviceSettings(DeviceSetting[] settings) throws SettingException, RemoteException, MicroscopeLockedException, MicroscopeException, InterruptedException;

	/**
	 * Sets the given device property to the given value.
	 * Does not synchronize to the devices afterwards (i.e. does not wait for the actions to be finished).
	 * 
	 * @param setting Structure containing device, property and value configuration.
	 * @throws SettingException
	 * @throws MicroscopeLockedException
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 * @throws RemoteException
	 */
	void applyDeviceSettingAsync(DeviceSetting setting) throws SettingException, MicroscopeLockedException, MicroscopeException, InterruptedException, RemoteException;

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
	 * @throws SettingException
	 * @throws MicroscopeLockedException
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 * @throws RemoteException
	 */
	void applyDeviceSettingsAsync(DeviceSetting[] settings) throws SettingException, MicroscopeLockedException, MicroscopeException, InterruptedException, RemoteException;

	/**
	 * Locks the microscope for a longer time (e.g. for the duration of a
	 * measurement), such that only the locker may acquire the write lock to the
	 * microscope. However, read locks may be acquired, as long as the thread is
	 * not actually acquiring the write-lock... Must be surrounded by a
	 * try-finally block with unlockExclusiveWrite in the finally statement.
	 * 
	 * @throws MicroscopeLockedException
	 *             Thrown if microscope is already locked for a longer time by
	 *             somebody else.
	 * @throws RemoteException
	 */
	void lockExclusiveWrite() throws MicroscopeLockedException, RemoteException;

	/**
	 * Unlocks the previously acquired lock.
	 * @throws MicroscopeLockedException Thrown if this microscope object is not the owner of the exclusive write lock.
	 * @throws RemoteException
	 */
	void unlockExclusiveWrite() throws MicroscopeLockedException, RemoteException;

	/**
	 * Initializes the microscope with the configuration read from the reader (e.g. a FileReader to load a configuration file).
	 * If the configuration contained errors, a MicroscopeConfigurationException is thrown.
	 * If the configuration could be loaded, there still exists the possibility that YouScope believes that this configuration is
	 * not complete, belongs to a different version or was created by a different program, which all give a hint that the configuration process should be rerun.
	 * In this case a short message is passed as
	 * return value, giving a hint how should be proceeded, and which should be displayed to the user. If the configuration is believed to
	 * be complete and compatible, null is returned.
	 * 
	 * @param configurationReader A reader to read in the configuration, e.g. a file reader.
	 * @return A message describing why this configuration may be defective, or null, if this configuration is believed to be not defective.
	 * @throws MicroscopeConfigurationException
	 * @throws MicroscopeLockedException
	 * @throws RemoteException
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 */
	String loadConfiguration(RMIReader configurationReader) throws MicroscopeConfigurationException, MicroscopeLockedException, RemoteException, InterruptedException, MicroscopeException;

	/**
	 * Writes the current microscope configuration to the supplied writer (e.g. a file writer).
	 * @param configurationWriter The writer to which the configuration should be written.
	 * @throws MicroscopeConfigurationException Thrown if the configuration could not be generated.
	 * @throws RemoteException
	 * @throws MicroscopeLockedException
	 */
	public void saveConfiguration(RMIWriter configurationWriter) throws MicroscopeConfigurationException, RemoteException, MicroscopeLockedException;

	/**
	 * Returns the warning message generated and returned when loaded the last configuration, or null if no warning appeared.
	 * See return value of loadConfigFile(...);
	 * @return Last warning message when loading a configuration.
	 * @throws RemoteException
	 */
	String getLastConfigurationWarning() throws RemoteException;

	/**
	 * Uninitializes the microscope. All loaded devices are unload.
	 * 
	 * @throws MicroscopeException
	 * @throws MicroscopeLockedException
	 * @throws RemoteException
	 * @throws InterruptedException
	 */
	void uninitializeMicroscope() throws MicroscopeException, MicroscopeLockedException, RemoteException, InterruptedException;

	/**
	 * Stops the XY stage of the microscope and prevents ASAP any other thread
	 * to obtain access to the microscope.
	 * @throws RemoteException
	 */
	void emergencyStop() throws RemoteException;

	/**
	 * Resets the emergency-stop state, such that microscope can be accessed
	 * again.
	 * @throws RemoteException
	 */
	void resetEmergencyStop() throws RemoteException;

	/**
	 * Returns true if microscope is currently in the emergency-stop state.
	 * 
	 * @return TRUE if emergency-stopped.
	 * @throws RemoteException
	 */
	boolean isEmergencyStopped() throws RemoteException;

	/**
	 * Adds a listener which gets informed when the state of the microscope
	 * changes.
	 * 
	 * @param listener
	 *            The listener.
	 * @throws RemoteException
	 */
	void addMessageListener(MessageListener listener) throws RemoteException;

	/**
	 * Removes a previously added listener.
	 * 
	 * @param listener
	 *            The listener.
	 * @throws RemoteException
	 */
	void removeMessageListener(MessageListener listener) throws RemoteException;

	/**
	 * Returns the state device with the given name.
	 * @param name Name of state device.
	 * 
	 * @return State device.
	 * @throws DeviceException
	 * @throws RemoteException
	 */
	public StateDevice getStateDevice(String name) throws DeviceException, RemoteException;

	/**
	 * Returns a list of all installed state devices.
	 * 
	 * @return List of all installed state devices.
	 * @throws RemoteException
	 */
	public StateDevice[] getStateDevices() throws RemoteException;

	/**
	 * Adds a listener which gets informed if the configuration of the microscope changes.
	 * @param listener Listener to add.
	 * @throws RemoteException
	 */
	public void addConfigurationListener(MicroscopeConfigurationListener listener) throws RemoteException;

	/**
	 * Removes a previously added listener
	 * @param listener Listener to remove.
	 * @throws RemoteException
	 */
	public void removeConfigurationListener(MicroscopeConfigurationListener listener) throws RemoteException;
}
