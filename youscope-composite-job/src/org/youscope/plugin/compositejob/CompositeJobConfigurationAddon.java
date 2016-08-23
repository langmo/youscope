/**
 * 
 */
package org.youscope.plugin.compositejob;

import java.awt.Component;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.job.basicjobs.CompositeJob;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.JobsDefinitionPanel;

/**
 * @author Moritz Lang
 */
class CompositeJobConfigurationAddon extends ComponentAddonUIAdapter<CompositeJobConfiguration>
{
	private JobsDefinitionPanel jobPanel = null;
	
    CompositeJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(),  client, server);
	}
    
    static ComponentMetadataAdapter<CompositeJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<CompositeJobConfiguration>(CompositeJob.DEFAULT_TYPE_IDENTIFIER, 
				CompositeJobConfiguration.class, 
				CompositeJob.class, 
				"Job Container", 
				new String[]{"Containers"},
				"A job containing other jobs for grouping. The child-jobs are executed in the given order whenever the composite job is executed.",
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

	@Override
	protected void initializeDefaultConfiguration(CompositeJobConfiguration configuration) throws AddonException {
		// do nothing.
	}
}
