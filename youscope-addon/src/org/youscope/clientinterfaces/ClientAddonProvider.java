package org.youscope.clientinterfaces;

import java.util.List;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUI;
import org.youscope.addon.component.ComponentMetadata;
import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.ConfigurationException;

/**
 * Provider with which YouScop client addons can be constructed.
 * @author Moritz Lang
 *
 */
public interface ClientAddonProvider
{	
	/**
	 * Creates a configuration addon with the given ID.
	 * @param typeIdentifier type identifier of the addon/configuration.
	 * @return The created addon with the given type ID.
	 * @throws AddonException Thrown if no factory for the given configuration addon exists, or if the creation of the addon failed.
	 */
	ComponentAddonUI<?> createComponentAddonUI(String typeIdentifier) throws AddonException;
	
	/**
	 * Creates a configuration addon with the given ID and configuration class.
	 * @param typeIdentifier type identifier of the addon/configuration.
	 * @param configurationClass The configuration class which should be created by the factory.
	 * @return The created addon with the given type ID.
	 * @throws AddonException Thrown if no factory for the given configuration addon exists, or if the addon does not create configurations of the given configuration class.
	 */
	<T extends Configuration>ComponentAddonUI<? extends T> createComponentAddonUI(String typeIdentifier, Class<T> configurationClass) throws AddonException;
	
	/**
	 * Creates an configuration addon compatible with the configuration, and initializes it with the configuration.
	 * @param configuration Configuration to edit.
	 * @return The created addon, initialized with the given configuration.
	 * @throws AddonException Thrown if no factory for the given configuration type exists, or if the addon does not create configurations of the given configuration class.
	 * @throws ConfigurationException Thrown if corresponding addon was found, but configuration was invalid.
	 */
	<T extends Configuration>ComponentAddonUI<? extends T> createComponentAddonUI(T configuration) throws AddonException, ConfigurationException;
	
	/**
	 * Creates an configuration addon compatible with the configuration metadata.
	 * @param metadata The metadata for which a configuration addon should be constructed.
	 * @return The created addon, initialized with the given configuration.
	 * @throws AddonException Thrown if no factory for the given configuration metadata type exists.
	 */
	<T extends Configuration>ComponentAddonUI<T> createComponentAddonUI(ComponentMetadata<T> metadata) throws AddonException;
	
	/**
	 * Returns the type identifiers of all configurations being a sub-class of the provided configuration class. 
	 * @param configurationClass Configuration class for which all type identifiers of configurations being sub-classes of this configuration should be returned.
	 * @return All type identifiers of configurations conforming to the given configuration class.
	 */
	List<String> getComponentTypeIdentifiers(Class<? extends Configuration> configurationClass);
	
	/**
	 * Returns the metadata of all configurations being a sub-class of the provided configuration class. 
	 * @param configurationClass Configuration class for which all metadata of configurations being sub-classes of this configuration should be returned.
	 * @return All metadata of configurations conforming to the given configuration class.
	 */
	<T extends Configuration> List<ComponentMetadata<? extends T>> getComponentMetadata(Class<T> configurationClass);
	/**
	 * Returns the metadata of all configurations. 
	 * @return All metadata of configurations.
	 */
	List<ComponentMetadata<?>> getComponentMetadata();
	/**
	 * Returns the metadata of the configuration having the given type identifier
	 * @param typeIdentifier type identifier of the addon/configuration. 
	 * @return Configurations metadata having given type identifier.
	 * @throws AddonException Thrown if no factory for the given configuration addon exists, or if the creation of the addon failed.
	 */
	public ComponentMetadata<?> getComponentMetadata(String typeIdentifier) throws AddonException;
	
	/**
	 * Returns the metadata of the configuration having the given type identifier and given type of configuration.
	 * @param typeIdentifier type identifier of the addon/configuration. 
	 * @param configurationClass Configuration class for which metadata should be returned.
	 * @return Configurations metadata having given type identifier.
	 * @throws AddonException Thrown if no factory for the given configuration addon exists, if the creation of the addon failed, or if metadata is not metadata of given configuration class.
	 */
	public <T extends Configuration> ComponentMetadata<? extends T> getComponentMetadata(String typeIdentifier, Class<T> configurationClass) throws AddonException;
}
