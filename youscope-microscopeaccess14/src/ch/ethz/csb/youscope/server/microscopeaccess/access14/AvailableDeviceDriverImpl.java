/**
 * 
 */
package ch.ethz.csb.youscope.server.microscopeaccess.access14;

import java.util.Vector;

import mmcorej.CMMCore;
import mmcorej.StrVector;
import ch.ethz.csb.youscope.server.microscopeaccess.AvailableDeviceDriverInternal;
import ch.ethz.csb.youscope.server.microscopeaccess.PreInitDevicePropertyInternal;
import ch.ethz.csb.youscope.shared.microscope.DeviceSettingDTO;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeException;
import ch.ethz.csb.youscope.shared.microscope.PropertyType;
import ch.ethz.csb.youscope.shared.microscope.DeviceType;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeDriverException;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeLockedException;
 
/**
 * @author langmo
 *
 */
class AvailableDeviceDriverImpl implements AvailableDeviceDriverInternal
{
	private final String identifier;
	private final String description;
	private final String library;
	private final DeviceType deviceType;
	private final MicroscopeImpl microscope;
	private volatile String currentlyLoadedDeviceID = null;
	private volatile boolean serialPort = false;
	AvailableDeviceDriverImpl(MicroscopeImpl microscope, String library, String identifier, String description, DeviceType deviceType)
	{
		this.library = library;
		this.identifier = identifier;
		this.description = description;
		this.deviceType = deviceType;
		this.microscope = microscope;
	}
	
	@Override
	public DeviceType getType()
	{
		return deviceType;
	}

