/**
 * 
 */
package org.youscope.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentCreationException;
import org.youscope.addon.measurement.MeasurementAddonFactory;
import org.youscope.common.ImageListener;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.MeasurementConfiguration;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.PositionInformation;
import org.youscope.common.measurement.callback.CallbackProvider;
import org.youscope.common.measurement.job.basicjobs.ContinuousImagingJob;
import org.youscope.common.measurement.task.MeasurementTask;
import org.youscope.serverinterfaces.MeasurementProvider;

/**
 * @author Moritz Lang
 */
class MeasurementFactoryImpl extends UnicastRemoteObject implements MeasurementProvider
{
	/**
	 * 
	 */
	private static final long			serialVersionUID	= -668671947105140519L;

	private final MeasurementManager	measurementManager;

	MeasurementFactoryImpl(MeasurementManager measurementManager) throws RemoteException
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
		// Initialize measurement
		Measurement measurement = createMeasurement(configuration.getMeasurementRuntime());
		try
		{
			measurement.getSaver().setConfiguration(configuration);
			measurement.getSaver().setSaveSettings(configuration.getSaveSettings());
			measurement.setName(configuration.getName());
			measurement.setTypeIdentifier(configuration.getTypeIdentifier());

			// Add startup and shutdown device settings
			measurement.setStartupDeviceSettings(configuration.getDeviseSettingsOn());
			measurement.setFinishDeviceSettings(configuration.getDeviseSettingsOff());
		}
		catch(MeasurementRunningException e)
		{
			throw new ConfigurationException("Could not create measurement since it is already running.", e);
		}

		// Initialize the tasks of this measurement
		
		initializeMeasurement(measurement, configuration, callbackProvider);

		return measurement;
	}

	@Override
	public Measurement createContinuousMeasurement(String cameraID, String configGroup, String channel, int imagingPeriod, double exposure, ImageListener imageListener) throws RemoteException, ComponentCreationException
	{
		Measurement measurement = createMeasurement();
		ConstructionContextImpl initializer = new ConstructionContextImpl(measurement.getSaver(), null, measurement.getUUID());
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
			MeasurementTask task;
			if(imagingPeriod <= 0)
				task = measurement.addTask(0, false, 0);
			else
				task = measurement.addTask(imagingPeriod, true, 0);
			task.addJob(job);
		}
		catch(MeasurementRunningException e)
		{
			// Should not happen, since measurement cannot already run during
			// its construction.
			throw new ComponentCreationException("Can not initialize continuous measurement, since it is already running.", e);
		}
		return measurement;
	}
	
	private void initializeMeasurement(Measurement measurement, MeasurementConfiguration measurementConfiguration, CallbackProvider callbackProvider)
            throws ConfigurationException, RemoteException, ComponentCreationException
    {
        MeasurementAddonFactory addon =
                ServerSystem.getMeasurementAddonFactory(measurementConfiguration.getTypeIdentifier());
        if (addon == null)
            throw new ComponentCreationException("Type of measurement configuration (" + measurementConfiguration.getTypeIdentifier() + ") unknown.");
        ConstructionContextImpl constructionContext = new ConstructionContextImpl(measurement.getSaver(), callbackProvider, measurement.getUUID());
        try {
			addon.initializeMeasurement(measurement, measurementConfiguration, constructionContext);
		} catch (AddonException e) {
			throw new ComponentCreationException("Could not initialize measurement.", e);
		}
    }
}
