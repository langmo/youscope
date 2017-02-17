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
package org.youscope.addon.component.generic;

import java.awt.Component;
import java.awt.event.ActionListener;

import org.youscope.common.MessageListener;

/**
 * Interface all editors for certain types of properties of a configuration implement.
 * @author Moritz Lang
 *
 */
public interface PropertyEditor 
{
	/**
	 * Returns the UI element of the editor.
	 * @return UI element of the editor.
	 */
	Component getEditor();
	/**
	 * When called, editor commits all current edits.
	 * @throws GenericException
	 */
	void commitEdits() throws GenericException;
	
	/**
	 * Adds a listener to get notified about errors and similar.
	 * @param listener message listener to add.
	 */
	void addMessageListener(MessageListener listener);

	/**
	 * Removes a previously added listener
	 * @param listener message listener to remove.
	 */
	void removeMessageListener(MessageListener listener);
	
	/**
	 * Adds a listener to be notified when the value of the property edited changed.
	 * @param listener action listener.
	 */
	void addActionListener(ActionListener listener);
	
	/**
	 * Removes a previously added listener.
	 * @param listener listener to remove.
	 */
	void removeActionListener(ActionListener listener);
	
	/**
	 * Returns true if this editor can fill up extra space. Return false, if this would lead to an ugly UI.
	 * @return TRUE if editor can fill up extra space.
	 */
	boolean isFillSpace();
	
	/**
	 * Get the property types supported by this editor.
	 * @return Supported property types.
	 */
	Class<?>[] getSupportedTypes();
}
