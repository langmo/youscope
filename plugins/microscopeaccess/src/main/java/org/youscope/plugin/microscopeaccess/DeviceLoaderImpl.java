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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import org.youscope.addon.microscopeaccess.AvailableDeviceDriverInternal;
import org.youscope.addon.microscopeaccess.DeviceLoaderInternal;
import org.youscope.common.microscope.DeviceType;
import org.youscope.common.microscope.MicroscopeDriverException;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;

import mmcorej.CMMCore;
import mmcorej.LongVector;
import mmcorej.StrVector;

/**
 * @author langmo
 *
 */
class DeviceLoaderImpl implements DeviceLoaderInternal
{
	private final MicroscopeImpl microscope;
	private class DeviceDescription
	{
		private final String driver;
		private final String description;
		private final String library;
		private final DeviceType deviceType;
		DeviceDescription(String library, String identifier, String description, DeviceType deviceType)
		{
			this.library = library;
			this.driver = identifier;
			this.description = description;
			this.deviceType = deviceType;
		}
		boolean isHub()
		{
			return deviceType == DeviceType.HubDevice;
		}
		
		DeviceType getType()
		{
			return deviceType;
		}

		String getDriverID()
		{
			return driver;
		}

		String getDescription()
		{
			return description;
		}

		String getLibraryID()
		{
			return library;
		}
	}
	private static ArrayList<DeviceDescription> availableDeviceDrivers = new ArrayList<DeviceDescription>();
	private static ArrayList<DeviceDescription> availableHubDrivers = new ArrayList<DeviceDescription>();
	private static ArrayList<DeviceDescription> availableStandAloneDrivers = new ArrayList<DeviceDescription>();
	private static boolean driverLoaded = false;
	private final String driverFolder;
	DeviceLoaderImpl(MicroscopeImpl microscope, String driverFolder)
	{
		this.microscope = microscope;
		this.driverFolder = driverFolder;
	}
	
	@Override
	public synchronized AvailableDeviceDriverInternal[] getAvailableDeviceDrivers() throws MicroscopeDriverException
	{
		loadAllDrivers();
		// Only return drivers which are either hubs or parts of libraries which don't have a hub
		AvailableDeviceDriverInternal[] drivers = new AvailableDeviceDriverInternal[availableStandAloneDrivers.size()];
		for(int i=0; i<drivers.length; i++)
		{
			DeviceDescription description = availableStandAloneDrivers.get(i);
			drivers[i] = new AvailableDeviceDriverImpl(microscope, description.getLibraryID(), description.getDriverID(), description.getDescription(), description.getType());
		}
		return drivers;
	}
	
