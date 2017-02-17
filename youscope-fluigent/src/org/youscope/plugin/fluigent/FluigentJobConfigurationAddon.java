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
package org.youscope.plugin.fluigent;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;
import java.io.FileReader;
import java.rmi.RemoteException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.microscope.Device;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.scripteditor.ScriptEditor;

/**
 * @author Moritz Lang
 */
public class FluigentJobConfigurationAddon extends ComponentAddonUIAdapter<FluigentJobConfiguration>
{

    private static final String JAVA_SCRIPT_ENGINE = "Oracle Nashorn";
    private static final String JAVA_SCRIPT_STYLE = "YouScope.ScriptStyle.JavaScript";
    private static final String MATLAB_SCRIPTING = "Matlab Scripting";
    private static final String MATLAB_SCRIPTING_STYLE = "YouScope.ScriptStyle.Matlab";
    
    private static final String PROPERTY_LAST_FILE_ = "YouScope.Fluigent.lastFile";
    
    // script
	private final ScriptEditor scriptField = new ScriptEditor();
	private final JComboBox<String> engineNamesField = new JComboBox<String>();
	private final JButton templateButton = new JButton("Create from template");
	private final JComboBox<String> fluigentDeviceField = new JComboBox<String>();
	private final SimpleTimingTable timingTable = new SimpleTimingTable(new String[0]);
	
	private String[] flowRateUnits = new String[0];
	
