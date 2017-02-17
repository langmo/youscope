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
package org.youscope.plugin.scripting;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;

import javax.script.ScriptException;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.uielements.ImageLoadingTools;
import org.youscope.uielements.scripteditor.ScriptEditor;
import org.youscope.uielements.scripteditor.UndoRedoListener;

/**
 * @author langmo
 * 
 */
class ScriptEditorFrame implements UndoRedoListener
{
	private final ScriptEditor scriptEditor = new ScriptEditor();
	
	private UndoAction						undoAction			= new UndoAction();
	private RedoAction						redoAction			= new RedoAction();

	private File							lastFile			= null;
	private String[]						fileExtensions		= null;
	private final YouScopeClient	client;
	private final YouScopeFrame				frame;
	
	/**
	 * Listeners which should be notified when a script should be executed.
	 */
	private Vector<EvaluationListener>		evaluationListeners	= new Vector<EvaluationListener>();

	ScriptEditorFrame(YouScopeClient client, YouScopeFrame frame)
	{
		this(client, frame, null);
	}

	ScriptEditorFrame(YouScopeClient client, YouScopeFrame frame, File file)
	 {
		 this.client = client;
		 this.frame = frame;
		 
		frame.setClosable(true);
		frame.setMaximizable(true);
		frame.setResizable(true);
		frame.setTitle("Script Editor");
		 
		scriptEditor.addUndoRedoListener(this);
				
		 // Initialize tool bar
		 JToolBar toolBar = new JToolBar();
		 toolBar.setFloatable(false);
		 toolBar.setRollover(true);
		 // open/save
		 toolBar.add(new OpenAction());
		 toolBar.add(new SaveAction());
		 toolBar.add(new SaveAsAction());
		 toolBar.addSeparator();
		 // Redo/Undo
		 toolBar.add(undoAction);
		 toolBar.add(redoAction);
		 toolBar.addSeparator();
		 // Debugging
		 toolBar.add(new RunAction());
		 
		  // Open the file which should be loaded.
		  if(file != null)
			  openFile(file);
		  
		  
		  
		  JPanel contentPane = new JPanel(new BorderLayout());
		  contentPane.add(toolBar, BorderLayout.NORTH);
		  contentPane.add(scriptEditor, BorderLayout.CENTER);
		  
		  frame.setContentPane(contentPane);
		  frame.setSize(new Dimension(500,400));
		  frame.setVisible(true);
	 }

	public void addEvaluationListener(EvaluationListener listener)
	{
		synchronized(evaluationListeners)
		{
			evaluationListeners.add(listener);
		}
	}

	public void removeEvaluationListener(EvaluationListener listener)
	{
		synchronized(evaluationListeners)
		{
			evaluationListeners.remove(listener);
		}
	}

	private void evalFile(File file)
	{
		synchronized(evaluationListeners)
		{
			try
			{
				for(EvaluationListener listener : evaluationListeners)
				{
					listener.evalFile(file);
				}
			}
			catch(ScriptException e)
			{
				if(e.getLineNumber() <= 0)
					return;
				
				scriptEditor.setErrorInLine(e.getLineNumber());
			}
		}
	}

	public void setFileExtensions(String[] fileExtensions)
	{
		this.fileExtensions = fileExtensions;
	}

	private void runScript()
	{
		saveFile(lastFile);
		evalFile(lastFile);
	}

	private void updateScriptStyle(File file)
	{
		if(file == null || file.getName() == null)
		{
			scriptEditor.setFileNameExtension(null);
			return;
		}
		String lastFileName = file.getName();
		int pointIndex = lastFileName.lastIndexOf(".");
		if(pointIndex < 0 || pointIndex >= lastFileName.length() - 1)
		{
			scriptEditor.setFileNameExtension(null);
			return;
		}
		scriptEditor.setFileNameExtension(lastFileName.substring(pointIndex + 1));
		return;
		
	}
	
	private void openFile(File file)
	 {
		 if(file == null)
		 {
		 	String folderToOpen = (String) client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_LAST_SCRIPT_PATH);
			JFileChooser fileChooser = new JFileChooser(folderToOpen);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if(fileExtensions != null && fileExtensions.length > 0)
			{
				for(String fileExtension : fileExtensions)
				{
					fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Script File (" + fileExtension + ")", fileExtension));
				}
			}
			if(lastFile != null)
				fileChooser.setSelectedFile(lastFile);
			
			int returnVal = fileChooser.showDialog(null, "Load");
			if(returnVal != JFileChooser.APPROVE_OPTION)
				return;
			file = fileChooser.getSelectedFile();
		
		 }			
		 
		 // Set script layout
		 updateScriptStyle(file);
		 
