/**
 * 
 */
package org.youscope.addon.postprocessing;

import org.youscope.addon.AddonException;
import org.youscope.addon.tool.ToolAddonUI;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 *
 */
public interface PostProcessorAddonFactory
{
	/**
     * Returns a new post-processing addon UI for the given type identifier, or null if factory does not support the post-processing type identifier.
     * @param typeIdentifier The type identifier for which a post-processor should be created.
     * @param client YouScope client.
     * @param server YouScope server.
	 * @param measurementFolder The folder where the measurement is saved which should be post-processed.
     * 
     * @return The created addon UI.
	 * @throws AddonException 
     */
    ToolAddonUI createPostProcessorUI(String typeIdentifier, YouScopeClient client, YouScopeServer server, String measurementFolder) throws AddonException;

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
