package ch.ethz.csb.youscope.client.addon;

import java.util.List;

import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.configuration.Configuration;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationMetadata;

/**
 * Provider with which YouScop client addons can be constructed.
 * @author Moritz Lang
 *
 */
public interface ClientAddonProvider
{
	/**
	 * Returns all configuration factories.
	 * @return all configuration factories.
	 */
	List<ConfigurationAddonFactory> getConfigurationAddonFactories();
	
	/**
	 * Returns a configuration addon factory which can create configuration addons with the given ID.
	 * If no such factory exists, a configuration addon exception is thrown.
	 * @param typeIdentifier The identifier of the configuration addon for which a factory should be returned.
	 * @return A factory which can create configuration addons with the given ID.
	 * @throws AddonException Thrown if no factory for the given configuration ID exists.
	 */
	ConfigurationAddonFactory getConfigurationAddonFactory(String typeIdentifier) throws AddonException;
	
	/**
	 * Creates a configuration addon with the given ID.
	 * @param typeIdentifier type identifier of the addon/configuration.
	 * @return The created addon with the given type ID.
	 * @throws AddonException Thrown if no factory for the given configuration addon exists, or if the creation of the addon failed.
	 */
	ConfigurationAddon<?> createConfigurationAddon(String typeIdentifier) throws AddonException;
	
	/**
	 * Creates a configuration addon with the given ID and configuration class.
	 * @param typeIdentifier type identifier of the addon/configuration.
	 * @param configurationClass The configuration class which should be created by the factory.
	 * @return The created addon with the given type ID.
	 * @throws AddonException Thrown if no factory for the given configuration addon exists, or if the addon does not create configurations of the given configuration class.
	 */
	<T extends Configuration>ConfigurationAddon<? extends T> createConfigurationAddon(String typeIdentifier, Class<T> configurationClass) throws AddonException;
	
	/**
	 * Creates an configuration addon compatible with the configuration, and initializes it with the configuration.
	 * @param configuration Configuration to edit.
	 * @return The created addon, initialized with the given configuration.
	 * @throws AddonException Thrown if no factory for the given configuration type exists, or if the addon does not create configurations of the given configuration class.
	 * @throws ConfigurationException Thrown if corresponding addon was found, but configuration was invalid.
	 */
	<T extends Configuration>ConfigurationAddon<? extends T> createConfigurationAddon(T configuration) throws AddonException, ConfigurationException;
	
	/**
	 * Creates an configuration addon compatible with the configuration metadata.
	 * @param metadata The metadata for which a configuration addon should be constructed.
	 * @return The created addon, initialized with the given configuration.
	 * @throws AddonException Thrown if no factory for the given configuration metadata type exists.
	 */
	<T extends Configuration>ConfigurationAddon<T> createConfigurationAddon(ConfigurationMetadata<T> metadata) throws AddonException;
	
	/**
	 * Returns the type identifiers of all configurations being a sub-class of the provided configuration class. 
	 * @param configurationClass Configuration class for which all type identifiers of configurations being sub-classes of this configuration should be returned.
	 * @return All type identifiers of configurations conforming to the given configuration class.
	 */
	List<String> getConformingTypeIdentifiers(Class<? extends Configuration> configurationClass);
	
	/**
	 * Returns the metadata of all configurations being a sub-class of the provided configuration class. 
	 * @param configurationClass Configuration class for which all metadata of configurations being sub-classes of this configuration should be returned.
	 * @return All metadata of configurations conforming to the given configuration class.
	 */
	<T extends Configuration> List<ConfigurationMetadata<? extends T>> getConformingConfigurationMetadata(Class<T> configurationClass);
	/**
	 * Returns the metadata of all configurations. 
	 * @return All metadata of configurations.
	 */
	List<ConfigurationMetadata<?>> getConfigurationMetadata();
}
