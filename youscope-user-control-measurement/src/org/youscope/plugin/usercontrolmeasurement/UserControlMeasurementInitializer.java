/**
 * 
 */
package org.youscope.plugin.usercontrolmeasurement;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.measurement.MeasurementInitializer;
import org.youscope.common.PositionInformation;
import org.youscope.common.callback.CallbackCreationException;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.task.MeasurementTask;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author langmo
 * 
 */
class UserControlMeasurementInitializer implements MeasurementInitializer<UserControlMeasurementConfiguration>
{

	@Override
	public void initializeMeasurement(Measurement measurement, UserControlMeasurementConfiguration configuration, ConstructionContext jobInitializer) throws ConfigurationException, AddonException
	{
		MeasurementTask mainTask;
		try
		{
			mainTask = measurement.addTask(0, false, 0, -1);
	
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
				throw new AddonException("Could not create measurement callback for the user control measurement.", e1);
			}
			
			// ping callback
			try
			{
				callback.pingCallback();
			}
			catch(RemoteException e)
			{
				throw new AddonException("Measurement callback for the user control measurement is not responding.", e);
			}
			job.setMeasurementCallback(callback);
			mainTask.addJob(job);
		}
		catch(MeasurementRunningException e)
		{
			throw new AddonException("Could not create measurement since it is already running.", e);
		}
		catch (RemoteException e)
		{
			throw new AddonException("Could not create measurement due to remote exception.", e);
		}			
	}
}
