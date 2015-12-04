/**
 * 
 */
package org.youscope.plugin.customjob;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.JobsDefinitionPanel;

/**
 * @author Moritz Lang
 *
 */
class CustomJobDefinitionFrame
{
	private final YouScopeFrame									frame;
	
	private final JTextField customJobNameField 									= new JTextField();
		
	private final ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
	private final YouScopeClient client;
	
	CustomJobDefinitionFrame(YouScopeClient client, YouScopeServer server, YouScopeFrame frame)
	{
		this(client, server, frame, new CustomJobConfiguration());
	}
	CustomJobDefinitionFrame(YouScopeClient client, YouScopeServer server, YouScopeFrame frame, final CustomJobConfiguration customJob)
	{
		this.frame = frame;
		this.client = client;
		
		frame.setTitle("Custom Job Template Definition");
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setMaximizable(false);
		
		DynamicPanel elementsPanel = new DynamicPanel();
		elementsPanel.add(new JLabel("Name of job template:"));
		elementsPanel.add(customJobNameField);
		elementsPanel.add(new JLabel("Job definition:"));
		
		final JobsDefinitionPanel jobPanel = new JobsDefinitionPanel(client, server, frame);
        jobPanel.setJobs(customJob.getJobs());
        elementsPanel.addFill(jobPanel);
        
		JButton closeButton = new JButton("Save");
		closeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	String jobName = customJobNameField.getText();
                	if(jobName.length()<3)
                	{
                		JOptionPane.showMessageDialog(null, "Job template name must be longer than three characters.", "Could not initialize microplate type", JOptionPane.INFORMATION_MESSAGE);
                		return;
                	}
                	customJob.setCustomJobName(jobName);
                	customJob.setJobs(jobPanel.getJobs());
                	
                	try
					{
						CustomJobManager.saveCustomJob(customJob);
					}
					catch(CustomJobException e1)
					{
						CustomJobDefinitionFrame.this.client.sendError("Could not save custom job template.", e1);
						return;
					}
                	
                    CustomJobDefinitionFrame.this.frame.setVisible(false); 
                    for(ActionListener listener : listeners)
                    {
                    	listener.actionPerformed(new ActionEvent(this, 154, "Custom job template created or edited."));
                    }
                }
            });
		
		customJobNameField.setText(customJob.getCustomJobName());
        
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(elementsPanel, BorderLayout.CENTER);
        contentPane.add(closeButton, BorderLayout.SOUTH);
        frame.setContentPane(contentPane);
        frame.pack();
	}
	
	public void addActionListener(ActionListener listener)
	{
		listeners.add(listener);
	}
	public void removeActionListener(ActionListener listener)
	{
		listeners.remove(listener);
	}
}
