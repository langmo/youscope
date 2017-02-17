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
package org.youscope.addon.tool;

import org.youscope.addon.AddonException;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author langmo
 */
public interface ToolAddonFactory
{

    /**
     * Returns the UI of a Tool for the given type identifier.
     * If tools with the given type identifier are not supported by this factory, throws an {@link AddonException}.
     * 
     * @param typeIdentifier The type identifier of the tool.
     * @param client YouScope client.
     * @param server YouScope server.
     * 
     * @return The created tool UI.
     * @throws AddonException Thrown if tools with given type identifier are not supported by this factory.* 
     */
    ToolAddonUI createToolUI(String typeIdentifier, YouScopeClient client, YouScopeServer server) throws AddonException;
    
    /**
	 * Returns all tool type identifiers supported by this addon
	 * 
	 * @return Tool type identifiers.
	 */
	String[] getSupportedTypeIdentifiers();

	/**
	 * Returns true if this addon supports tools with the given type identifier, false otherwise.
	 * 
	 * @param typeIdentifier The type identifier of the tool for which it should be queried if this addon supports it.
	 * @return True if this addon supports tools with the given type identifier, false otherwise.
	 */
	boolean isSupportingTypeIdentifier(String typeIdentifier);
    
    /**
	 * Returns the metadata (like human readable name) for the tool with the given type identifier.
	 * @param typeIdentifier The type identifier of the tool for which the metadata should be returned.
	 * @return The metadata of the given tool.
	 * @throws AddonException Thrown if type identifier is not supported by the factory.
	 */
	ToolMetadata getToolMetadata(String typeIdentifier) throws AddonException;
}
