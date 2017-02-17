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
package org.youscope.addon.celldetection;

/**
 * A factory to create cell detection addons.
 * All implementations should export themselves with the help of Java's service loader technology.
 * @author Moritz Lang
 *
 */
public interface CellVisualizationAddonFactory
{
	/**
     * Returns a new Cell Visualization Addon for the given ID, or null if this factory does not support addons with the given ID.
     * @param ID The ID for which a Cell Visualization Addon should be created.
     * 
     * @return New Addon.
     */
    public CellVisualizationAddon createCellVisualizationAddon(String ID);    
    
    /**
	 * Returns a list of all Cell Visualization Addon IDs types supported by this factory.
	 * 
	 * @return List of supported Cell Visualization Addons.
	 */
	String[] getCellVisualizationAddonIDs();

	/**
	 * Returns true if this factory supports Cell Visualization Addons with the given ID, false otherwise.
	 * @param ID The ID of the Cell Visualization Addon for which it should be queried if this factory supports it.
	 * @return True if this factory supports addons with the given ID, false otherwise.
	 */
	boolean supportsCellVisualizationAddonID(String ID);
}
