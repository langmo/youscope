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
package org.youscope.addon.component;

import org.youscope.addon.AddonException;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.Component;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.serverinterfaces.ConstructionContext;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * General interface for all addons which create and provide a UI for the configuration of a measurement components.
 * See {@link ComponentAddonFactoryAdapter} for an adapter class to simplify implementation.
 * @author Moritz Lang
 *
 */
public interface ComponentAddonFactory
{
	/**
	 * Creates an addon for the given configuration.
     * Throws a construction addon exception if the configuration type provided is not supported.
     * 
	 * @param positionInformation The position in the measurement hierarchy where the addon should be constructed.
	 * @param configuration The configuration of the addon.
	 * @param constructionContext The context of the construction, providing information and functionality in the construction of the addon.
	 * @return The constructed addon.
	 * @throws ConfigurationException Thrown if the configuration is invalid.
	 * @throws AddonException Thrown if an error occurred during the construction.
	 */
	Component createComponent(PositionInformation positionInformation, Configuration configuration, ConstructionContext constructionContext) throws ConfigurationException, AddonException;

	/**
     * Returns a new UI to configure a component of the given type identifier.
     * @param typeIdentifier The ID for which a configuration addon should be created.
     * @param client Interface to allow the addon to communicate with the client.
     * @param server Interface to the server.
     * @return The created addon for a given configuration type.
     * @throws AddonException Thrown if configuration type identifier is not supported by this factory, or if any general error occured during the configuration addon creation.
     */
    ComponentAddonUI<?> createComponentUI(String typeIdentifier, YouScopeClient client, YouScopeServer server) throws AddonException;

    /**
	 * Returns a list of all component type identifiers supported by this factory.
	 * A type identifier is a string unique for a given component type, with which the component type
	 * is identified.
	 * 
	 * @return List of supported component types.
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
	ComponentMetadata<?> getComponentMetadata(String typeIdentifier) throws AddonException;
}
