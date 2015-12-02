/**
 * 
 */
package ch.ethz.csb.youscope.client.addon.job;

import ch.ethz.csb.youscope.client.addon.ConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;

/**
 * Interface for addons which implement custom jobs.
 * @author langmo 
 * @deprecated Use {@link ConfigurationAddon} instead.
 */
@Deprecated
public interface JobConfigurationAddon
{
	/**
     * Should create the UI elements to configure the job in the given Frame.
     * However, the frame should not be made visible.
     * @param frame The frame in which the graphical elements to configure the job should be initialized.
     * 
     */
    void createUI(YouScopeFrame frame);

    /**
     * Initializes the panel to the configuration data.
     * 
     * @param job The configuration data.
     * @throws ConfigurationException 
     */
    void setConfigurationData(JobConfiguration job) throws ConfigurationException;

    /**
     * Returns the configuration data.
     * 
     * @return Configuration data or NULL, if job was not yet configured.
     */
    JobConfiguration getConfigurationData();
    
    /**
     * Adds a listener to this configuration, which should e.g. be informed if the configuration finished.
     * @param listener The listener to add.
     */
    void addConfigurationListener(JobConfigurationAddonListener listener);
    
    /**
     * Removes a previously added listener.
     * @param listener The listener to remove.
     */
    void removeConfigurationListener(JobConfigurationAddonListener listener);
    
    /**
	 * Returns the configuration ID of this addon.
	 * 
	 * @return Configuration ID of this addon
	 */
	String getConfigurationID();
}
