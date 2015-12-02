/**
 * 
 */
package ch.ethz.csb.youscope.addon.waitforuser;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddon;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.callback.CallbackCreationException;
import ch.ethz.csb.youscope.shared.measurement.job.Job;

/**
 * @author langmo
 * 
 */
class WaitForUserJobConstructionAddon implements JobConstructionAddon
{

	@Override
	public Job createJob(JobConfiguration generalJobConfiguration, ConstructionContext initializer, PositionInformation positionInformation) throws RemoteException, ConfigurationException
	{
		if(generalJobConfiguration instanceof WaitForUserJobConfigurationDTO)
		{
			WaitForUserJobConfigurationDTO jobConfiguration = (WaitForUserJobConfigurationDTO)generalJobConfiguration;
			String message = jobConfiguration.getMessage();
			if(message == null)
				message = "No message";
			WaitForUserJob waitForUserJob = new WaitForUserJobImpl(positionInformation);
			try
			{
				waitForUserJob.setMessage(message);
			}
			catch(MeasurementRunningException e)
			{
				throw new ConfigurationException("Newly created job already running.", e);
			}
			
			// Get callback.
			WaitForUserCallback callback;
			try
			{
				callback = initializer.getCallbackProvider().createCallback(WaitForUserCallback.TYPE_IDENTIFIER, WaitForUserCallback.class);
						
			}
			catch(CallbackCreationException e1)
			{
				throw new ConfigurationException("Could not create measurement callback for wait for user job.", e1);
			}
			
			// ping callback
			try
			{
				callback.pingCallback();
			}
			catch(RemoteException e)
			{
				throw new ConfigurationException("Measurement callback for wait for user job is not responding.", e);
			}
			try
			{
				waitForUserJob.setMeasurementCallback(callback);
			}
			catch(MeasurementRunningException e)
			{
				throw new ConfigurationException("Newly created job already running.", e);
			}
			
			return waitForUserJob;
		}
		throw new ConfigurationException("Configuration is not supported by this addon.");
	}
}
