/**
 * 
 */
package org.youscope.addon.postprocessing;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 *
 */
public interface PostProcessorAddonFactory
{
	/**
     * Returns a new post-processing addon for the given ID, or null if addon does not support the post-processing with the given ID.
     * @param ID The ID for which a post-processor should be created.
     * @param client Interface to allow the addon to communicate with the client.
     * @param server Interface to the server.
	 * @param measurementFolder The folder where the measurement is saved.
     * 
     * @return The created addon to configure a measurement.
     */
    PostProcessorAddon createMeasurementConfigurationAddon(String ID, YouScopeClient client, YouScopeServer server, String measurementFolder);

    /**
	 * Returns a list of all measurement post-processor types supported by this addon.
	 * 
	 * @return List of supported post-processors.
	 */
	String[] getSupportedPostProcessorIDs();

	/**
	 * Returns true if this addon supports post-processors with the given ID, false otherwise.
	 * @param ID The ID of the measurement post-processor for which it should be queried if this addon supports its construction.
	 * @return True if this addon supports post-processors with the given ID, false otherwise.
	 */
	boolean supportsPostProcessorID(String ID);

    /**
     * Should return a short human readable name of the post-processor which corresponds to the given ID. If the addon does
     * not support the post-processor with the given ID, null should be returned.
     * @param ID The ID of the post-processor for which the human readable name should be returned.
     * 
     * @return Human readable name of the post-processor.
     */
    String getPostProcessorName(String ID);
}
