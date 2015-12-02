/**
 * 
 */
package ch.ethz.csb.youscope.addon.lifecelldetection;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.addon.YouScopeProperties;
import ch.ethz.csb.youscope.client.uielements.DeviceSettingsPanel;
import ch.ethz.csb.youscope.client.uielements.MeasurementConfigurationPage;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.microscope.CameraDevice;
import ch.ethz.csb.youscope.shared.microscope.Property;
import ch.ethz.csb.youscope.shared.microscope.DeviceSettingDTO;
import ch.ethz.csb.youscope.shared.microscope.Microscope;

/**
 * @author langmo
 *
 */
class StartAndEndConfigurationPage extends MeasurementConfigurationPage<CellDetectionMeasurementConfiguration>
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -8833466993051293347L;

	private DeviceSettingsPanel							deviceSettingsOn;
	private DeviceSettingsPanel							deviceSettingsOff;
	
	private final YouScopeClient	client;
	private final YouScopeServer			server;
	
	StartAndEndConfigurationPage(YouScopeClient client, YouScopeServer server)
	{
		this.server = server;
		this.client = client;
	}
	@Override
	public void createUI(YouScopeFrame parentFrame)
	{
		setLayout(new GridLayout(2, 1));
		deviceSettingsOn = new DeviceSettingsPanel(client, server, true);
		deviceSettingsOff = new DeviceSettingsPanel(client, server, true);
		
		JPanel onPanel = new JPanel(new BorderLayout(2, 2));
		onPanel.add(new JLabel("Device Settings when measurement starts:"),
				BorderLayout.NORTH);
		onPanel.add(deviceSettingsOn, BorderLayout.CENTER);
		add(onPanel);
		JPanel offPanel = new JPanel(new BorderLayout(2, 2));
		offPanel.add(new JLabel("Device Settings when measurement ends:"),
				BorderLayout.NORTH);
		offPanel.add(deviceSettingsOff, BorderLayout.CENTER);
		add(offPanel);

		setBorder(new TitledBorder("Measurement Start and End Settings"));
	}
	
	@Override
	public void loadData(CellDetectionMeasurementConfiguration configuration)
	{
		deviceSettingsOn.setSettings(configuration.getDeviseSettingsOn());
		deviceSettingsOff.setSettings(configuration.getDeviseSettingsOff());
	}

	@Override
	public boolean saveData(CellDetectionMeasurementConfiguration configuration)
	{
		configuration.setDeviseSettingsOn(deviceSettingsOn.getSettings());
		configuration.setDeviseSettingsOff(deviceSettingsOff.getSettings());
		return true;
	}

	@Override
	public void setToDefault(CellDetectionMeasurementConfiguration configuration)
	{
		if (client.getProperties().getProperty(YouScopeProperties.PROPERTY_PREINITIALIZE_CAMERA_SETTINGS, true))
		{
			try
			{
				Microscope microscope = server.getMicroscope();
				CameraDevice camera = microscope.getCameraDevice();
				String deviceName = camera.getDeviceID();
				Property[] properties = camera.getEditableProperties();
				DeviceSettingDTO[] cameraDeviceSettings = new DeviceSettingDTO[properties.length];
				for(int i=0; i< properties.length; i++)
				{
					cameraDeviceSettings[i] = new DeviceSettingDTO();
					cameraDeviceSettings[i].setAbsoluteValue(true);
					cameraDeviceSettings[i].setDeviceProperty(deviceName, properties[i].getPropertyID());
					cameraDeviceSettings[i].setValue(properties[i].getValue());
				}
				configuration.setDeviseSettingsOn(cameraDeviceSettings);
			}
			catch (Exception e)
			{
				client.sendError("Could not pre-initialize measurement startup settings. Letting these settings empty and continuing.", e);
				configuration.setDeviseSettingsOn(new DeviceSettingDTO[0]);
			}
		}
	}

	@Override
	public String getPageName()
	{
		return "Start and End Settings";
	}

}
