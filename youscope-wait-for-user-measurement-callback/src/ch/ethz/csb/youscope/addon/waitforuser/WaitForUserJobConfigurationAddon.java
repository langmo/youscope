/**
 * 
 */
package ch.ethz.csb.youscope.addon.waitforuser;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddonListener;
import ch.ethz.csb.youscope.client.uielements.StandardFormats;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;

/**
 * @author Moritz Lang
 */
class WaitForUserJobConfigurationAddon implements JobConfigurationAddon
{
	private JTextArea							messageField				= new JTextArea(6, 30);

	private WaitForUserJobConfigurationDTO	job = new WaitForUserJobConfigurationDTO();

	private YouScopeFrame									frame;
	private Vector<JobConfigurationAddonListener> configurationListeners = new Vector<JobConfigurationAddonListener>();

	private final YouScopeClient client;
	
	/**
	 * Constructor.
	 * @param client
	 * @param server
	 */
	WaitForUserJobConfigurationAddon(YouScopeClient client, YouScopeServer server)
	{
		this.client = client;
	}
	
	
	@Override
	public void createUI(YouScopeFrame frame)
	{
		this.frame = frame;
		frame.setTitle("Wait For User Job");
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setMaximizable(false);
		
		GridBagLayout elementsLayout = new GridBagLayout();
		JPanel elementsPanel = new JPanel(elementsLayout);
		GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
		GridBagConstraints bottomConstr = StandardFormats.getBottomContstraint();
		
		StandardFormats.addGridBagElement(new JLabel("User message:"), elementsLayout, newLineConstr, elementsPanel);
		StandardFormats.addGridBagElement(new JScrollPane(messageField), elementsLayout, bottomConstr, elementsPanel);

		JButton addJobButton = new JButton("Add Job");
		addJobButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				WaitForUserJobConfigurationAddon.this.job.setMessage(messageField.getText());
				for(JobConfigurationAddonListener listener : configurationListeners)
				{
					listener.jobConfigurationFinished(WaitForUserJobConfigurationAddon.this.job);
				}

				try
				{
					WaitForUserJobConfigurationAddon.this.frame.setVisible(false);
				}
				catch(Exception e1)
				{
					// Should not happen!
					client.sendError("Could not close window.", e1);
				}
			}
		});

		// Load state
		messageField.setText(job.getMessage());

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(elementsPanel, BorderLayout.CENTER);
		contentPane.add(addJobButton, BorderLayout.SOUTH);
		frame.setContentPane(contentPane);
		frame.pack();
	}

	@Override
	public void setConfigurationData(JobConfiguration job) throws ConfigurationException
	{
		if(!(job instanceof WaitForUserJobConfigurationDTO))
			throw new ConfigurationException("Configuration not supported by this addon.");
		this.job = (WaitForUserJobConfigurationDTO)job;
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
		return WaitForUserJobConfigurationDTO.TYPE_IDENTIFIER;
	}
}
