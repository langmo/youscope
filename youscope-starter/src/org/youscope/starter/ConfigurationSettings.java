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
package org.youscope.starter;

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
    protected final static String propertiesFile = "youscope_properties.prop";

    public static final String		SETTINGS_CONFIG_FILE_LAST_0	= "YouScope.configFileLast0";
	public static final String		SETTINGS_CONFIG_FILE_LAST_1	= "YouScope.configFileLast1";
	public static final String		SETTINGS_CONFIG_FILE_LAST_2	= "YouScope.configFileLast2";
	public static final String		SETTINGS_CONFIG_FILE_LAST_3	= "YouScope.configFileLast3";
	
    public static final String SETTINGS_SERVER_URL = "YouScope.server.lastURL";

    public static final String SETTINGS_SERVER_PORT = "YouScope.server.lastPort";

    public static final String SETTINGS_STARTUP_TYPE = "YouScope.general.startupType";
    
    private final Properties properties;
    
    private ConfigurationSettings(Properties properties)
    {
    	this.properties = properties;
    }
    
    public  static ConfigurationSettings loadProperties()
    {
        Properties properties = new Properties();
        // Get saved properties
        FileInputStream in = null;
        try
        {
            in = new FileInputStream(propertiesFile);
            properties.load(in);
            in.close();
        } catch (IOException e)
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
        if (properties == null)
            return;

        // Save properties
        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream(propertiesFile);
            properties.store(out, "YouScope property file.");
            out.close();
        } catch (IOException e)
        {
            System.err.println("Configuration properties could not be saved: " + e.getMessage());
        }
        finally
        {
        	if(out!=null)
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
        if (properties == null || name == null)
            return defaultValue;
        return properties.getProperty(name, defaultValue);
    }

    public void setProperty(String name, String value)
    {
        if (properties == null || name == null)
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
}
