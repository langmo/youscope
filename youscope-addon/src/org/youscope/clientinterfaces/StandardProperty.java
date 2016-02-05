package org.youscope.clientinterfaces;

/**
 * Standard (default) properties of YouScope.
 * @author Moritz Lang
 *
 */
public enum StandardProperty
{
	/**
     * Name of the boolean property indicating if camera settings should be preinitialized.
     */
    PROPERTY_PREINITIALIZE_CAMERA_SETTINGS ("YouScope.Measurement.cameraStartupSettings", false),
    
    /**
     * Name of the skin which is loaded at startup.
     */
    PROPERTY_SKIN ("YouScope.skin", "YouScope.Skin.System"),

    /**
     * Name of the boolean property indicating if a measurement control should be docked to the main window (true) or shown in an own window.
     */
    PROPERTY_DOCK_MEASUREMENT_CONTROL ("YouScope.Client.dockMeasurementControl", true),

    /**
     * Name of boolean property indicating if youscope is configured.
     */
    PROPERTY_IS_CONFIGURED ("YouScope.isConfigured", false),

    /**
     * Name of the String property indicating where the last measurement was saved.
     */
    PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER ("YouScope.Measurement.lastMeasurementSaveFolder", null),

    /**
     * Name of the last channel group used in a channel configuration.
     */
    PROPERTY_LAST_CHANNEL_GROUP ("YouScope.Imaging.lastChannelGroup", null),
    
    /**
     * Name of the last channel used in a channel configuration.
     */
    PROPERTY_LAST_CHANNEL ("YouScope.Imaging.lastChannel", null),

    /**
     * Last path to the folder where scripts where loaded or saved.
     */
    PROPERTY_LAST_SCRIPT_PATH("YouScope.Scripting.lastScriptPath", null),
    
    /**
     * If and how the positions of frames should be stored. Check enum FramePositionStorage.StorageType for options.
     */
    PROPERTY_POSITION_STORAGE ("YouScope.FramePositionStorage", FramePositionStorageType.NONE.getIdentifier()),
    
    /**
     * Last absolute path of an image directly saved to disk from e.g. the LiveView.
     */
    PROPERTY_LAST_IMAGE_SAVE_FILE_NAME("YouScope.LastImageSaveFileName", null),

    /**
     * Standard file name makro for images in measurement.
     */
    PROPERTY_MEASUREMENT_STANDARD_IMAGE_FILE_NAME("YouScope.Measurement.StandardImageFileName", "%N_position%4w%2p_time%n"),
    
    /**
     * Standard file extension for images in measurement.
     */
    PROPERTY_MEASUREMENT_STANDARD_IMAGE_FILE_TYPE("YouScope.Measurement.StandardImageFileType", "tif"),
    
    /**
     * Last exposure time in ms used in live stream, and similar.
     */
    PROPERTY_STREAM_LAST_EXPOSURE("YouScope.LiveStream.LastExposure", 50.0),
    /**
     * Last period in ms used in live stream, and similar.
     */
    PROPERTY_STREAM_LAST_PERIOD("YouScope.LiveStream.LastPeriod", 100),
    /**
     * Name of last camera used in live stream, and similar.
     */
    PROPERTY_STREAM_LAST_CAMERA("YouScope.LiveStream.LastCamera", null),
    /**
     * if contrast should be increased automatically in live stream, and similar.
     */
    PROPERTY_STREAM_LAST_AUTO_CONTRAST ("YouScope.LiveStream.LastAutoContrast", false),
	/**
     * Name of the last channel group used in live stream, and similar.
     */
    PROPERTY_STREAM_LAST_CHANNEL_GROUP ("YouScope.LiveStream.LastChannelGroup", null),
    /**
     * Name of the last channel used in live stream, and similar.
     */
    PROPERTY_STREAM_LAST_CHANNEL ("YouScope.LiveStream.LastChannel", null),
    
    /**
     * Name of the last channel used in live stream, and similar.
     */
    PROPERTY_STREAM_USE_DEFAULT_SETTINGS ("YouScope.LiveStream.UseDefaultSettings", false),
    
