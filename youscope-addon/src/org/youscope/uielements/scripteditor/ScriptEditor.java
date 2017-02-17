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

import java.awt.Font;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.JScrollPane;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;


/**
 * An editor to edit script files of various types. Supports syntax highlighting and undo/redo actions.
 * 
 * @author Moritz Lang
 */
public class ScriptEditor extends JScrollPane implements UndoableEditListener
{
	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= 6468211923133983336L;
	private ScriptEditorPane				scriptPane = new ScriptEditorPane();
	private UndoManager						undoManager			= new UndoManager();
	
	private static final Font				EDITOR_FONT			= new Font(Font.MONOSPACED, Font.PLAIN, 12);
	
	private final ArrayList<UndoRedoListener> undoRedoListeners = new ArrayList<UndoRedoListener>();
	/**
	 * Constructor.
	 */
	public ScriptEditor()
	{
		scriptPane.setMargin(new Insets(0, 0, 0, 0));
		scriptPane.setFont(EDITOR_FONT);
		setViewportView(scriptPane); 
		ScriptEditorLineNumber lineNumber = new ScriptEditorLineNumber(scriptPane);
		setRowHeaderView(lineNumber);
		
		// Add Undo/Redo functionality
		scriptPane.getDocument().addUndoableEditListener(this);
		
		
	}
	
	@Override
	public void undoableEditHappened(UndoableEditEvent e)
	{
		undoManager.addEdit(e.getEdit());
		synchronized(undoRedoListeners)
		{
			for(UndoRedoListener listener : undoRedoListeners)
			{
				listener.undoRedoStateChanged();
			}
		}
	}
	
	/**
	 * Adds a listener which gets informed when an undo/redo event happened.
	 * @param listener Listener to be added.
	 */
	public void addUndoRedoListener(UndoRedoListener listener)
	{
		synchronized(undoRedoListeners)
		{
			undoRedoListeners.add(listener);
		}
	}
	/**
	 * Removes a previously added listener.
	 * @param listener Listener to be removed.
	 */
	public void removeUndoRedoListener(UndoRedoListener listener)
	{
		synchronized(undoRedoListeners)
		{
			undoRedoListeners.remove(listener);
		}
	}
	
	/**
	 * Undoes the last action, if possible.
	 * @throws CannotUndoException
	 */
	public void undo() throws CannotUndoException
	{
		undoManager.undo();
	}
	
	/**
	 * Returns true if the last action can be undone.
	 * @return True if undo is possible.
	 */
	public boolean canUndo()
	{
		return undoManager.canUndo();
	}
	
	/**
	 * Returns a description of the last action which can be undone, if available.
	 * @return Description of last undoable action.
	 */
	public String getUndoPresentationName()
	{
		return undoManager.getUndoPresentationName();
	}
	
	/**
	 * Redoes the last action which was undone, if available.
	 * @throws CannotRedoException
	 */
	public void redo() throws CannotRedoException
	{
		undoManager.redo();
	}
	
	/**
	 * Returns true if an action can be redone.
	 * @return True if redo is possible.
	 */
	public boolean canRedo()
	{
		return undoManager.canRedo();
	}
	
	/**
	 * Returns a description of the action which can be redone, if available.
	 * @return Description of redo action.
	 */
	public String getRedoPresentationName()
	{
		return undoManager.getRedoPresentationName();
	}
	
	/**
	 * Sets the extension of the file name which is displayed. Activates a search for a suitable code formatting for the file type, and sets
	 * this formatting if available, resp. a plain text formatting.
	 * @param extension Extension of the file which should be displayed by the editor, or null for plain formatting.
	 */
	public void setFileNameExtension(String extension)
	{
		scriptPane.setEditorKit(ScriptEditorKit.getEditorKitByFileNameExtension(extension));
	}
	
	/**
	 * Searches for a script style with the specific ID and sets the source code formatting of the editor to this style, if found.
	 * Sets the editor to plain text formatting if not found.
	 * @param scriptStyleID The ID of the script style, or null for plain formatting.
	 */
	public void setScriptStyleID(String scriptStyleID)
	{
		scriptPane.setEditorKit(ScriptEditorKit.getEditorKitByScriptStyleID(scriptStyleID));
	}
	
	/**
	 * Sets the text which is displayed (without formatting).
	 * @param content Content which should be displayed.
	 */
	public void setText(String content)
	{
		scriptPane.setText(content);
	}
	
	/**
	 * Returns the current text of the editor (without formatting).
	 * @return Content of the editor.
	 */
	public String getText()
	{
		return scriptPane.getText();
	}
	
	/**
	 * Marks that in the given line number an error occurred.
	 * @param lineNumber Index (starting at 0) of the line where the error occurred.
	 */
	public void setErrorInLine(int lineNumber)
	{
		scriptPane.setErrorInLine(lineNumber);
	}
}