	@Override
	public String getDriverID()
	{
		return identifier;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public String getLibraryID()
	{
		return library;
	}

	/**
	 * Loads the pre-initialization properties for a given device.
	 * This is a little bit tricky: Micro Manager does not allow to access the pre-init-properties of
	 * a driver directly, but needs to load the device. We thus first try to find a device which is already loaded and from the same driver.
	 * If this is not true, we load a dummy device, get the needed information and afterwards unload it again.
	 * @param accessID The access-ID
	 * @throws MicroscopeDriverException
	 */
	/*private void loadProperties(int accessID) throws MicroscopeDriverException
	{
		if(deviceProperties != null)
			return;
	
		String deviceName = TEMP_DEVICE_NAME;
		boolean dummyDevice = true;
		
		// First, try to find a loaded device from the same driver.
		for(DeviceInternal loadedDevice : microscope.getDevices())
		{
			if(loadedDevice.getLibraryID().equals(library) && loadedDevice.getDriverID().equals(identifier))
			{
				deviceName = loadedDevice.getDeviceID();
				dummyDevice = false;
				break;
			}
		}
		
		
		// Now, get the properties.
		Vector<PreInitDevicePropertyInternal> preInitProps;
		CMMCore core = null;
		try
		{
			// Get access to microManager
			core = microscope.startWrite(accessID);
			
			// Construct a temporal device, if needed.
			if(dummyDevice)
			{
				core.loadDevice(deviceName, getLibraryID(), getDriverID());
			}
			
			// Try finally block to make sure that the dummy device is unloaded again.
			try
			{
				// Get properties which must be pre-initialized
				StrVector propertyNames = core.getDevicePropertyNames(deviceName);
				preInitProps = new Vector<PreInitDevicePropertyInternal>();
				for(String propertyName : propertyNames)
				{
					if(core.isPropertyPreInit(deviceName, propertyName))
					{
						// Get property type.
						PropertyType propertyType;
						mmcorej.PropertyType mmPropertyType = core.getPropertyType(deviceName, propertyName);
						if(mmPropertyType == mmcorej.PropertyType.Float)
							propertyType = PropertyType.PROPERTY_FLOAT;
						else if(mmPropertyType == mmcorej.PropertyType.Integer)
							propertyType = PropertyType.PROPERTY_INTEGER;
						else
							propertyType = PropertyType.PROPERTY_STRING;
						
						// Get allowed values.
						String currentValue = core.getProperty(deviceName, propertyName);
						String[] propertyValues;
						StrVector mmPropertyValues = core.getAllowedPropertyValues(deviceName, propertyName);
						if(mmPropertyValues == null || mmPropertyValues.size() == 0)
						{
							propertyValues = null;
						}
						else
						{
							propertyValues = new String[(int)mmPropertyValues.size()];
							for(int i = 0; i < propertyValues.length; i++)
							{
								propertyValues[i] = mmPropertyValues.get(i);
							}
							propertyType = PropertyType.PROPERTY_SELECTABLE;
						}
						preInitProps.add(new PreInitDevicePropertyImpl(microscope, propertyName, propertyType, propertyValues, currentValue));
					}
				} 
			}
			finally
			{
				// Unload temporal device
				if(core != null && dummyDevice)
				{
					try
					{
						core.unloadDevice(deviceName);
					}
					catch(Exception e)
					{
						throw new MicroscopeDriverException("Could not unload temporarly constructed device \""+TEMP_DEVICE_NAME+"\". Unload it manually.", e);
					}
				}
			}
		}
		catch(MicroscopeLockedException e)
		{
			throw new MicroscopeDriverException("Could not get driver information since microscope is locked.", e);
		}
		catch(Exception e)
		{
			throw new MicroscopeDriverException("Could not load driver information. ", e);
		}
		finally
		{
			microscope.unlockWrite();
		}
		
		deviceProperties = preInitProps.toArray(new PreInitDevicePropertyInternal[preInitProps.size()]);
	}*/
	
	/*@Override
	public PreInitDevicePropertyInternal[] getPreInitDeviceProperties(int accessID) throws MicroscopeDriverException
	{
		loadProperties(accessID);
		return deviceProperties;
	}
	
	@Override
	public void addDevice(String name, String library, String identifier, DeviceSettingDTO[] preInitSettings, int accessID) throws MicroscopeDriverException
	{
		if(name == null || name.length() < 1
				|| library == null || library.length() < 1
				|| identifier == null || identifier.length() <1)
			throw new MicroscopeDriverException("Either the name, the library or the identifier of the driver which should be loaded is invalid");
		
		if(preInitSettings == null)
			preInitSettings = new DeviceSettingDTO[0];
		
		try
		{
			// Get access to microManager
			CMMCore core = microscope.startWrite(accessID);
			
			// Construct a device
			core.loadDevice(name, library, identifier);
			
			// Set pre-init settings
			for(DeviceSettingDTO setting : preInitSettings)
			{
				if(setting.isAbsoluteValue() == false)
					throw new MicroscopeDriverException("Relative values are not allowed for properties when initializing a device.");
				core.setProperty(setting.getDevice(), setting.getProperty(), setting.getStringValue());
			}
			
			// Initialize device.
			core.initializeDevice(name);
			microscope.initializeDevice(name, library, identifier, accessID);
		}
		catch(MicroscopeLockedException e)
		{
			throw new MicroscopeDriverException("Could not add driver since microscope is locked.", e);
		}
		catch(MicroscopeException e)
		{
			throw new MicroscopeDriverException("Could not add driver information.", e);
		}
		catch(Exception e)
		{
			throw new MicroscopeDriverException("Could not add driver: " + e.getMessage());
		}
		finally
		{
			microscope.unlockWrite();
		}
	}*/

	@Override
	public boolean isSerialPortDriver(int accessID) throws MicroscopeLockedException, MicroscopeDriverException
	{
		if(currentlyLoadedDeviceID == null)
			throw new MicroscopeDriverException("Device driver must be loaded before properties of the device can be queried.");
		/*for(PreInitDevicePropertyInternal property : getPreInitDeviceProperties(accessID))
		{
			if(property.getPropertyID().equals("Port"))
				return true;
		}
		return false;*/
		return serialPort;
	}

	@Override
	public PreInitDevicePropertyInternal[] loadDevice(String deviceID, int accessID) throws MicroscopeDriverException, MicroscopeLockedException
	{
		if(deviceID == null || deviceID.length() < 1)
			throw new MicroscopeDriverException("The intended ID of the device is null or empty.");
		if(currentlyLoadedDeviceID != null)
			throw new MicroscopeDriverException("Device already loaded. Initialize device or unload it.");
		try
		{
			// Get access to microManager
			CMMCore core = microscope.startWrite(accessID);
			
			// Construct a device
			core.loadDevice(deviceID, library, identifier);
			currentlyLoadedDeviceID = deviceID;
			PreInitDevicePropertyInternal[] properties = loadProperties(accessID);
			return properties;
			
		}
		catch(MicroscopeLockedException e)
		{
			throw new MicroscopeDriverException("Could not load driver for device since microscope is locked.", e);
		}
		catch(MicroscopeException e)
		{
			throw new MicroscopeDriverException("Could not load driver for device.", e);
		}
		catch(Exception e)
		{
			throw new MicroscopeDriverException("Could not load driver for device: " + e.getMessage());
		}
		finally
		{
			microscope.unlockWrite();
		}
	}

	@Override
	public synchronized void initializeDevice(DeviceSettingDTO[] preInitSettings, int accessID) throws MicroscopeDriverException, MicroscopeLockedException
	{
		if(currentlyLoadedDeviceID == null)
			throw new MicroscopeDriverException("Device driver must be loaded before device is initialized.");
		if(preInitSettings == null)
			preInitSettings = new DeviceSettingDTO[0];
		
		try
		{
			// Get access to microManager
			CMMCore core = microscope.startWrite(accessID);
			
			// Set pre-init settings
			for(DeviceSettingDTO setting : preInitSettings)
			{
				if(setting.isAbsoluteValue() == false)
					throw new MicroscopeDriverException("Relative values are not allowed for properties when initializing a device.");
				if(!setting.getDevice().equals(currentlyLoadedDeviceID))
					throw new MicroscopeDriverException("Provided pre-initialization device property has a different deviceID then the loaded device.");
				core.setProperty(setting.getDevice(), setting.getProperty(), setting.getStringValue());
			}
			
			// Initialize device.
			core.initializeDevice(currentlyLoadedDeviceID);
			microscope.initializeDevice(currentlyLoadedDeviceID, library, identifier, accessID);
			currentlyLoadedDeviceID = null;
		}
		catch(MicroscopeException e)
		{
			try
			{
				unloadDevice(accessID);
			}
			catch(Exception e1)
			{
				throw new MicroscopeDriverException("Could not initialize driver. Automatic driver unloading failed. Configuration might got corrupted. Restarting YouScope is recommended.", e1);
			}
			throw new MicroscopeDriverException("Could not initilize driver. Check if device is correctly connected, or retry with different pre-initialization settings.", e);
		}
		catch(Exception e)
		{
			try
			{
				unloadDevice(accessID);
			}
			catch(Exception e1)
			{
				throw new MicroscopeDriverException("Could not initialize driver. Automatic driver unloading failed. Configuration might got corrupted. Restarting YouScope is recommended.", e1);
			}
			throw new MicroscopeDriverException("Could not initilize driver. Check if device is correctly connected, or retry with different pre-initialization settings. ", e);
		}
		finally
		{
			microscope.unlockWrite();
		}
		
	}

	@Override
	public synchronized void unloadDevice(int accessID) throws MicroscopeLockedException, MicroscopeDriverException
	{
		if(currentlyLoadedDeviceID == null)
			return;
		try
		{
			// Get access to microManager
			CMMCore core = microscope.startWrite(accessID);
			// unload device
			core.unloadDevice(currentlyLoadedDeviceID);
			
			currentlyLoadedDeviceID = null;
		}
		catch(MicroscopeLockedException e)
		{
			throw new MicroscopeDriverException("Could not unload device driver with ID " + currentlyLoadedDeviceID + " since microscope is locked.", e);
		}
		catch(Exception e)
		{
			throw new MicroscopeDriverException("Could not unload device driver with ID " + currentlyLoadedDeviceID + " since microscope is locked.", e);
		}
		finally
		{
			microscope.unlockWrite();
		}
	}
	
	
	private synchronized PreInitDevicePropertyInternal[] loadProperties(int accessID) throws MicroscopeDriverException
	{
		if(currentlyLoadedDeviceID == null)
			throw new MicroscopeDriverException("No device driver loaded.");
	
		// Now, get the properties.
		Vector<PreInitDevicePropertyInternal> preInitProps;
		CMMCore core = null;
		serialPort = false;
		try
		{
			// Get access to microManager
			core = microscope.startWrite(accessID);
					
			// Get properties which must be pre-initialized
			StrVector propertyNames = core.getDevicePropertyNames(currentlyLoadedDeviceID);
			preInitProps = new Vector<PreInitDevicePropertyInternal>();
			for(String propertyName : propertyNames)
			{
				if(core.isPropertyPreInit(currentlyLoadedDeviceID, propertyName))
				{
					// Get property type.
					PropertyType propertyType;
					mmcorej.PropertyType mmPropertyType = core.getPropertyType(currentlyLoadedDeviceID, propertyName);
					if(mmPropertyType == mmcorej.PropertyType.Float)
						propertyType = PropertyType.PROPERTY_FLOAT;
					else if(mmPropertyType == mmcorej.PropertyType.Integer)
						propertyType = PropertyType.PROPERTY_INTEGER;
					else
						propertyType = PropertyType.PROPERTY_STRING;
					
					// Get allowed values.
					String currentValue = core.getProperty(currentlyLoadedDeviceID, propertyName);
					String[] propertyValues;
					StrVector mmPropertyValues = core.getAllowedPropertyValues(currentlyLoadedDeviceID, propertyName);
					if(mmPropertyValues == null || mmPropertyValues.size() == 0)
					{
						propertyValues = null;
					}
					else
					{
						propertyValues = new String[(int)mmPropertyValues.size()];
						for(int i = 0; i < propertyValues.length; i++)
						{
							propertyValues[i] = mmPropertyValues.get(i);
						}
						propertyType = PropertyType.PROPERTY_SELECTABLE;
					}
					if(propertyName.equals("Port"))
						serialPort = true;
					preInitProps.add(new PreInitDevicePropertyImpl(microscope, propertyName, propertyType, propertyValues, currentValue));
				}
			} 
		}
		catch(MicroscopeLockedException e)
		{
			throw new MicroscopeDriverException("Could not get driver information since microscope is locked.", e);
		}
		catch(Exception e)
		{
			throw new MicroscopeDriverException("Could not load driver information. ", e);
		}
		finally
		{
			microscope.unlockWrite();
		}
		
		return preInitProps.toArray(new PreInitDevicePropertyInternal[preInitProps.size()]);
	}
	
	
	
}
