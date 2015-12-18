/**
 * 
 */
package org.youscope.client;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;

import org.youscope.clientinterfaces.StandardProperty;


/**
 * @author langmo
 */
class ConfigurationSettings
{
    // Property loading and saving
    protected final static String propertiesFile = "youscope_properties.prop";

    public static final String SETTINGS_CONFIG_FILE_LAST_0 = "YouScope.configFileLast0";

    public static final String SETTINGS_CONFIG_FILE_LAST_1 = "YouScope.configFileLast1";

    public static final String SETTINGS_CONFIG_FILE_LAST_2 = "YouScope.configFileLast2";

    public static final String SETTINGS_CONFIG_FILE_LAST_3 = "YouScope.configFileLast3";

    private volatile Properties properties = new Properties();

    private static volatile ConfigurationSettings instance = new ConfigurationSettings();
    
    private static Object ioLock = new Object();

    protected Properties loadProperties()
    {
        synchronized (ioLock)
        {
            // Get saved properties
            Reader in;
            try
            {
                in = new InputStreamReader(new FileInputStream(propertiesFile), "UTF-8");
                properties.load(in);
                in.close();
            } catch (IOException e)
            {
                ClientSystem.err.println("Could not load last settings. New settings might be generated.", e);
            }
        }
        
        return properties;
    }

    static synchronized void deleteProperty(String name)
    {
        Properties properties = instance.loadProperties();
        if (properties == null || name == null)
            return;
        properties.remove(name);
        instance.saveProperties();
    }

    protected void saveProperties()
    {
        synchronized (ioLock)
        {
            if (properties == null)
                return;

            // Save properties
            Writer out;
            try
            {
                out = new OutputStreamWriter(new FileOutputStream(propertiesFile), "UTF-8");
                
                Properties tmp = new Properties() {
                    /**
                     * Serial Version UID
                     */
                    private static final long serialVersionUID = 3118991856807806224L;

                    @Override
                    public synchronized Enumeration<Object> keys() {
                        return Collections.enumeration(new TreeSet<Object>(super.keySet()));
                    }
                };
                tmp.putAll(properties);
                
                tmp.store(out, "Configuration Settings");
                out.close();
            } catch (IOException e)
            {
                ClientSystem.err.println("Configuration properties could not be saved: " + e.getMessage());
            }
        }
    }

    static synchronized String getProperty(String name, String defaultValue)
    {
        Properties properties = instance.loadProperties();
        if (properties == null)
            return defaultValue;
        return properties.getProperty(name, defaultValue);
    }

    static synchronized void setProperty(String name, String value)
    {
        Properties properties = instance.loadProperties();
        if (properties == null)
            return;
        properties.setProperty(name, value);
        instance.saveProperties();
    }

    static int getProperty(String name, int defaultValue)
    {
        try
        {
            return Integer.parseInt(getProperty(name, Integer.toString(defaultValue)));
        } catch (@SuppressWarnings("unused") NumberFormatException e)
        {
            return defaultValue;
        }
    }

    static double getProperty(String name, double defaultValue)
    {
        try
        {
            return Double.parseDouble(getProperty(name, Double.toString(defaultValue)));
        } catch (@SuppressWarnings("unused") NumberFormatException e)
        {
            return defaultValue;
        }
    }

    static boolean getProperty(String name, boolean defaultValue)
    {
        return Boolean.parseBoolean(getProperty(name, Boolean.toString(defaultValue)));
    }
    static Object getProperty(StandardProperty property)
    {
    	if(property.isBooleanProperty())
    		return getProperty(property.getPropertyName(), property.getDefaultAsBoolean());
    	else if(property.isIntegerProperty())
    		return getProperty(property.getPropertyName(), property.getDefaultAsInteger());
    	else if(property.isDoubleProperty())
    		return getProperty(property.getPropertyName(), property.getDefaultAsDouble());
    	else
    		return getProperty(property.getPropertyName(), property.getDefaultAsString());
    }
    static void setProperty(StandardProperty property, Object value)
    {
    	if(property.isBooleanProperty())
    	{
    		if(value instanceof Boolean)
    			setProperty(property.getPropertyName(), (Boolean)value);
    		else if(value instanceof Number)
    			setProperty(property.getPropertyName(), ((Number)value).doubleValue() != 0);
    		else
    			setProperty(property.getPropertyName(), Boolean.parseBoolean(value.toString()));
    	}
    	else if(property.isDoubleProperty())
    	{
    		if(value instanceof Number)
    			setProperty(property.getPropertyName(), ((Number)value).doubleValue());
    		else if(value instanceof Boolean)
    			setProperty(property.getPropertyName(), ((Boolean)value)? 1.0 : 0.0);
    		else
    		{
    			try
    			{
    				setProperty(property.getPropertyName(), Double.parseDouble(value.toString()));
    			}
    			catch(@SuppressWarnings("unused") NumberFormatException e)
    			{
    				// do nothing.
    			}
    		}
    	}
    	else if(property.isIntegerProperty())
    	{
    		if(value instanceof Number)
    			setProperty(property.getPropertyName(), ((Number)value).intValue());
    		else if(value instanceof Boolean)
    			setProperty(property.getPropertyName(), ((Boolean)value)? 1 : 0);
    		else
    		{
    			try
    			{
    				setProperty(property.getPropertyName(), Integer.parseInt(value.toString()));
    			}
    			catch(@SuppressWarnings("unused") NumberFormatException e)
    			{
    				// do nothing.
    			}
    		}
    	}
    	else
    	{
    		setProperty(property.getPropertyName(), value.toString());
    	}
    }
    
    public static String[] getProperty(String name, String[] defaultValue)
    {
        String list = getProperty(name, (String)null);
        if (list == null || list.length() <=0) {
            return defaultValue;
        }

        String[] splits = list.split(",");
        String[] result = new String[splits.length];
        
        for (int i = 0; i < splits.length; i++)
        {
            result[i] = splits[i].trim();
        }
        
        return result;
    }
    

    static void setProperty(String name, int value)
    {
        setProperty(name, Integer.toString(value));
    }

    static void setProperty(String name, double value)
    {
        setProperty(name, Double.toString(value));
    }

    static void setProperty(String name, boolean value)
    {
        setProperty(name, Boolean.toString(value));
    }
}
