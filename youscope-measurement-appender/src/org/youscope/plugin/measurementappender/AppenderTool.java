/**
 * 
 */
package org.youscope.plugin.measurementappender;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.youscope.addon.AddonException;
import org.youscope.addon.ConfigurationManagement;
import org.youscope.addon.tool.ToolAddonUIAdapter;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.addon.tool.ToolMetadataAdapter;
import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.configuration.Configuration;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.common.saving.MeasurementFileLocations;
import org.youscope.common.saving.SaveSettingsConfiguration;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.IntegerTextField;
import org.youscope.uielements.PeriodField;

/**
 * @author Moritz Lang
 *
 */
public class AppenderTool extends ToolAddonUIAdapter
{
	/**
	 * The type identifier is a unique ID of your tool. No other tool or other element of YouScope should
	 * have the same type identifier as your tool, thus, change this identifier to something unique. The general notation is
	 * your_identifier DOT tool_identifier, in which your_identifier is e.g. SmithAnton, MyCompanyName, or LonleyProgrammer1972.
	 * tool_identifer is the name of your tool, e.g. FancyFocus, MySpecialDeviceController, or HelloWorldTool.
	 */
	public final static String TYPE_IDENTIFIER = "YouScope.Appender.Tool";
	
	private final String lastFolder;
	private final String lastConfigFile;
	private final JTextField folderField = new JTextField();
	private final JTextField configFileField = new JTextField();

	private final IntegerTextField imageNumberField = new IntegerTextField(1);
	private final PeriodField previousRuntime = new PeriodField();
	
