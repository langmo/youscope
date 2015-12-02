/**
 * 
 */
package ch.ethz.csb.youscope.addon.microplatejob;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import ch.ethz.csb.youscope.client.addon.ConfigurationAddonListener;
import ch.ethz.csb.youscope.client.addon.ConfigurationAddonTools;
import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddonListener;
import ch.ethz.csb.youscope.client.uielements.JobConfigurationPage;
import ch.ethz.csb.youscope.client.uielements.JobConfigurationPanel;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.Configuration;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;


/**
 * @author langmo
 *
 */
public class MicroplateJobConfigurationAddon implements JobConfigurationAddon
{
	private YouScopeFrame									frame;
	private final JobConfigurationPanel<MicroplateJobConfigurationDTO> contentPane;
	
	/**
	 * Constructor.
	 * @param microscope Interface to the microscope.
	 * @param client Interface to the client.
	 */
	MicroplateJobConfigurationAddon(YouScopeClient client, YouScopeServer server)
	{
		ArrayList<JobConfigurationPage<MicroplateJobConfigurationDTO>> pages = new ArrayList<JobConfigurationPage<MicroplateJobConfigurationDTO>>();
		pages.add(new MicroplatePage(client));
		pages.add(new WellSelectionPage(client, server));
		pages.add(new ImagingProtocolPage(client, server));
		
		contentPane = new JobConfigurationPanel<MicroplateJobConfigurationDTO>(pages);
		
		
	}
	
	@Override
	public void createUI(YouScopeFrame frame)
	{
		this.frame = frame;
		
		frame.setTitle("Microplate Job Configuration");
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
				
				MicroplateJobConfigurationAddon.this.frame.endLoading();
			}
		})).start();
	}

	@Override
	public void setConfigurationData(JobConfiguration jobConfiguration) throws ConfigurationException
	{
		if(!(jobConfiguration instanceof MicroplateJobConfigurationDTO))
			throw new ConfigurationException("Only microplate job configurations accepted by this addon.");
		contentPane.setConfigurationData((MicroplateJobConfigurationDTO)jobConfiguration);
	}
	
	@Override
	public MicroplateJobConfigurationDTO getConfigurationData()
	{
		MicroplateJobConfigurationDTO jobConfiguration = contentPane.getConfigurationData();
		if(jobConfiguration == null)
		{
			// Set to standard configuration if not set from somebody else
			jobConfiguration = new MicroplateJobConfigurationDTO();
			contentPane.setToDefault(jobConfiguration);
			
			contentPane.setConfigurationData(jobConfiguration);
		}
		return jobConfiguration;
	}

	private HashMap<JobConfigurationAddonListener, ListenerMapper> listenerMappers = new HashMap<JobConfigurationAddonListener, ListenerMapper>();
	private static class ListenerMapper implements ConfigurationAddonListener<JobConfiguration>
	{
		private final JobConfigurationAddonListener listener;
		ListenerMapper(JobConfigurationAddonListener listener)
		{
			this.listener = listener;
		}
		@Override
		public void configurationFinished(JobConfiguration configuration) {
			listener.jobConfigurationFinished(configuration);
		}
	}
	
	@Override
	public void addConfigurationListener(JobConfigurationAddonListener listener)
	{
		ListenerMapper mapper = new ListenerMapper(listener);
		listenerMappers.put(listener, mapper);
		
		contentPane.addConfigurationListener(mapper);
	}

	@Override
	public void removeConfigurationListener(JobConfigurationAddonListener listener)
	{
		ListenerMapper mapper = listenerMappers.get(listener);
		if(mapper == null)
			return;
		listenerMappers.remove(listener);
		contentPane.removeConfigurationListener(mapper);
	}
	
	private void initilizeFrame()
	{
		// Initialize configuration data for content pane, if yet not done...
		getConfigurationData();
		
		// create content pane
		contentPane.createUI(frame);
		contentPane.addConfigurationListener(new ConfigurationAddonListener<JobConfiguration>()
		{
			@Override
			public void configurationFinished(JobConfiguration configuration) {
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
	public String getConfigurationID()
	{
		return MicroplateJobConfigurationDTO.TYPE_IDENTIFIER;
	}
}
