/**
 * 
 */
package ch.ethz.csb.youscope.addon.customjob;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddon;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.measurement.job.JobCreationException;

/**
 * @author Moritz Lang
 * 
 */
class CustomJobConstructionAddon implements JobConstructionAddon
{

	@Override
	public Job createJob(JobConfiguration generalJobConfiguration, ConstructionContext initializer, PositionInformation positionInformation) throws RemoteException, ConfigurationException, JobCreationException
	{
		if(generalJobConfiguration instanceof CustomJobConfigurationDTO)
		{
			CustomJobConfigurationDTO jobConfiguration = (CustomJobConfigurationDTO)generalJobConfiguration;
			CustomJob job = new CustomJobImpl(positionInformation);
			job.setName(jobConfiguration.getCustomJobName());
			for(JobConfiguration childJobConfig : jobConfiguration.getJobs())
			{
				try
				{
					Job childJob = initializer.getComponentProvider().createJob(positionInformation, childJobConfig);
					job.addJob(childJob);
				}
				catch(Exception e)
				{
					throw new ConfigurationException("Could not create job.", e);
				}
			}
			return job;
		}
		throw new ConfigurationException("Configuration is not supported by this addon.");
	}
}
