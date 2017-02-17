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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.youscope.addon.microscopeaccess.AutoFocusDeviceInternal;
import org.youscope.addon.microscopeaccess.CameraDeviceInternal;
import org.youscope.addon.microscopeaccess.DeviceInternal;
import org.youscope.addon.microscopeaccess.DeviceLoaderInternal;
import org.youscope.addon.microscopeaccess.FloatPropertyInternal;
import org.youscope.addon.microscopeaccess.FocusDeviceInternal;
import org.youscope.addon.microscopeaccess.IntegerPropertyInternal;
import org.youscope.addon.microscopeaccess.MicroscopeInternal;
import org.youscope.addon.microscopeaccess.PropertyInternal;
import org.youscope.addon.microscopeaccess.ShutterDeviceInternal;
import org.youscope.addon.microscopeaccess.StageDeviceInternal;
import org.youscope.addon.microscopeaccess.StateDeviceInternal;
import org.youscope.common.MessageListener;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.microscope.DeviceType;
import org.youscope.common.microscope.MicroscopeConfigurationListener;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.PropertyType;
import org.youscope.common.microscope.SettingException;

import mmcorej.CMMCore;

/**
 * @author Moritz Lang
 */
class MicroscopeImpl implements MicroscopeInternal
{
	private final CMMCore								core;

	private final String driverFolder;
	
	private volatile boolean					emergencyStopped				= false;

	/**
	 * The accessID which currently has the exclusive write access to the microscope, or -1 if access is currently not
	 * exclusively locked.
	 */
	private volatile int									currentLockingAccessID			= -1;

	/**
	 * The number of times the currently locking entity (see currentLockingAccessID) has locked the microscope exclusively.
	 * The number is increased by one every time lockExclusiveWrite is called and decreased by one every time unlockExclusiveWrite is called.
	 * If currentLockingNumLocks drops to zero, the microscope is unlocked.
	 */
	private volatile int									currentLockingNumLocks			= 0;

	/**
	 * Lock that prevents reading and writing to the microscope at the same
	 * time. Both locks (read and write) should be acquired only for a short
	 * period of time.
	 */
	private final ReentrantReadWriteLock		shortTimeMicroscopeAccessLock	= new ReentrantReadWriteLock();

	private ArrayList<MessageListener>	listeners						= new ArrayList<MessageListener>();
	
	private final MicroscopeConfigurationImpl microscopeConfiguration;
	
	private final Hashtable<String, DeviceImpl> devices = new Hashtable<String, DeviceImpl>();
		
	private String standardFocusDevice = null;
	
	private String standardCameraDevice = null;
	
	private String standardShutterDevice = null;
	
	private String standardStageDevice = null;
	
	private String standardAutoFocusDevice = null;
	
	private final Vector<MicroscopeConfigurationListener> microscopeConfigurationListeners = new Vector<MicroscopeConfigurationListener>();
	
	public MicroscopeImpl(CMMCore core, String driverFolder)
	{
		this.core = core;
		this.driverFolder = driverFolder;
		microscopeConfiguration = new MicroscopeConfigurationImpl(this);
		
		addConfigurationListener(microscopeConfiguration);
	}	
	
