/**
 * 
 */
package org.youscope.plugin.simplemeasurement;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import org.youscope.addon.component.ComponentAddonUIListener;
import org.youscope.addon.measurement.MeasurementConfigurationAddon;
import org.youscope.addon.measurement.MeasurementAddonUIPage;
import org.youscope.addon.measurement.MeasurementAddonUIAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.serverinterfaces.YouScopeServer;


/**
 * @author langmo
 *
 */
class SimpleMeasurementConfigurationAddon implements MeasurementConfigurationAddon<SimpleMeasurementConfigurationDTO>
{
	private YouScopeFrame									frame;
	private final MeasurementAddonUIAdapter<SimpleMeasurementConfigurationDTO> contentPane;
	
	/**
	 * Constructor.
	 * @param microscope Interface to the microscope.
	 * @param client Interface to the client.
	 */
	SimpleMeasurementConfigurationAddon(YouScopeClient client, YouScopeServer server)
	{
		ArrayList<MeasurementAddonUIPage<SimpleMeasurementConfigurationDTO>> pages = new ArrayList<MeasurementAddonUIPage<SimpleMeasurementConfigurationDTO>>();
		pages.add(new StartPage());
		pages.add(new GeneralSettingsPage(client, server));
		pages.add(new StartAndEndSettingsPage(client, server));
		pages.add(new ImagingProtocolPage(client, server));
		pages.add(new MiscPage(client, server));
		
		contentPane = new MeasurementAddonUIAdapter<SimpleMeasurementConfigurationDTO>(pages);
		
		
	}
	
	@Override
	public void createUI(YouScopeFrame frame)
	{
		this.frame = frame;
		
		frame.setTitle("Simple Measurement Configuration");
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setMaximizable(false);
		
		frame.startInitializing();
		(new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				initilizeFrame();
				
				SimpleMeasurementConfigurationAddon.this.frame.endLoading();
			}
		})).start();
	}

	@Override
	public void setConfiguration(Configuration measurementConfiguration) throws ConfigurationException
	{
		if(!(measurementConfiguration instanceof SimpleMeasurementConfigurationDTO))
			throw new ConfigurationException("Only simple measurement configurations accepted by this addon.");
		contentPane.setConfigurationData((SimpleMeasurementConfigurationDTO)measurementConfiguration);
	}
	
	@Override
	public SimpleMeasurementConfigurationDTO getConfiguration()
	{
		SimpleMeasurementConfigurationDTO measurementConfiguration = contentPane.getConfigurationData();
		if(measurementConfiguration == null)
		{
			// Set to standard configuration if not set from somebody else
			measurementConfiguration = new SimpleMeasurementConfigurationDTO();
			contentPane.setToDefault(measurementConfiguration);
			contentPane.setConfigurationData(measurementConfiguration);
		}
		return measurementConfiguration;
	}

	@Override
	public void addUIListener(ComponentAddonUIListener<? super SimpleMeasurementConfigurationDTO> listener)
	{
		contentPane.addConfigurationListener(listener);
	}

	@Override
	public void removeUIListener(ComponentAddonUIListener<? super SimpleMeasurementConfigurationDTO> listener)
	{
		contentPane.removeConfigurationListener(listener);
	}
	
	private void initilizeFrame()
	{
		// Initialize configuration data for content pane, if yet not done...
		getConfiguration();
		
		// create content pane
		contentPane.createUI(frame);
		contentPane.addConfigurationListener(new ComponentAddonUIListener<SimpleMeasurementConfigurationDTO>()
		{
			@Override
			public void configurationFinished(SimpleMeasurementConfigurationDTO configuration)
			{
				frame.setVisible(false);
			}
		});
		contentPane.addSizeChangeListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				frame.pack();
			}
		});
		
		frame.setContentPane(contentPane);
		frame.pack();
		
	}
}
