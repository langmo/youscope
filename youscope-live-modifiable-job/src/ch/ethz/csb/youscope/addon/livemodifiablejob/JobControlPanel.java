package ch.ethz.csb.youscope.addon.livemodifiablejob;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.border.TitledBorder;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.uielements.DynamicPanel;
import ch.ethz.csb.youscope.client.uielements.JobsDefinitionPanel;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;

class JobControlPanel extends DynamicPanel
{
    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = -2205972694294578191L;
    private final JobHolder jobHolder;
    
    public JobControlPanel(final YouScopeClient client, final YouScopeServer server, final YouScopeFrame frame, final JobHolder jobHolder) throws Exception
    {
        this.jobHolder = jobHolder;
        final JobsDefinitionPanel childJobsField = new JobsDefinitionPanel(client, server, frame);
        final JCheckBox jobEnabledField = new JCheckBox("JobEnabled");
        
        LiveModifiableJob job = jobHolder.getJob();
        if(job == null)
        {
            throw new Exception("Job already uninitialized.");
        }
        JobConfiguration[] childJobConfigs = job.getChildJobConfigurations();
        childJobsField.setJobs(childJobConfigs);
        jobEnabledField.setSelected(job.isEnabled());
        
        setBorder(new TitledBorder("Control of job " + jobHolder.toString()));
        add(jobEnabledField);
        addFill(childJobsField);
        final JButton updateChildJobsButton = new JButton("Update job configuration");
        add(updateChildJobsButton);
        updateChildJobsButton.addActionListener(new ActionListener()
            {                
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    try
                    {
                        updateChildJobs(childJobsField.getJobs());
                    } catch (Exception e1)
                    {
                        client.sendError("Could not update child jobs.", e1);
                    }
                }
            });
        jobEnabledField.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    setJobEnabled(jobEnabledField.isSelected());
                } catch (Exception e1)
                {
                    client.sendError("Could not change enable state.", e1);
                }
            }
        });
    }
    
    public boolean isStillValid()
    {
        return this.jobHolder.getJob() != null;
    }
    private void updateChildJobs(JobConfiguration[] jobs) throws Exception
    {
        LiveModifiableJob job = jobHolder.getJob();
        if(job == null)
        {
            throw new Exception("Job already uninitialized.");
        }
        job.setChildJobConfigurations(jobs);
    }
    private void setJobEnabled(final boolean enabled) throws Exception
    {
        LiveModifiableJob job = jobHolder.getJob();
        if(job == null)
        {
            throw new Exception("Job already uninitialized.");
        }
        job.setEnabled(enabled);
    }
    
}