	/**
	 * This way of finding the libraries is a little big buggy in version 1.4 (see getLibraryNamesByFolderSearch()).
	 * Maybe replace the workaround by this version if the MicroManager guys have fixed the bugs...
	 * @return List of library names (shortened file name without prefix and suffix).
	 * @throws MicroscopeDriverException
	 */
	@SuppressWarnings("unused")
	private String[] getLibraryNamesByMicroManager() throws MicroscopeDriverException
	{
		try
		{
			StrVector deviceLibraries = CMMCore.getDeviceLibraries();
			return deviceLibraries.toArray();
		}
		catch(Exception e)
		{
			throw new MicroscopeDriverException("Could not load available libraries: " + e.getMessage());
		} 
		finally
		{
			microscope.unlockRead();
		}
	}
	/**
	 * MicroManager is a little bit buggy in its library search algorithm. The current version (1.4) either complains about finding a library
	 * twice, or finds it only once, but cannot load it then. This is due to the fact that when trying to find a library, MicroManager uses
	 * a search path consisting of the current directory and all subdirectories + all windows path variables + user added paths. 
	 * However, if it wants to actually load a library, there is a bug such that it only searches in the current directory and the path.
	 * This forces one, if one doesn't want to have all drivers floating around in the current directory, to add the driver folder to the
	 * windows path. This solves the loading, but when MicroManager now tries to find all libraries, it can happen that it finds something twice.
	 * This is due to a second bug, since path names from the windows path variable stay unchanged, whereas MicroManager replaces all spaces from
	 * the current directory + subdirectory pathes in "%20". By doing so, MicroManager does not realize that one path corresponds to an other
	 * and searches for libraries in both (i.e. twice in the same folder). It then finds the library twice and throws an error. Wonderful. 
	 * Since we might or might not have added the driver directory to the path, and this directory might or might not lie below the
	 * current directory, let's better search on our own...
	 * @param driverFolder The folder where the drivers are installed.
	 * @return List of library names (shortened file name without prefix and suffix).
	 */
	private static String[] getLibraryNamesByFolderSearch(String driverFolder)
	{
		
		final String prefix;
		if(MicroscopeConnectionFactoryImpl.isWindows32() || MicroscopeConnectionFactoryImpl.isWindows64())
			prefix = "mmgr_dal_";
		else
			prefix = "libmmgr_dal_";


		File driverPath = new File(driverFolder);
		File[] libraryFiles = driverPath.listFiles(new FilenameFilter()
    	{
			@Override
			public boolean accept(File dir, String name)
			{
				if(!new File(dir, name).isFile())
					return false;
				name = name.toLowerCase();
				if(name.indexOf(prefix) == 0)
					return true;
				return false;
			}
    	});
		String[] libraryNames = new String[libraryFiles.length];
		for(int i=0; i<libraryFiles.length; i++)
		{
			String fileName = libraryFiles[i].getName().substring(prefix.length());
			if(fileName.indexOf('.') > 0)
				fileName = fileName.substring(0, fileName.indexOf('.'));
			libraryNames[i] = fileName;
		}
		return libraryNames;
	}
	
