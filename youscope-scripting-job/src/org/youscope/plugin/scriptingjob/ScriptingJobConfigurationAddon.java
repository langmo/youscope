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
package org.youscope.plugin.scriptingjob;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Vector;

import javax.script.ScriptEngineFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.job.basicjobs.ScriptingJob;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.JobsDefinitionPanel;
import org.youscope.uielements.StandardFormats;

/**
 * @author Moritz Lang
 */
class ScriptingJobConfigurationAddon extends ComponentAddonUIAdapter<ScriptingJobConfiguration>
{
    /**
     * Default script engine is Oracle Nashorn, supporting JavaScript.
     */
    private static final String ORACLE_NASHORN = "Oracle Nashorn";
    
    private JobsDefinitionPanel jobDefinitions;
	
    private JComboBox<String> engineNamesField;

    private final String[] SCRIPT_LOCATION_CHOICES =
        { "Microscope Computer (Server)", "This Computer (Client)" };
    
    private JButton loadButton;
    private final JLabel warningDifferentComputerLabel = new JLabel("<html><b>Remark:</b><br />Please enter the location where the script file is on<br />the microscope computer, not on this computer.</html>");

    private JComboBox<String> scriptLocationField = new JComboBox<String>(SCRIPT_LOCATION_CHOICES);

    private JTextField	scriptFileField					= new JTextField();
	
    ScriptingJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
    {
    	super(getMetadata(), client, server);
	}
    
