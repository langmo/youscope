/**
 * 
 */
package org.youscope.plugin.microplatemeasurement;

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
public class MicroplateMeasurementConfigurationAddon implements MeasurementConfigurationAddon<MicroplateMeasurementConfigurationDTO>
{
	private YouScopeFrame									frame;
	private final MeasurementAddonUIAdapter<MicroplateMeasurementConfigurationDTO> contentPane;
	
	/**
	 * Constructor.
	 * @param microscope Interface to the microscope.
	 * @param client Interface to the client.
	 */
	MicroplateMeasurementConfigurationAddon(YouScopeClient client, YouScopeServer server)
	{
		ArrayList<MeasurementAddonUIPage<MicroplateMeasurementConfigurationDTO>> pages = new ArrayList<MeasurementAddonUIPage<MicroplateMeasurementConfigurationDTO>>();
		pages.add(new StartPage());
		pages.add(new GeneralSettingsPage(client, server));
		pages.add(new MicroplatePage(client));
		pages.add(new WellSelectionPage(client, server));
		pages.add(new StartAndEndSettingsPage(client, server));
		pages.add(new ImagingProtocolPage(client, server));
		pages.add(new MiscPage(client, server));
		
		contentPane = new MeasurementAddonUIAdapter<MicroplateMeasurementConfigurationDTO>(pages);
		
		
	}
	
	@Override
	public void createUI(YouScopeFrame frame)
	{
		this.frame = frame;
		
		frame.setTitle("Microplate Measurement Configuration");
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
				
				MicroplateMeasurementConfigurationAddon.this.frame.endLoading();
			}
		})).start();
	}

	@Override
	public void setConfiguration(Configuration measurementConfiguration) throws ConfigurationException
	{
		if(!(measurementConfiguration instanceof MicroplateMeasurementConfigurationDTO))
			throw new ConfigurationException("Only microplate measurement configurations accepted by this addon.");
		contentPane.setConfigurationData((MicroplateMeasurementConfigurationDTO)measurementConfiguration);
	}
	
	@Override
	public MicroplateMeasurementConfigurationDTO getConfiguration()
	{
		MicroplateMeasurementConfigurationDTO measurementConfiguration = contentPane.getConfigurationData();
		if(measurementConfiguration == null)
		{
			// Set to standard configuration if not set from somebody else
			measurementConfiguration = new MicroplateMeasurementConfigurationDTO();
			contentPane.setToDefault(measurementConfiguration);
			
			contentPane.setConfigurationData(measurementConfiguration);
		}
		return measurementConfiguration;
	}

	@Override
	public void addUIListener(ComponentAddonUIListener<? super MicroplateMeasurementConfigurationDTO> listener)
	{
		contentPane.addConfigurationListener(listener);
	}

	@Override
	public void removeUIListener(ComponentAddonUIListener<? super MicroplateMeasurementConfigurationDTO> listener)
	{
		contentPane.removeConfigurationListener(listener);
	}
	
	private void initilizeFrame()
	{
		// Initialize configuration data for content pane, if yet not done...
		getConfiguration();
		
		// create content pane
		contentPane.createUI(frame);
		contentPane.addConfigurationListener(new ComponentAddonUIListener<MicroplateMeasurementConfigurationDTO>()
		{
			@Override
			public void configurationFinished(MicroplateMeasurementConfigurationDTO configuration)
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