	@Override
	public void initializeMicroscope(int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException
	{
		try
		{
			CMMCore core = startWrite(accessID);
			
			// Deactivate MicroManager auto-shutter. We do it on our own...
			core.setAutoShutter(false);
						
			// initialize image gatherer
			ImageGatherer.initialize(this, core);
		}
		finally
		{
			unlockWrite();
		}
	}
	
	@Override
	public void uninitializeMicroscope(int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException
	{
		// stop getting images.
		ImageGatherer.uninitialize();
		
		// Set the system shutdown settings.
		stateChanged("Applying shutdown settings...");
		try
		{
			applyDeviceSettings(microscopeConfiguration.getSystemShutdownSettings(), accessID);
		}
		catch(SettingException e)
		{
			throw new MicroscopeException("Could not apply shutdown settings.", e);
		}
		stateChanged("Applied shutdown settings.");
		
		stateChanged("Uninitializing microscope...");
		
		try
		{
			// Do the uninitialization.
			CMMCore core = startWrite(accessID);
			core.unloadAllDevices();
			
			// Remove all references
			microscopeUninitialized();
		}
		catch(MicroscopeLockedException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not unload all devices. Microscope might be in an invalid state. Please restart YouScope.", e);
		}
		finally
		{
			unlockWrite();
		}
		
		stateChanged("Microscope uninitialized.");
	}


	void lockWrite(int accessID) throws MicroscopeLockedException
	{
		shortTimeMicroscopeAccessLock.writeLock().lock();
		if(currentLockingAccessID != -1 && currentLockingAccessID != accessID)
		{
			// Exclusively locked by other entity.
			throw new MicroscopeLockedException("Microscope is locked and, thus, its state can not be modified (locked by ID: " + currentLockingAccessID +", current ID: " + accessID + ")");
		}
	}

	void lockRead()
	{
		shortTimeMicroscopeAccessLock.readLock().lock();
	}


	void unlockWrite()
	{
		shortTimeMicroscopeAccessLock.writeLock().unlock();
	}

	void unlockRead()
	{
		shortTimeMicroscopeAccessLock.readLock().unlock();
	}

	/**
	 * Should be used only by methods which do not change the state of the
	 * microscope. Must be surrounded by a try-finally block with the function
	 * endRead in the finally statement. The CMMCore object must never be used
	 * outside of this block nor stored locally.
	 */
	CMMCore startRead() throws MicroscopeException
	{
		lockRead();
		synchronized(this)
		{
			if(emergencyStopped)
				throw new MicroscopeException("Microscope is in the emergency-stop state. Until emergency state is not manually reset, no operation is allowed.");
			return core;
		}
	}

	/**
	 * Should be used by methods which (may) change the state of the microscope.
	 * Must be surrounded by a try-finally block with the function endWrite in
	 * the finally statement. The CMMCore object must never be used outside of
	 * this block nor stored locally. This function may throw an exception if
	 * the microscope is locked for writing. However, even than the finally
	 * clause with the endWrite has to be called.
	 */
	CMMCore startWrite(int accessID) throws MicroscopeLockedException
	{
		lockWrite(accessID);
		synchronized(this)
		{
			if(emergencyStopped)
				throw new MicroscopeLockedException("Microscope is in the emergency-stop state. Until emergency state is not manually reset, no operation is allowed.");
			return core;
		}
	}

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
	 */
	@Override
	public void lockExclusiveWrite(int accessID) throws MicroscopeLockedException
	{
		try
		{
			// Make sure that no other thread has writing access before locking
			// the microscope
			shortTimeMicroscopeAccessLock.writeLock().lock();
			if(currentLockingAccessID == -1 && accessID == -1)
				return;
			else if(currentLockingAccessID == -1 || currentLockingAccessID == accessID)
			{
				// Lock the microscope.
				currentLockingAccessID = accessID;
				currentLockingNumLocks++;
			}
			else
			{
				// Microscope already locked.
				throw new MicroscopeLockedException("Microscope cannot be locked since it is already locked by another thread (locked by ID: " + currentLockingAccessID +", current ID: " + accessID + ")");
			}
		}
		finally
		{
			shortTimeMicroscopeAccessLock.writeLock().unlock();
		}
	}

	@Override
	public void unlockExclusiveWrite(int accessID) throws MicroscopeLockedException
	{
		try
		{
			// Make sure that no other thread has writing access before unlocking the microscope
			shortTimeMicroscopeAccessLock.writeLock().lock();
			if(currentLockingAccessID == -1 && accessID == -1)
				return;
			else if(currentLockingAccessID == -1)
			{
				// Microscope is not locked by this entity.
				throw new MicroscopeLockedException("Microscope cannot be unlocked since it is not locked (current ID: " + accessID + ")");
			}
			else if(currentLockingAccessID != accessID)
			{
				throw new MicroscopeLockedException("Microscope cannot be unlocked since it is locked by another thread (locked by ID: " + currentLockingAccessID +", current ID: " + accessID + ")");
			}
			else
			{
				// Microscope locked by this entity.
				currentLockingNumLocks--;
				if(currentLockingNumLocks <= 0)
				{
					currentLockingNumLocks = 0;
					currentLockingAccessID = -1;
				}
			}
		}
		finally
		{
			shortTimeMicroscopeAccessLock.writeLock().unlock();
		}
	}

	@Override
	public void addMessageListener(MessageListener listener)
	{
		synchronized(listeners)
		{
			listeners.add(listener);
		}
	}

	@Override
	public void removeMessageListener(MessageListener listener)
	{
		synchronized(listeners)
		{
			listeners.remove(listener);
		}
	}

	void stateChanged(String description)
	{
		synchronized(listeners)
		{
			for(int i = 0; i < listeners.size(); i++)
			{
				MessageListener listener = listeners.get(i);
				try
				{
					listener.sendMessage(description);
				}
				catch(@SuppressWarnings("unused") RemoteException e)
				{
					listeners.remove(i);
					i--;
				}
			}
		}
	}

	void errorOccured(String description, Throwable throwable)
	{
		synchronized(listeners)
		{
			for(int i = 0; i < listeners.size(); i++)
			{
				MessageListener listener = listeners.get(i);
				try
				{
					listener.sendErrorMessage(description, throwable);
				}
				catch(@SuppressWarnings("unused") RemoteException e)
				{
					listeners.remove(i);
					i--;
				}
			}
		}
	}

	@Override
	public void emergencyStop()
	{
		Exception xyStopException = null;
		synchronized(this)
		{
			emergencyStopped = true;

			try
			{
				core.stop(core.getXYStageDevice());
			}
			catch(Exception e)
			{
				xyStopException = e;
			}
		}
		if(xyStopException != null)
		{
			errorOccured("Could not stop XY-stage during emergency stop procedure.", xyStopException);
		}
		stateChanged("Microscope is set to the emergency-stop state. Until emergency state is not manually reset, no operation is allowed.");
	}

	@Override
	public synchronized void resetEmergencyStop()
	{
		emergencyStopped = false;
	}

	@Override
	public boolean isEmergencyStopped()
	{
		return emergencyStopped;
	}

	/**
	 * Checks if the provided setting is valid. If not, a setting exception is thrown.
	 * @param setting Setting to test.
	 * @param onlyAbsolute True if an exception should be thrown if the setting corresponds to relative values.
	 * @throws SettingException
	 */
	void isSettingValid(DeviceSetting setting, boolean onlyAbsolute) throws SettingException
	{
		if(setting.getDevice() == null || setting.getDevice().length() <= 0)
			throw new SettingException("Device name of device setting is null or empty.");
		if(setting.getProperty() == null || setting.getProperty().length() <= 0)
			throw new SettingException("Device property name of device setting for device " + setting.getDevice()+ " is null or empty.");
		if(onlyAbsolute && !setting.isAbsoluteValue())
			throw new SettingException("Device setting for device " + setting.getDevice()+ " is a relative setting, but only absolute ones are allowed.");
		if(setting.getDevice().equals("Core"))
			throw new SettingException("The MicroManager core is not a device in YouScope.");
		DeviceInternal device;
		try
		{
			device = getDevice(setting.getDevice());
		}
		catch(DeviceException e)
		{
			throw new SettingException("Device " + setting.getDevice() + " does not exist.", e);
		}
		
		PropertyInternal property;
		try
		{
			property = device.getProperty(setting.getProperty());
		}
		catch(DeviceException e)
		{
			throw new SettingException("Property " + setting.getProperty() + " of device " + setting.getDevice() + " does not exist.", e);
		}
		
		// Check if when setting is relative, it can be relative...
		PropertyType type = property.getType();
		if(setting.isAbsoluteValue() == false)
		{
			
			if(type != PropertyType.PROPERTY_FLOAT && type != PropertyType.PROPERTY_INTEGER)
			{
				throw new SettingException("Property " + setting.getProperty() + " of device " + setting.getDevice() + " cannot be set relatively, since it has neither integer nor double values.");
			}
		}
		
		// Check if correct type.
		if(type == PropertyType.PROPERTY_FLOAT)
		{
			try
			{
				setting.getFloatValue();			
			}
			catch(NumberFormatException e)
			{
				throw new SettingException("Property " + setting.getProperty() + " of device " + setting.getDevice() + " is not a float value.", e);
			}
		}
		else if(type == PropertyType.PROPERTY_INTEGER)
		{
			try
			{
				setting.getIntegerValue();			
			}
			catch(NumberFormatException e)
			{
				throw new SettingException("Property " + setting.getProperty() + " of device " + setting.getDevice() + " is not an integer value.", e);
			}
		}
	}
	
	/**
	 * Calls isSettingValid() for all settings.
	 * @param settings Settings to check if they are valid.
	 * @param onlyAbsolute True if an exception should be thrown if the setting corresponds to relative values.
	 * @throws SettingException
	 */
	void areSettingsValid(DeviceSetting[] settings, boolean onlyAbsolute) throws SettingException
	{
		for(DeviceSetting setting : settings)
		{
			isSettingValid(setting, onlyAbsolute);
		}
	}
	
	DeviceImpl initializeDevice(String deviceID, String libraryID, String driverID, int accessID) throws MicroscopeException
	{
		DeviceType deviceType = getDeviceType(deviceID);
		DeviceImpl device;
		switch(deviceType)
		{
			case CameraDevice:
				device = new CameraDeviceImpl(this, deviceID, libraryID, driverID);
				if(standardCameraDevice == null)
					standardCameraDevice = deviceID;
				break;
			case StageDevice:
				device = new FocusDeviceImpl(this, deviceID, libraryID, driverID);
				if(standardFocusDevice == null)
					standardFocusDevice = deviceID;
				break;
			case AutoFocusDevice:
				device = new AutoFocusDeviceImpl(this, deviceID, libraryID, driverID);
				if(standardAutoFocusDevice == null)
					standardAutoFocusDevice = deviceID;
				break;
			case XYStageDevice:
				device = new StageDeviceImpl(this, deviceID, libraryID, driverID);
				if(standardStageDevice == null)
					standardStageDevice = deviceID;
				break;
			case StateDevice:
				device = new StateDeviceImpl(this, deviceID, libraryID, driverID);
				break;
			case ShutterDevice:
				device = new ShutterDeviceImpl(this, deviceID, libraryID, driverID);
				if(standardShutterDevice == null)
					standardShutterDevice = deviceID;
				break;
			case SerialDevice:
				device = new SerialDeviceImpl(this, deviceID, libraryID, driverID);
				break;
			default:
				device = new DeviceImpl(this, deviceID, libraryID, driverID, deviceType);
				break;
		}
		device.initializeDevice(accessID);
		devices.put(deviceID, device);
		
		stateChanged("Device " + deviceID + " loaded (Driver: " + libraryID + "." + driverID + ").");
		return device;
	}
	
	
	@Override
	public DeviceImpl[] getDevices()
	{
		DeviceImpl[] retVal = devices.values().toArray(new DeviceImpl[0]);
		Arrays.sort(retVal);
		return retVal;
	}

	@Override
	public DeviceInternal[] getDevices(DeviceType type)
	{
		Vector<DeviceImpl> devicesofType = new Vector<DeviceImpl>();
		for(DeviceImpl device : devices.values())
		{
			if(device.getType().equals(type))
				devicesofType.addElement(device);
		}
		DeviceImpl[] retVal = devicesofType.toArray(new DeviceImpl[devicesofType.size()]);
		Arrays.sort(retVal);
		return retVal;
	}
	
	
	@Override
	public DeviceImpl getDevice(String name) throws DeviceException
	{
		if(name == null || name.length() <= 0)
			throw new DeviceException("Name not set or to short.");
		if(name.equals("Core"))
			throw new DeviceException("The microManager core is not a device in YouScope.");
		
		DeviceImpl device = devices.get(name);
		if(device == null)
			throw new DeviceException("Device " + name + " does not exist.");
		return device;
	}
	
	
	@Override
	public FocusDeviceInternal getFocusDevice() throws DeviceException
	{
		if(standardFocusDevice == null)
			throw new DeviceException("Standard focus device not set.");
		return getFocusDevice(standardFocusDevice);
	}
	
	@Override
	public AutoFocusDeviceInternal getAutoFocusDevice() throws DeviceException
	{
		if(standardAutoFocusDevice == null)
			throw new DeviceException("Standard auto-focus device not set.");
		return getAutoFocusDevice(standardAutoFocusDevice);
	}

	@Override
	public CameraDeviceInternal getCameraDevice() throws DeviceException
	{
		if(standardCameraDevice == null)
			throw new DeviceException("Standard camera device not set.");
		return getCameraDevice(standardCameraDevice);
	}

	@Override
	public FocusDeviceInternal getFocusDevice(String name) throws DeviceException
	{
		DeviceInternal device = getDevice(name);
		if(device instanceof FocusDeviceInternal)
			return (FocusDeviceInternal)device;
		throw new DeviceException("Device " + name + " is not a focus device.");
	}
	
	@Override
	public AutoFocusDeviceInternal getAutoFocusDevice(String name) throws DeviceException
	{
		DeviceInternal device = getDevice(name);
		if(device instanceof AutoFocusDeviceInternal)
			return (AutoFocusDeviceInternal)device;
		throw new DeviceException("Device " + name + " is not an auto-focus device.");
	}

	@Override
	public StageDeviceInternal getStageDevice() throws DeviceException
	{
		if(standardStageDevice == null)
			throw new DeviceException("Standard stage device not set.");
		return getStageDevice(standardStageDevice);
	}

	@Override
	public StageDeviceInternal getStageDevice(String name) throws DeviceException
	{
		DeviceInternal device = getDevice(name);
		if(device instanceof StageDeviceInternal)
			return (StageDeviceInternal)device;
		throw new DeviceException("Device " + name + " is not a stage device.");
	}
	
	@Override
	public CameraDeviceInternal getCameraDevice(String name) throws DeviceException
	{
		DeviceInternal device = getDevice(name);
		if(device instanceof CameraDeviceInternal)
			return (CameraDeviceInternal)device;
		throw new DeviceException("Device " + name + " is not a camera device.");
	}

	@Override
	public void applyDeviceSettingAsync(DeviceSetting setting, int accessID) throws MicroscopeLockedException, SettingException, InterruptedException, MicroscopeException
	{
		if(setting == null)
			throw new SettingException("Device setting is null pointer.");
		if(Thread.interrupted())
			throw new InterruptedException();
		try
		{
			lockWrite(accessID);
			isSettingValid(setting, false);
			
			PropertyInternal property = getDevice(setting.getDevice()).getProperty(setting.getProperty());
			if(setting.isAbsoluteValue() == false)
			{
				if(property instanceof FloatPropertyInternal)
				{
					((FloatPropertyInternal)property).setValueRelative(setting.getFloatValue(), accessID);
				}
				else if(property instanceof IntegerPropertyInternal)
				{
					((IntegerPropertyInternal)property).setValueRelative(setting.getIntegerValue(), accessID);
				}
				else
					throw new SettingException("Only integer and float properties can be set relatively.");
			}
			else
			{
				property.setValue(setting.getStringValue(), accessID);
			}
		}
		catch(DeviceException e)
		{
			throw new SettingException("Cannot find device property of setting.", e);
		}
		finally
		{
			unlockWrite();
		}
	}

	@Override
	public void applyDeviceSetting(DeviceSetting setting, int accessID) throws MicroscopeLockedException, SettingException, InterruptedException, MicroscopeException
	{
		if(setting == null)
			throw new SettingException("Device setting is null pointer.");
		try
		{
			lockWrite(accessID);
			applyDeviceSettingAsync(setting, accessID);
			if(Thread.interrupted())
				throw new InterruptedException();
			getDevice(setting.getDevice()).waitForDevice();
		}
		catch(DeviceException e)
		{
			throw new SettingException("Cannot find device property of setting.", e);
		}
		finally
		{
			unlockWrite();
		}
	}
	
	@Override
	public void applyDeviceSettingsAsync(DeviceSetting[] settings, int accessID) throws MicroscopeLockedException, MicroscopeException, InterruptedException, SettingException
	{
		if(Thread.interrupted())
			throw new InterruptedException();
		try
		{
			lockWrite(accessID);
			for(DeviceSetting setting : settings)
			{
				if(setting == null)
					throw new SettingException("Device setting is null pointer.");
				applyDeviceSettingAsync(setting, accessID);
			}
		}
		finally
		{
			unlockWrite();
		}
	}
	
	@Override
	public void applyDeviceSettings(DeviceSetting[] settings, int accessID) throws MicroscopeLockedException, MicroscopeException, InterruptedException, SettingException
	{
		// Synchronous way: apply each setting one by one.
		if(Thread.interrupted())
			throw new InterruptedException();
		try
		{
			lockWrite(accessID);
			for(DeviceSetting setting : settings)
			{
				if(setting == null)
					throw new SettingException("Device setting is null.");
				applyDeviceSetting(setting, accessID);
			}
		}
		finally
		{
			unlockWrite();
		}
		/*
		Alternative way: apply first all device settings, and then wait for devices.
		
		try
		{
			lockWrite(accessID);
			applyDeviceSettingsAsync(settings, accessID);
			Vector<String> alreadyWaitedFor = new Vector<String>();
			settingsIterator: for(DeviceSetting setting : settings)
			{
				String device = setting.getDevice();
				for(String waitedDevice : alreadyWaitedFor)
				{
					if(waitedDevice.compareTo(device) == 0)
					{
						continue settingsIterator;
					}
				}
				getDevice(device).waitForDevice();
				alreadyWaitedFor.addElement(device);
			}
		}
		catch(DeviceException e)
		{
			throw new SettingException("Cannot find device property of setting.", e);
		}
		finally
		{
			unlockWrite();
		}
		*/
	}

	@Override
	public DeviceLoaderInternal getDeviceLoader() throws UnsupportedOperationException
	{
		// Test if the microManager version is too old to support the functions required by this feature.
		try
		{
			// These functions only exist in microManager 1.4, but not in 1.3
			CMMCore.class.getMethod("getDeviceLibraries", new Class<?>[] {});
			CMMCore.class.getMethod("unloadDevice", new Class<?>[] {String.class});
		}
		catch(Exception e)
		{
			throw new UnsupportedOperationException("Manipulation of the installed device drivers is only supported for driver versions corresponding to MicroManager 1.4 and higher. Please either install a new version of uManager and configure YouScope respectively, or edit the respective parts of the configuration file manually or by using the uManager UI.", e);
		}		
		
		return new DeviceLoaderImpl(this, driverFolder);
	}
	
	private DeviceType getDeviceType(String device) throws MicroscopeException
	{
		mmcorej.DeviceType type;
		try
		{
			type = startRead().getDeviceType(device);
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Device " + device + " does not exist.", e);
		}
		finally
		{
			unlockRead();
		}
		
		
		if(type == mmcorej.DeviceType.UnknownType)
			return DeviceType.UnknownType;
		else if(type == mmcorej.DeviceType.AnyType)
			return DeviceType.AnyType;
		else if(type == mmcorej.DeviceType.CameraDevice)
			return DeviceType.CameraDevice;
		else if(type == mmcorej.DeviceType.ShutterDevice)
			return DeviceType.ShutterDevice;
		else if(type == mmcorej.DeviceType.StateDevice)
			return DeviceType.StateDevice;
		else if(type == mmcorej.DeviceType.StageDevice)
			return DeviceType.StageDevice;
		else if(type == mmcorej.DeviceType.XYStageDevice)
			return DeviceType.XYStageDevice;
		else if(type == mmcorej.DeviceType.SerialDevice)
			return DeviceType.SerialDevice;
		else if(type == mmcorej.DeviceType.GenericDevice)
			return DeviceType.GenericDevice;
		else if(type == mmcorej.DeviceType.AutoFocusDevice)
			return DeviceType.AutoFocusDevice;
		else if(type == mmcorej.DeviceType.ImageProcessorDevice)
			return DeviceType.ImageProcessorDevice;
		//else if(type == mmcorej.DeviceType.ImageStreamerDevice)
		//	return DeviceType.ImageStreamerDevice;
		else if(type == mmcorej.DeviceType.SignalIODevice)
			return DeviceType.SignalIODevice;
		else if(type == mmcorej.DeviceType.MagnifierDevice)
			return DeviceType.MagnifierDevice;
		//else if(type == mmcorej.DeviceType.ProgrammableIODevice)
		//	return DeviceType.ProgrammableIODevice;
		else
			return DeviceType.UnknownType;
	}

	@Override
	public FocusDeviceInternal[] getFocusDevices()
	{
		Vector<FocusDeviceImpl> devicesofType = new Vector<FocusDeviceImpl>();
		for(DeviceImpl device : devices.values())
		{
			if(device instanceof FocusDeviceImpl)
				devicesofType.addElement((FocusDeviceImpl)device);
		}
		FocusDeviceImpl[] retVal = devicesofType.toArray(new FocusDeviceImpl[devicesofType.size()]);
		Arrays.sort(retVal);
		return retVal;
	}
	
	@Override
	public AutoFocusDeviceInternal[] getAutoFocusDevices()
	{
		Vector<AutoFocusDeviceImpl> devicesofType = new Vector<AutoFocusDeviceImpl>();
		for(DeviceImpl device : devices.values())
		{
			if(device instanceof AutoFocusDeviceImpl)
				devicesofType.addElement((AutoFocusDeviceImpl)device);
		}
		AutoFocusDeviceImpl[] retVal = devicesofType.toArray(new AutoFocusDeviceImpl[devicesofType.size()]);
		Arrays.sort(retVal);
		return retVal;
	}

	@Override
	public void setShutterDevice(String deviceName, int accessID) throws DeviceException, MicroscopeLockedException
	{
		try
		{
			lockWrite(accessID);
			// Test if device exists and is of right type.
			if(deviceName != null)
				getShutterDevice(deviceName);
			// Set standard device.
			standardShutterDevice = deviceName;
		}
		finally
		{
			unlockWrite();
		}
		
		stateChanged("Standard shutter device set to " + deviceName + ".");
	}

	@Override
	public ShutterDeviceInternal getShutterDevice() throws DeviceException
	{
		if(standardShutterDevice == null)
			throw new DeviceException("Standard shutter device not set.");
		return getShutterDevice(standardShutterDevice);
	}

	@Override
	public ShutterDeviceInternal[] getShutterDevices()
	{
		Vector<ShutterDeviceImpl> devicesofType = new Vector<ShutterDeviceImpl>();
		for(DeviceImpl device : devices.values())
		{
			if(device instanceof ShutterDeviceImpl)
				devicesofType.addElement((ShutterDeviceImpl)device);
		}
		ShutterDeviceImpl[] retVal = devicesofType.toArray(new ShutterDeviceImpl[devicesofType.size()]);
		Arrays.sort(retVal);
		return retVal;
	}

	@Override
	public ShutterDeviceInternal getShutterDevice(String name) throws DeviceException
	{
		DeviceInternal device = getDevice(name);
		if(device instanceof ShutterDeviceInternal)
			return (ShutterDeviceInternal)device;
		throw new DeviceException("Device " + name + " is not a shutter device.");
	}

	@Override
	public void setFocusDevice(String deviceName, int accessID) throws DeviceException, MicroscopeLockedException
	{
		try
		{
			lockWrite(accessID);
			// Test if device exists and is of right type.
			if(deviceName != null)
				getFocusDevice(deviceName);
			// Set standard device.
			standardFocusDevice = deviceName;
		}
		finally
		{
			unlockWrite();
		}
		
		stateChanged("Standard focus device set to " + deviceName + ".");
	}

	@Override
	public StageDeviceInternal[] getStageDevices()
	{
		Vector<StageDeviceImpl> devicesofType = new Vector<StageDeviceImpl>();
		for(DeviceImpl device : devices.values())
		{
			if(device instanceof StageDeviceImpl)
				devicesofType.addElement((StageDeviceImpl)device);
		}
		StageDeviceImpl[] retVal = devicesofType.toArray(new StageDeviceImpl[devicesofType.size()]); 
		Arrays.sort(retVal);
		return retVal;
	}

	@Override
	public void setStageDevice(String deviceName, int accessID) throws DeviceException, MicroscopeLockedException
	{
		try
		{
			lockWrite(accessID);
			// Test if device exists and is of right type.
			if(deviceName != null)
				getStageDevice(deviceName);
			// Set standard device.
			standardStageDevice = deviceName;
		}
		finally
		{
			unlockWrite();
		}
		
		stateChanged("Standard stage device set to " + deviceName + ".");
	}

	@Override
	public void setAutoFocusDevice(String deviceName, int accessID) throws DeviceException, MicroscopeLockedException
	{
		try
		{
			lockWrite(accessID);
			// Test if device exists and is of right type.
			if(deviceName != null)
				getAutoFocusDevice(deviceName);
			// Set standard device.
			standardAutoFocusDevice = deviceName;
		}
		finally
		{
			unlockWrite();
		}
		
		stateChanged("Standard auto-focus set to " + deviceName + ".");
	}
	
	@Override
	public void setCameraDevice(String deviceName, int accessID) throws DeviceException, MicroscopeLockedException
	{
		try
		{
			lockWrite(accessID);
			// Test if device exists and is of right type.
			if(deviceName != null)
				getCameraDevice(deviceName);
			// Set standard device.
			standardCameraDevice = deviceName;
		}
		finally
		{
			unlockWrite();
		}
		
		stateChanged("Standard camera set to " + deviceName + ".");
	}

	@Override
	public CameraDeviceInternal[] getCameraDevices()
	{
		Vector<CameraDeviceImpl> devicesofType = new Vector<CameraDeviceImpl>();
		for(DeviceInternal device : devices.values())
		{
			if(device instanceof CameraDeviceImpl)
				devicesofType.addElement((CameraDeviceImpl)device);
		}
		CameraDeviceImpl[] retVal = devicesofType.toArray(new CameraDeviceImpl[devicesofType.size()]);
		Arrays.sort(retVal);
		return retVal;
	}

	@Override
	public StateDeviceInternal getStateDevice(String name) throws DeviceException
	{
		DeviceInternal device = getDevice(name);
		if(device instanceof StateDeviceInternal)
			return (StateDeviceInternal)device;
		throw new DeviceException("Device " + name + " is not a state device.");
	}

	@Override
	public StateDeviceInternal[] getStateDevices()
	{
		Vector<StateDeviceImpl> devicesofType = new Vector<StateDeviceImpl>();
		for(DeviceImpl device : devices.values())
		{
			if(device instanceof StateDeviceImpl)
				devicesofType.addElement((StateDeviceImpl)device);
		}
		StateDeviceImpl[] retVal = devicesofType.toArray(new StateDeviceImpl[devicesofType.size()]);
		Arrays.sort(retVal);
		return retVal;
	}

	@Override
	public MicroscopeConfigurationImpl getMicroscopeConfiguration()
	{
		return microscopeConfiguration;
	}

	void microscopeUninitialized()
	{
		devices.clear();
		standardFocusDevice = null;
		standardCameraDevice = null;
		standardShutterDevice = null;
		standardStageDevice = null;
		standardAutoFocusDevice = null;
		
		synchronized(microscopeConfigurationListeners)
		{
			for(int i=0; i<microscopeConfigurationListeners.size(); i++)
			{
				try
				{
					microscopeConfigurationListeners.elementAt(i).microscopeUninitialized();
				}
				catch(@SuppressWarnings("unused") RemoteException e)
				{
					// remove listener
					microscopeConfigurationListeners.removeElementAt(i);
					i--;
				}
			}
		}
	}

	void deviceRemoved(String deviceID)
	{
		// Remove local references on device.
		devices.remove(deviceID);
		if(deviceID.equals(standardFocusDevice))
			standardFocusDevice = null;
		if(deviceID.equals(standardCameraDevice))
			standardCameraDevice = null;
		if(deviceID.equals(standardShutterDevice))
			standardShutterDevice = null;
		if(deviceID.equals(standardStageDevice))
			standardStageDevice = null;
		if(deviceID.equals(standardAutoFocusDevice))
			standardAutoFocusDevice = null;
		
		// Tell other objects which might reference the device that it is removed.
		synchronized(microscopeConfigurationListeners)
		{
			for(int i=0; i<microscopeConfigurationListeners.size(); i++)
			{
				try
				{
					microscopeConfigurationListeners.elementAt(i).deviceRemoved(deviceID);
				}
				catch(@SuppressWarnings("unused") RemoteException e)
				{
					// remove listener
					microscopeConfigurationListeners.removeElementAt(i);
					i--;
				}
			}
		}
		
		stateChanged("Device " + deviceID + " unloaded.");
	}

	void labelChanged(DeviceSetting oldLabel, DeviceSetting newLabel)
	{
		synchronized(microscopeConfigurationListeners)
		{
			for(int i=0; i<microscopeConfigurationListeners.size(); i++)
			{
				try
				{
					microscopeConfigurationListeners.elementAt(i).labelChanged(oldLabel, newLabel);
				}
				catch(@SuppressWarnings("unused") RemoteException e)
				{
					// remove listener
					microscopeConfigurationListeners.removeElementAt(i);
					i--;
				}
			}
		}
	}

	@Override
	public void addConfigurationListener(MicroscopeConfigurationListener listener)
	{
		synchronized(microscopeConfigurationListeners)
		{
			microscopeConfigurationListeners.addElement(listener);
		}
	}

	@Override
	public void removeConfigurationListener(MicroscopeConfigurationListener listener)
	{
		synchronized(microscopeConfigurationListeners)
		{
			microscopeConfigurationListeners.removeElement(listener);
		}
	}
}
