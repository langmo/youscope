/**
 * 
 */
package ch.ethz.csb.youscope.addon.usercontrolmeasurement;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.server.addon.measurement.MeasurementConstructionAddon;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.MeasurementConfiguration;
import ch.ethz.csb.youscope.shared.measurement.Measurement;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.callback.CallbackCreationException;
import ch.ethz.csb.youscope.shared.measurement.job.JobCreationException;
import ch.ethz.csb.youscope.shared.measurement.task.MeasurementTask;

/**
 * @author langmo
 * 
 */
class UserControlMeasurementConstructionAddon implements MeasurementConstructionAddon
{

	@Override
	public void initializeTasksOfMeasurement(Measurement measurement, MeasurementConfiguration measurementConfiguration, ConstructionContext jobInitializer) throws ConfigurationException, RemoteException, JobCreationException
	{
		if(!(measurementConfiguration instanceof UserControlMeasurementConfigurationDTO))
		{
			throw new ConfigurationException("Measurement configuration is not a simple measurement.");
		}
		UserControlMeasurementConfigurationDTO configuration = (UserControlMeasurementConfigurationDTO)measurementConfiguration;

		MeasurementTask mainTask;
		try
		{
			mainTask = measurement.addTask(0, false, 0, -1);
		}
		catch(MeasurementRunningException e)
		{
			throw new ConfigurationException("Could not create measurement since it is already running.", e);
		}
		
		try
		{
			// Allow user to control microscope during this type of measurement
			measurement.setLockMicroscopeWhileRunning(false);
			
			// initialize main job
			UserControlMeasurementJobImpl job = new UserControlMeasurementJobImpl(new PositionInformation(null));
			job.setStageDevice(configuration.getStageDevice());
			job.setStageTolerance(configuration.getStageTolerance());
			job.addImageListener(jobInitializer.getMeasurementSaver().getSaveImageListener("img"));			
			
			// Get callback.
			UserControlMeasurementCallback callback;
			try
			{
				callback = jobInitializer.getCallbackProvider().createCallback(UserControlMeasurementCallback.TYPE_IDENTIFIER, 
						UserControlMeasurementCallback.class);
			}
			catch(CallbackCreationException e1)
			{
				throw new ConfigurationException("Could not create measurement callback for the user control measurement.", e1);
			}
			
			// ping callback
			try
			{
				callback.pingCallback();
			}
			catch(RemoteException e)
			{
				throw new ConfigurationException("Measurement callback for the user control measurement is not responding.", e);
			}
			job.setMeasurementCallback(callback);
			mainTask.addJob(job);
		}
		catch(MeasurementRunningException e)
		{
			throw new ConfigurationException("Could not create measurement since it is already running.", e);
		}			
	}
}
