/**
 * 
 */
package ch.ethz.csb.youscope.addon.repeatjob;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddon;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;
import ch.ethz.csb.youscope.shared.measurement.ComponentCreationException;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.measurement.job.JobCreationException;

/**
 * Addon to create repeat jobs.
 * @author Moritz Lang
 * 
 */
class RepeatJobConstructionAddon implements JobConstructionAddon
{

	@Override
	public Job createJob(JobConfiguration generalJobConfiguration, ConstructionContext initializer, PositionInformation positionInformation) throws RemoteException, ConfigurationException, JobCreationException
	{
		if(generalJobConfiguration instanceof RepeatJobConfigurationDTO)
		{
			RepeatJobConfigurationDTO jobConfiguration = (RepeatJobConfigurationDTO)generalJobConfiguration;
			RepeatJob job = new RepeatJobImpl(positionInformation);
			try
			{
				job.setNumRepeats(jobConfiguration.getNumRepeats());
			}
			catch(MeasurementRunningException e1)
			{
				throw new JobCreationException("Newly created job already running.", e1);
			}
			for(JobConfiguration childJobConfig : jobConfiguration.getJobs())
			{
				Job childJob;
				try {
					childJob = initializer.getComponentProvider().createJob(positionInformation, childJobConfig);
				} catch (ComponentCreationException e1) {
					throw new JobCreationException("Could not create child job.", e1);
				}
				try
				{
					job.addJob(childJob);
				}
				catch(MeasurementRunningException e)
				{
					throw new JobCreationException("Newly created job already running.", e);
				}
			}
			return job;
		}
		throw new ConfigurationException("Configuration is not supported by this addon.");
	}
}
