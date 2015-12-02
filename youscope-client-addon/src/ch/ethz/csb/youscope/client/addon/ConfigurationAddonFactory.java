/**
 * 
 */
package ch.ethz.csb.youscope.client.addon;

import ch.ethz.csb.youscope.client.addon.ConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationMetadata;

/**
 * A factory to get configuration addons for a given type ids.
 * @author Moritz Lang
 */
public interface ConfigurationAddonFactory 
{
	/**
     * Returns a new configuration addon for the given type identifier.
     * @param typeIdentifier The ID for which a configuration addon should be created.
     * @param client Interface to allow the addon to communicate with the client.
     * @param server Interface to the server.
     * @return The created addon for a given configuration type.
     * @throws AddonException Thrown if configuration type identifier is not supported by this factory, or if any general error occured during the configuration addon creation.
     */
    ConfigurationAddon<?> createConfigurationAddon(String typeIdentifier, YouScopeClient client, YouScopeServer server) throws AddonException;

    /**
	 * Returns a list of all configuration type identifiers supported by this factory.
	 * 
	 * @return List of supported configuration types.
	 */
	String[] getSupportedTypeIdentifiers();

	/**
	 * Returns true if this factory supports configurations with the given ID, false otherwise.
	 * @param typeIdentifier The ID of the configuration for which it should be queried if this factory supports its construction.
	 * @return True if this factory supports creating configurations with the given ID, false otherwise.
	 */
	boolean isSupportingTypeIdentifier(String typeIdentifier);
	
	/**
	 * Returns the metadata (like human readable name) for a given configuration type.
	 * @param typeIdentifier The type identifier of the configuration for which the metadata should be returned.
	 * @return The metadata of the given configuration.
	 * @throws AddonException Thrown if identifier is not supported by the addon.
	 */
	ConfigurationMetadata<?> getConfigurationMetadata(String typeIdentifier) throws AddonException;
}
