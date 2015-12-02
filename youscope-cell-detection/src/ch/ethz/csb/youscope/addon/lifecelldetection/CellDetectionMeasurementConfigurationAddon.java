/**
 * 
 */
package ch.ethz.csb.youscope.addon.lifecelldetection;

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
 * @author Moritz Lang
 */
class ContinuousLifeCellDetectionMeasurementConfigurationAddon implements MeasurementConfigurationAddon
{

	private YouScopeFrame									frame;
	
	private final MeasurementConfigurationPanel<CellDetectionMeasurementConfiguration> contentPane;
	
	/**
	 * Constructor.
	 * @param microscope Interface to the microscope.
	 * @param client Interface to the client.
	 */
	ContinuousLifeCellDetectionMeasurementConfigurationAddon(YouScopeClient client, YouScopeServer server)
	{
		ArrayList<MeasurementConfigurationPage<CellDetectionMeasurementConfiguration>> pages = new ArrayList<MeasurementConfigurationPage<CellDetectionMeasurementConfiguration>>();
		pages.add(new StartPage());
		pages.add(new GeneralSettingsPage(client, server));
		pages.add(new StartAndEndConfigurationPage(client, server));
		pages.add(new ImagingConfigurationPage(client, server));
		
		contentPane = new MeasurementConfigurationPanel<CellDetectionMeasurementConfiguration>(pages);
		
	}

	@Override
	public void createUI(YouScopeFrame frame)
	{
		this.frame = frame;
		frame.setTitle("Continuous Cell Detection Measurement");
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
				ContinuousLifeCellDetectionMeasurementConfigurationAddon.this.frame.endLoading();
			}
		})).start();
	}

	@Override
	public CellDetectionMeasurementConfiguration getConfigurationData()
	{
		CellDetectionMeasurementConfiguration measurementConfiguration = contentPane.getConfigurationData();
		if(measurementConfiguration == null)
		{
			// Set to standard configuration if not set from somebody else
			measurementConfiguration = new CellDetectionMeasurementConfiguration();
			contentPane.setToDefault(measurementConfiguration);
			
			contentPane.setConfigurationData(measurementConfiguration);
		}
		return measurementConfiguration;
	}

	@Override
	public void setConfigurationData(MeasurementConfiguration measurementConfiguration) throws ConfigurationException
	{
		if(!(measurementConfiguration instanceof CellDetectionMeasurementConfiguration))
			throw new ConfigurationException("Only continuous life cell detection measurement configurations accepted by this addon.");
		contentPane.setConfigurationData((CellDetectionMeasurementConfiguration)measurementConfiguration);
	}

	private void initilizeFrame()
	{
		getConfigurationData();
		
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

}