    static ComponentMetadataAdapter<ScriptingJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<ScriptingJobConfiguration>(ScriptingJob.DEFAULT_TYPE_IDENTIFIER, 
				ScriptingJobConfiguration.class, 
				ScriptingJob.class, 
				"Scripting", 
				new String[]{"Misc"},
				"A job which can be defined by one of the supported scripting languages, e.g. Matlab or JavaScript.",
				"icons/script-code.png");
	}
    
    @Override
	protected Component createUI(ScriptingJobConfiguration configuration) throws AddonException
	{
		setTitle("Scripting Job");
		setResizable(true);
		setMaximizable(true);
		
        GridBagLayout topElementsLayout = new GridBagLayout();
        JPanel topElementsPanel = new JPanel(topElementsLayout);
        GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();

        GridBagConstraints bottomConstr = new GridBagConstraints();
        bottomConstr.weighty = 1.0;

        // Let user choose if script should be executed on client or on server side.
        if(!getClient().isLocalServer())
        {
	        StandardFormats.addGridBagElement(new JLabel("Computer on which scripts should be run (standard: server):"),
	        		topElementsLayout, newLineConstr, topElementsPanel);
	        scriptLocationField.addActionListener(new ActionListener()
	            {
	                @Override
	                public void actionPerformed(ActionEvent arg0)
	                {
	                	// Load engine names.
	                    actualizeSupportedScriptEngines();
	                    
	                    actualizeDesign();
	                }
	            });
	        StandardFormats.addGridBagElement(scriptLocationField, topElementsLayout, newLineConstr, topElementsPanel);
        }
        
        // Select the engine.
        engineNamesField = new JComboBox<String>();
        StandardFormats.addGridBagElement(new JLabel("Script Engine:"), topElementsLayout, newLineConstr, topElementsPanel);
        StandardFormats.addGridBagElement(engineNamesField, topElementsLayout, newLineConstr, topElementsPanel);

        // Select the script file.        
        StandardFormats.addGridBagElement(new JLabel("Script File:"), topElementsLayout, newLineConstr, topElementsPanel);
        StandardFormats.addGridBagElement(warningDifferentComputerLabel, topElementsLayout, newLineConstr, topElementsPanel);
        JPanel scriptButtonsPanel = new JPanel(new BorderLayout());
        scriptButtonsPanel.add(scriptFileField, BorderLayout.CENTER);        
        loadButton = new JButton("Select");
        loadButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    JFileChooser fileChooser =
                            new JFileChooser((String) getClient().getPropertyProvider().getProperty(StandardProperty.PROPERTY_LAST_SCRIPT_PATH));
                    
                    String engineName = (String) engineNamesField.getSelectedItem();
                    if(engineName == null)
                    {
                    	sendErrorMessage("No script engine selected.", null);
                    	return;
                    }
                    ScriptEngineFactory engineFactory;
					try {
						engineFactory = getClient().getAddonProvider().getScriptEngineFactory(engineName);
					} catch (AddonException e) {
						sendErrorMessage("Could not get script engine with type identifier " + engineName+".", e);
						return;
					}
                    
                    String[] engineFileEndings;
                    if(engineFactory == null)
                    {
                    	engineFileEndings = new String[0];
                    }
                    else
                    {
                    	engineFileEndings = engineFactory.getExtensions().toArray(new String[0]);
                    }
                    
                    if(engineFileEndings.length > 0)
                    {
                    	fileChooser.setSelectedFile(new File("unnamed." + engineFileEndings[0]));
                    	String filterDesc = "Script Files (";
                    	boolean first = true;
                    	for(String engineFileEnding : engineFileEndings)
                    	{
                    		if(first)
                    			first = false;
                    		else
                    			filterDesc += ", ";
                    		filterDesc += "." + engineFileEnding;
                    	}
                    	filterDesc += ")";
                    	fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(filterDesc, engineFileEndings));
                    }
                                       
                    File file;
                    while(true)
                    {
                    	int returnVal = fileChooser.showDialog(null, "Load");
                    	if (returnVal != JFileChooser.APPROVE_OPTION)
                    	{
                    		return;
                    	}
                    	file = fileChooser.getSelectedFile().getAbsoluteFile();
                    	if(!file.exists())
                    	{
                    		JOptionPane.showMessageDialog(null, "File " + file.toString() + " does not exist.\nTo create a yet not existing file, use the \"New File\" button.", "File does not exist", JOptionPane. INFORMATION_MESSAGE);
                    	}
                    	else
                    		break;
                    }
                    
                    getClient().getPropertyProvider().setProperty(StandardProperty.PROPERTY_LAST_SCRIPT_PATH, fileChooser
                            .getCurrentDirectory().getAbsolutePath());

                    scriptFileField.setText(file.toString());
                }
            });
        scriptButtonsPanel.add(loadButton, BorderLayout.EAST);
        StandardFormats.addGridBagElement(scriptButtonsPanel, topElementsLayout, newLineConstr, topElementsPanel);

        // Center elements
        StandardFormats.addGridBagElement(new JLabel("From script callable jobs:"), topElementsLayout, newLineConstr, topElementsPanel);
        jobDefinitions = new JobsDefinitionPanel(getClient(), getServer(), getContainingFrame());
        jobDefinitions.setJobs(configuration.getJobs());
        
        // Load state
        if (configuration.isUseClientScriptEngine())
        {
            scriptLocationField.setSelectedIndex(1);
        } else
        {
            scriptLocationField.setSelectedIndex(0);
        }

        actualizeSupportedScriptEngines();
        if(configuration.getScriptEngine() != null)
        	engineNamesField.setSelectedItem(configuration.getScriptEngine());
        if(configuration.getScriptFile() != null)
        {
        	try
			{
				scriptFileField.setText(new File(configuration.getScriptFile().toURI()).toString());
			}
			catch(URISyntaxException e1)
			{
				getClient().sendError("Could not read in stored script file location.", e1);
			}
        }

        actualizeDesign();
        
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(topElementsPanel, BorderLayout.NORTH);
        contentPane.add(jobDefinitions, BorderLayout.CENTER);
        
        return contentPane;
    }

	private void actualizeDesign()
	{
		// Set settings depending on if script file is on this computer.
        if(scriptLocationField.getSelectedIndex() == 1 || getClient().isLocalServer())
        {
        	loadButton.setEnabled(true);
        	warningDifferentComputerLabel.setVisible(false);
        }
        else
        {
        	loadButton.setEnabled(false);
        	warningDifferentComputerLabel.setVisible(true);
        }
	}
    private void actualizeSupportedScriptEngines()
    {
    	Object lastItem = engineNamesField.getSelectedItem();
        engineNamesField.removeAllItems();
        String[] engines;
        if (scriptLocationField.getSelectedIndex() == 1)
        {
            Vector<String> engineNames = new Vector<String>();

            for(ScriptEngineFactory factory : getClient().getAddonProvider().getScriptEngineFactories())
            {
                String engineName = factory.getEngineName();
                engineNames.add(engineName);
            }

            engines = engineNames.toArray(new String[engineNames.size()]);
        } 
        else
        {
            try
            {
                engines =getServer().getProperties().getSupportedScriptEngines();
            } 
            catch (RemoteException e)
            {
            	getClient().sendError("Could not obtain script engines supported by the server.", e);
                engines = new String[0];
            }
        }

        for (String engineName : engines)
        {
            engineNamesField.addItem(engineName);
        }
        if(lastItem != null)
        	engineNamesField.setSelectedItem(lastItem);
        else
        	engineNamesField.setSelectedItem(ORACLE_NASHORN);
    }

	@Override
	protected void commitChanges(ScriptingJobConfiguration configuration)
	{
		File scriptFile = new File(scriptFileField.getText());
    	boolean clientScriptEngine = (scriptLocationField.getSelectedIndex() == 1);
    	
    	URL scriptURL;
		try
		{
			scriptURL = scriptFile.toURI().toURL();
		}
		catch(@SuppressWarnings("unused") MalformedURLException e)
		{
			scriptURL = null;
		}
    	
		configuration.setJobs(jobDefinitions.getJobs());
		
        configuration.setScriptEngine((String) engineNamesField.getSelectedItem());
        configuration.setScriptFile(scriptURL);
        configuration.setUseClientScriptEngine(clientScriptEngine);
	}

	@Override
	protected void initializeDefaultConfiguration(ScriptingJobConfiguration configuration) throws AddonException {
		// do nothing.
	}
}
