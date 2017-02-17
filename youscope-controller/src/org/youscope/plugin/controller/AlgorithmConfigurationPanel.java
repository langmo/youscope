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
package org.youscope.plugin.controller;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.rmi.RemoteException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.table.ColumnDefinition;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.scripteditor.ScriptEditor;

class AlgorithmConfigurationPanel extends JPanel
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 1873800314613882666L;
	private final ScriptEditor controllerScript = new ScriptEditor();
	private final JComboBox<String> engineNamesField = new JComboBox<String>();
	private final JButton templateButton = new JButton("Create from template"); 
	private final YouScopeClient client;
	private static final String MOZILLA_RHINO = "Oracle Nashorn";
    private static final String MOZILLA_RHINO_STYLE = "YouScope.ScriptStyle.JavaScript";
    private static final String MATLAB_SCRIPTING = "Matlab Scripting";
    private static final String MATLAB_SCRIPTING_STYLE = "YouScope.ScriptStyle.Matlab";
    private static final String PROPERTY_LAST_FILE_ = "YouScope.controller.lastFile";
    
    private ColumnDefinition<?>[] inputColumns = null;
    private ColumnDefinition<?>[] outputColumns = null;
    
	AlgorithmConfigurationPanel(ControllerJobConfiguration controllerConfiguration, final YouScopeClient client, final YouScopeServer server)
	{
		super(new BorderLayout());
		setOpaque(false);
		this.client = client;		
		
		DynamicPanel algorithmPanel = new DynamicPanel();
		algorithmPanel.add(new JLabel("Controller script engine:"));
		String[] scriptEngines;
		try
		{
			scriptEngines = server.getProperties().getSupportedScriptEngines();
		}
		catch(RemoteException e2)
		{
			client.sendError("Could not load script engine names. Trying to recover.", e2);
			scriptEngines = new String[]{"Mozilla Rhino"};
		} 
		for(String engine : scriptEngines)
		{
			engineNamesField.addItem(engine);
		}
		engineNamesField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				actualizeScriptStyle();
			}
		});
		engineNamesField.setOpaque(false);
		algorithmPanel.add(engineNamesField);
		algorithmPanel.add(new JLabel("Controller algorithm:"));
		controllerScript.setScriptStyleID("YouScope.ScriptStyle.JavaScript");
		algorithmPanel.addFill(controllerScript);
		
		JPanel loadSavePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        loadSavePanel.setOpaque(false);
        JButton loadButton = new JButton("Load");
        loadButton.setOpaque(false);
        loadSavePanel.add(loadButton);
        loadButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                	String lastProtocol = client.getPropertyProvider().getProperty(PROPERTY_LAST_FILE_, "");
                    JFileChooser fileChooser = new JFileChooser(lastProtocol);
                    fileChooser.setSelectedFile(new File(lastProtocol)); 
                    
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
                    		JOptionPane.showMessageDialog(null, "File " + file.toString() + " does not exist.\nPlease select an existing file.", "File does not exist", JOptionPane. INFORMATION_MESSAGE);
                    	}
                    	else
                    		break;
                    }
                    
                    client.getPropertyProvider().setProperty(PROPERTY_LAST_FILE_, file.toString());
                    
                    BufferedReader reader = null;
                    String protocol = "";
                    try
					{
						reader = new BufferedReader(new FileReader(file));
						while(true)
						{
							String line = reader.readLine();
							if(line == null)
								break;
							protocol += line + "\n";
						}
					}
					catch(Exception e1)
					{
						client.sendError("Could not load Onix protocol " + file.toString()+ ".", e1);
						return;
					}
					finally
					{
						if(reader != null)
						{
							try
							{
								reader.close();
							}
							catch(Exception e1)
							{
								client.sendError("Could not close protocol " + file.toString()+ ".", e1);
							}
						}						
					}
					
					controllerScript.setText(protocol);
                }
            });
        
        JButton saveButton = new JButton("Save");
        saveButton.setOpaque(false);
        loadSavePanel.add(saveButton);
        saveButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
            	String lastProtocol = client.getPropertyProvider().getProperty(PROPERTY_LAST_FILE_, "");
                JFileChooser fileChooser = new JFileChooser(lastProtocol);
                fileChooser.setSelectedFile(new File(lastProtocol)); 
                                   
                File file;
                while(true)
                {
                	int returnVal = fileChooser.showDialog(null, "Save");
                	if (returnVal != JFileChooser.APPROVE_OPTION)
                	{
                		return;
                	}
                	file = fileChooser.getSelectedFile().getAbsoluteFile();
                	if(file.exists())
                	{
                		returnVal = JOptionPane.showConfirmDialog(null, "File " + file.toString() + " does already exist.\nOverwrite?", "File does already exist", JOptionPane.YES_NO_OPTION);
                		if(returnVal == JOptionPane.YES_OPTION)
                			break;
                	}
                	else
                		break;
                }
                
                client.getPropertyProvider().setProperty(PROPERTY_LAST_FILE_, file.toString());
                
                String text = controllerScript.getText();
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
            }
        });
        templateButton.setOpaque(false);
        loadSavePanel.add(templateButton);
		templateButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				loadTemplate();
			}
		});

		add(algorithmPanel, BorderLayout.CENTER);
		add(loadSavePanel, BorderLayout.SOUTH);
		
		// load settings
		controllerScript.setText(controllerConfiguration.getControllerScript());
		engineNamesField.setSelectedItem(controllerConfiguration.getControllerScriptEngine());
		actualizeScriptStyle();
	}
	private void actualizeScriptStyle()
	{
		String selectedEngine = (String)engineNamesField.getSelectedItem();
		if(MATLAB_SCRIPTING.equals(selectedEngine))
		{
			controllerScript.setScriptStyleID(MATLAB_SCRIPTING_STYLE);
			templateButton.setEnabled(true);
		}
		else if(MOZILLA_RHINO.equals(selectedEngine))
		{
			controllerScript.setScriptStyleID(MOZILLA_RHINO_STYLE);
			templateButton.setEnabled(true);
		}
		else
		{
			controllerScript.setScriptStyleID(null);
			templateButton.setEnabled(false);
		}
	}
	
	void setInputColumns(ColumnDefinition<?>[] inputColumns)
	{
		this.inputColumns = inputColumns;
	}
	void setOutputColumns(ColumnDefinition<?>[] outputColumns)
	{
		this.outputColumns = outputColumns;
	}
	
	void commitChanges(ControllerJobConfiguration configuration)
	{
		configuration.setControllerScript(controllerScript.getText());
		configuration.setControllerScriptEngine(engineNamesField.getSelectedItem()==null ? null : engineNamesField.getSelectedItem().toString());
	}
	
	private void loadTemplate()
	{
		if(controllerScript.getText().trim().length() > 0)
		{
			int returnVal = JOptionPane.showConfirmDialog(null, "Loading the template deletes the current controller script.\nContinue?", "Loading template warning", JOptionPane.YES_NO_OPTION);
    		if(returnVal != JOptionPane.YES_OPTION)
    			return;
		}
		
		String selectedEngine = (String)engineNamesField.getSelectedItem();
		
		ColumnDefinition<?>[] inputColumns = this.inputColumns == null ? new ColumnDefinition<?>[0] : this.inputColumns;
		ColumnDefinition<?>[] outputColumns = this.outputColumns == null ? new ColumnDefinition<?>[0] : this.outputColumns;	
		
		if(MATLAB_SCRIPTING.equals(selectedEngine))
		{
			controllerScript.setText(ScriptTemplates.generateMatlabTemplate(inputColumns, outputColumns));
		}
		else if(MOZILLA_RHINO.equals(selectedEngine))
		{
			controllerScript.setText(ScriptTemplates.generateJavaScriptTemplate(inputColumns, outputColumns));
		}
		else
		{
			client.sendError("No template available for script engine " + selectedEngine + ".");
		}
	}
}