    /**
     * If the stream should start automatically.
     */
    PROPERTY_STREAM_AUTOSTART("YouScope.LiveStream.Autostart", true),
    /**
     * Default exposure time in ms used in live stream, and similar. Negative for not set.
     */
    PROPERTY_STREAM_DEFAULT_EXPOSURE("YouScope.LiveStream.DefaultExposure", 50.0),
    /**
     * Default period in ms used in live stream, and similar. Negative for not set.
     */
    PROPERTY_STREAM_DEFAULT_PERIOD("YouScope.LiveStream.DefaultPeriod", 100),
    /**
     * Name of default camera used in live stream, and similar.
     */
    PROPERTY_STREAM_DEFAULT_CAMERA("YouScope.LiveStream.DefaultCamera", null),
    /**
     * if contrast should be increased automatically in live stream, and similar.
     */
    PROPERTY_STREAM_DEFAULT_AUTO_CONTRAST ("YouScope.LiveStream.DefaultAutoContrast", false),
	/**
     * Name of the default channel group used in live stream, and similar.
     */
    PROPERTY_STREAM_DEFAULT_CHANNEL_GROUP ("YouScope.LiveStream.DefaultChannelGroup", null),
    /**
     * Name of the default channel used in live stream, and similar.
     */
    PROPERTY_STREAM_DEFAULT_CHANNEL ("YouScope.LiveStream.DefaultChannel", null);
	
    
    private final String propertyName;
    private final Object defaultValue;
    StandardProperty(String propertyName, String defaultString)
    {
    	this.propertyName = propertyName;
    	this.defaultValue = defaultString;
    }
    StandardProperty(String propertyName, int defaultInt)
    {
    	this.propertyName = propertyName;
    	this.defaultValue = defaultInt;
    }
    StandardProperty(String propertyName, double defaultDouble)
    {
    	this.propertyName = propertyName;
    	this.defaultValue = defaultDouble;
    }
    StandardProperty(String propertyName, boolean defaultBoolean)
    {
    	this.propertyName = propertyName;
    	this.defaultValue = defaultBoolean;
    }
    /**
     * Returns the class of the default property. Posibilities are
     * {@link String#getClass()}, {@link Integer#getClass()}, {@link Double#getClass()}, or {@link Boolean#getClass()}.
     * @return Class of property.
     */
    public Class<?> getDefaultValueType()
    {
    	return defaultValue.getClass();
    }
    /**
     * Returns true if property value should be of type {@link String}.
     * @return True if property type is string.
     */
    public boolean isStringProperty()
    {
    	return defaultValue instanceof String;
    }
    /**
     * Returns true if property value should be of type {@link Boolean}.
     * @return True if property type is boolean.
     */
    public boolean isBooleanProperty()
    {
    	return defaultValue instanceof Boolean;
    }
    /**
     * Returns true if property value should be of type {@link Double}.
     * @return True if property type is double.
     */
    public boolean isDoubleProperty()
    {
    	return defaultValue instanceof Double;
    }
    /**
     * Returns true if property value should be of type {@link Integer}.
     * @return True if property type is integer.
     */
    public boolean isIntegerProperty()
    {
    	return defaultValue instanceof Integer;
    }
    /**
     * Returns the name of the property.
     * @return name of property.
     */
    public String getPropertyName()
    {
    	return propertyName;
    }
    /**
     * returns the default value of the property.
     * @return Default value of property.
     */
    public Object getDefaultValue()
    {
    	return defaultValue;
    }
    
    /**
     * Returns the default value as a string.
     * @return default value as string.
     */
    public String getDefaultAsString()
    {
    	if(defaultValue == null)
    		return "";
    	return defaultValue.toString();
    }
    /**
     * Returns the default value as an integer.
     * @return default value as integer.
     */
    public int getDefaultAsInteger()
    {
    	if(defaultValue == null)
    		return 0;
    	else if(defaultValue instanceof Number)
    		return ((Number) defaultValue).intValue();
    	else if(defaultValue instanceof Boolean)
    		return ((Boolean)defaultValue) ? 1 : 0;
    	else
    	{
    		try
    		{
    			return Integer.parseInt(defaultValue.toString());
    		}
    		catch(@SuppressWarnings("unused") NumberFormatException e)
    		{
    			return 0;
    		}
    	}
    }
    /**
     * Returns the default value as a double.
     * @return default value as double.
     */
    public double getDefaultAsDouble()
    {
    	if(defaultValue == null)
    		return 0;
    	else if(defaultValue instanceof Number)
    		return ((Number) defaultValue).doubleValue();
    	else if(defaultValue instanceof Boolean)
    		return ((Boolean)defaultValue) ? 1 : 0;
    	else
    	{
    		try
    		{
    			return Double.parseDouble(defaultValue.toString());
    		}
    		catch(@SuppressWarnings("unused") NumberFormatException e)
    		{
    			return 0;
    		}
    	}
    }
    /**
     * Returns the default value as a boolean.
     * @return default value as boolean.
     */
    public boolean getDefaultAsBoolean()
    {
    	if(defaultValue == null)
    		return false;
    	else if(defaultValue instanceof Number)
    		return ((Number) defaultValue).doubleValue() != 0;
    	else if(defaultValue instanceof Boolean)
    		return ((Boolean)defaultValue);
    	else
    	{
    		return Boolean.parseBoolean(defaultValue.toString());
    	}
    }
    @Override
    public String toString()
    {
    	return propertyName;
    }
}
