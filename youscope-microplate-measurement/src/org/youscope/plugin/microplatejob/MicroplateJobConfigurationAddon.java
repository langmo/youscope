/**
 * 
 */
package org.youscope.plugin.microplatejob;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentAddonUIListener;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.JobConfiguration;
import org.youscope.common.measurement.job.Job;
import org.youscope.serverinterfaces.YouScopeServer;


/**
 * @author Moritz Lang
 *
 */
public class MicroplateJobConfigurationAddon  extends ComponentAddonUIAdapter<MicroplateJobConfigurationDTO>
{
	private final JobConfigurationPanel<MicroplateJobConfigurationDTO> contentPane;
	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws AddonException 
	 */
	public MicroplateJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(),  client, server);
		ArrayList<JobConfigurationPage<MicroplateJobConfigurationDTO>> pages = new ArrayList<JobConfigurationPage<MicroplateJobConfigurationDTO>>();
		pages.add(new MicroplatePage(client));
		pages.add(new WellSelectionPage(client, server));
		pages.add(new ImagingProtocolPage(client, server));
		
		contentPane = new JobConfigurationPanel<MicroplateJobConfigurationDTO>(pages);
	}
	static ComponentMetadataAdapter<MicroplateJobConfigurationDTO> getMetadata()
	{
		return new ComponentMetadataAdapter<MicroplateJobConfigurationDTO>(MicroplateJobConfigurationDTO.TYPE_IDENTIFIER, 
				MicroplateJobConfigurationDTO.class, 
				Job.class, 
				"Microplate", 
				new String[]{"containers"},
				"icons/map.png");
	}
	
	@Override
	protected Component createUI(MicroplateJobConfigurationDTO configuration) throws AddonException
	{
		setTitle("Microplate Job Configuration");
		setResizable(true);
		setMaximizable(false);
		this.setCommitButton(false);
		
		// Initialize configuration data for content pane, if yet not done...
		getConfiguration();
		
		// create content pane
		contentPane.createUI(getContainingFrame());
		contentPane.addConfigurationListener(new ComponentAddonUIListener<JobConfiguration>()
		{
			@Override
			public void configurationFinished(JobConfiguration configuration) {
				MicroplateJobConfigurationAddon.this.configurationFinished();
			}
		});
		contentPane.addSizeChangeListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				getContainingFrame().pack();
			}
		});
		return contentPane;
	}

	@Override
	public void setConfiguration(Configuration jobConfiguration) throws AddonException, ConfigurationException 
	{
		if(!(jobConfiguration instanceof MicroplateJobConfigurationDTO))
			throw new AddonException("Only microplate job configurations accepted by this addon.");
		contentPane.setConfigurationData((MicroplateJobConfigurationDTO)jobConfiguration);
	}
	
	@Override
	public MicroplateJobConfigurationDTO getConfiguration()
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
	
	@Override
	protected void commitChanges(MicroplateJobConfigurationDTO configuration) {
		// do nothing; we use our own configuration management, thus, this function is not called.
		
	}
}
