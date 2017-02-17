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
package org.youscope.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentCreationException;
import org.youscope.addon.measurement.MeasurementAddonFactory;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.MetadataProperty;
import org.youscope.common.PositionInformation;
import org.youscope.common.callback.CallbackProvider;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.image.ImageListener;
import org.youscope.common.job.basicjobs.ContinuousImagingJob;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.common.saving.SaveSettings;
import org.youscope.common.saving.SaveSettingsConfiguration;
import org.youscope.common.task.Task;
import org.youscope.common.task.TaskException;
import org.youscope.common.util.ConfigurationTools;
import org.youscope.serverinterfaces.MeasurementProvider;

/**
 * @author Moritz Lang
 */
class MeasurementProviderImpl extends UnicastRemoteObject implements MeasurementProvider
{
	/**
	 * 
	 */
	private static final long			serialVersionUID	= -668671947105140519L;

	private final MeasurementManager	measurementManager;

	MeasurementProviderImpl(MeasurementManager measurementManager) throws RemoteException
	{
		this.measurementManager = measurementManager;
	}

	@Override
	public Measurement createMeasurement(int measurementRuntime) throws RemoteException
	{
		return new MeasurementRMI(new MeasurementImpl(measurementRuntime), measurementManager);
	}

	@Override
	public Measurement createMeasurement() throws RemoteException
	{
		return new MeasurementRMI(new MeasurementImpl(), measurementManager);
	}

	@Override
	public Measurement createMeasurement(MeasurementConfiguration configuration) throws RemoteException, ConfigurationException, ComponentCreationException
	{
		return createMeasurement(configuration, null);
	}

	@Override
	public Measurement createMeasurement(MeasurementConfiguration configuration, CallbackProvider callbackProvider) throws RemoteException, ConfigurationException, ComponentCreationException
	{
		// make local copy.
		configuration = ConfigurationTools.deepCopy(configuration, MeasurementConfiguration.class);
		// Initialize measurement
		Measurement measurement = createMeasurement(configuration.getMaxRuntime());
		ConstructionContextImpl constructionContext = new ConstructionContextImpl(measurement.getSaver(), callbackProvider);
		try
		{
			measurement.getMetadata().setConfiguration(configuration);
			measurement.setName(configuration.getName());
			measurement.getMetadata().setDescription(configuration.getDescription());
			measurement.getMetadata().setMetadataProperties(configuration.getMetadataProperties().toArray(new MetadataProperty[0]));
			
			SaveSettings saveSettings;
			SaveSettingsConfiguration saveSettingsConfiguration = configuration.getSaveSettings();
			if(saveSettingsConfiguration != null)
			{
				saveSettings = constructionContext.getComponentProvider().createComponent(new PositionInformation(), saveSettingsConfiguration, SaveSettings.class);
			}
			else
				saveSettings = null;
			measurement.getSaver().setSaveSettings(saveSettings);
			measurement.setTypeIdentifier(configuration.getTypeIdentifier());

			// Add startup and shutdown device settings
			measurement.setStartupDeviceSettings(configuration.getDeviseSettingsOn());
			measurement.setFinishDeviceSettings(configuration.getDeviseSettingsOff());
		}
		catch(ComponentRunningException e)
		{
			throw new ConfigurationException("Could not create measurement since it is already running.", e);
		}

		// Initialize the tasks of this measurement
		
		initializeMeasurement(measurement, configuration, constructionContext);

		return measurement;
	}

	@Override
	public Measurement createContinuousMeasurement(String cameraID, String configGroup, String channel, int imagingPeriod, double exposure, ImageListener imageListener) throws RemoteException, ComponentCreationException
	{
		Measurement measurement = createMeasurement();
		ConstructionContextImpl initializer = new ConstructionContextImpl(measurement.getSaver(), null);
		try
		{
			measurement.setName("Continuous Imaging");
			measurement.setLockMicroscopeWhileRunning(false);

			// Create continuous pulling job.
			ContinuousImagingJob job;
			try {
				job = initializer.getComponentProvider().createJob(new PositionInformation(), ContinuousImagingJob.DEFAULT_TYPE_IDENTIFIER, ContinuousImagingJob.class);
			} catch (ComponentCreationException e) {
				throw new ComponentCreationException("Continuous measurement needs the continuos imaging plugin.", e);
			}
			job.setChannel(configGroup, channel);
			job.setExposure(exposure);
			job.setBurstImaging(imagingPeriod <= 0);
			job.addImageListener(imageListener);
			job.setCamera(cameraID);

			// Add a task for the continuous pulling job.
			Task task;
			if(imagingPeriod <= 0)
				task = measurement.addTask(0, false, 0);
			else
				task = measurement.addTask(imagingPeriod, true, 0);
			try {
				task.addJob(job);
			} catch (TaskException e) {
				throw new ComponentCreationException("Could not add job to task.", e);
			}
		}
		catch(ComponentRunningException e)
		{
			// Should not happen, since measurement cannot already run during
			// its construction.
			throw new ComponentCreationException("Can not initialize continuous measurement, since it is already running.", e);
		}
		return measurement;
	}
	
	private void initializeMeasurement(Measurement measurement, MeasurementConfiguration measurementConfiguration, ConstructionContextImpl constructionContext)
            throws ConfigurationException, RemoteException, ComponentCreationException
    {
		// make local copy.
		if(measurementConfiguration != null)
			measurementConfiguration = ConfigurationTools.deepCopy(measurementConfiguration, MeasurementConfiguration.class);
        MeasurementAddonFactory addon =
                ServerSystem.getMeasurementAddonFactory(measurementConfiguration.getTypeIdentifier());
        if (addon == null)
            throw new ComponentCreationException("Type of measurement configuration (" + measurementConfiguration.getTypeIdentifier() + ") unknown.");
        try {
			addon.initializeMeasurement(measurement, measurementConfiguration, constructionContext);
		} catch (AddonException e) {
			throw new ComponentCreationException("Could not initialize measurement.", e);
		}
    }
}
