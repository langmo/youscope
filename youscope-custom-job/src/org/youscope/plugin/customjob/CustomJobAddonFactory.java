/**
 * 
 */
package org.youscope.plugin.customjob;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactory;
import org.youscope.addon.component.ComponentAddonUI;
import org.youscope.addon.component.ComponentMetadata;
import org.youscope.addon.component.ConstructionContext;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.JobConfiguration;
import org.youscope.common.measurement.Component;
import org.youscope.common.measurement.PositionInformation;
import org.youscope.common.measurement.job.Job;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 *
 */
public class CustomJobAddonFactory implements ComponentAddonFactory
{
	private CustomJobConfiguration[] loadCustomJobs() throws CustomJobException
	{
		return CustomJobManager.loadCustomJobs();
	}
	
	@Override
	public ComponentAddonUI<?> createComponentUI(String typeIdentifier, YouScopeClient client,
			YouScopeServer server) throws AddonException 
	{
		//Check if a custom job should be constructed.
		CustomJobConfiguration[] customJobs;
		try
		{
			customJobs = loadCustomJobs();
		}
		catch(CustomJobException e)
		{
			throw new AddonException("Could not load custom job configurations.", e);
		}
		for(CustomJobConfiguration job : customJobs)
		{
			if(!job.getTypeIdentifier().equals(typeIdentifier))
				continue;
			CustomJobConfiguration myJob;
			try
			{
				myJob = job.clone();
			}
			catch(CloneNotSupportedException e)
			{
				throw new AddonException("Could not copy configuration of custom job.", e);
			}
			
			CustomJobConfigurationAddon addon = new CustomJobConfigurationAddon(client, server, myJob.getTypeIdentifier(), myJob.getCustomJobName());
			try
			{
				addon.setConfiguration(myJob);
			}
			catch(ConfigurationException e)
			{
				throw new AddonException("Could not initialize custom job configuration.", e);
			}
			return addon;
		}
		throw new AddonException("Factory does not support creation of measurement components with ID " + typeIdentifier+".");
	
	}

	@Override
	public String[] getSupportedTypeIdentifiers() {
		CustomJobConfiguration[] customJobs;
		try
		{
			customJobs = loadCustomJobs();
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
	public boolean isSupportingTypeIdentifier(String typeIdentifier) {
		for(String addonID : getSupportedTypeIdentifiers())
		{
			if(addonID.equals(typeIdentifier))
				return true;
		}
		return false;
	}

	@Override
	public ComponentMetadata<?> getComponentMetadata(String typeIdentifier) throws AddonException {
		CustomJobConfiguration[] customJobs;
		try
		{
			customJobs=loadCustomJobs();
		}
		catch(CustomJobException e)
		{
			throw new AddonException("Could not load custom job configurations.", e);
		}
		for(int i=0; i<customJobs.length; i++)
		{
			if(customJobs[i].getTypeIdentifier().equals(typeIdentifier))
			{
				return CustomJobConfigurationAddon.getMetadata(customJobs[i].getTypeIdentifier(), customJobs[i].getCustomJobName());
			}
		}
		throw new AddonException("Factory does not support creation of measurement components with ID " + typeIdentifier+".");
	}

	@Override
	public Component createComponent(PositionInformation positionInformation, Configuration configuration,
			ConstructionContext constructionContext) throws ConfigurationException, AddonException 
	{
		if(positionInformation == null)
			throw new AddonException("Position information is null");
		else if(configuration == null)
			throw new AddonException("Configuration is null");
		else if(constructionContext == null)
			throw new AddonException("Construction context is null");
		else if(!isSupportingTypeIdentifier(configuration.getTypeIdentifier()))
			throw new AddonException("Configuration with type identifier " + configuration.getTypeIdentifier() + " not supported by this factory.");
		else if(!(configuration instanceof CustomJobConfiguration))
			throw new AddonException("Configuration with type identifier " + configuration.getTypeIdentifier() + " has class " + configuration.getClass().getName()+", which is not of class " + CustomJobConfiguration.class.getName() + " which is expected.");
		
		CustomJobConfiguration jobConfiguration = (CustomJobConfiguration)configuration;
		CustomJob job;
		try
		{
			job = new CustomJobImpl(positionInformation);
			job.setName(jobConfiguration.getCustomJobName());
			for(JobConfiguration childJobConfig : jobConfiguration.getJobs())
			{
			
				Job childJob = constructionContext.getComponentProvider().createJob(positionInformation, childJobConfig);
				job.addJob(childJob);
			}
		}
		catch(Exception e)
		{
			throw new ConfigurationException("Could not create custom job.", e);
		}
		return job;
	}
}
