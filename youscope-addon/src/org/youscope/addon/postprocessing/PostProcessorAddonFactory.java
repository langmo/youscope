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
package org.youscope.addon.postprocessing;

import org.youscope.addon.AddonException;
import org.youscope.addon.AddonMetadata;
import org.youscope.addon.AddonUI;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.saving.MeasurementFileLocations;
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
	 * @param measurementFileLocations Informations on the folders where the measurement is saved which should be post-processed.
     * 
     * @return The created addon UI.
	 * @throws AddonException  Thrown if type identifier is not supported by this factory, or if an error in the UI construction occured.
     */
	AddonUI<? extends AddonMetadata> createPostProcessorUI(String typeIdentifier, YouScopeClient client, YouScopeServer server, MeasurementFileLocations measurementFileLocations) throws AddonException;

    /**
	 * Returns all measurement post-processor type identifiers supported by this addon.
	 * 
	 * @return post-processor type identifiers.
	 */
	String[] getSupportedTypeIdentifiers();

	/**
	 * Returns true if this factory supports creation of post-processors with the given type identifiers, and false otherwise.
	 * @param typeIdentifier The type identifier of the measurement post-processor for which it should be queried if this factory supports its construction.
	 * @return True if this factory supports post-processors with the given type identifier, and false otherwise.
	 */
	boolean isSupportingTypeIdentifier(String typeIdentifier);

	/**
	 * Returns the metadata (like human readable name) for a given post processor type identifier.
	 * @param typeIdentifier The type identifier of the post processor for which the metadata should be returned.
	 * @return The metadata of the given post processor.
	 * @throws AddonException Thrown if identifier is not supported by the factory.
	 */
	AddonMetadata getPostProcessorMetadata(String typeIdentifier) throws AddonException;
}
