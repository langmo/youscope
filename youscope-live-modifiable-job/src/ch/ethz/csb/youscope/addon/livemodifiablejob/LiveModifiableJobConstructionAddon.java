/**
 * 
 */
package ch.ethz.csb.youscope.addon.livemodifiablejob;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddon;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;
import ch.ethz.csb.youscope.shared.measurement.ComponentCreationException;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.callback.CallbackCreationException;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.measurement.job.JobCreationException;

/**
 * @author langmo
 */
class LiveModifiableJobConstructionAddon implements JobConstructionAddon
{

    @Override
    public Job createJob(JobConfiguration generalJobConfiguration, ConstructionContext initializer, PositionInformation positionInformation) throws RemoteException, ConfigurationException, JobCreationException
    {
        if (generalJobConfiguration instanceof LiveModifiableJobConfigurationDTO)
        {           
            LiveModifiableJobConfigurationDTO jobConfiguration = (LiveModifiableJobConfigurationDTO) generalJobConfiguration;
            LiveModifiableJobImpl modifiableJob = new LiveModifiableJobImpl(positionInformation);
            
            // Get callback.
            LiveModifiableJobCallback callback;
            try
            {
                callback = initializer.getCallbackProvider().createCallback(LiveModifiableJobCallback.TYPE_IDENTIFIER, LiveModifiableJobCallback.class);
            } catch (CallbackCreationException e1)
            {
            	throw new JobCreationException("Could not obtain measurement callback for live modifiable job.", e1);
            }

            // ping callback
            try
            {
                callback.pingCallback();
            } catch (RemoteException e)
            {
                throw new JobCreationException("Callback for live modifiable job is not responding.", e);
            }
            
            modifiableJob.setData(initializer, callback);
            modifiableJob.setEnabled(jobConfiguration.isEnabledAtStartup());
            try
            {
                modifiableJob.setChildJobConfigurations(jobConfiguration.getJobs());
            } catch (CloneNotSupportedException e1)
            {
                throw new JobCreationException("Child jobs do not allow for clone().", e1);
            }

            // Add all child jobs
            try
            {
                for (JobConfiguration childJobConfig : jobConfiguration.getJobs())
                {
                    Job childJob =
                            initializer.getComponentProvider().createJob(positionInformation, childJobConfig);
                    modifiableJob.addJob(childJob);
                }
            } catch (MeasurementRunningException e)
            {
                throw new JobCreationException("Newly created job already running.", e);
            } catch (ComponentCreationException e) {
            	 throw new JobCreationException("Error while creating child job.", e);
			}

            return modifiableJob;
        }
		throw new ConfigurationException("Configuration is not supported by this addon.");
    }
}
