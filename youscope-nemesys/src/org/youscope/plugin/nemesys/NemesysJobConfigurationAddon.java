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
package org.youscope.plugin.nemesys;

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
import org.youscope.addon.component.ComponentAddonTools;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.microscope.Device;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.scripteditor.ScriptEditor;

/**
 * @author Moritz Lang
 */
class NemesysJobConfigurationAddon extends ComponentAddonUIAdapter<NemesysJobConfiguration>
{

    private static final String MOZILLA_RHINO = "Oracle Nashorn";
    private static final String MOZILLA_RHINO_STYLE = "YouScope.ScriptStyle.JavaScript";
    private static final String MATLAB_SCRIPTING = "Matlab Scripting";
    private static final String MATLAB_SCRIPTING_STYLE = "YouScope.ScriptStyle.Matlab";
    
    private static final String PROPERTY_LAST_FILE_ = "YouScope.Nemesys.lastFile";
    
    // script
	private final ScriptEditor scriptField = new ScriptEditor();
	private final JComboBox<String> engineNamesField = new JComboBox<String>();
	private final JButton templateButton = new JButton("Create from template");
	private final JComboBox<String> nemesysDeviceField = new JComboBox<String>();
	private final SimpleTimingTable timingTable = new SimpleTimingTable(new String[0]);
	
	private final DynamicPanel scriptPanel = new DynamicPanel();
	private final DynamicPanel timingTablePanel = new DynamicPanel();
	
	private String[] flowUnits = new String[0];
	private String[] volumeUnits = new String[0];
	private double[] flowRateMax = new double[0];
	
