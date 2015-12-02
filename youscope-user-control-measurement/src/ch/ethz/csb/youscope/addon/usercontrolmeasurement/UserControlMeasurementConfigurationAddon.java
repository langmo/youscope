/**
 * 
 */
package ch.ethz.csb.youscope.addon.usercontrolmeasurement;

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
class UserControlMeasurementConfigurationAddon implements MeasurementConfigurationAddon
{
	private YouScopeFrame									frame;
	private final MeasurementConfigurationPanel<UserControlMeasurementConfigurationDTO> contentPane;
	
	/**
	 * Constructor.
	 * @param microscope Interface to the microscope.
	 * @param client Interface to the client.
	 */
	UserControlMeasurementConfigurationAddon(YouScopeClient client, YouScopeServer server)
	{
		ArrayList<MeasurementConfigurationPage<UserControlMeasurementConfigurationDTO>> pages = new ArrayList<MeasurementConfigurationPage<UserControlMeasurementConfigurationDTO>>();
		pages.add(new StartPage());
		pages.add(new GeneralSettingsPage(client, server));
		pages.add(new MonitorPage(client, server));
		
		contentPane = new MeasurementConfigurationPanel<UserControlMeasurementConfigurationDTO>(pages);
		
		
	}
	
	@Override
	public void createUI(YouScopeFrame frame)
	{
		this.frame = frame;
		
		frame.setTitle("User Control Measurement Configuration");
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
				
				UserControlMeasurementConfigurationAddon.this.frame.endLoading();
			}
		})).start();
	}

	@Override
	public void setConfigurationData(MeasurementConfiguration measurementConfiguration) throws ConfigurationException
	{
		if(!(measurementConfiguration instanceof UserControlMeasurementConfigurationDTO))
			throw new ConfigurationException("Only user control measurement configurations accepted by this addon.");
		contentPane.setConfigurationData((UserControlMeasurementConfigurationDTO)measurementConfiguration);
	}
	
	@Override
	public UserControlMeasurementConfigurationDTO getConfigurationData()
	{
		UserControlMeasurementConfigurationDTO measurementConfiguration = contentPane.getConfigurationData();
		if(measurementConfiguration == null)
		{
			// Set to standard configuration if not set from somebody else
			measurementConfiguration = new UserControlMeasurementConfigurationDTO();
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
