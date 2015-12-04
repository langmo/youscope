/**
 * 
 */
package org.youscope.plugin.usercontrolmeasurement;

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
class UserControlMeasurementConfigurationAddon implements MeasurementConfigurationAddon<UserControlMeasurementConfiguration>
{
	private YouScopeFrame									frame;
	private final MeasurementAddonUIAdapter<UserControlMeasurementConfiguration> contentPane;
	
	/**
	 * Constructor.
	 * @param microscope Interface to the microscope.
	 * @param client Interface to the client.
	 */
	UserControlMeasurementConfigurationAddon(YouScopeClient client, YouScopeServer server)
	{
		ArrayList<MeasurementAddonUIPage<UserControlMeasurementConfiguration>> pages = new ArrayList<MeasurementAddonUIPage<UserControlMeasurementConfiguration>>();
		pages.add(new StartPage());
		pages.add(new GeneralSettingsPage(client, server));
		pages.add(new MonitorPage(client, server));
		
		contentPane = new MeasurementAddonUIAdapter<UserControlMeasurementConfiguration>(pages);
		
		
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
	public void setConfiguration(Configuration measurementConfiguration) throws ConfigurationException
	{
		if(!(measurementConfiguration instanceof UserControlMeasurementConfiguration))
			throw new ConfigurationException("Only user control measurement configurations accepted by this addon.");
		contentPane.setConfigurationData((UserControlMeasurementConfiguration)measurementConfiguration);
	}
	
	@Override
	public UserControlMeasurementConfiguration getConfiguration()
	{
		UserControlMeasurementConfiguration measurementConfiguration = contentPane.getConfigurationData();
		if(measurementConfiguration == null)
		{
			// Set to standard configuration if not set from somebody else
			measurementConfiguration = new UserControlMeasurementConfiguration();
			contentPane.setToDefault(measurementConfiguration);
			contentPane.setConfigurationData(measurementConfiguration);
		}
		return measurementConfiguration;
	}

	@Override
	public void addUIListener(ComponentAddonUIListener<? super UserControlMeasurementConfiguration> listener)
	{
		contentPane.addConfigurationListener(listener);
	}

	@Override
	public void removeUIListener(ComponentAddonUIListener<? super UserControlMeasurementConfiguration> listener)
	{
		contentPane.removeConfigurationListener(listener);
	}
	
	private void initilizeFrame()
	{
		// Initialize configuration data for content pane, if yet not done...
		getConfiguration();
		
		// create content pane
		contentPane.createUI(frame);
		contentPane.addConfigurationListener(new ComponentAddonUIListener<UserControlMeasurementConfiguration>()
		{
			@Override
			public void configurationFinished(UserControlMeasurementConfiguration configuration)
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
