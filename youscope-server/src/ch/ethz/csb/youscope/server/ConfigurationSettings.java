/**
 * 
 */
package ch.ethz.csb.youscope.server;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author langmo
 */
class ConfigurationSettings
{
	// Properties
	protected final static String	propertiesFile				= "youscope_properties.prop";

	public static final String		SETTINGS_CONFIG_FILE_LAST_0	= "CSB::configFileLast0";
	public static final String		SETTINGS_CONFIG_FILE_LAST_1	= "CSB::configFileLast1";
	public static final String		SETTINGS_CONFIG_FILE_LAST_2	= "CSB::configFileLast2";
	public static final String		SETTINGS_CONFIG_FILE_LAST_3	= "CSB::configFileLast3";

	private final Properties		properties;

	private ConfigurationSettings(Properties properties)
	{
		this.properties = properties;
	}

	public static ConfigurationSettings loadProperties()
	{
		Properties properties = new Properties();
		// Get saved properties
		FileInputStream in = null;
		try
		{
			in = new FileInputStream(propertiesFile);
			properties.load(in);
			in.close();
		}
		catch(IOException e)
		{
			System.out.println("Could not load last settings: " + e.getMessage());
		}
		finally
		{
			if(in != null)
			{
				try {
					in.close();
				} catch (@SuppressWarnings("unused") IOException e) {
					// do nothing.
				}
			}
		}
		return new ConfigurationSettings(properties);
	}

	public void saveProperties()
	{
		// Check if already loaded.
		if(properties == null)
			return;

		// Save properties
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(propertiesFile);
			properties.store(out, "YouScope configuration properties.");
			out.close();
		}
		catch(IOException e)
		{
			System.err.println("Configuration properties could not be saved: " + e.getMessage());
		}
		finally
		{
			if(out != null)
			{
				try {
					out.close();
				} catch (@SuppressWarnings("unused") IOException e) {
					// do nothing.
				}
			}
		}
	}

	public String getProperty(String name, String defaultValue)
	{
		if(properties == null)
			return defaultValue;
		return properties.getProperty(name, defaultValue);
	}

	public void setProperty(String name, String value)
	{
		if(properties == null || name == null)
			return;
		if(value == null)
			deleteProperty(name);
		else
			properties.setProperty(name, value);
	}

	public void deleteProperty(String name)
	{
		if(properties == null || name == null)
			return;
		properties.remove(name);
	}

	public int getProperty(String name, int defaultValue)
	{
		try
		{
			return Integer.parseInt(getProperty(name, Integer.toString(defaultValue)));
		}
		catch(@SuppressWarnings("unused") NumberFormatException e)
		{
			return defaultValue;
		}
	}

	public double getProperty(String name, double defaultValue)
	{
		try
		{
			return Double.parseDouble(getProperty(name, Double.toString(defaultValue)));
		}
		catch(@SuppressWarnings("unused") NumberFormatException e)
		{
			return defaultValue;
		}
	}

	public boolean getProperty(String name, boolean defaultValue)
	{
		return Boolean.parseBoolean(getProperty(name, Boolean.toString(defaultValue)));
	}

	public void setProperty(String name, int value)
	{
		setProperty(name, Integer.toString(value));
	}

	public void setProperty(String name, double value)
	{
		setProperty(name, Double.toString(value));
	}

	public void setProperty(String name, boolean value)
	{
		setProperty(name, Boolean.toString(value));
	}
}