	private synchronized void loadAllDrivers() throws MicroscopeDriverException
	{
		if(driverLoaded)
			return;
		
		String[] deviceLibraries = getLibraryNamesByFolderSearch(driverFolder); 
		
		try
		{
			CMMCore core = microscope.startRead();
			for(String deviceLibrary : deviceLibraries)
			{
				StrVector devices;
				StrVector deviceDescriptions;
				LongVector deviceTypes;
				try
				{
					devices = core.getAvailableDevices(deviceLibrary); 
					deviceDescriptions = core.getAvailableDeviceDescriptions(deviceLibrary); 
					deviceTypes = core.getAvailableDeviceTypes(deviceLibrary);
				}
				catch(Exception e)
				{
					System.out.println("Could not load information about devices from library " + deviceLibrary + ": " + e.getMessage());
					continue;
				}
				
				if(devices.size() != deviceDescriptions.size() || devices.size() != deviceTypes.size())
				{
					System.out.println("Library " + deviceLibrary + " has inconsistent definition of available devices, their descriptions, and their types.");
					continue;
				}
				for(int i=0; i<devices.size(); i++)
				{
					String device = devices.get(i);
					String deviceDescription = deviceDescriptions.get(i);
					int deviceTypeID = deviceTypes.get(i);
					DeviceType deviceType = getDeviceTypeFromID(deviceTypeID);
					DeviceDescription driver = new DeviceDescription(deviceLibrary, device, deviceDescription, deviceType);
					availableDeviceDrivers.add(driver);
					if(driver.isHub())
						availableHubDrivers.add(driver);
				}
			}
		}
		catch(MicroscopeException e)
		{
			throw new MicroscopeDriverException("Could not get access to the microscope.", e);
		}
		finally
		{
			microscope.unlockRead();
		}
		for(DeviceDescription driver : availableDeviceDrivers)
		{
			if(driver.isHub())
				availableStandAloneDrivers.add(driver);
			else
			{
				boolean foundHub = false;
				for(DeviceDescription hub : availableHubDrivers)
				{
					if(hub.getLibraryID().equals(driver.getLibraryID()))
					{
						foundHub = true;
						break;
					}
				}
				if(!foundHub)
					availableStandAloneDrivers.add(driver);
			}
		}
		driverLoaded = true;
	}
	private static DeviceType getDeviceTypeFromID(int deviceTypeID)
	{
		if(deviceTypeID == mmcorej.DeviceType.UnknownType.swigValue())
			return DeviceType.UnknownType;
		else if(deviceTypeID == mmcorej.DeviceType.AnyType.swigValue())
			return DeviceType.AnyType;
		else if(deviceTypeID == mmcorej.DeviceType.CameraDevice.swigValue())
			return DeviceType.CameraDevice;
		else if(deviceTypeID == mmcorej.DeviceType.ShutterDevice.swigValue())
			return DeviceType.ShutterDevice;
		else if(deviceTypeID == mmcorej.DeviceType.StateDevice.swigValue())
			return DeviceType.StateDevice;
		else if(deviceTypeID == mmcorej.DeviceType.StageDevice.swigValue())
			return DeviceType.StageDevice;
		else if(deviceTypeID == mmcorej.DeviceType.XYStageDevice.swigValue())
			return DeviceType.XYStageDevice;
		else if(deviceTypeID == mmcorej.DeviceType.SerialDevice.swigValue())
			return DeviceType.SerialDevice;
		else if(deviceTypeID == mmcorej.DeviceType.GenericDevice.swigValue())
			return DeviceType.GenericDevice;
		else if(deviceTypeID == mmcorej.DeviceType.AutoFocusDevice.swigValue())
			return DeviceType.AutoFocusDevice;
		else if(deviceTypeID == mmcorej.DeviceType.ImageProcessorDevice.swigValue())
			return DeviceType.ImageProcessorDevice;
		else if(deviceTypeID == mmcorej.DeviceType.SignalIODevice.swigValue())
			return DeviceType.SignalIODevice;
		else if(deviceTypeID == mmcorej.DeviceType.MagnifierDevice.swigValue())
			return DeviceType.MagnifierDevice;
		else if(deviceTypeID == mmcorej.DeviceType.HubDevice.swigValue())
			return DeviceType.HubDevice;
		else
			return DeviceType.UnknownType;
	}

	@Override
	public void removeDevice(String deviceID, int accessID) throws MicroscopeDriverException, MicroscopeLockedException
	{
		try
		{
			// Get access to microManager
			CMMCore core = microscope.startWrite(accessID);
			
			try
			{
				// Unload device.
				core.unloadDevice(deviceID);
			}
			catch(Exception e)
			{
				throw new MicroscopeDriverException("Could not unload device " + deviceID + ".", e);
			}
			
			// remove references on device.
			microscope.deviceRemoved(deviceID);			
		}
		finally
		{
			microscope.unlockWrite();
		}
		
	}

	@Override
	public AvailableDeviceDriverInternal getAvailableDeviceDriver(String libraryID, String driverID) throws MicroscopeDriverException
	{
		loadAllDrivers();
		for(DeviceDescription description : availableStandAloneDrivers)
		{
			if(description.getLibraryID().equals(libraryID) && description.getDriverID().equals(driverID))
				return new AvailableDeviceDriverImpl(microscope, description.getLibraryID(), description.getDriverID(), description.getDescription(), description.getType());
		}
		return null;
	}
	AvailableDeviceDriverImpl getDeviceDriver(String libraryID, String driverID) throws MicroscopeDriverException
	{
		for(DeviceDescription description : availableDeviceDrivers)
		{
			if(description.getLibraryID().equals(libraryID) && description.getDriverID().equals(driverID))
				return new AvailableDeviceDriverImpl(microscope, description.getLibraryID(), description.getDriverID(), description.getDescription(), description.getType());
		}
		return null;
	}
}
