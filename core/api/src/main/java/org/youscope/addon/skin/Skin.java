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

import javax.swing.UIManager;

import org.youscope.addon.AddonException;
import org.youscope.addon.AddonMetadata;

/**
 * Interface providing all needed functionality for a YouScope skin.
 * @author Moritz Lang
 *
 */
public interface Skin 
{
	/**
	 * Returns the metadata (like human readable name) for the look and feel.
	 * @return The metadata of the given look and feel.
	 */
	AddonMetadata getMetadata();
	
	/**
	 * When this function is called, the addon should set its look and feel by calling {@link UIManager#setLookAndFeel(javax.swing.LookAndFeel)} or {@link UIManager#setLookAndFeel(String)}.
	 * @throws AddonException Thrown if look and feel cannot be set.
	 */
	void applySkin() throws AddonException;
}
