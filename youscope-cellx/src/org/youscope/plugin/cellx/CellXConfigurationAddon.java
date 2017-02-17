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
package org.youscope.plugin.cellx;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;

/**
 * @author Moritz Lang
 */
class CellXConfigurationAddon  extends ComponentAddonUIAdapter<CellXConfiguration>
{
	
	private final JTextField	configFileField					= new JTextField();
	
	private final JCheckBox trackCellsField = new JCheckBox("Track cells.");
	
	private static final String PROPERTY_LAST_CELLX_CONFIGFILE = "YouScope.CellX.ConfigFile";
    
    /**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws AddonException 
	 */
	public CellXConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
	}
	
	static ComponentMetadataAdapter<CellXConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<CellXConfiguration>(CellXConfiguration.TYPE_IDENTIFIER, 
				CellXConfiguration.class, 
				CellXAddon.class, "CellX Cell-Detection", new String[]{"Cell Detection"}, "Uses the CellX algorithm to segment cells, and returns the segmentation image and the cells' positions and properties.",
				"icons/smiley-mr-green.png");
	}
    
    @Override
	protected Component createUI(CellXConfiguration configuration)
			throws AddonException {
    	setTitle("CellX cell detection");
		setResizable(true);
		setMaximizable(true);
		
		// Central panel
		DynamicPanel centralPanel = new DynamicPanel();
		
		setDescription("Detects and tracks cells using the CellX algorithms.\nA configuration file for the cell detection algorithm must be created using CellX. In CellX, proceed through all configuration sets and then click File -> Export Parameters. Afterwards, exit the CellX Gui and select the respective file in this dialog box.");
		
        // CellX configuration file
        JPanel configFilePanel = new JPanel(new BorderLayout());
        configFilePanel.setOpaque(false);
        configFileField.setText(getClient().getPropertyProvider().getProperty(PROPERTY_LAST_CELLX_CONFIGFILE, ""));
        configFilePanel.add(configFileField, BorderLayout.CENTER);
		JButton selectConfigButton = new JButton("Select File");
		selectConfigButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    JFileChooser fileChooser = new JFileChooser(getClient().getPropertyProvider().getProperty(PROPERTY_LAST_CELLX_CONFIGFILE, ""));
                    fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("CellX configuration (.xml)", "xml"));
                                       
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
                    		JOptionPane.showMessageDialog(null, "File " + file.toString() + " does not exist.", "File does not exist", JOptionPane. INFORMATION_MESSAGE);
                    	}
                    	else
                    		break;
                    }
                    
                    getClient().getPropertyProvider().setProperty(PROPERTY_LAST_CELLX_CONFIGFILE, file.getAbsolutePath());

                    configFileField.setText(file.toString());
                }
            });
        configFilePanel.add(selectConfigButton, BorderLayout.EAST);
        centralPanel.add(new JLabel("CellX config file:"));
        centralPanel.add(configFilePanel);
        
        trackCellsField.setOpaque(false);
        centralPanel.add(trackCellsField);
  
        JButton createConfigFileButton = new JButton("Start CellX Gui");
        createConfigFileButton.addActionListener(new ActionListener()
        {
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				configureCellX();
			}
        });
        centralPanel.add(createConfigFileButton);
        
        centralPanel.addFillEmpty();
        
        configFileField.setText(configuration.getConfigurationFile());
		trackCellsField.setSelected(configuration.isTrackCells());

        return centralPanel;
    }

    private void configureCellX()
    {
    	// Check if all necessary files exist
		File cellxDirectory = new File("cellx");
		if(!cellxDirectory.exists() || !cellxDirectory.isDirectory())
		{
			sendErrorMessage("Directory " + cellxDirectory.getAbsolutePath() + " does not exist. Check the CellX addon installation for consistency.", null);
			return;
		}
		
		File cellxFile = new File(cellxDirectory, "CellXGui.jar");
		if(!cellxFile.exists() || !cellxFile.isFile())
		{
			sendErrorMessage("CellX Gui " + cellxFile.getAbsolutePath() + " does not exist. Check the CellX addon installation for consistency.", null);
			return;
		}
    	try
		{
			Runtime.getRuntime().exec(new String[]{"javaw", "-jar", "CellXGui.jar"}, null, cellxDirectory);
		}
		catch(IOException e)
		{
			sendErrorMessage("Error while starting CellX Gui (" + cellxFile.getAbsolutePath() + ").", e);
			return;
		}
    }
    
    /*private void processImage()
    {
    	ImageEvent image = orgImage;
    	if(image == null)
    		return;
    	
    	getContainingFrame().startLoading();
    	
    	Runnable runner = new Runnable()
    	{
    		@Override
			public void run()
			{
				try
				{
					processImageInternal();
				}
				catch(Exception e)
				{
					sendErrorMessage("Could not detect cells in image.", e);
				}
				getContainingFrame().endLoading();
			}
    	};
    	new Thread(runner).start();
    }
    
    private void processImageInternal() throws Exception
    {
    	// Get configuration
    	CellXConfiguration detectionConfiguration = getConfiguration().clone();
    	detectionConfiguration.setGenerateLabelImage(true);
    	LabelVisualizerConfiguration visualizationConfiguration = new LabelVisualizerConfiguration();
    	
    	// Get algorithms for visualization and detection
    	ServiceLoader<CellVisualizationAddonFactory> visualizationAddonFactories = ServiceLoader.load(CellVisualizationAddonFactory.class, CellDetectionMeasurementConstructionAddon.class.getClassLoader());
		CellDetectionAddon detectionAlgorithm = new CellXAddon(new PositionInformation(), detectionConfiguration);
		CellVisualizationAddon visualizationAlgorithm = null;
    
		for(CellVisualizationAddonFactory addonFactory : visualizationAddonFactories)
		{
			if(addonFactory.supportsCellVisualizationAddonID(LabelVisualizerConfiguration.CONFIGURATION_ID))
			{
				visualizationAlgorithm = addonFactory.createCellVisualizationAddon(LabelVisualizerConfiguration.CONFIGURATION_ID);
				break;
			}
		}
		if(visualizationAlgorithm == null)
		{
			throw new SettingException("Cell visualization algorithm with ID "+LabelVisualizerConfiguration.CONFIGURATION_ID + " could not be found.");
		}
		
		// Initialize algorithms
		SimpleMeasurementContext measurementContext = new SimpleMeasurementContext();
		try
		{
			detectionAlgorithm.initialize(measurementContext);
		}
		catch(CellDetectionException e)
		{
			throw new SettingException("Could not initialize cell detection algorithm.", e);
		}
		try
		{
			visualizationAlgorithm.initialize(visualizationConfiguration);
		}
		catch(CellVisualizationException e)
		{
			throw new SettingException("Could not initialize cell visualization algorithm.", e);
		}
		
		// Detect cells
		CellDetectionResult detectionResult;
		try
		{
			detectionResult = detectionAlgorithm.detectCells(orgImage);
		}
		catch(CellDetectionException e)
		{
			throw new MicroscopeException("Error occured while detecting cells.", e);
		}
		
		// Visualize Cells
		ImageEvent visualizationResult;
		try
		{
			visualizationResult = visualizationAlgorithm.visualizeCells(orgImage, detectionResult);
		}
		catch(CellVisualizationException e)
		{
			throw new MicroscopeException("Error occured while visualizing cells.", e);
		}
		
		try
		{
			detectionAlgorithm.uninitialize(measurementContext);
			visualizationAlgorithm.uninitialize();
		}
		catch(CellDetectionException e)
		{
			sendErrorMessage("Could not uninitialize cell detection algorithm.", e);
		}
		catch(CellVisualizationException e)
		{
			sendErrorMessage("Could not uninitialize cell visualization algorithm.", e);
		}
		
		detectImagePanel.setImage(visualizationResult);
    }*/

	@Override
	protected void commitChanges(CellXConfiguration configuration)
			{
		configuration.setConfigurationFile(configFileField.getText());
    	configuration.setTrackCells(trackCellsField.isSelected());
		
	}

	@Override
	protected void initializeDefaultConfiguration(CellXConfiguration configuration) throws AddonException {
		// do nothing.
	}
}
