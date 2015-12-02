/**
 * 
 */
package ch.ethz.csb.youscope.addon.repeatjob;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddonListener;
import ch.ethz.csb.youscope.client.uielements.DescriptionPanel;
import ch.ethz.csb.youscope.client.uielements.DynamicPanel;
import ch.ethz.csb.youscope.client.uielements.IntegerTextField;
import ch.ethz.csb.youscope.client.uielements.JobsDefinitionPanel;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;

/**
 * Addon to configure repeat jobs.
 * @author Moritz Lang
 */
class RepeatJobConfigurationAddon  implements JobConfigurationAddon
{

    private RepeatJobConfigurationDTO job = new RepeatJobConfigurationDTO();

    private YouScopeFrame									frame;
    private YouScopeClient client; 
    private YouScopeServer server;
    private Vector<JobConfigurationAddonListener> configurationListeners = new Vector<JobConfigurationAddonListener>();

	private JobsDefinitionPanel jobPanel;
	private IntegerTextField numRepeatsField = new IntegerTextField(1);
	
    RepeatJobConfigurationAddon(YouScopeClient client, YouScopeServer server)
    {
		this.client = client;
		this.server = server;
	}
	@Override
	public void createUI(YouScopeFrame frame)
	{
		this.frame = frame;
		frame.setTitle("Job Container");
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setMaximizable(false);
        
		JButton addJobButton = new JButton("Add Job Container");
        addJobButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	job.setJobs(jobPanel.getJobs());
                	job.setNumRepeats(numRepeatsField.getValue());
                	for(JobConfigurationAddonListener listener : configurationListeners)
    				{
    					listener.jobConfigurationFinished(RepeatJobConfigurationAddon.this.job);
    				}

                    RepeatJobConfigurationAddon.this.frame.setVisible(false);
                }
            });
        
        jobPanel = new JobsDefinitionPanel(client, server, frame);
        
        DynamicPanel topPanel = new DynamicPanel();
        DescriptionPanel descriptionPanel = new DescriptionPanel("Repeats all sub-jobs for a given amount of times.\nAdd wait jobs to control timing.");
        topPanel.add(descriptionPanel);
        topPanel.add(new JLabel("Number of times sub-jobs should be repeated:"));
        numRepeatsField.setMinimalValue(0);
        numRepeatsField.setOpaque(true);
        topPanel.add(numRepeatsField);
        topPanel.add(new JLabel("Sub-jobs to be repeated:"));
        
        // load state
        jobPanel.setJobs(job.getJobs());
        numRepeatsField.setValue(job.getNumRepeats());
        
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(addJobButton, BorderLayout.SOUTH);
        contentPane.add(topPanel, BorderLayout.NORTH);
        contentPane.add(jobPanel, BorderLayout.CENTER);
        frame.setContentPane(contentPane);
        frame.pack();
    }

    @Override
	public void setConfigurationData(JobConfiguration job) throws ConfigurationException
	{
		if(!(job instanceof RepeatJobConfigurationDTO))
			throw new ConfigurationException("Configuration not supported by this addon.");
		this.job = (RepeatJobConfigurationDTO)job;
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
		return RepeatJobConfigurationDTO.TYPE_IDENTIFIER;
	}
}
