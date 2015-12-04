/**
 * 
 */
package org.youscope.plugin.usercontrolmeasurement;

import java.rmi.RemoteException;

import org.youscope.addon.component.ConstructionContext;
import org.youscope.addon.measurement.MeasurementConstructionAddon;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.MeasurementConfiguration;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.PositionInformation;
import org.youscope.common.measurement.callback.CallbackCreationException;
import org.youscope.common.measurement.job.JobCreationException;
import org.youscope.common.measurement.task.MeasurementTask;

/**
 * @author langmo
 * 
 */
class UserControlMeasurementConstructionAddon implements MeasurementConstructionAddon
{

	@Override
	public void initializeMeasurement(Measurement measurement, MeasurementConfiguration measurementConfiguration, ConstructionContext jobInitializer) throws ConfigurationException, RemoteException, JobCreationException
	{
		if(!(measurementConfiguration instanceof UserControlMeasurementConfiguration))
		{
			throw new ConfigurationException("Measurement configuration is not a simple measurement.");
		}
		UserControlMeasurementConfiguration configuration = (UserControlMeasurementConfiguration)measurementConfiguration;

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
