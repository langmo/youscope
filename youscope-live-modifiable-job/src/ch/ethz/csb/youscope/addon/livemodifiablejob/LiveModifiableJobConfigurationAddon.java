/**
 * 
 */
package ch.ethz.csb.youscope.addon.livemodifiablejob;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddonListener;
import ch.ethz.csb.youscope.client.uielements.DynamicPanel;
import ch.ethz.csb.youscope.client.uielements.JobsDefinitionPanel;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;

/**
 * @author Moritz Lang
 */
class LiveModifiableJobConfigurationAddon implements JobConfigurationAddon
{
    private JCheckBox enabledAtStartField = new JCheckBox("Enabled at startup", true);

    private LiveModifiableJobConfigurationDTO job = new LiveModifiableJobConfigurationDTO();

    private YouScopeFrame frame;

    private Vector<JobConfigurationAddonListener> configurationListeners = new Vector<JobConfigurationAddonListener>();

    private JobsDefinitionPanel jobPanel;

    private final YouScopeClient client;

    private final YouScopeServer server;

    LiveModifiableJobConfigurationAddon(YouScopeClient client, YouScopeServer server)
    {
        this.client = client;
        this.server = server;
    }

    @Override
    public void createUI(YouScopeFrame frame)
    {
        this.frame = frame;
        frame.setTitle("Live Modifiable Job");
        frame.setResizable(true);
        frame.setClosable(true);
        frame.setMaximizable(false);

        DynamicPanel mainPanel = new DynamicPanel();
        mainPanel.add(new JLabel("Executed jobs when enabled:"));
        jobPanel = new JobsDefinitionPanel(client, server, frame);
        jobPanel.setJobs(job.getJobs());
        mainPanel.addFill(jobPanel);
        enabledAtStartField.setSelected(job.isEnabledAtStartup());
        mainPanel.add(enabledAtStartField);

        JButton addJobButton = new JButton("Add Job");
        addJobButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    LiveModifiableJobConfigurationAddon.this.job.setJobs(jobPanel.getJobs());
                    LiveModifiableJobConfigurationAddon.this.job.setEnabledAtStartup(enabledAtStartField.isSelected());
                    for (JobConfigurationAddonListener listener : configurationListeners)
                    {
                        listener.jobConfigurationFinished(LiveModifiableJobConfigurationAddon.this.job);
                    }

                    try
                    {
                        LiveModifiableJobConfigurationAddon.this.frame.setVisible(false);
                    }
                    catch (@SuppressWarnings("unused") Exception e1)
                    {
                        // Should not happen!
                    }
                }
            });
        mainPanel.add(addJobButton);

        frame.setContentPane(mainPanel);
        frame.pack();
    }

    @Override
    public void setConfigurationData(JobConfiguration job) throws ConfigurationException
    {
        if (!(job instanceof LiveModifiableJobConfigurationDTO))
            throw new ConfigurationException("Configuration not supported by this addon.");
        this.job = (LiveModifiableJobConfigurationDTO) job;
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
        return LiveModifiableJobConfigurationDTO.TYPE_IDENTIFIER;
    }
}
