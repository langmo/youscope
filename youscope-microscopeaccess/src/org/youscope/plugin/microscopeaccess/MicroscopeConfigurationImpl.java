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

import java.util.Vector;

import org.youscope.addon.microscopeaccess.MicroscopeConfigurationInternal;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.microscope.MicroscopeConfigurationListener;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.SettingException;

import mmcorej.CMMCore;

/**
 * @author Moritz Lang
 *
 */
class MicroscopeConfigurationImpl implements MicroscopeConfigurationInternal, MicroscopeConfigurationListener
{
	private final MicroscopeImpl microscope;
	private final Vector<String> imageSynchronizationDevices = new Vector<String>();
	private final Vector<DeviceSetting> systemStartupSettings = new Vector<DeviceSetting>();
	private final Vector<DeviceSetting> systemShutdownSettings = new Vector<DeviceSetting>();
	private int communicationTimeout = 5000; // [ms]
	private int communicationPingPeriod = 10; // [ms]
	private int imageBufferSize = -1;
	MicroscopeConfigurationImpl(MicroscopeImpl microscope)
	{
		this.microscope = microscope;		
	}
	
	@Override 
	public void setImageSynchronizationDevices(String[] devices, int accessID) throws SettingException, MicroscopeLockedException
	{
		try
		{
			// Get access to microManager
			CMMCore core = microscope.startWrite(accessID);
			core.removeImageSynchroAll();
			imageSynchronizationDevices.clear();
			microscope.stateChanged("All image synchronization devices removed.");
			if(devices != null)
			{
				for(String device : devices)
				{
					addImageSynchronizationDevice(device, accessID);
				}
			}
		}
		catch(MicroscopeLockedException e)
		{
			throw e;
		}
		finally
		{
			microscope.unlockWrite();
		}
		
		microscope.stateChanged("Image synchronization devices actualized.");
	}
	void waitForImageSynchro() throws MicroscopeException, InterruptedException, DeviceException
	{
		try
		{
			microscope.lockRead();
			for(String deviceID : imageSynchronizationDevices)
			{
				microscope.getDevice(deviceID).waitForDevice();
			}
		}
		finally
		{
			microscope.unlockRead();
		}
	}
	@Override
	public void addImageSynchronizationDevice(String device, int accessID) throws SettingException, MicroscopeLockedException
	{
		try
		{
			// Get access to microManager
			CMMCore core = microscope.startWrite(accessID);
			if(!imageSynchronizationDevices.contains(device))
			{
				core.assignImageSynchro(device);
				imageSynchronizationDevices.add(device);
			}
		}
		catch(MicroscopeLockedException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new SettingException("Could not add image synchronization device: " + e.getMessage());
		}
		finally
		{
			microscope.unlockWrite();
		}
		microscope.stateChanged("Device "+device+" added to list of image synchronization devices.");
	}
	
	@Override
	public void removeImageSynchronizationDevice(String device, int accessID) throws SettingException, MicroscopeLockedException
	{
		try
		{
			// Get access to microManager
			CMMCore core = microscope.startWrite(accessID);
			core.removeImageSynchro(device);
			imageSynchronizationDevices.remove(device);
		}
		catch(MicroscopeLockedException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new SettingException("Could not add image synchronization device: " + e.getMessage());
		}
		finally
		{
			microscope.unlockWrite();
		}
		microscope.stateChanged("Device "+device+" removed from list of image synchronization devices.");
	}
	
	
	
	@Override
	public String[] getImageSynchronizationDevices()
	{
		return imageSynchronizationDevices.toArray(new String[imageSynchronizationDevices.size()]);
	}

	@Override
	public int getCommunicationTimeout()
	{
		return communicationTimeout;
	}
	
	@Override
	public void setCommunicationTimeout(int timeout, int accessID) throws SettingException, MicroscopeLockedException
	{
		if(timeout <= 0)
			throw new SettingException("Communication Timeout must be positive.");
		
		try
		{
			CMMCore core = microscope.startWrite(accessID);
			// Set current value.
			communicationTimeout = timeout;
			core.setProperty("Core", "TimeoutMs", Integer.toString(timeout));
		}
		catch(Exception e)
		{
			throw new SettingException("Could not set communication timeout.", e);
		}
		finally
		{
			microscope.unlockWrite();
		}
		microscope.stateChanged("Maximal accepted response time of all hardware devices set to " + Integer.toString(timeout)+"ms.");
	}
	
	@Override
	public int getCommunicationPingPeriod()
	{
		return communicationPingPeriod;
	}
	
	@Override
	public void setCommunicationPingPeriod(int pingPeriod, int accessID) throws SettingException, MicroscopeLockedException
	{
		if(pingPeriod <= 0)
			throw new SettingException("Communication ping period must be positive.");
		
		try
		{
			microscope.lockWrite(accessID);
			// Set current value.
			communicationPingPeriod = pingPeriod;
			
		}
		finally
		{
			microscope.unlockWrite();
		}
		microscope.stateChanged("Communication ping period set to " + Integer.toString(communicationPingPeriod)+"ms.");
	}

	

	@Override
	public DeviceSetting[] getSystemStartupSettings()
	{
		return systemStartupSettings.toArray(new DeviceSetting[systemStartupSettings.size()]);
	}

