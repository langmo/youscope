/**
 * 
 */
package ch.ethz.csb.youscope.client.addon.measurement;

import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.MeasurementConfiguration;

/**
 * @author langmo Interface for addons which implement custom jobs.
 */
public interface MeasurementConfigurationAddon
{
    /**
     * Should create the UI elements to configure the measurement in the given Frame.
     * However, the frame should not be made visible.
     * @param frame The frame in which the graphical elements to configure the measurements should be initialized.
     * 
     */
    void createUI(YouScopeFrame frame);

    /**
     * Initializes the addon to the configuration data.
     * 
     * @param measurementConfiguration The configuration data.
     * @throws ConfigurationException 
     */
    void setConfigurationData(MeasurementConfiguration measurementConfiguration) throws ConfigurationException;

    /**
     * Returns the configuration data.
     * 
     * @return Configuration data of this measurement.
     */
    MeasurementConfiguration getConfigurationData();
    
    /**
     * Adds a listener to this configuration, which should e.g. be informed if the configuration finished.
     * @param listener The listener to add.
     */
    void addConfigurationListener(MeasurementConfigurationAddonListener listener);
    
    /**
     * Removes a previously added listener.
     * @param listener The listener to remove.
     */
    void removeConfigurationListener(MeasurementConfigurationAddonListener listener);
}
