/**
 * 
 */
package ch.ethz.csb.youscope.addon.microplatemeasurement;

import java.rmi.RemoteException;
import java.util.ServiceLoader;

import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.server.addon.measurement.MeasurementConstructionAddon;
import ch.ethz.csb.youscope.shared.Well;
import ch.ethz.csb.youscope.shared.addon.pathoptimizer.PathOptimizer;
import ch.ethz.csb.youscope.shared.addon.pathoptimizer.PathOptimizerPosition;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;
import ch.ethz.csb.youscope.shared.configuration.MeasurementConfiguration;
import ch.ethz.csb.youscope.shared.configuration.RegularPeriod;
import ch.ethz.csb.youscope.shared.configuration.VaryingPeriodDTO;
import ch.ethz.csb.youscope.shared.measurement.ComponentCreationException;
import ch.ethz.csb.youscope.shared.measurement.Measurement;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.EditableJobContainer;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.measurement.job.JobCreationException;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.ChangePositionJob;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.CompositeJob;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.FocusingJob;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.StatisticsJob;
import ch.ethz.csb.youscope.shared.measurement.task.MeasurementTask;
import ch.ethz.csb.youscope.shared.microscope.DeviceSettingDTO;

/**
 * @author langmo
 * 
 */
public class MicroplateMeasurementConstructionAddon implements MeasurementConstructionAddon
{

