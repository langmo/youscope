/**
 * 
 */
package ch.ethz.csb.youscope.server.addon.job;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.server.addon.ConstructionAddonFactory;
import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.measurement.job.JobCreationException;

/**
 * Addon to construct a job from its corresponding configuration file.
 * @author Moritz Lang
 * @deprecated Use {@link ConstructionAddonFactory} instead.
 */
@Deprecated
public interface JobConstructionAddon
{
	/**
	 * Called when this addon should initialize the job (and possibly all of its sub-jobs)according to the configuration.
	 * @param jobConfiguration The configuration of this job.
	 * @param initializer An interface providing information about the environment in which the job is initialized, and allowing to initialize sub-jobs or even whole measurements.
	 * @param well The well in which the job gets initialized, or null if this job will not belong to a well measurement.
	 * @param positionInformation The logical position information with which the job should be initialized.
	 * @return The newly created job.
	 * @throws RemoteException
	 * @throws ConfigurationException
	 * @throws JobCreationException 
	 */ 
	Job createJob(JobConfiguration jobConfiguration, ConstructionContext initializer, PositionInformation positionInformation) throws RemoteException, ConfigurationException, JobCreationException;
}
