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
/**
 * 
 */
package org.youscope.addon.measurement;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUI;
import org.youscope.addon.component.ComponentMetadata;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.serverinterfaces.ConstructionContext;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 */
public interface MeasurementAddonFactory
{
	/**
	 * Called when this addon should initialize the the measurement according to its configuration.
	 * @param measurement The measurement which should be initialized.
	 * @param configuration The configuration according to which the measurement should be initialized.
	 * @param constructionContext An interface to an object allowing to initialize the various measurement components.
	 * @throws ConfigurationException Thrown if the configuration is invalid.
	 * @throws AddonException Thrown if an error occurred during the initialization.
	 */
	void initializeMeasurement(Measurement measurement, MeasurementConfiguration configuration, ConstructionContext constructionContext) throws ConfigurationException, AddonException;
    /**
     * Returns a new measurement configuration addon for the given ID, or null if addon does not support the configuration of measurements witht the given ID.
     * @param typeIdentifier The type identifier of the measurement.
     * @param client Interface to allow the addon to communicate with the client.
     * @param server Interface to the server.
     * 
     * @return The created addon to configure a measurement.
     * @throws AddonException 
     */
	ComponentAddonUI<? extends MeasurementConfiguration> createMeasurementUI(String typeIdentifier, YouScopeClient client, YouScopeServer server) throws AddonException;

    /**
	 * Returns a list of all measurement configuration types supported by this addon
	 * 
	 * @return List of supported configurations.
	 */
	String[] getSupportedTypeIdentifiers();

	/**
	 * Returns true if this addon supports measurement configurations with the given ID, false otherwise.
	 * @param ID The ID of the measurement configuration for which it should be querried if this addon supports its construction.
	 * @return True if this addon supports measurement configurations with the given ID, false otherwise.
	 */
	boolean isSupportingTypeIdentifier(String ID);

	/**
	 * Returns the metadata (like human readable name) for a given measurement type.
	 * @param typeIdentifier The type identifier of the configuration for which the metadata should be returned.
	 * @return The metadata of the given configuration.
	 * @throws AddonException Thrown if identifier is not supported by the addon.
	 */
	ComponentMetadata<? extends MeasurementConfiguration> getComponentMetadata(String typeIdentifier) throws AddonException;
}
