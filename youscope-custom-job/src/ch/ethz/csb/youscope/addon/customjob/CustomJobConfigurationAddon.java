/**
 * 
 */
package ch.ethz.csb.youscope.addon.customjob;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddonListener;
import ch.ethz.csb.youscope.client.uielements.JobsDefinitionPanel;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;

/**
 * @author Moritz Lang
 */
class CustomJobConfigurationAddon  implements JobConfigurationAddon
{
    private CustomJobConfigurationDTO job = new CustomJobConfigurationDTO();

    private YouScopeFrame									frame;
    private YouScopeClient client; 
    private YouScopeServer server;
    private Vector<JobConfigurationAddonListener> configurationListeners = new Vector<JobConfigurationAddonListener>();
	
	private Exception occuredErrorWhileConstruction = null;
	
    CustomJobConfigurationAddon(YouScopeClient client, YouScopeServer server)
    {
		this.client = client;
		this.server = server;
	}
    
    void setErrorOccured(Exception e)
    {
    	occuredErrorWhileConstruction = e;
    }
    
	@Override
	public void createUI(YouScopeFrame frame)
	{
		this.frame = frame;
		frame.setTitle(job.getCustomJobName());
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setMaximizable(false);
		
		if(occuredErrorWhileConstruction != null)
		{
			frame.setToErrorState("Could not create custom job from template.", occuredErrorWhileConstruction);
			return;
		}
        
		final JobsDefinitionPanel jobPanel = new JobsDefinitionPanel(client, server, frame);
        jobPanel.setJobs(job.getJobs());
		
		JButton addJobButton = new JButton("Add Job");
        addJobButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	job.setJobs(jobPanel.getJobs());
                	for(JobConfigurationAddonListener listener : configurationListeners)
    				{
    					listener.jobConfigurationFinished(CustomJobConfigurationAddon.this.job);
    				}

                    CustomJobConfigurationAddon.this.frame.setVisible(false);
                }
            });
        
        
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(addJobButton, BorderLayout.SOUTH);
        contentPane.add(jobPanel, BorderLayout.CENTER);
        frame.setContentPane(contentPane);
        frame.pack();
    }

    @Override
	public void setConfigurationData(JobConfiguration job) throws ConfigurationException
	{
		if(!(job instanceof CustomJobConfigurationDTO))
			throw new ConfigurationException("Configuration not supported by this addon.");
		this.job = (CustomJobConfigurationDTO)job;
	}

	@Override
	public JobConfiguration getConfigurationData()
	{
		return job;
	}

	@Override
	public void addConfigurationListener(JobConfigurationAddonListener listener)
	{
		configurationListeners.add(listener);
	}

	@Override
	public void removeConfigurationListener(JobConfigurationAddonListener listener)
	{
		configurationListeners.remove(listener);
	}
	@Override
	public String getConfigurationID()
	{
		return job.getTypeIdentifier();
	}
}
