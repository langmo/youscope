/**
 * 
 */
package ch.ethz.csb.youscope.addon.waitjob;

import java.awt.Component;

import javax.swing.JLabel;

import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.WaitJob;
import ch.ethz.csb.youscope.client.addon.ConfigurationAddonAdapter;
import ch.ethz.csb.youscope.client.addon.ConfigurationMetadataAdapter;
import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.uielements.DynamicPanel;
import ch.ethz.csb.youscope.client.uielements.PeriodField;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.addon.AddonException;

/**
 * @author Moritz Lang
 */
class WaitJobConfigurationAddon extends ConfigurationAddonAdapter<WaitJobConfiguration>
{
    private final PeriodField waitTimeField = new PeriodField();
	
    /**
     * Constructor
	 * @param client  
     * @param server 
     * @throws AddonException 
	 */
    WaitJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
    {
    	super(getMetadata(), client, server);
	}
    
    static ConfigurationMetadataAdapter<WaitJobConfiguration> getMetadata()
	{
		return new ConfigurationMetadataAdapter<WaitJobConfiguration>(WaitJob.DEFAULT_TYPE_IDENTIFIER, 
				WaitJobConfiguration.class, 
				WaitJob.class, 
				"Wait", 
				new String[]{"Elementary"},
				"icons/alarm-clock-select.png");
	}
    
    @Override
	protected Component createUI(WaitJobConfiguration configuration) throws AddonException {
		setTitle("Wait");
		setResizable(false);
		setMaximizable(false);
        
        waitTimeField.setDuration((int)configuration.getWaitTime());
        
        DynamicPanel contentPane = new DynamicPanel();
        contentPane.add(new JLabel("Wait time:"));
        contentPane.add(waitTimeField);

        return contentPane;
    }

	@Override
	protected void commitChanges(WaitJobConfiguration configuration)
	{
		configuration.setWaitTime(waitTimeField.getDuration());
	}

    
}
