/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
package org.youscope.clientinterfaces;

import java.util.List;

import javax.script.ScriptEngineFactory;

import org.youscope.addon.AddonException;
import org.youscope.addon.AddonMetadata;
import org.youscope.addon.AddonUI;
import org.youscope.addon.component.ComponentAddonUI;
import org.youscope.addon.component.ComponentMetadata;
import org.youscope.addon.tool.ToolAddonUI;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.saving.MeasurementFileLocations;

/**
 * Provider with which YouScope client addons can be constructed.
 * @author Moritz Lang
 *
 */
public interface ClientAddonProvider
{	
	/**
	 * Creates a component addon user interface for the given type identifier.
	 * @param typeIdentifier type identifier of the addon/configuration.
	 * @return The created addon UI with the given type identifier.
	 * @throws AddonException Thrown if no factory for the given component addon exists, or if the creation of the addon failed.
	 */
	ComponentAddonUI<?> createComponentUI(String typeIdentifier) throws AddonException;
	
	/**
	 * Creates a component addon user interface with the given type identifier, producing component comfigurations of the given configuration class.
	 * @param typeIdentifier type identifier of the addon/configuration.
	 * @param configurationClass The configuration class which the addon user interface produces.
	 * @return The created addon UI with the given type identifier.
	 * @throws AddonException Thrown if no factory for the given component addon exists, or if the addon does not create configurations of the given configuration class.
	 */
	<T extends Configuration>ComponentAddonUI<? extends T> createComponentUI(String typeIdentifier, Class<T> configurationClass) throws AddonException;
	
	/**
	 * Creates an component addon user interface compatible with the configuration, and initializes it with the configuration.
	 * @param configuration Configuration to edit.
	 * @return The created component addon UI, initialized with the given configuration.
	 * @throws AddonException Thrown if no factory for the given component type exists, or if the addon does not create configurations of the given configuration class.
	 * @throws ConfigurationException Thrown if corresponding component addon was found, but configuration was invalid.
	 */
	<T extends Configuration>ComponentAddonUI<? extends T> createComponentUI(T configuration) throws AddonException, ConfigurationException;
	
	/**
	 * Creates an component addon user interface compatible with the configuration metadata.
	 * @param metadata The metadata for which a component addon UI should be constructed.
	 * @return The created component addon UI.
	 * @throws AddonException Thrown if no factory for the given component metadata exists.
	 */
	<T extends Configuration>ComponentAddonUI<T> createComponentUI(ComponentMetadata<T> metadata) throws AddonException;
	
	/**
	 * Returns the type identifiers of all component addons capable of consuming and creating configuration being sub-classes of the provided configuration class. 
	 * @param configurationClass Configuration class for which all component type identifiers of configurations being sub-classes of this configuration should be returned.
	 * @return All type identifiers of component conforming to the given configuration class.
	 */
	List<String> getComponentTypeIdentifiers(Class<? extends Configuration> configurationClass);
	
	/**
	 * Returns the type identifiers of all components. 
	 * @return All type identifiers of measurement components.
	 */
	List<String> getComponentTypeIdentifiers();
	
	/**
	 * Returns the metadata of all components being able to produce or consume sub-class of the provided configuration class. 
	 * @param configurationClass Configuration class for which all metadata of componentsshould be returned.
	 * @return All metadata of components conforming to the given configuration class.
	 */
	<T extends Configuration> List<ComponentMetadata<? extends T>> getComponentMetadata(Class<T> configurationClass);
	/**
	 * Returns the metadata of all components. 
	 * @return metadata of all components.
	 */
	List<ComponentMetadata<?>> getComponentMetadata();
	/**
	 * Returns the metadata of the component having the given type identifier
	 * @param typeIdentifier type identifier of the component. 
	 * @return The metadata of the component having given type identifier.
	 * @throws AddonException Thrown if no factory for the given component exists, or if the creation of the metadata failed.
	 */
	public ComponentMetadata<?> getComponentMetadata(String typeIdentifier) throws AddonException;
	
