/**
 * 
 */
package org.youscope.plugin.measurementappender;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadata;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.IntegerTextField;
import org.youscope.uielements.PeriodField;
import org.youscope.uielements.SubConfigurationPanel;
import org.youscope.common.saving.SaveSettings;
import org.youscope.common.saving.SaveSettingsConfiguration;

/**
 * @author Moritz Lang
 */
class AppenderSaveSettingsUI extends ComponentAddonUIAdapter<AppenderSaveSettingsConfiguration>
{
	private final JTextField folderField = new JTextField();

	private final IntegerTextField imageNumberField = new IntegerTextField(1);
	private final PeriodField previousRuntime = new PeriodField();
	
	private SubConfigurationPanel<SaveSettingsConfiguration> saveSettingPanel = null;
    /**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws AddonException 
	 */
	public AppenderSaveSettingsUI(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
	}
	
	static ComponentMetadataAdapter<AppenderSaveSettingsConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<AppenderSaveSettingsConfiguration>(AppenderSaveSettingsConfiguration.TYPE_IDENTIFIER, 
				AppenderSaveSettingsConfiguration.class, 
				SaveSettings.class, "Appender Save Settings", new String[]{"save settings"});
	}
	/**
	 * Get all possible enclosed configurations. That's necessary, because we have to prevent ourself from appearing in this list, otherwise, we can get an infinite recursion...
	 * @param client YouScope client.
	 * @return List of all possible enclosed configurations.
	 */
	private static List<ComponentMetadata<? extends SaveSettingsConfiguration>> getEnclosedConfigurationMetadata(YouScopeClient client)
	{
		ArrayList<ComponentMetadata<? extends SaveSettingsConfiguration>> components = new ArrayList<ComponentMetadata<? extends SaveSettingsConfiguration>>();
		for(ComponentMetadata<? extends SaveSettingsConfiguration> metadata : client.getAddonProvider().getComponentMetadata(SaveSettingsConfiguration.class))
		{
			if(metadata.getTypeIdentifier().equals(AppenderSaveSettingsConfiguration.TYPE_IDENTIFIER))
				continue;
			components.add(metadata);
		}
		return components;
	}
	
	@Override
	protected Component createUI(AppenderSaveSettingsConfiguration configuration) throws AddonException
	{
		setTitle("Appender save settings");
		setResizable(false);
		setMaximizable(false);
		
		// internally, we count zero based, but for the user we count one based.
		imageNumberField.setMinimalValue(1);
		imageNumberField.setValue((int)configuration.getDeltaEvaluationNumber()+1);
		folderField.setText(configuration.getBaseFolder());
		previousRuntime.setDuration(configuration.getPreviousRuntime());
		
		saveSettingPanel = new SubConfigurationPanel<SaveSettingsConfiguration>("Enclosed save settings:", null, SaveSettingsConfiguration.class, getClient(), getContainingFrame(), getEnclosedConfigurationMetadata(getClient()));
		
		if(configuration.getEncapsulatedSaveSettings() == null)
			saveSettingPanel.setConfiguration(getClient().getPropertyProvider().getProperty(StandardProperty.PROPERTY_MEASUREMENT_STANDARD_SAVE_SETTINGS_TYPE).toString());
		else
			saveSettingPanel.setConfiguration(configuration.getEncapsulatedSaveSettings());
		
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
		contentPane.add(new JLabel("Image number to proceed with:"));
		contentPane.add(imageNumberField);
		contentPane.add(new JLabel("Runtime of previous measurement:"));
		contentPane.add(previousRuntime);
		
		contentPane.add(saveSettingPanel);
		
		return contentPane;
    }

    @Override
	protected void commitChanges(AppenderSaveSettingsConfiguration configuration)
    {   
    	configuration.setBaseFolder(folderField.getText());
    	// internally, we count zero based, but for the user we count one based.
    	configuration.setDeltaEvaluationNumber(imageNumberField.getValue()-1);
    	configuration.setEncapsulatedSaveSettings(saveSettingPanel == null ? null : saveSettingPanel.getConfiguration());
    	configuration.setPreviousRuntime(previousRuntime.getDurationLong());
    }

	@Override
	protected void initializeDefaultConfiguration(AppenderSaveSettingsConfiguration configuration) throws AddonException 
	{
		String lastFolder = (String) getClient().getPropertyProvider().getProperty(StandardProperty.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER);
		configuration.setBaseFolder(lastFolder == null ? "" : lastFolder);
		configuration.setDeltaEvaluationNumber(0);
		configuration.setEncapsulatedSaveSettings(null);
		configuration.setPreviousRuntime(3600*1000);
	}
}