	@Override
	public void setSystemStartupSettings(DeviceSetting[] settings, int accessID) throws SettingException, MicroscopeLockedException
	{
		try
		{
			microscope.lockWrite(accessID);
		
			microscope.areSettingsValid(settings, true);
			systemStartupSettings.clear();
			microscope.stateChanged("All startup device settings removed.");
			if(settings != null)
			{
				for(DeviceSetting setting : settings)
				{
					addSystemStartupSetting(setting, accessID);
				}
			}
		}
		finally
		{
			microscope.unlockWrite();
		}
	}

	@Override
	public void addSystemStartupSetting(DeviceSetting setting, int accessID) throws SettingException, MicroscopeLockedException
	{
		try
		{
			microscope.lockWrite(accessID);
			microscope.isSettingValid(setting, true);
					
			// Remove old settings for the same device property.
			for(int i=0; i<systemStartupSettings.size(); i++)
			{
				DeviceSetting oldSetting = systemStartupSettings.elementAt(i);
				if(oldSetting.getDevice().equals(setting.getDevice()) && oldSetting.getProperty().equals(setting.getProperty()))
				{
					systemStartupSettings.removeElementAt(i);
					i--;
				}
			}
			
			systemStartupSettings.addElement(setting);
		}
		finally
		{
			microscope.unlockWrite();
		}
		microscope.stateChanged("Added startup setting " + setting.getDevice() + "." + setting.getProperty() + " = " + setting.getStringValue()+".");
	}

	@Override
	public void deviceRemoved(String deviceName)
	{
		imageSynchronizationDevices.remove(deviceName);
		for(int i=0; i<systemStartupSettings.size(); i++)
		{
			if(systemStartupSettings.elementAt(i).getDevice().equals(deviceName))
			{
				systemStartupSettings.removeElementAt(i);
				i--;
			}
		}
		for(int i=0; i<systemShutdownSettings.size(); i++)
		{
			if(systemShutdownSettings.elementAt(i).getDevice().equals(deviceName))
			{
				systemShutdownSettings.removeElementAt(i);
				i--;
			}
		}
	}

	@Override
	public void microscopeUninitialized()
	{
		imageSynchronizationDevices.clear();
		systemStartupSettings.clear();
		systemShutdownSettings.clear();
		communicationTimeout = 5000;
	}

	@Override
	public void labelChanged(DeviceSetting oldLabel, DeviceSetting newLabel)
	{
		for(int i=0; i<systemStartupSettings.size(); i++)
		{
			if(systemStartupSettings.elementAt(i).equals(oldLabel))
				systemStartupSettings.setElementAt(newLabel, i);
		}
	}

	@Override
	public DeviceSetting[] getSystemShutdownSettings()
	{
		return systemShutdownSettings.toArray(new DeviceSetting[systemShutdownSettings.size()]);
	}

	@Override
	public void setSystemShutdownSettings(DeviceSetting[] settings, int accessID) throws SettingException, MicroscopeLockedException
	{
		try
		{
			microscope.lockWrite(accessID);
			microscope.areSettingsValid(settings, true);
			systemShutdownSettings.clear();
			microscope.stateChanged("All shutdown device settings removed.");
			if(settings != null)
			{
				for(DeviceSetting setting : settings)
				{
					addSystemShutdownSetting(setting, accessID);
				}
			}
		}
		finally
		{
			microscope.unlockWrite();
		}
	}

	@Override
	public void addSystemShutdownSetting(DeviceSetting setting, int accessID) throws SettingException, MicroscopeLockedException
	{
		try
		{
			microscope.lockWrite(accessID);
			microscope.isSettingValid(setting, true);
			
			// Remove old settings for the same device property.
			for(int i=0; i<systemShutdownSettings.size(); i++)
			{
				DeviceSetting oldSetting = systemShutdownSettings.elementAt(i);
				if(oldSetting.getDevice().equals(setting.getDevice()) && oldSetting.getProperty().equals(setting.getProperty()))
				{
					systemShutdownSettings.removeElementAt(i);
					i--;
				}
			}
			
			systemShutdownSettings.addElement(setting);
		}
		finally
		{
			microscope.unlockWrite();
		}
		microscope.stateChanged("Added shutdown setting " + setting.getDevice() + "." + setting.getProperty() + " = " + setting.getStringValue()+".");
	}

	@Override
	public void setImageBufferSize(int sizeMB, int accessID) throws SettingException, MicroscopeLockedException, UnsupportedOperationException
	{
		if(sizeMB < 0)
		{
			imageBufferSize = -1;
			microscope.stateChanged("Image buffer size unset. This setting will only get activated when saving and reloading configuration.");
			return;
		}
		else if(sizeMB == 0)
			throw new SettingException("Image buffer size must be at least one MB.");
		try
		{
			CMMCore core = microscope.startWrite(accessID);
			// size given in MB.
			core.setCircularBufferMemoryFootprint(sizeMB);
		}
		catch(Exception e1)
		{
			throw new SettingException("Could not set circular buffer size.", e1);
		}
		finally
		{
			microscope.unlockWrite();
		}
		imageBufferSize = sizeMB;
		microscope.stateChanged("Image buffer size set to " + Integer.toString(sizeMB) + "MB.");
	}

	@Override
	public int getImageBufferSize() throws UnsupportedOperationException
	{
		return imageBufferSize;
	}
}
