/**
 * 
 */
package ch.ethz.csb.youscope.addon.waitjob;

import java.awt.Component;

import javax.swing.JLabel;

import ch.ethz.csb.youscope.client.addon.ConfigurationAddonAdapter;
import ch.ethz.csb.youscope.client.addon.ConfigurationMetadataAdapter;
import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.uielements.DynamicPanel;
import ch.ethz.csb.youscope.client.uielements.JobsDefinitionPanel;
import ch.ethz.csb.youscope.client.uielements.PeriodField;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.WaitJob;

/**
 * @author Moritz Lang
 */
class ExecuteAndWaitJobConfigurationAddon extends ConfigurationAddonAdapter<ExecuteAndWaitJobConfiguration>
{
    private JobsDefinitionPanel jobPanel;
	private final PeriodField waitTimeField = new PeriodField();
	
    ExecuteAndWaitJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
    {
    	super(getMetadata(), client, server);
	}
    
    static ConfigurationMetadataAdapter<ExecuteAndWaitJobConfiguration> getMetadata()
	{
		return new ConfigurationMetadataAdapter<ExecuteAndWaitJobConfiguration>(ExecuteAndWaitJobConfiguration.TYPE_IDENTIFIER, 
				ExecuteAndWaitJobConfiguration.class, 
				WaitJob.class, 
				"Execute and Wait", 
				new String[]{"Containers"},
				"icons/alarm-clock--exclamation.png");
	}
    
    
    @Override
	protected Component createUI(ExecuteAndWaitJobConfiguration configuration) throws AddonException {
		setTitle("Execute and Wait");
		setResizable(true);
		setMaximizable(false);
        
        jobPanel = new JobsDefinitionPanel(getClient(), getServer(), getContainingFrame());
        jobPanel.setJobs(configuration.getJobs());
        waitTimeField.setDuration((int)configuration.getWaitTime());
        
        DynamicPanel contentPane = new DynamicPanel();
        contentPane.add(new JLabel("Total fixed execution time:"));
        contentPane.add(waitTimeField);
        contentPane.add(new JLabel("Jobs to execute:"));
        contentPane.addFill(jobPanel);
        return contentPane;
    }

	@Override
	protected void commitChanges(ExecuteAndWaitJobConfiguration configuration)
	{
		configuration.setJobs(jobPanel.getJobs());
		configuration.setWaitTime(waitTimeField.getDuration());
	}
}
