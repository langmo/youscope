/**
 * 
 */
package ch.ethz.csb.youscope.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import ch.ethz.csb.youscope.server.addon.measurement.MeasurementConstructionAddon;
import ch.ethz.csb.youscope.shared.ImageListener;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.MeasurementConfiguration;
import ch.ethz.csb.youscope.shared.measurement.ComponentCreationException;
import ch.ethz.csb.youscope.shared.measurement.Measurement;
import ch.ethz.csb.youscope.shared.measurement.MeasurementProvider;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.callback.CallbackProvider;
import ch.ethz.csb.youscope.shared.measurement.job.JobCreationException;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.ContinuousImagingJob;
import ch.ethz.csb.youscope.shared.measurement.task.MeasurementTask;

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
	public Measurement createMeasurement(MeasurementConfiguration configuration) throws RemoteException, ConfigurationException, JobCreationException
	{
		return createMeasurement(configuration, null);
	}

	@Override
	public Measurement createMeasurement(MeasurementConfiguration configuration, CallbackProvider callbackProvider) throws RemoteException, ConfigurationException, JobCreationException
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
	public Measurement createContinuousMeasurement(String cameraID, String configGroup, String channel, int imagingPeriod, double exposure, ImageListener imageListener) throws RemoteException, JobCreationException
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
				throw new JobCreationException("Continuous measurement needs the continuos imaging plugin.", e);
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
			throw new JobCreationException("Can not initialize continuous measurement, since it is already running.", e);
		}
		return measurement;
	}
	
	private void initializeMeasurement(Measurement measurement, MeasurementConfiguration measurementConfiguration, CallbackProvider callbackProvider)
            throws ConfigurationException, RemoteException, JobCreationException
    {
        MeasurementConstructionAddon addon =
                ServerSystem.getMeasurementConstructionAddon(measurementConfiguration
                        .getTypeIdentifier());
        if (addon == null)
            throw new ConfigurationException("Type of measurement configuration ("
                    + measurementConfiguration.getTypeIdentifier() + ") unknown.");
        ConstructionContextImpl constructionContext = new ConstructionContextImpl(measurement.getSaver(), callbackProvider, measurement.getUUID());
        addon.initializeTasksOfMeasurement(measurement, measurementConfiguration, constructionContext);
    }
}
