/**
 * 
 */
package ch.ethz.csb.youscope.client.addon.job;

import javax.swing.ImageIcon;

import ch.ethz.csb.youscope.client.addon.ConfigurationAddonFactory;
import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;

/**
 * @author Moritz Lang
 * @deprecated Use {@link ConfigurationAddonFactory} instead.
 */
@Deprecated
public interface JobConfigurationAddonFactory
{
	/**
     * Returns a new job configuration addon for the given ID, or null if addon does not support the configuration of jobs with the given ID.
     * @param ID The ID for which a job should be created.
     * @param client Interface to allow the addon to communicate with the client.
     * @param server Interface to the server.
     * 
     * @return The created addon to configure a job.
     */
    JobConfigurationAddon createJobConfigurationAddon(String ID, YouScopeClient client, YouScopeServer server);

    /**
     * Returns the implementation (not an interface) of the configurations produced by the addon with the given ID, or null, if this addon does not support the configuration of jobs with the given ID.
     * YouScope uses the class definition internally to obtain information about the functionality (=implemented interfaces) of the respective configuration, e.g. to search for
     * job configurations with a given functionality. 
     * @param ID The ID for which a job should be created.
     * @return The class of the job implementation, or null.
     */
    Class<? extends JobConfiguration> getConfigurationClass(String ID);
    
    /**
	 * Returns a list of all job configuration types supported by this addon
	 * 
	 * @return List of supported configurations.
	 */
	String[] getSupportedConfigurationIDs();

	/**
	 * Returns true if this addon supports job configurations with the given ID, false otherwise.
	 * @param ID The ID of the job configuration for which it should be queried if this addon supports its construction.
	 * @return True if this addon supports job configurations with the given ID, false otherwise.
	 */
	boolean supportsConfigurationID(String ID);

    /**
     * Should return a short human readable name of the job which corresponds to the given ID. If the addon does
     * not support the configuration of jobs with the given ID, null should be returned.
     * The name may or may not consist of sub-strings separated by slashes (e.g. "Misc/Foo"). The last string corresponds to the base name.
     * In this case, a client may or may not display some kind of folder structure to navigate to a given addon.
     * @param ID The ID of the job for which the human readable name should be returned.
     * 
     * @return Human readable name of the job.
     */
    String getJobName(String ID);
    
    /**
     * Returns an icon for this job, or null, if this job does not have an own icon.
     * @param ID The ID of the job for which the icon should be returned.
     * @return Icon for the job, or null.
     */
    ImageIcon getJobIcon(String ID);
}
