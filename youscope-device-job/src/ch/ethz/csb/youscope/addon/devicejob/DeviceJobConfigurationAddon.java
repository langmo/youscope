/**
 * 
 */
package ch.ethz.csb.youscope.addon.devicejob;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;

import ch.ethz.csb.youscope.client.addon.ConfigurationAddonAdapter;
import ch.ethz.csb.youscope.client.addon.ConfigurationMetadataAdapter;
import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.uielements.DeviceSettingsPanel;
import ch.ethz.csb.youscope.client.uielements.DynamicPanel;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.DeviceSettingJob;

/**
 * @author Moritz Lang
 */
class DeviceJobConfigurationAddon extends ConfigurationAddonAdapter<DeviceJobConfiguration>
{

    private DeviceSettingsPanel deviceSettingsField;
    
    DeviceJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
    {
    	super(getMetadata(),  client, server);
	}
    
    static ConfigurationMetadataAdapter<DeviceJobConfiguration> getMetadata()
	{
		return new ConfigurationMetadataAdapter<DeviceJobConfiguration>(DeviceSettingJob.DEFAULT_TYPE_IDENTIFIER, 
				DeviceJobConfiguration.class, 
				DeviceSettingJob.class, 
				"Device Settings", 
				new String[]{"Elementary"},
				"icons/property-blue.png");
	}
    
    @Override
	protected Component createUI(DeviceJobConfiguration configuration) throws AddonException
	{
		setTitle("Device Settings");
		setResizable(true);
		setMaximizable(false);
		
		deviceSettingsField = new DeviceSettingsPanel(configuration.getDeviceSettings(), getClient(), getServer());

        // Initialize layout
        DynamicPanel contentPane = new DynamicPanel();
        contentPane.add(new JLabel("Device Settings:"));
        contentPane.addFill(deviceSettingsField);
        setPreferredSize(new Dimension(600,400));
        return contentPane;
    }

	@Override
	protected void commitChanges(DeviceJobConfiguration configuration) 
	{
		configuration.setDeviceSettings(deviceSettingsField.getSettings());
	}
}