	private final DynamicPanel scriptPanel = new DynamicPanel();
	private final DynamicPanel timingTablePanel = new DynamicPanel();
	
	
	// MISC
	private final JLabel								fluigentTableLabel				= new JLabel("Fluigent-table file name (without extension):");
	private final JTextField							fluigentTableField				= new JTextField();
	private final JCheckBox								saveFluigentTableField			= new JCheckBox("Save Fluigent state to file", true);
	
	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws AddonException 
	 */
	public FluigentJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(),  client, server);
	}
    
	static ComponentMetadataAdapter<FluigentJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<FluigentJobConfiguration>(FluigentJobConfiguration.TYPE_IDENTIFIER, 
				FluigentJobConfiguration.class, 
				FluigentJob.class, 
				"fluigent control", 
				new String[]{"microfluidics"},
				"Changes medium flow rates of a Fluigent pump system.",
				"icons/beaker.png");
	}
    
	@Override
	protected Component createUI(FluigentJobConfiguration configuration) throws AddonException
	{
		setTitle("Fluigent Job");
		setResizable(true);
		setMaximizable(true);
		setPreferredSize(new Dimension(800, 600));
		
		DynamicPanel mainPanel = new DynamicPanel();
		
		mainPanel.add(new JLabel("Fluigent device:"));
		
		try
		{
			Device[] devices = getServer().getMicroscope().getDevices();
			for(Device device : devices)
			{
				if(device.getDriverID().equals("Fluigent") && device.getLibraryID().equals("FluigentPump"))
				{
					fluigentDeviceField.addItem(device.getDeviceID());
				}
			}
		}
		catch(Exception e)
		{
			sendErrorMessage("Could not load Fluigent device IDs.", e);
		}
		fluigentDeviceField.setOpaque(false);
		fluigentDeviceField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				fluigentDeviceChanged();
			}
		});
		mainPanel.add(fluigentDeviceField);
		saveFluigentTableField.setOpaque(false);
		mainPanel.add(saveFluigentTableField);
		mainPanel.add(fluigentTableLabel);
		mainPanel.add(fluigentTableField);
		saveFluigentTableField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				boolean selected = saveFluigentTableField.isSelected();
				
				fluigentTableLabel.setVisible(selected);
				fluigentTableField.setVisible(selected);
				getContainingFrame().pack();
				
			}
		});
				
		mainPanel.add(new JLabel("Script engine:"));
		String[] scriptEngines;
		try
		{
			scriptEngines = getServer().getProperties().getSupportedScriptEngines();
		}
		catch(RemoteException e2)
		{
			sendErrorMessage("Could not load script engine names. Trying to recover.", e2);
			scriptEngines = new String[]{"Oracle Nashorn"};
		} 
		engineNamesField.addItem(FluigentJobConfiguration.SCRIPT_ENGINE_TIMETABLE);
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
		mainPanel.add(engineNamesField);
		mainPanel.add(new JLabel("Fluigent Protocol:"));
		
		scriptField.setScriptStyleID("YouScope.ScriptStyle.JavaScript");
		scriptPanel.addFill(scriptField);
		
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
                	String lastProtocol = getClient().getPropertyProvider().getProperty(PROPERTY_LAST_FILE_, "");
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
                    
                    getClient().getPropertyProvider().setProperty(PROPERTY_LAST_FILE_, file.toString());
                    
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
						sendErrorMessage("Could not load Fluigent protocol " + file.toString()+ ".", e1);
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
								sendErrorMessage("Could not close protocol " + file.toString()+ ".", e1);
							}
						}						
					}
					
					scriptField.setText(protocol);
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
            	String lastProtocol = getClient().getPropertyProvider().getProperty(PROPERTY_LAST_FILE_, "");
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
                
                getClient().getPropertyProvider().setProperty(PROPERTY_LAST_FILE_, file.toString());
                
                String text = scriptField.getText();
        		try
        		{
        			PrintStream fileStream = new PrintStream(file);
        			fileStream.print(text);
        			fileStream.close();
        		}
        		catch(Exception e)
        		{
        			sendErrorMessage("Could not save file.", e);
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
		JButton plotScriptButton = new JButton("Plot protocol");
		plotScriptButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				plotProtocol();
			}
		});
		plotScriptButton.setOpaque(false);
		loadSavePanel.add(plotScriptButton);
		scriptPanel.add(loadSavePanel);
		
		timingTablePanel.addFill(timingTable);
		JPanel timingTableButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		timingTableButtonsPanel.setOpaque(false);
		JButton plotProtocolButton = new JButton("Plot protocol");
		plotProtocolButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				plotProtocol();
			}
		});
		plotProtocolButton.setOpaque(false);
		timingTableButtonsPanel.add(plotProtocolButton);
		timingTablePanel.add(timingTableButtonsPanel);
		
		mainPanel.addFill(scriptPanel);
		mainPanel.addFill(timingTablePanel);
		
		loadSettingsIntoLayout(configuration);
				
		return mainPanel;
    }
	
	private void actualizeScriptStyle()
	{
		String selectedEngine = (String)engineNamesField.getSelectedItem();
		if(FluigentJobConfiguration.SCRIPT_ENGINE_TIMETABLE.equals(selectedEngine))
		{
			timingTablePanel.setVisible(true);
			scriptPanel.setVisible(false);
		}
		else
		{
			timingTablePanel.setVisible(false);
			scriptPanel.setVisible(true);
			if(MATLAB_SCRIPTING.equals(selectedEngine))
			{
				scriptField.setScriptStyleID(MATLAB_SCRIPTING_STYLE);
				templateButton.setEnabled(true);
			}
			else if(JAVA_SCRIPT_ENGINE.equals(selectedEngine))
			{
				scriptField.setScriptStyleID(JAVA_SCRIPT_STYLE);
				templateButton.setEnabled(true);
			}
			else
			{
				scriptField.setScriptStyleID(null);
				templateButton.setEnabled(false);
			}
		}
	}
	
	private void plotProtocol()
	{
		YouScopeFrame childFrame = getContainingFrame().createModalChildFrame();
		FluigentJobConfiguration config;
		try {
			config = getConfiguration();
		} catch (Exception e) {
			sendErrorMessage("Cannot plot protocol.", e);
			return;
		} 
		new ProtocolVisualizerFrame(getClient(), flowRateUnits,  config.getScript(), config.getScriptEngine()).createUI(childFrame);
		childFrame.setVisible(true);
	}
	private void loadTemplate()
	{
		if(scriptField.getText().trim().length() > 0)
		{
			int returnVal = JOptionPane.showConfirmDialog(null, "Loading the template deletes the current controller script.\nContinue?", "Loading template warning", JOptionPane.YES_NO_OPTION);
    		if(returnVal != JOptionPane.YES_OPTION)
    			return;
		}
		String selectedEngine = (String)engineNamesField.getSelectedItem();
		
		if(MATLAB_SCRIPTING.equals(selectedEngine))
		{
			scriptField.setText(ScriptTemplates.generateMatlabTemplate(flowRateUnits.length));
		}
		else if(JAVA_SCRIPT_ENGINE.equals(selectedEngine))
		{
			scriptField.setText(ScriptTemplates.generateJavaScriptTemplate(flowRateUnits.length));
		}
		else
		{
			sendErrorMessage("No template available for script engine " + selectedEngine + ".", null);
		}
	}
	
	private void fluigentDeviceChanged()
	{
		String fluigentDevice = fluigentDeviceField.getSelectedItem() == null ? null : fluigentDeviceField.getSelectedItem().toString();
		if(fluigentDevice != null)
		{
			try
			{
				int numFlowUnits = Integer.parseInt(getServer().getMicroscope().getDevice(fluigentDevice).getProperty("numFlowUnits").getValue());
				flowRateUnits = new String[numFlowUnits];
				for(int i=0; i<numFlowUnits; i++)
				{
					flowRateUnits[i] = getServer().getMicroscope().getDevice(fluigentDevice).getProperty("flowUnit" + Integer.toString(i+1) + ".flowRateUnit").getValue();
				}
			}
			catch(NumberFormatException e)
			{
				flowRateUnits = new String[0];
				timingTable.setFlowRateUnits(flowRateUnits);
				sendErrorMessage("Could not parse number of flow units.", e);
				return;
			}
			catch(Exception e)
			{
				flowRateUnits = new String[0];
				timingTable.setFlowRateUnits(flowRateUnits);
				sendErrorMessage("Could not obtain number of flow units.", e);
				return;
			}
		}
		else
		{
			flowRateUnits = new String[0];
		}
		timingTable.setFlowRateUnits(flowRateUnits);
	}
	
	private void loadSettingsIntoLayout(FluigentJobConfiguration configuration)
	{
		String tableName = configuration.getTableSaveName();
		if(tableName == null)
		{
			fluigentTableField.setText("fluigent-state");
			saveFluigentTableField.setSelected(false);
			fluigentTableField.setVisible(false);
			fluigentTableLabel.setVisible(false);
		}
		else
		{
			if (tableName.length() < 1)
			{
				tableName = "fluigent-state";
			}
			fluigentTableField.setText(tableName);
			saveFluigentTableField.setSelected(true);
			fluigentTableField.setVisible(true);
			fluigentTableLabel.setVisible(true);
		}
		
		
		if(configuration.getFluigentDevice() != null)
			fluigentDeviceField.setSelectedItem(configuration.getFluigentDevice());
		else if(fluigentDeviceField.getItemCount() > 0)
			fluigentDeviceField.setSelectedIndex(0);
		actualizeScriptStyle();
		if(FluigentJobConfiguration.SCRIPT_ENGINE_TIMETABLE.equals(configuration.getScriptEngine()))
		{
			try
			{
				timingTable.setTimings(TimingProcessor.getTimings(configuration.getScript()));
			}
			catch(ResourceException e)
			{
				sendErrorMessage("Could not load old protocol. Initializing empty protocol", e);
			}
		}
		else
			scriptField.setText(configuration.getScript());
		if(configuration.getScriptEngine() != null)
			engineNamesField.setSelectedItem(configuration.getScriptEngine());
		else if(engineNamesField.getItemCount() > 0)
			engineNamesField.setSelectedIndex(0);
		fluigentDeviceChanged();
	}

	@Override
	protected void commitChanges(FluigentJobConfiguration configuration){
		configuration.setFluigentDevice(fluigentDeviceField.getSelectedItem() == null ? null : fluigentDeviceField.getSelectedItem().toString());
    	configuration.setScriptEngine(engineNamesField.getSelectedItem() == null ? null : engineNamesField.getSelectedItem().toString());
    	configuration.setTableSaveName(saveFluigentTableField.isSelected() ? fluigentTableField.getText() : null);
    	if(FluigentJobConfiguration.SCRIPT_ENGINE_TIMETABLE.equals(configuration.getScriptEngine()))
		{
    		configuration.setScript(TimingProcessor.getProtocol(timingTable.getTimings()));
		}
    	else
    	{
    		configuration.setScript(scriptField.getText());
		}
		
	}

	@Override
	protected void initializeDefaultConfiguration(FluigentJobConfiguration configuration) throws AddonException {
		// do nothing.
	}
}
