/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.youscope.plugin.usercontrolmeasurement;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.measurement.MeasurementInitializer;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.PositionInformation;
import org.youscope.common.callback.CallbackCreationException;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.task.Task;
import org.youscope.common.task.TaskException;
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
		Task mainTask;
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
			try {
				mainTask.addJob(job);
			} catch (TaskException e) {
				throw new AddonException("Could not add job to task.", e);
			}
		}
		catch(ComponentRunningException e)
		{
			throw new AddonException("Could not create measurement since it is already running.", e);
		}
		catch (RemoteException e)
		{
			throw new AddonException("Could not create measurement due to remote exception.", e);
		}			
	}
}