	/**
	 * Returns the metadata of this tool. The metadata consists of a unique identifier for the tool, a human readable name of the tool, an array of names
	 * of folders under which the tool should be displayed in YouScope (could be empty to display it as a default tool), and similar. To not have to
	 * implement all functions of the interface {@link ToolMetadata} ourselves, we return an instance of the adapter class {@link ToolMetadataAdapter}, which does most of the
	 * boilerplate code for us.
	 * @return Metadata of tool.
	 */
	public static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "Measurement Appender", new String[]{"Misc"}, 
				"Tool to append a finished measurement with data from a new measurement.",
				"icons/projection-screen-presentation.png");
	}
	
	/**
	 * Constructor. Provides the adapter class {@link ToolAddonUIAdapter} with all necessary information to provide the standard
	 * functionality which has to be exposed by every tool, such that we can concentrate on the fundamentals. 
	 * Do not initialize the UI elements here (do this in {@link #createUI()}). 
	 * @param client Interface to the YouScope client, e.g. allowing to open or close new windows, or permanently save settings.
	 * @param server Interface to the YouScope server, e.g. allowing access to the microscope.
	 * @throws AddonException 
	 */
	public AppenderTool(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server); 
		lastFolder = null;
		lastConfigFile = null;
	}
	
	/**
	 * Same as {@link #AppenderTool(YouScopeClient, YouScopeServer)}, only called when used as a measurement post processor. In that case,
	 * we can readout some data from the measurement which is post processed.
	 * @param client Interface to the YouScope client, e.g. allowing to open or close new windows, or permanently save settings.
	 * @param server Interface to the YouScope server, e.g. allowing access to the microscope.
	 * @param lastFileLocations The file locations of the last measurement we want to append.
	 * @throws AddonException
	 */
	public AppenderTool(YouScopeClient client, YouScopeServer server, MeasurementFileLocations lastFileLocations) throws AddonException
	{
		super(getMetadata(), client, server); 
		lastFolder = lastFileLocations.getMeasurementBaseFolder();
		lastConfigFile = lastFileLocations.getMeasurementConfigurationFilePath();
	}
	@Override
	public java.awt.Component createUI()
	{
		/**
		 *  This is the place to setup the UI elements of the first window of your tool.
		 */
		// Title of the window, as well as basic configuration.
		setMaximizable(false);
		setResizable(true);
		setTitle("Appender Tool");
			        
		/**
		 *  You might find out that stuff by some kind of wizzard, but since I am lazy there is no wizard, yet, 
		 *  and one has to enter the stuff by hand.
		 *  You might e.g. want to only ask for the path for the config file. By having the config file, you can construct the
		 *  previous save settings. With the previous save settings, you can query the location of the image table. Then, you can load the
		 *  image table and search for the highest image number (=delta-1) and the time when the last image was taken (=last runtime). Similarly, you can
		 *  reconstruct the base folder of the measurement by asking the old save settings for their base folder, and compare it with the folder where you found the config.
		 */
		// internally, we count zero based, but for the user we count one based.
		imageNumberField.setMinimalValue(1);
		imageNumberField.setValue(100);
		String lastFolder = this.lastFolder;
		if(lastFolder == null)
			lastFolder = (String) getClient().getPropertyProvider().getProperty(StandardProperty.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER);
		folderField.setText(lastFolder == null ? "" : lastFolder);
		previousRuntime.setDuration(3600*1000);
		String lastConfigFile = this.lastConfigFile;
		configFileField.setText(lastConfigFile == null ? "" : lastConfigFile);
		
		DynamicPanel contentPane = new DynamicPanel();
		contentPane.add(new JLabel("Directory of measurement which should be appended:"));
		JPanel folderPanel = new JPanel(new BorderLayout(5, 0));
		folderPanel.add(folderField, BorderLayout.CENTER);
		if(getClient().isLocalServer())
		{
			JButton openFolderChooser = new JButton("Edit");
			openFolderChooser.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					JFileChooser fileChooser = new JFileChooser(folderField.getText());
					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int returnVal = fileChooser.showDialog(null, "Open");
					if(returnVal == JFileChooser.APPROVE_OPTION)
					{
						folderField.setText(fileChooser.getSelectedFile().getAbsolutePath());
					}
				}
			});
			folderPanel.add(openFolderChooser, BorderLayout.EAST);
		}
		contentPane.add(folderPanel);
		
		contentPane.add(new JLabel("Config file of measurement which should be appended:"));
		JPanel configFilePanel = new JPanel(new BorderLayout(5, 0));
		configFilePanel.add(configFileField, BorderLayout.CENTER);
		if(getClient().isLocalServer())
		{
			JButton openFolderChooser = new JButton("Edit");
			openFolderChooser.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					JFileChooser fileChooser = new JFileChooser(configFileField.getText());
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					int returnVal = fileChooser.showDialog(null, "Open");
					if(returnVal == JFileChooser.APPROVE_OPTION)
					{
						configFileField.setText(fileChooser.getSelectedFile().getAbsolutePath());
					}
				}
			});
			configFilePanel.add(openFolderChooser, BorderLayout.EAST);
		}
		contentPane.add(configFilePanel);
		
		contentPane.add(new JLabel("Image number to proceed with:"));
		contentPane.add(imageNumberField);
		contentPane.add(new JLabel("Runtime of previous measurement:"));
		contentPane.add(previousRuntime);
		
        JButton appendButton = new JButton("Append Measurement");
        appendButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				appendMeasurement();
				if(isSeparateFrame())
					getContainingFrame().setVisible(false);
			}
		});
        contentPane.add(appendButton);
		
        return contentPane;
	}
	private void appendMeasurement()
	{
		MeasurementConfiguration lastConfig;
		try {
			Configuration lastConfigTemp = ConfigurationManagement.loadConfiguration(configFileField.getText());
			if(!(lastConfigTemp instanceof MeasurementConfiguration))
			{
				getClient().sendError("Provided configuration is not a measurement configuration.");
				return;
			}
			lastConfig = (MeasurementConfiguration) lastConfigTemp;
		} catch (IOException e) {
			getClient().sendError("Could not open last config.", e);
			return;
		}
		AppenderSaveSettingsConfiguration newSaveSettings = new AppenderSaveSettingsConfiguration();
		newSaveSettings.setBaseFolder(folderField.getText());
		newSaveSettings.setDeltaEvaluationNumber(imageNumberField.getValue()-1);
		newSaveSettings.setPreviousRuntime(previousRuntime.getDurationLong());
		SaveSettingsConfiguration lastSaveSettings = lastConfig.getSaveSettings();
		if(lastSaveSettings instanceof AppenderSaveSettingsConfiguration)
		{
			// in case we want to append a measurement more than once...
			lastSaveSettings = ((AppenderSaveSettingsConfiguration)lastSaveSettings).getEncapsulatedSaveSettings();
		}
		newSaveSettings.setEncapsulatedSaveSettings(lastSaveSettings);
		lastConfig.setSaveSettings(newSaveSettings);
		getClient().editMeasurement(lastConfig);
	}
}