	@Override
	public void initializeTasksOfMeasurement(Measurement measurement, MeasurementConfiguration measurementConfiguration, ConstructionContext jobInitializer) throws ConfigurationException, RemoteException, JobCreationException
	{
		if(!(measurementConfiguration instanceof MicroplateMeasurementConfigurationDTO))
		{
			throw new ConfigurationException("Measurement configuration is not a microplate measurement.");
		}
		MicroplateMeasurementConfigurationDTO configuration = (MicroplateMeasurementConfigurationDTO)measurementConfiguration;

		String stageDeviceID = configuration.getStageDevice();		
		
		int wellTime = configuration.getTimePerWell();

		// If iteration through wells should be AFAP, put everything in one task
		MeasurementTask mainTask = null;
		if(wellTime <= 0)
		{
			if(configuration.getPeriod() instanceof RegularPeriod)
			{
				RegularPeriod period = (RegularPeriod)configuration.getPeriod();
				try
				{
					mainTask = measurement.addTask(period.getPeriod(), period.isFixedTimes(), period.getStartTime(), period.getNumExecutions());
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
					mainTask = measurement.addMultiplePeriodTask(period.getPeriods(), period.getBreakTime(), period.getStartTime(), period.getNumExecutions());
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
		}

		// Iterate over all wells and positions
		MicroplatePositionConfigurationDTO posConf = configuration.getMicroplatePositions();
		boolean isMultiPos = posConf.getWellNumPositionsX() > 1 || posConf.getWellNumPositionsY() > 1;
		boolean isWellMeasurement = !posConf.isAliasMicroplate();
		
		String pathOptimizerID = configuration.getPathOptimizerID();
		if(pathOptimizerID == null)
			pathOptimizerID = "CSB::NonOptimizedOptimizer";
		PathOptimizer pathOptimizer = getPathOptimizer(pathOptimizerID);
		if(pathOptimizer == null)
			throw new ConfigurationException("Path optimizer with ID \"" + pathOptimizerID + "\" not known.");
		if(!pathOptimizer.isApplicable(posConf))
			throw new ConfigurationException("Path optimizer with ID \"" + pathOptimizerID + "\" is not applicable for given position configuration.");
		Iterable<PathOptimizerPosition> path = pathOptimizer.getPath(posConf);
		if(path == null)
			throw new ConfigurationException("Path created by path optimizer \"" + pathOptimizerID + "\" is null. Try another optimizer.");
		
		// Iterate over all positions
		int elementNum = -1;
		
		for(PathOptimizerPosition position : path)
		{
				elementNum++;
				if(elementNum == 0 && stageDeviceID != null)
				{
					// Add zero position to startup and shutdown settings
					try
					{
						measurement.addStartupDeviceSetting(new DeviceSettingDTO(stageDeviceID, "PositionX", (float)position.getX()));
						measurement.addStartupDeviceSetting(new DeviceSettingDTO(stageDeviceID, "PositionY", (float)position.getY()));
						measurement.addFinishDeviceSetting(new DeviceSettingDTO(stageDeviceID, "PositionX", (float)position.getX()));
						measurement.addFinishDeviceSetting(new DeviceSettingDTO(stageDeviceID, "PositionY", (float)position.getY()));
					}
					catch(MeasurementRunningException e)
					{
						throw new ConfigurationException("Could not create measurement since it is already running.", e); 
					}
				}

				PositionInformation positionInformation;
				String locationString;
				if(isWellMeasurement && isMultiPos)
				{
					Well well = new Well(position.getWellY(), position.getWellX());
					positionInformation = new PositionInformation(well);
					positionInformation = new PositionInformation(positionInformation, PositionInformation.POSITION_TYPE_YTILE, position.getPositionY());
					positionInformation = new PositionInformation(positionInformation, PositionInformation.POSITION_TYPE_XTILE, position.getPositionX());
					
					locationString = "well " + well.getWellName() + ", tile [" + Integer.toString(position.getPositionY() + 1) + ", " + Integer.toString(position.getPositionX() + 1) + "]";
				}
				else if(isWellMeasurement)
				{
					Well well = new Well(position.getWellY(), position.getWellX());
					positionInformation = new PositionInformation(well);
					
					locationString = "well " + well.getWellName();
				}
				else if(isMultiPos)
				{
					positionInformation = new PositionInformation(null);
					positionInformation = new PositionInformation(positionInformation, PositionInformation.POSITION_TYPE_MAIN_POSITION, position.getWellX());
					positionInformation = new PositionInformation(positionInformation, PositionInformation.POSITION_TYPE_YTILE, position.getPositionY());
					positionInformation = new PositionInformation(positionInformation, PositionInformation.POSITION_TYPE_XTILE, position.getPositionX());
					
					locationString = "position " + Integer.toString(position.getWellX()+1) + ", tile [" + Integer.toString(position.getPositionY() + 1) + ", " + Integer.toString(position.getPositionX() + 1) + "]";
				}
				else
				{
					positionInformation = new PositionInformation(null);
					positionInformation = new PositionInformation(positionInformation, PositionInformation.POSITION_TYPE_MAIN_POSITION, position.getWellX());
					
					locationString = "position " + Integer.toString(position.getWellX()+1);
				}

				MeasurementTask wellTask;
				if(wellTime <= 0)
				{
					// Put everything in one job
					wellTask = mainTask;
				}
				else
				{
					// Get properties for actual well
					int startTime = configuration.getPeriod().getStartTime() + elementNum * wellTime;

					if(configuration.getPeriod() instanceof RegularPeriod)
					{
						RegularPeriod period = (RegularPeriod)configuration.getPeriod();
						int mainPeriod = period.getPeriod();
						boolean fixedTimes = period.isFixedTimes();
						if(mainPeriod <= 0)
						{
							mainPeriod = posConf.getTotalMeasuredPositions() * wellTime;
							fixedTimes = true;
						}
						try
						{
							wellTask = measurement.addTask(mainPeriod, fixedTimes, startTime, period.getNumExecutions());
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
							wellTask = measurement.addMultiplePeriodTask(period.getPeriods(), period.getBreakTime(), startTime, period.getNumExecutions());
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
				}

				try
				{
					// Create a job container in which all jobs at the given position are put into.
					EditableJobContainer jobContainer;
					if(configuration.getStatisticsFileName() == null)
					{
						CompositeJob job;
						try {
							job = jobInitializer.getComponentProvider().createJob(positionInformation, CompositeJob.DEFAULT_TYPE_IDENTIFIER, CompositeJob.class);
						} catch (ComponentCreationException e) {
							throw new JobCreationException("Microplate measurements need the composite job plugin.",e);
						}
						job.setName("Job container for " + locationString);
						wellTask.addJob(job);
						jobContainer = job;
					}
					else
					{
						StatisticsJob job;
						try {
							job = jobInitializer.getComponentProvider().createJob(positionInformation, StatisticsJob.DEFAULT_TYPE_IDENTIFIER, StatisticsJob.class);
						} catch (ComponentCreationException e) {
							throw new JobCreationException("Microplate measurements need the statistic job plugin.",e);
						}
							
						job.setName("Job container/analyzer for " + locationString);
						job.addTableListener(jobInitializer.getMeasurementSaver().getSaveTableDataListener(configuration.getStatisticsFileName()));
						wellTask.addJob(job);
						jobContainer = job;
					}
					
					// Set position to well
					ChangePositionJob positionJob;
					try {
						positionJob = jobInitializer.getComponentProvider().createJob(positionInformation, ChangePositionJob.DEFAULT_TYPE_IDENTIFIER, ChangePositionJob.class);
					} catch (ComponentCreationException e) {
						throw new JobCreationException("Microplate measurements need the change position job plugin.", e);
					}
					positionJob.setPosition(position.getX(), position.getY());
					positionJob.setStageDevice(stageDeviceID);
					positionJob.setName("Moving stage to " + locationString);
					jobContainer.addJob(positionJob);

					// Set Focus
					if(configuration.getFocusConfiguration() != null)
					{
						FocusingJob focusingJob;
						try {
							focusingJob = jobInitializer.getComponentProvider().createJob(positionInformation, FocusingJob.DEFAULT_TYPE_IDENTIFIER, FocusingJob.class);
						} catch (ComponentCreationException e) {
							throw new JobCreationException("Microplate measurements need the focusing job plugin.", e);
						}
						focusingJob.setFocusAdjustmentTime(configuration.getFocusConfiguration().getAdjustmentTime());
						focusingJob.setPosition(position.getFocus(), false);
						focusingJob.setFocusDevice(configuration.getFocusConfiguration().getFocusDevice());
						focusingJob.setName("Setting focus for " + locationString);
						jobContainer.addJob(focusingJob);
						
					}

					// Add all other configured jobs
					JobConfiguration[] jobConfigurations = configuration.getJobs();
					for(JobConfiguration jobConfiguration : jobConfigurations)
					{
						Job job;
						try {
							job = jobInitializer.getComponentProvider().createJob(positionInformation, jobConfiguration);
						} catch (ComponentCreationException e) {
							throw new JobCreationException("Could not create child job of microplate measurement.", e);
						}
						jobContainer.addJob(job);
					}
				}
				catch(MeasurementRunningException e)
				{
					throw new ConfigurationException("Could not create measurement since it is already running.", e);
				}
					
		}
	}
	
	private static Iterable<PathOptimizer> getPathOptimizers()
    {
        ServiceLoader<PathOptimizer> pathOptimizers =
                ServiceLoader.load(PathOptimizer.class,
                		MicroplateMeasurementConstructionAddon.class.getClassLoader());
        return pathOptimizers;
    }
	
	private static PathOptimizer getPathOptimizer(String pathOptimizerID)
	{
		for (PathOptimizer addon : getPathOptimizers())
        {
        	if(addon.getOptimizerID().equals(pathOptimizerID))
                return addon;
        }
        return null;
	}
}