		 // Load file
		FileReader fileReader = null;
        BufferedReader bufferedReader = null;	
		try
        {
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            String content = "";
            while (true)
            {
                String line = bufferedReader.readLine();
                if (line == null)
                    break;
                content += line + "\n";
            }
            scriptEditor.setText(content);
        } 
		catch (Exception e)
        {
        	client.sendError("Could not load script file.", e);
            return;
        } 
		finally
        {
            if (fileReader != null)
            {
                try
                {
                    fileReader.close();
                } 
                catch (IOException e)
                {
                	client.sendError("Could not close script open stream.", e);
                }
            }
            if (bufferedReader != null)
            {
                try
                {
                    bufferedReader.close();
                }
                catch (IOException e)
                {
                	client.sendError("Could not close script open stream.", e);
                }
            }
        }
        client.getPropertyProvider().setProperty(StandardProperty.PROPERTY_LAST_SCRIPT_PATH, file.getParent());
        lastFile = file; 
        //scriptPane.setFileNameExtension(file.)
        frame.setTitle(lastFile.toString() + " - Script Editor");
	 }

	private void saveFile(File file)
	{
		if(file == null)
		{
			String folderToOpen = (String) client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_LAST_SCRIPT_PATH);
			JFileChooser fileChooser = new JFileChooser(folderToOpen);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if(fileExtensions != null && fileExtensions.length > 0)
			{
				fileChooser.setSelectedFile(new File("unnamed" + fileExtensions[0]));
				for(String fileExtension : fileExtensions)
				{
					fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Script File (" + fileExtension + ")", fileExtension));
				}
			}
			else
			{
				fileChooser.setSelectedFile(new File("unnamed.txt"));
			}
			int returnVal = fileChooser.showDialog(null, "Save");
			if(returnVal == JFileChooser.APPROVE_OPTION)
			{
				file = fileChooser.getSelectedFile();
			}
			else
			{
				return;
			}
		}
		String text = scriptEditor.getText();
		try
		{
			PrintStream fileStream = new PrintStream(file);
			fileStream.print(text);
			fileStream.close();
		}
		catch(Exception e)
		{
			client.sendError("Could not save file.", e);
			return;
		}
		client.getPropertyProvider().setProperty(StandardProperty.PROPERTY_LAST_SCRIPT_PATH, file.getParent());
		lastFile = file;
		frame.setTitle(lastFile.toString() + " - Script Editor");
		openFile(file);
	}

	

	private class UndoAction extends AbstractAction
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -6048441076343951413L;

		public UndoAction()
		{
			super("");
			Icon icon = ImageLoadingTools.getResourceIcon("icons/arrow-return-270-left.png", "undo");
			if(icon != null)
				putValue(SMALL_ICON, icon);
			else
				putValue(NAME, "undo");
			
			updateUndoState();
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			try
			{
				scriptEditor.undo();
			}
			catch(CannotUndoException ex)
			{
				client.sendError("Unable to undo.", ex);
			}
			updateUndoState();
			redoAction.updateRedoState();
		}

		protected void updateUndoState()
		{
			if(scriptEditor != null && scriptEditor.canUndo())
			{
				setEnabled(true);
				putValue(SHORT_DESCRIPTION, scriptEditor.getUndoPresentationName());
			}
			else
			{
				setEnabled(false);
				putValue(SHORT_DESCRIPTION, "No actions can be undone");
			}
		}
	}

	private class RedoAction extends AbstractAction
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= 5732259944443930627L;

		public RedoAction()
		{
			super("");
			Icon icon = ImageLoadingTools.getResourceIcon("icons/arrow-return-270.png", "redo");
			if(icon != null)
				putValue(SMALL_ICON, icon);
			else
				putValue(NAME, "redo");

			updateRedoState();
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			try
			{
				scriptEditor.redo();
			}
			catch(CannotRedoException ex)
			{
				client.sendError("Unable to redo.", ex);
			}
			updateRedoState();
			undoAction.updateUndoState();
		}

		protected void updateRedoState()
		{
			if(scriptEditor != null &&scriptEditor.canRedo())
			{
				setEnabled(true);
				putValue(SHORT_DESCRIPTION, scriptEditor.getRedoPresentationName());
			}
			else
			{
				setEnabled(false);
				putValue(SHORT_DESCRIPTION, "No actions can be redone");
			}
		}
	}

	private class SaveAction extends AbstractAction
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -5540629005472843104L;

		SaveAction()
		{
			super("");
			Icon icon = ImageLoadingTools.getResourceIcon("icons/disk.png", "save");
			if(icon != null)
				putValue(SMALL_ICON, icon);
			else
				putValue(NAME, "save");
			putValue(SHORT_DESCRIPTION, "Save file.");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			saveFile(lastFile);
		}
	}

	private class SaveAsAction extends AbstractAction
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -5177892563951805617L;

		SaveAsAction()
		{
			super("");
			Icon icon = ImageLoadingTools.getResourceIcon("icons/disk--pencil.png", "save as");
			if(icon != null)
				putValue(SMALL_ICON, icon);
			else
				putValue(NAME, "save as");
			putValue(SHORT_DESCRIPTION, "Save file as.");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			saveFile(null);
		}
	}

	private class RunAction extends AbstractAction
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -925553205265988070L;

		RunAction()
		{
			super("");
			Icon icon = ImageLoadingTools.getResourceIcon("icons/script--arrow.png", "run");
			if(icon != null)
				putValue(SMALL_ICON, icon);
			else
				putValue(NAME, "run");
			putValue(SHORT_DESCRIPTION, "Run script.");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			runScript();
		}
	}

	private class OpenAction extends AbstractAction
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -3414955361086251289L;

		OpenAction()
		{
			super("");
			Icon icon = ImageLoadingTools.getResourceIcon("icons/folder-horizontal-open.png", "open");
			if(icon != null)
				putValue(SMALL_ICON, icon);
			else
				putValue(NAME, "run");
			putValue(SHORT_DESCRIPTION, "Open file.");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			openFile(null);
		}
	}

	@Override
	public void undoRedoStateChanged()
	{
		undoAction.updateUndoState();
		redoAction.updateRedoState();
	}
}
