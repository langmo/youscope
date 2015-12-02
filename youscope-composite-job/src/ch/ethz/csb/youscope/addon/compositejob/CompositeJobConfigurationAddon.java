/**
 * 
 */
package ch.ethz.csb.youscope.addon.compositejob;

import java.awt.Component;

import ch.ethz.csb.youscope.client.addon.ConfigurationAddonAdapter;
import ch.ethz.csb.youscope.client.addon.ConfigurationMetadataAdapter;
import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.uielements.JobsDefinitionPanel;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.CompositeJob;

/**
 * @author Moritz Lang
 */
class CompositeJobConfigurationAddon extends ConfigurationAddonAdapter<CompositeJobConfiguration>
{
	private JobsDefinitionPanel jobPanel = null;
	
    CompositeJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(),  client, server);
	}
    
    static ConfigurationMetadataAdapter<CompositeJobConfiguration> getMetadata()
	{
		return new ConfigurationMetadataAdapter<CompositeJobConfiguration>(CompositeJob.DEFAULT_TYPE_IDENTIFIER, 
				CompositeJobConfiguration.class, 
				CompositeJob.class, 
				"Job Container", 
				new String[]{"Containers"},
				"icons/box.png");
	}
    
    @Override
	protected Component createUI(CompositeJobConfiguration configuration) throws AddonException 
    {
		setTitle("Job Container");
		setResizable(true);
		setMaximizable(false);
        
		jobPanel = new JobsDefinitionPanel(getClient(), getServer(), getContainingFrame());
        jobPanel.setJobs(configuration.getJobs());

        return jobPanel;
    }

	@Override
	protected void commitChanges(CompositeJobConfiguration configuration)
	{
		configuration.setJobs(jobPanel.getJobs());
	}
}