	/**
	 * Returns the metadata of the component having the given type identifier, with the component producing/consuming the given type of configurations.
	 * @param typeIdentifier type identifier of the component. 
	 * @param configurationClass Configuration class of the component.
	 * @return Configurations metadata having the given type identifier.
	 * @throws AddonException Thrown if no factory for the given component exists, if the creation of the metadata failed, or if component cannot consume/produce given configuration class.
	 */
	public <T extends Configuration> ComponentMetadata<? extends T> getComponentMetadata(String typeIdentifier, Class<T> configurationClass) throws AddonException;
	
	/**
	 * Creates a post processor user interface for the given type identifier.
	 * @param typeIdentifier type identifier of the post processor.
	 * @param measurementFileLocations Information on the folders where the measurement which should be processed is located.
	 * @return The created post processor UI with the given type identifier.
	 * @throws AddonException Thrown if no factory for the given post processor addon exists, or if the creation of the post processor failed.
	 */
	AddonUI<? extends AddonMetadata> createPostProcessorUI(String typeIdentifier,  MeasurementFileLocations measurementFileLocations) throws AddonException;
	
	/**
	 * Creates an post processor user interface compatible with the metadata.
	 * @param metadata The metadata for which a post processor addon UI should be constructed.
	 * @param measurementFileLocations Information on the folders where the measurement which should be processed is located.
	 * @return The created post processor addon UI.
	 * @throws AddonException Thrown if no factory for the given post processor metadata exists.
	 */
	<T extends AddonMetadata>AddonUI<T> createPostProcessorUI(T metadata,  MeasurementFileLocations measurementFileLocations) throws AddonException;
	
	/**
	 * Returns the type identifiers of all post processors. 
	 * @return All type identifiers of post processors.
	 */
	List<String> getPostProcessorTypeIdentifiers();

	/**
	 * Returns the metadata of all post processors. 
	 * @return metadata of all post processors. 
	 */
	List<AddonMetadata> getPostProcessorMetadata();
	/**
	 * Returns the metadata of the post processor having the given type identifier
	 * @param typeIdentifier type identifier of the post processor. 
	 * @return The metadata of the post processor having given type identifier.
	 * @throws AddonException Thrown if no factory for the given post processor exists, or if the creation of the metadata failed.
	 */
	public AddonMetadata getPostProcessorMetadata(String typeIdentifier) throws AddonException;
	
	/**
	 * Creates a tool user interface for the given type identifier.
	 * @param typeIdentifier type identifier of the tool.
	 * @return The created tool UI with the given type identifier.
	 * @throws AddonException Thrown if no factory for the given tool exists, or if the creation of the tool failed.
	 */
	ToolAddonUI createToolUI(String typeIdentifier) throws AddonException;
	
	/**
	 * Creates a tool user interface compatible with the metadata.
	 * @param metadata The metadata for which a tool UI should be constructed.
	 * @return The created tool UI.
	 * @throws AddonException Thrown if no factory for the given tool metadata exists.
	 */
	ToolAddonUI createToolUI(ToolMetadata metadata) throws AddonException;
	
	/**
	 * Returns the type identifiers of all tools. 
	 * @return All type identifiers of tools.
	 */
	List<String> getToolTypeIdentifiers();

	/**
	 * Returns the metadata of all tools. 
	 * @return metadata of all tools. 
	 */
	List<ToolMetadata> getToolMetadata();
	/**
	 * Returns the metadata of the tool having the given type identifier
	 * @param typeIdentifier type identifier of the tool. 
	 * @return The metadata of the tool having given type identifier.
	 * @throws AddonException Thrown if no factory for the given tool exists, or if the creation of the metadata failed.
	 */
	public ToolMetadata getToolMetadata(String typeIdentifier) throws AddonException;
	
    
    /**
	 * Returns all script engine factories supported by the client.
	 * @return List of all script engines.
	 */
	List<ScriptEngineFactory> getScriptEngineFactories();
	
	/**
	 * Returns a script engine factory with the given type identifier. 
	 * 
	 * @param typeIdentifier The type identifier/name of the script engine factory. Same as {@link ScriptEngineFactory#getEngineName()}.
	 * @return script engine factory for given type identifier.
	 * @throws AddonException Thrown if no script engine factory for the given type identifier exists.
	 */
	ScriptEngineFactory getScriptEngineFactory(String typeIdentifier) throws AddonException;
}
