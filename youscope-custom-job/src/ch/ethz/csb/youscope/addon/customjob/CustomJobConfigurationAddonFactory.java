/**
 * 
 */
package ch.ethz.csb.youscope.addon.customjob;

import javax.swing.ImageIcon;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddonFactory;
import ch.ethz.csb.youscope.client.uielements.ImageLoadingTools;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;

/**
 * @author Moritz Lang
 *
 */
public class CustomJobConfigurationAddonFactory implements JobConfigurationAddonFactory
{
	private CustomJobConfigurationDTO[] customJobs = null;
	private void loadCustomJobs() throws CustomJobException
	{
		if(customJobs == null)
			customJobs = CustomJobManager.loadCustomJobs();
	}
	
	@Override
	public JobConfigurationAddon createJobConfigurationAddon(String ID, YouScopeClient client, YouScopeServer server)
	{
		CustomJobConfigurationAddon addon = new CustomJobConfigurationAddon(client, server);
		//Check if a custom job should be constructed.
		try
		{
			loadCustomJobs();
		}
		catch(CustomJobException e)
		{
			addon.setErrorOccured(e);
			return addon;
		}
		for(CustomJobConfigurationDTO job : customJobs)
		{
			if(!job.getTypeIdentifier().equals(ID))
				continue;
			CustomJobConfigurationDTO myJob;
			try
			{
				myJob = job.clone();
			}
			catch(CloneNotSupportedException e)
			{
				addon.setErrorOccured(e);
				return addon;
			}
			
			try
			{
				addon.setConfigurationData(myJob);
			}
			catch(ConfigurationException e)
			{
				addon.setErrorOccured(e);
				return addon;
			}
			return addon;
		}
		return null;
	
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		try
		{
			loadCustomJobs();
		}
		catch(@SuppressWarnings("unused") CustomJobException e)
		{
			return new String[0];
		}
		String[] ids = new String[customJobs.length];
		for(int i=0; i<customJobs.length; i++)
		{
			ids[i] = customJobs[i].getTypeIdentifier();
		}
		return ids;
	}

	@Override
	public boolean supportsConfigurationID(String ID)
	{
		for(String addonID : getSupportedConfigurationIDs())
		{
			if(addonID.compareToIgnoreCase(ID) == 0)
				return true;
		}
		return false;
	}

	@Override
	public String getJobName(String ID)
	{
		try
		{
			loadCustomJobs();
		}
		catch(@SuppressWarnings("unused") CustomJobException e)
		{
			return null;
		}
		for(int i=0; i<customJobs.length; i++)
		{
			if(customJobs[i].getTypeIdentifier().equals(ID))
				return "Custom/" + customJobs[i].getCustomJobName();
		}
		return null;
		
	}

	@Override
	public Class<? extends JobConfiguration> getConfigurationClass(String ID)
	{
		try
		{
			loadCustomJobs();
		}
		catch(@SuppressWarnings("unused") CustomJobException e)
		{
			return null;
		}
		for(int i=0; i<customJobs.length; i++)
		{
			if(customJobs[i].getTypeIdentifier().equals(ID))
				return CustomJobConfigurationDTO.class;
		}
		return null;
	}

	@Override
	public ImageIcon getJobIcon(String ID)
	{
		return ImageLoadingTools.getResourceIcon("icons/block-share.png", "Custom Job Configuration");
	}
}
