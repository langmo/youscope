/**
 * 
 */
package ch.ethz.csb.youscope.client.addon.job;

import java.util.EventListener;

import ch.ethz.csb.youscope.client.addon.ConfigurationAddonListener;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;

/**
 * Listener which should be called by job configuration addons when configuration of a job finished.
 * @author Moritz Lang
 * @deprecated Use {@link ConfigurationAddonListener} instead.
 */
@Deprecated
public interface JobConfigurationAddonListener extends EventListener
{
	/**
     * Should be invoked when the configuration is finished.
     * @param configuration The finished configuration.
     */
    void jobConfigurationFinished(JobConfiguration configuration);
}
