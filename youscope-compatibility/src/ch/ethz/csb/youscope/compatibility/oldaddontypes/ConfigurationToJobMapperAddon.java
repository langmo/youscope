package ch.ethz.csb.youscope.compatibility.oldaddontypes;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;

import ch.ethz.csb.youscope.client.addon.ConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.ConfigurationAddonListener;
import ch.ethz.csb.youscope.client.addon.ConfigurationAddonTools;
import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddonListener;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;

@SuppressWarnings("deprecation")
class ConfigurationToJobMapperAddon implements JobConfigurationAddon {

	private final ConfigurationAddon<? extends JobConfiguration> nativeAddon;
	private final String typeIdentifier;
	private final YouScopeClient client;
	private volatile YouScopeFrame frame = null;
	private final ArrayList<JobConfigurationAddonListener> jobListeners = new ArrayList<JobConfigurationAddonListener>();
	public ConfigurationToJobMapperAddon(String typeIdentifier, ConfigurationAddon<? extends JobConfiguration> nativeAddon, YouScopeClient client) 
	{
		this.nativeAddon = nativeAddon;
		nativeAddon.addConfigurationListener(new ConfigurationAddonListener<JobConfiguration>() 
		{

			@Override
			public void configurationFinished(JobConfiguration configuration) 
			{
				configurationFinished(configuration);
			}
		});
		this.typeIdentifier = typeIdentifier;
		this.client = client;
	}

	@Override
	public void createUI(YouScopeFrame frame) 
	{
		this.frame = frame;
		JPanel contentPane = new JPanel(new BorderLayout());
		try {
			contentPane.add(nativeAddon.toPanel(frame), BorderLayout.CENTER);
		} catch (AddonException e1) {
			frame.setToErrorState("Could not initialize frame.", e1);
			client.sendError("Error while creating configuration UI.", e1);
			return;
		}
		JButton commitButton = new JButton("Commit");
		commitButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				configurationFinished(null);
			}
		});
		contentPane.add(commitButton, BorderLayout.SOUTH);
		frame.setContentPane(contentPane);
		String title = nativeAddon.getConfigurationMetadata().getTypeName();
		title = title.substring(0, 1).toUpperCase()+title.substring(1);
		frame.setTitle(title);
		frame.pack();
	}
	
	private void configurationFinished(JobConfiguration configuration)
	{
		if(configuration == null)
		{
			configuration = nativeAddon.getConfiguration();
		}
		
		try 
		{
			configuration.checkConfiguration();
		}
		catch (ConfigurationException e) 
		{
			YouScopeFrame errorFrame = ConfigurationAddonTools.displayConfigurationInvalid(e, client);
			if(this.frame != null)
				frame.addModalChildFrame(errorFrame);
			errorFrame.setVisible(true);
			
			return;
		}
		synchronized(jobListeners)
		{
			for(JobConfigurationAddonListener configurationListener : jobListeners)
			{
				configurationListener.jobConfigurationFinished(configuration);
			}
		}
		synchronized(this)
		{
			if(frame != null)
				frame.setVisible(false);
		}
	}

	@Override
	public void setConfigurationData(JobConfiguration job) throws ConfigurationException 
	{
		try 
		{
			nativeAddon.setConfiguration(job);
		} 
		catch (AddonException e) 
		{
			throw new ConfigurationException("Failed due to addon exception.", e);
		}
	}

	@Override
	public JobConfiguration getConfigurationData() 
	{
		try {
			return nativeAddon.getConfiguration();
		} catch (Exception e) 
		{
			client.sendError("Could not get configuration data from job addon.", e);
			return null;
		}
	}

	@Override
	public synchronized void addConfigurationListener(JobConfigurationAddonListener listener) 
	{
		jobListeners.add(listener);
	}

	@Override
	public synchronized void removeConfigurationListener(JobConfigurationAddonListener listener) 
	{
		jobListeners.remove(listener);
	}

	@Override
	public String getConfigurationID() 
	{
		return typeIdentifier;
	}

}
