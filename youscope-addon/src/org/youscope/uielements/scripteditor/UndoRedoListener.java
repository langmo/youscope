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
package org.youscope.uielements.scripteditor;

import java.util.EventListener;

/**
 * Interface to get informed when the undo/redo state changed. This information can be e.g. used to update undo/redo buttons, e.g. to deactivate the
 * undo button if undo is not possible.
 * @author Moritz Lang
 *
 */
public interface UndoRedoListener extends EventListener
{
	/**
	 * Fires when the undo/redo state changed (see interface description).
	 */
	public void undoRedoStateChanged();
}
