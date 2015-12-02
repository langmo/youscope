/**
 * 
 */
package ch.ethz.csb.youscope.addon.composedimaging;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.server.addon.measurement.MeasurementConstructionAddon;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.MeasurementConfiguration;
import ch.ethz.csb.youscope.shared.configuration.RegularPeriod;
import ch.ethz.csb.youscope.shared.configuration.VaryingPeriodDTO;
import ch.ethz.csb.youscope.shared.measurement.Measurement;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.measurement.job.JobCreationException;
import ch.ethz.csb.youscope.shared.measurement.task.MeasurementTask;

/**
 * @author langmo
 * 
 */
public class ComposedImagingMeasurementConstructionAddon implements MeasurementConstructionAddon
{

	@Override
	public void initializeTasksOfMeasurement(Measurement measurement, MeasurementConfiguration measurementConfiguration, ConstructionContext jobInitializer) throws ConfigurationException, RemoteException, JobCreationException
	{
		if(!(measurementConfiguration instanceof ComposedImagingMeasurementConfiguration))
		{
			throw new ConfigurationException("Measurement configuration is not a composed imaging measurement.");
		}
		ComposedImagingMeasurementConfiguration configuration = (ComposedImagingMeasurementConfiguration)measurementConfiguration;

		MeasurementTask task;
		if(configuration.getPeriod() instanceof RegularPeriod)
		{
			RegularPeriod period = (RegularPeriod)configuration.getPeriod();
			try
			{
				task = measurement.addTask(period.getPeriod(), period.isFixedTimes(), period.getStartTime(), period.getNumExecutions());
			}
			catch(MeasurementRunningException e)
			{
				throw new ConfigurationException("Could not create measurement since it is already running.", e);
			}
		}
		else if(configuration.getPeriod() instanceof VaryingPeriodDTO)
		{
			VaryingPeriodDTO period = (VaryingPeriodDTO)configuration.getPeriod();
			try
			{
				task = measurement.addMultiplePeriodTask(period.getPeriods(), period.getBreakTime(), period.getStartTime(), period.getNumExecutions());
			}
			catch(MeasurementRunningException e)
			{
				throw new ConfigurationException("Could not create measurement since it is already running.", e);
			}
		}
		else
		{
			throw new ConfigurationException("Period type is not supported.");
		}
		
		ComposedImagingJobConfiguration jobConfiguration = new ComposedImagingJobConfiguration();
		jobConfiguration.setChannel(configuration.getChannelGroup(), configuration.getChannel());
		jobConfiguration.setExposure(configuration.getExposure());
		jobConfiguration.setImageSaveName(configuration.getImageSaveName());
		jobConfiguration.setNumPixels(configuration.getNumPixels());
		jobConfiguration.setNx(configuration.getNx());
		jobConfiguration.setNy(configuration.getNy());
		jobConfiguration.setOverlap(configuration.getOverlap());
		jobConfiguration.setPixelSize(configuration.getPixelSize());
		jobConfiguration.setSaveImages(configuration.isSaveImages());
		
		try
		{
			Job job = jobInitializer.getComponentProvider().createJob(new PositionInformation(), jobConfiguration);
			task.addJob(job);
		}
		catch(Exception e)
		{
			throw new ConfigurationException("Could not create measurement.", e);
		}
	}
}
