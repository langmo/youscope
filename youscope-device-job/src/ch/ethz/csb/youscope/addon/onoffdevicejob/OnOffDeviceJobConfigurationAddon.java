/**
 * 
 */
package ch.ethz.csb.youscope.addon.onoffdevicejob;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import ch.ethz.csb.youscope.client.addon.ConfigurationAddonAdapter;
import ch.ethz.csb.youscope.client.addon.ConfigurationMetadataAdapter;
import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.uielements.DeviceSettingsPanel;
import ch.ethz.csb.youscope.client.uielements.DynamicPanel;
import ch.ethz.csb.youscope.client.uielements.PeriodField;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.microscope.DeviceSettingDTO;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.CompositeJob;

/**
 * @author Moritz Lang
 */
class OnOffDeviceJobConfigurationAddon extends ConfigurationAddonAdapter<OnOffDeviceJobConfiguration>
{
    private static final String[] TYPES_STRINGS =
        { "Set Devices", "Set Devices - Wait", "Set Devices - Wait - Set Devices" };

    private final JComboBox<String> typeChooserBox = new JComboBox<String>(TYPES_STRINGS);

    private DeviceSettingsPanel deviceSettingsOnField;
    private final JLabel deviceSettingsOnLabel = new JLabel("Device Settings upon activation:");

    private DeviceSettingsPanel deviceSettingsOffField;
    private final JLabel deviceSettingsOffLabel = new JLabel("Device Settings upon deactivation:");
    
    private final PeriodField exposureField = new PeriodField();
    private final JLabel exposureLabel = new JLabel("Exposure:");

    OnOffDeviceJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
    {
    	super(getMetadata(),  client, server);	
	}
    
    static ConfigurationMetadataAdapter<OnOffDeviceJobConfiguration> getMetadata()
	{
		return new ConfigurationMetadataAdapter<OnOffDeviceJobConfiguration>(OnOffDeviceJobConfiguration.TYPE_IDENTIFIER, 
				OnOffDeviceJobConfiguration.class, 
				CompositeJob.class, 
				"Set Device - Wait - Set Device", 
				new String[]{"Misc"},
				"icons/property.png");
	}
    
    @Override
	protected Component createUI(OnOffDeviceJobConfiguration configuration) throws AddonException
	{
		setTitle("Device Settings Job");
		setResizable(true);
		setMaximizable(false);
		
		deviceSettingsOnField = new DeviceSettingsPanel(configuration.getDeviceSettingsOn(), getClient(), getServer());
		deviceSettingsOffField = new DeviceSettingsPanel(configuration.getDeviceSettingsOff(), getClient(), getServer());
		
        // Initialize layout
        DynamicPanel contentPane = new DynamicPanel();
        
        if(configuration.getDeviceSettingsOff().length > 0)
		{
			typeChooserBox.setSelectedIndex(2);
		}
		else if(configuration.getExposure() > 0)
		{
			typeChooserBox.setSelectedIndex(1);
		}
		else
		{
			typeChooserBox.setSelectedIndex(0);
		}
        typeChooserBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
            	hideElements();
            	notifyLayoutChanged();
            }
        });
        contentPane.add(new JLabel("Type:"));
        contentPane.add(typeChooserBox);

        contentPane.add(deviceSettingsOnLabel);
        contentPane.addFill(deviceSettingsOnField);
        contentPane.add(exposureLabel);
        exposureField.setDuration((int)configuration.getExposure());
        contentPane.add(exposureField);
        contentPane.add(deviceSettingsOffLabel);
        contentPane.addFill(deviceSettingsOffField);
        
        hideElements();
        return contentPane;
    }

    private void hideElements()
    {
    	int idx = typeChooserBox.getSelectedIndex(); 
    	deviceSettingsOffLabel.setVisible(idx >= 2);
    	deviceSettingsOffField.setVisible(idx >= 2);
    	exposureLabel.setVisible(idx >= 1);
    	exposureField.setVisible(idx >= 1);
    }

    @Override
	protected void commitChanges(OnOffDeviceJobConfiguration configuration) 
	{
		if(typeChooserBox.getSelectedIndex() == 0)
		{
			configuration.setDeviceSettingsOn(deviceSettingsOnField.getSettings());
			configuration.setExposure(0);
			configuration.setDeviceSettingsOff(new DeviceSettingDTO[0]);
		}
		else if(typeChooserBox.getSelectedIndex() == 1)
		{
			configuration.setDeviceSettingsOn(deviceSettingsOnField.getSettings());
			configuration.setExposure(exposureField.getDuration());
			configuration.setDeviceSettingsOff(new DeviceSettingDTO[0]);
		}
		else
		{
			configuration.setDeviceSettingsOn(deviceSettingsOnField.getSettings());
			configuration.setExposure(exposureField.getDuration());
			configuration.setDeviceSettingsOff(deviceSettingsOffField.getSettings());
		}
	}
}
