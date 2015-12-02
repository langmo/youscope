/**
 * 
 */
package ch.ethz.csb.youscope.addon.microplatemeasurement;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.addon.measurement.MeasurementConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.measurement.MeasurementConfigurationAddonListener;
import ch.ethz.csb.youscope.client.uielements.MeasurementConfigurationPage;
import ch.ethz.csb.youscope.client.uielements.MeasurementConfigurationPanel;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.MeasurementConfiguration;


/**
 * @author langmo
 *
 */
public class MicroplateMeasurementConfigurationAddon implements MeasurementConfigurationAddon
{
	private YouScopeFrame									frame;
	private final MeasurementConfigurationPanel<MicroplateMeasurementConfigurationDTO> contentPane;
	
	/**
	 * Constructor.
	 * @param microscope Interface to the microscope.
	 * @param client Interface to the client.
	 */
	MicroplateMeasurementConfigurationAddon(YouScopeClient client, YouScopeServer server)
	{
		ArrayList<MeasurementConfigurationPage<MicroplateMeasurementConfigurationDTO>> pages = new ArrayList<MeasurementConfigurationPage<MicroplateMeasurementConfigurationDTO>>();
		pages.add(new StartPage());
		pages.add(new GeneralSettingsPage(client, server));
		pages.add(new MicroplatePage(client));
		pages.add(new WellSelectionPage(client, server));
		pages.add(new StartAndEndSettingsPage(client, server));
		pages.add(new ImagingProtocolPage(client, server));
		pages.add(new MiscPage(client, server));
		
		contentPane = new MeasurementConfigurationPanel<MicroplateMeasurementConfigurationDTO>(pages);
		
		
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
	public void setConfigurationData(MeasurementConfiguration measurementConfiguration) throws ConfigurationException
	{
		if(!(measurementConfiguration instanceof MicroplateMeasurementConfigurationDTO))
			throw new ConfigurationException("Only microplate measurement configurations accepted by this addon.");
		contentPane.setConfigurationData((MicroplateMeasurementConfigurationDTO)measurementConfiguration);
	}
	
	@Override
	public MicroplateMeasurementConfigurationDTO getConfigurationData()
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
	public void addConfigurationListener(MeasurementConfigurationAddonListener listener)
	{
		contentPane.addConfigurationListener(listener);
	}

	@Override
	public void removeConfigurationListener(MeasurementConfigurationAddonListener listener)
	{
		contentPane.removeConfigurationListener(listener);
	}
	
	private void initilizeFrame()
	{
		// Initialize configuration data for content pane, if yet not done...
		getConfigurationData();
		
		// create content pane
		contentPane.createUI(frame);
		contentPane.addConfigurationListener(new MeasurementConfigurationAddonListener()
		{
			@Override
			public void measurementConfigurationFinished(MeasurementConfiguration configuration)
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
