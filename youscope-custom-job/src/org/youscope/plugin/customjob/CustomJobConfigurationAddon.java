/**
 * 
 */
package org.youscope.plugin.customjob;

import java.awt.Component;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.JobsDefinitionPanel;

/**
 * @author Moritz Lang
 */
class CustomJobConfigurationAddon extends ComponentAddonUIAdapter<CustomJobConfiguration>
{
	private JobsDefinitionPanel jobPanel = null; 
	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @param typeIdentifier Type identifier of custom job.
	 * @param name Name of custom job.
	 * @throws AddonException 
	 */
	public CustomJobConfigurationAddon(YouScopeClient client, YouScopeServer server, String typeIdentifier, String name) throws AddonException
	{
		super(getMetadata(typeIdentifier, name),  client, server);
	}
    
	static ComponentMetadataAdapter<CustomJobConfiguration> getMetadata(String typeIdentifier, String name)
	{
		return new ComponentMetadataAdapter<CustomJobConfiguration>(typeIdentifier, 
				CustomJobConfiguration.class, 
				CustomJob.class, 
				name, 
				new String[]{"custom"},
				"icons/block-share.png");
	}
    
    @Override
	protected Component createUI(CustomJobConfiguration configuration) throws AddonException
	{
		setTitle(configuration.getCustomJobName());
		setResizable(true);
		setMaximizable(false);
        
		jobPanel = new JobsDefinitionPanel(getClient(), getServer(), getContainingFrame());
        jobPanel.setJobs(configuration.getJobs());
		
		return jobPanel;
    }

	@Override
	protected void commitChanges(CustomJobConfiguration configuration) {
		configuration.setJobs(jobPanel.getJobs());
	}

	@Override
	protected void initializeDefaultConfiguration(CustomJobConfiguration configuration) throws AddonException {
		// do nothing.
	}
}
