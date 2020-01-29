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
package org.youscope.addon.skin;

import org.youscope.addon.AddonException;
import org.youscope.addon.AddonMetadata;

/**
 * Factory to provide different skins for the YouScope client.
 * @author Moritz Lang
 *
 */
public interface SkinFactory
{
	/**
	 * Creates a skin for the given type identifier
     * Throws a addon exception if the type identifier is not supported.
	 * @param typeIdentifier Type identifier of the look and feel.
     * 
	 * @return The constructed addon.
	 * @throws AddonException Thrown if an error occurred during the construction.
	 */
	Skin createSkin(String typeIdentifier) throws AddonException;

    /**
	 * Returns a list of all skin type identifiers supported by this factory.
	 * A type identifier is a string unique for a given look and feel type, with which the skin type
	 * is identified.
	 * 
	 * @return List of supported skin types.
	 */
	String[] getSupportedTypeIdentifiers();

	/**
	 * Returns true if this factory supports skins with the given ID, false otherwise.
	 * @param typeIdentifier The ID of the skin for which it should be queried if this factory supports its construction.
	 * @return True if this factory supports creating skins with the given ID, false otherwise.
	 */
	boolean isSupportingTypeIdentifier(String typeIdentifier);
	
	/**
	 * Returns the metadata (like human readable name) for a given skin.
	 * @param typeIdentifier The type identifier of the skin for which the metadata should be returned.
	 * @return The metadata of the given skin.
	 * @throws AddonException Thrown if identifier is not supported by the addon.
	 */
	AddonMetadata getMetadata(String typeIdentifier) throws AddonException;
}
