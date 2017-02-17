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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.youscope.addon.AddonException;
import org.youscope.addon.tool.ToolAddonUIAdapter;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.addon.tool.ToolMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 *
 */
class ScriptingTool extends ToolAddonUIAdapter implements EditFileListener
{
	/**
	 * The default script engine (JavaScript).
	 */
	private static final String ORACLE_NASHORN = "Oracle Nashorn";
		
	/**
	 * The name of the function to debug a scripting job.
	 */
	public static final String FUNCTION_DEBUG_SCRIPTING_JOB = "debugScriptingJob";
	
	/**
	 * The name of the function to debug a script file.
	 */
	public static final String FUNCTION_DEBUG_SCRIPT = "debugScript";
	
	private final Workspace workspace;
	private final FileSystem fileSystem;
	private final Console console;
	private final ScriptExcecuter scriptExecuter;
	
	/**
	 * Engine which is loaded when tool is started. Set to a different value if a certain engine is e.g. needed for debugging.
	 */
	private String engineToLoad = ORACLE_NASHORN;
	
	/**
	 * Vector of URLs to files which get loaded at startup of this tool.
	 */
	private Vector<URL> scriptURLs = new Vector<URL>();
	
	public final static String TYPE_IDENTIFIER = "YouScope.YouScopeScripting";
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "Scripting Console", new String[0], 
				"Environment to script the microscope/Youscope's behavior using one of the supported script languages, e.g. Matlab or JavaScript.",
				"icons/script-block.png");
	}
	
	/**
	 * Constructor.
	 * @param client Interface to the YouScope client.
	 * @param server Interface to the YouScope server.
	 * @throws AddonException 
	 */
	public ScriptingTool(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
		// Initialize sub-elements
		scriptExecuter = new ScriptExcecuter(client, server);
		fileSystem = new FileSystem(client);
		workspace = new Workspace();
		console = new Console();
	}
	@Override
	public java.awt.Component createUI()
	{	
		// Connect elements
		scriptExecuter.addMessageListener(console);
		console.addEvaluationListener(scriptExecuter);
		fileSystem.addEvaluationListener(scriptExecuter);
		fileSystem.addEditFileListener(this);
		scriptExecuter.addVariablesListener(workspace);
		
		setMaximizable(true);
		setResizable(true);
		setTitle("Script Console");
		
		// Initialize engine chooser
		JComboBox<String> engineNamesField = new JComboBox<String>(scriptExecuter.getScriptEngines());
     	
		for(int engineID = 0; engineID < engineNamesField.getItemCount(); engineID ++)
		{
			if(engineNamesField.getItemAt(engineID).toString().compareToIgnoreCase(engineToLoad) == 0)
			{
				engineNamesField.setSelectedIndex(engineID);
				break;
			}
		}
		engineNamesField.addActionListener(new ActionListener()
		{
			@Override
		    public void actionPerformed(ActionEvent arg0)
		    {
				if(!(arg0.getSource() instanceof JComboBox))
					return;
		    	String engineName = ((JComboBox<?>)arg0.getSource()).getSelectedItem().toString();
		      	loadEngine(engineName);            
		    }
		});
		JPanel engineNames = new JPanel(new BorderLayout());
		engineNames.add(engineNamesField, BorderLayout.CENTER);
		engineNames.setBorder(new TitledBorder("Active Script Engine"));
        
        // Initialize split panes
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(engineNames, BorderLayout.NORTH);
		rightPanel.add(workspace, BorderLayout.CENTER);
        JSplitPane commandWorkspaceSplitMane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT , false, console, rightPanel);
        commandWorkspaceSplitMane.setResizeWeight(1.0);
        commandWorkspaceSplitMane.setOneTouchExpandable(true);
        commandWorkspaceSplitMane.setBorder(new EmptyBorder(2, 2, 2, 2));
        
        JSplitPane fileSystemCommandSplitMane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT , false, fileSystem, commandWorkspaceSplitMane);
        fileSystemCommandSplitMane.setResizeWeight(0.0);
        fileSystemCommandSplitMane.setOneTouchExpandable(true);
        fileSystemCommandSplitMane.setBorder(new EmptyBorder(2, 2, 2, 2));
        
        // Open files which should have been opened.
        for(URL url : scriptURLs)
		{
			try
			{
				editFile(new File(url.toURI()));
			}
			catch(Exception e)
			{
				sendErrorMessage("Could not obtain file from script job where script is saved.", e);
			}
		}
		
        // Load the script engine.
		loadEngine((String)engineNamesField.getSelectedItem());
        
		return fileSystemCommandSplitMane;
	}
	
	private void loadEngine(String engineName)
	{
		console.clearConsole();
		console.outputMessage("Initializing script engine.\nDependend on the selected script engine this may take several seconds.");
		console.setEnabled(false);
    	workspace.variablesChanged(null);
    	class EngineLoader implements Runnable
    	{
    		private final String engineName;
    		EngineLoader(final String engineName)
    		{
    			this.engineName = engineName;
    		}
    		@Override
			public void run()
			{
    			try
    			{
    				scriptExecuter.loadEngine(engineName);
    				scriptExecuter.initializeStandardMode();
    			}
    			catch(Exception e)
    			{
    				console.clearConsole();
    				console.outputMessage("Script Engine with engine name " +  engineName + " could not be loaded.\n" + e.getMessage() + "\n=====================================================\n");
    				console.setEnabled(false);
					return;
    			}
    			console.clearConsole();
    			console.outputMessage("Active script engine: " + engineName + "\n=====================================================\n");
    			console.setEnabled(true);
			}
    	}
    	
    	(new Thread(new EngineLoader(engineName))).start();
    	
	}

	

	@Override
	public void editFile(File file)
	{
		YouScopeFrame clientFrame = getContainingFrame().createChildFrame();
		ScriptEditorFrame editor = new ScriptEditorFrame(getClient(), clientFrame, file);
		editor.addEvaluationListener(scriptExecuter);
	}
}
