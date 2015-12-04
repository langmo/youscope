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