	// MISC
	private final JLabel								nemesysTableLabel				= new JLabel("Nemesys-table file name (without extension):");
	private final JTextField							nemesysTableField				= new JTextField();
	private final JCheckBox								saveNemesysTableField			= new JCheckBox("Save Nemesys state to file", true);
	
	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws AddonException 
	 */
	public NemesysJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(),  client, server);
	}
    
	static ComponentMetadataAdapter<NemesysJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<NemesysJobConfiguration>(NemesysJobConfiguration.TYPE_IDENTIFIER, 
				NemesysJobConfiguration.class, 
				NemesysJob.class, 
				"nemesys control", 
				new String[]{"microfluidics"},
				"Sets the flow rates of a Nemesys syringe pump system.",
				"icons/beaker.png");
	}
    
	@Override
	protected Component createUI(NemesysJobConfiguration configuration) throws AddonException 
	{
		setTitle("Nemesys Job");
		setResizable(true);
		setMaximizable(true);
		setPreferredSize(new Dimension(800, 600));
		
		DynamicPanel mainPanel = new DynamicPanel();
		
		mainPanel.add(new JLabel("Nemesys device:"));
		
		try
		{
			Device[] devices = getServer().getMicroscope().getDevices();
			for(Device device : devices)
			{
				if(device.getDriverID().equals("Nemesys") && device.getLibraryID().equals("NemesysPump"))
				{
					nemesysDeviceField.addItem(device.getDeviceID());
				}
			}
		}
		catch(Exception e)
		{
			sendErrorMessage("Could not load Nemesys device IDs.", e);
		}
		nemesysDeviceField.setOpaque(false);
		nemesysDeviceField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				nemesysDeviceChanged();
			}
		});
		mainPanel.add(nemesysDeviceField);
		
		saveNemesysTableField.setOpaque(false);
		mainPanel.add(saveNemesysTableField);
		mainPanel.add(nemesysTableLabel);
		mainPanel.add(nemesysTableField);
		saveNemesysTableField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				boolean selected = saveNemesysTableField.isSelected();
				
				nemesysTableLabel.setVisible(selected);
				nemesysTableField.setVisible(selected);
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
			scriptEngines = new String[]{"Mozilla Rhino"};
		} 
		engineNamesField.addItem(NemesysJobConfiguration.SCRIPT_ENGINE_TIMETABLE);
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
		mainPanel.add(new JLabel("Nemesys Protocol:"));
		
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
						sendErrorMessage("Could not load Onix protocol " + file.toString()+ ".", e1);
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
		if(NemesysJobConfiguration.SCRIPT_ENGINE_TIMETABLE.equals(selectedEngine))
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
			else if(MOZILLA_RHINO.equals(selectedEngine))
			{
				scriptField.setScriptStyleID(MOZILLA_RHINO_STYLE);
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
		NemesysJobConfiguration config = getConfiguration();
		try {
			config.checkConfiguration();
		} catch (ConfigurationException e) {
			ComponentAddonTools.displayConfigurationInvalid(e, getClient());
			return;
		}
		new ProtocolVisualizerFrame(getClient(), flowUnits, flowRateMax,  config.getScript(), config.getScriptEngine()).createUI(childFrame);
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
		
		String nemesysDevice = nemesysDeviceField.getSelectedItem() == null ? null : nemesysDeviceField.getSelectedItem().toString();
		int numDosingUnits = 0;
		if(nemesysDevice == null)
			sendErrorMessage("No Nemesys device selected.", null);
		else
		{
			try
			{
				numDosingUnits = Integer.parseInt(getServer().getMicroscope().getDevice(nemesysDevice).getProperty("numDosingUnits").getValue());
			}
			catch(NumberFormatException e)
			{
				sendErrorMessage("Could not parse number of dosing units.", e);
			}
			catch(Exception e)
			{
				sendErrorMessage("Could not obtain number of dosing units.", e);
			}
		}
		
		if(MATLAB_SCRIPTING.equals(selectedEngine))
		{
			scriptField.setText(ScriptTemplates.generateMatlabTemplate(numDosingUnits));
		}
		else if(MOZILLA_RHINO.equals(selectedEngine))
		{
			scriptField.setText(ScriptTemplates.generateJavaScriptTemplate(numDosingUnits));
		}
		else
		{
			sendErrorMessage("No template available for script engine " + selectedEngine + ".", null);
		}
	}
	
	private void nemesysDeviceChanged()
	{
		String nemesysDevice = nemesysDeviceField.getSelectedItem() == null ? null : nemesysDeviceField.getSelectedItem().toString();
		if(nemesysDevice != null)
		{
			int numDosingUnits;
			try
			{
				numDosingUnits = Integer.parseInt(getServer().getMicroscope().getDevice(nemesysDevice).getProperty("numDosingUnits").getValue());
			}
			catch(NumberFormatException e)
			{
				flowUnits = new String[0];
				volumeUnits = new String[0];
				flowRateMax =new double[0];
				timingTable.setDosingUnits(flowUnits);
				sendErrorMessage("Could not parse number of volume or flow rate units.", e);
				return;
			}
			catch(Exception e)
			{
				flowUnits = new String[0];
				volumeUnits = new String[0];
				flowRateMax =new double[0];
				timingTable.setDosingUnits(flowUnits);
				sendErrorMessage("Could not obtain number of dosing units.", e);
				return;
			}
			flowUnits = new String[numDosingUnits];
			volumeUnits = new String[numDosingUnits];
			flowRateMax =new double[numDosingUnits];
			for(int i=0; i<flowUnits.length; i++)
			{
				try
				{
					flowUnits[i] = getServer().getMicroscope().getDevice(nemesysDevice).getProperty("syringe" + Integer.toString(i+1) + ".flowUnit").getValue();
					volumeUnits[i] = getServer().getMicroscope().getDevice(nemesysDevice).getProperty("syringe" + Integer.toString(i+1) + ".volumeUnit").getValue();
					flowRateMax[i] = Double.parseDouble(getServer().getMicroscope().getDevice(nemesysDevice).getProperty("syringe" + Integer.toString(i+1) + ".flowRateMax").getValue());
				}
				catch(Exception e)
				{
					flowUnits[i] = null;
					volumeUnits[i] = null;
					flowRateMax[i] = 0;
					sendErrorMessage("Could not obtain flow or volume unit of syringe " + Integer.toString(i+1)+".", e);
				}
			}
		}
		else
		{
			flowUnits = new String[0];
			volumeUnits = new String[0];
			flowRateMax = new double[0];
		}
		timingTable.setDosingUnits(flowUnits);
	}
	
	private void loadSettingsIntoLayout(NemesysJobConfiguration configuration)
	{
		String tableName = configuration.getTableSaveName();
		if(tableName == null)
		{
			nemesysTableField.setText("nemesys-state");
			saveNemesysTableField.setSelected(false);
			nemesysTableField.setVisible(false);
			nemesysTableLabel.setVisible(false);
		}
		else
		{
			if (tableName.length() < 1)
			{
				tableName = "nemesys-state";
			}
			nemesysTableField.setText(tableName);
			saveNemesysTableField.setSelected(true);
			nemesysTableField.setVisible(true);
			nemesysTableLabel.setVisible(true);
		}
		
		
		if(configuration.getNemesysDevice() != null)
			nemesysDeviceField.setSelectedItem(configuration.getNemesysDevice());
		else if(nemesysDeviceField.getItemCount() > 0)
			nemesysDeviceField.setSelectedIndex(0);
		actualizeScriptStyle();
		if(NemesysJobConfiguration.SCRIPT_ENGINE_TIMETABLE.equals(configuration.getScriptEngine()))
		{
			try
			{
				timingTable.setTimings(TimingProcessor.getTimings(configuration.getScript()));
			}
			catch(NemesysException e)
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
		nemesysDeviceChanged();
	}

	@Override
	protected void commitChanges(NemesysJobConfiguration configuration)
	{
		configuration.setNemesysDevice(nemesysDeviceField.getSelectedItem() == null ? null : nemesysDeviceField.getSelectedItem().toString());
		configuration.setScriptEngine(engineNamesField.getSelectedItem() == null ? null : engineNamesField.getSelectedItem().toString());
		configuration.setTableSaveName(saveNemesysTableField.isSelected() ? nemesysTableField.getText() : null);
    	if(NemesysJobConfiguration.SCRIPT_ENGINE_TIMETABLE.equals(configuration.getScriptEngine()))
		{
    		configuration.setScript(TimingProcessor.getProtocol(timingTable.getTimings()));
		}
    	else
    	{
    		configuration.setScript(scriptField.getText());
		}
	}

	@Override
	protected void initializeDefaultConfiguration(NemesysJobConfiguration configuration) throws AddonException {
		// do nothing.
	}
}
