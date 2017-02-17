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
package org.youscope.addon;

import org.youscope.addon.AddonException;
import org.youscope.clientinterfaces.YouScopeFrame;

/**
 * Provides the user interface of an addon.
 * @author Moritz Lang.
 * @param <T> Type of addon metadata.
 */
public interface AddonUI<T extends AddonMetadata>
{
	/**
	 * Creates a frame containing the UI elements of this addon. The returned frame should, yet, not be visible (<code>YouScopeFrame.setVisible(true)</code> should be
	 * called by the invoker of this function). A new frame can be created by calling <code>YouScopeClient.createFrame()</code>. The caller can decide to add this frame
	 * as a child or modal child frame to the frame the caller elements are displayed in by calling on its frame <code>addChildFrame()</code> or <code>addModalChildFrame()</code>.
	 * Only one of the functions <code>toFrame()</code> or <code>toPanel()</code> must be called for a given addon. Furthermore, this function must not be called more than once.
	 * To create more than one UI representation of a given addon, create a second addon UI using the corresponding factory.
	 * @return The frame containing the UI elements of the addon.
	 * @throws AddonException thrown if an error occurs during construction of the UI.
	 */
	YouScopeFrame toFrame() throws AddonException;
	
	/**
	 * Creates a UI component (usually a panel) containing the UI elements of this addon. 
	 * The tool should not close the containing frame, nor provide UI elements (e.g. buttons) closing the frame when invoked.
	 * Only one of the functions <code>toFrame()</code> or <code>toPanel()</code> must be called for a given tool. Furthermore, this function must not be called more than once.
	 * To create more than one UI representation of a given addon, create a second addon UI using the corresponding factory.
	 * @param containingFrame The frame containing the UI elements. Note that this frame is not necessarily visible when this function is invoked. The reference to the containing frame can e.g. 
	 * be used to register frame listeners to get notified when the containing frame opens or closes.
	 * @return The AWT component containing the UI elements of the addon.
	 * @throws AddonException thrown if an error occurs during creation of the addon UI.
	 */
	java.awt.Component toPanel(YouScopeFrame containingFrame) throws AddonException;

    /**
     * Returns the metadata (like human readable name) for the addon.
     * @return Metadata of the addon.
     */
    T getAddonMetadata();
}
