/**
 * 
 */
package ch.ethz.csb.youscope.client.addon;

/**
 * Interface through which an addon can save properties as well as access the properties of the YouScope client.
 * 
 * @author langmo
 */
public interface YouScopeProperties 
{
    /**
     * Name of the boolean property indicating if camera settings should be preinitialized.
     */
    public final static String PROPERTY_PREINITIALIZE_CAMERA_SETTINGS = "CSB::Measurement::cameraStartupSettings";

    /**
     * Name of the boolean property indicating if a measurement control should be docked to the main window (true) or shown in an own window.
     */
    public final static String PROPERTY_DOCK_MEASUREMENT_CONTROL = "CSB::Client::dockMeasurementControl";

    /**
     * Name of boolean property indicating if youscope is configured.
     */
    public final static String PROPERTY_IS_CONFIGURED = "CSB::isConfigured";

    /**
     * Name of the String property indicating where the last measurement was saved.
     */
    public final static String PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER = "CSB::Measurement::lastMeasurementSaveFolder";

    /**
     * Name of the last channel group used in a channel configuration.
     */
    public final static String PROPERTY_LAST_CHANNEL_GROUP = "CSB::Imaging::lastChannelGroup";
    
    /**
     * Name of the last channel used in a channel configuration.
     */
    public final static String PROPERTY_LAST_CHANNEL = "CSB::Imaging::lastChannel";

    /**
     * Last path to the folder where scripts where loaded or saved.
     */
    public final static String PROPERTY_LAST_SCRIPT_PATH = "CSB::Scripting::lastScriptPath";
    
    /**
     * If and how the positions of frames should be stored. Check enum FramePositionStorage.StorageType for options.
     */
    public final static String PROPERTY_POSITION_STORAGE = "CSB::FramePositionStorage";
    
    /**
     * Last absolute path of an image directly saved to disk from e.g. the LiveView.
     */
    public final static String PROPERTY_LAST_IMAGE_SAVE_FILE_NAME = "CSB::LastImageSaveFileName";

    /**
     * Last exposure time in ms used in live stream, and similar.
     */
    public final static String PROPERTY_LAST_LIVE_EXPOSURE = "CSB::LiveStream::Exposure";
    /**
     * Last period in ms used in live stream, and similar.
     */
    public final static String PROPERTY_LAST_LIVE_PERIOD = "CSB::LiveStream::Period";
    /**
     * Name of last camera (or empty string) used in live stream, and similar.
     */
    public final static String PROPERTY_LAST_LIVE_CAMERA = "CSB::LiveStream::Camera";
    
    /**
     * if contrast should be increased in live stream, and similar.
     */
    public final static String PROPERTY_LAST_LIVE_INCREASE_CONTRAST = "CSB::LiveStream::IncreaseContrast";
    /**
     * if contrast should be increased automatically in live stream, and similar.
     */
    public final static String PROPERTY_LAST_LIVE_AUTO_CONTRAST = "CSB::LiveStream::AutoContrast";
        
    /**
     * Returns the property with the given name, or default value if the property is not yet set or set invalidly. Properties are saved permanently,
     * such that a property set e.g. in one session can be re-obtained in another.
     * 
     * @param name The name of the property.
     * @param defaultValue Default value to return if property is not set or invalid.
     * @return The value of the property with the given name, or the default value.
     */
    String getProperty(String name, String defaultValue);

    /**
     * Returns the property with the given name, or default value if the property is not yet set or set invalidly. Properties are saved permanently,
     * such that a property set e.g. in one session can be re-obtained in another.
     * 
     * @param name The name of the property.
     * @param defaultValue Default value to return if property is not set or invalid.
     * @return The value of the property with the given name, or the default value.
     */
    int getProperty(String name, int defaultValue);

    /**
     * Returns the property with the given name, or default value if the property is not yet set or set invalidly. Properties are saved permanently,
     * such that a property set e.g. in one session can be re-obtained in another.
     * 
     * @param name The name of the property.
     * @param defaultValue Default value to return if property is not set or invalid.
     * @return The value of the property with the given name, or the default value.
     */
    double getProperty(String name, double defaultValue);

    /**
     * Returns the property with the given name, or default value if the property is not yet set or set invalidly. Properties are saved permanently,
     * such that a property set e.g. in one session can be re-obtained in another.
     * 
     * @param name The name of the property.
     * @param defaultValue Default value to return if property is not set or invalid.
     * @return The value of the property with the given name, or the default value.
     */
    boolean getProperty(String name, boolean defaultValue);

    /**
     * Sets the property with the given name to the given value. Properties are saved permanently, such that a property set e.g. in one session can be
     * re-obtained in another.
     * 
     * @param name Name of the property to set.
     * @param value Value to set the property to.
     */
    void setProperty(String name, String value);

    /**
     * Sets the property with the given name to the given value. Properties are saved permanently, such that a property set e.g. in one session can be
     * re-obtained in another.
     * 
     * @param name Name of the property to set.
     * @param value Value to set the property to.
     */
    void setProperty(String name, int value);

    /**
     * Sets the property with the given name to the given value. Properties are saved permanently, such that a property set e.g. in one session can be
     * re-obtained in another.
     * 
     * @param name Name of the property to set.
     * @param value Value to set the property to.
     */
    void setProperty(String name, double value);

    /**
     * Sets the property with the given name to the given value. Properties are saved permanently, such that a property set e.g. in one session can be
     * re-obtained in another.
     * 
     * @param name Name of the property to set.
     * @param value Value to set the property to.
     */
    void setProperty(String name, boolean value);

    /**
     * Returns the property with the given name, or default value if the property is not yet set or set invalidly. Properties are saved permanently,
     * such that a property set e.g. in one session can be re-obtained in another.
     * 
     * @param name The name of the property.
     * @param defaultValue Default value to return if property is not set or invalid.
     * @return The value of the property with the given name, or the default value.
     */
    String[] getProperty(String name, String[] defaultValue);
}
