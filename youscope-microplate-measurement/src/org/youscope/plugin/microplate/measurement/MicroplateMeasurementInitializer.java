/**
 * 
 */
package org.youscope.plugin.microplate.measurement;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentCreationException;
import org.youscope.addon.measurement.MeasurementInitializer;
import org.youscope.addon.pathoptimizer.PathOptimizerConfiguration;
import org.youscope.addon.pathoptimizer.PathOptimizerResource;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.EditableJobContainer;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.basicjobs.ChangePositionJob;
import org.youscope.common.job.basicjobs.CompositeJob;
import org.youscope.common.job.basicjobs.FocusingJob;
import org.youscope.common.job.basicjobs.StatisticsJob;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.SimpleMeasurementContext;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.resource.ResourceException;
import org.youscope.common.task.MeasurementTask;
import org.youscope.common.task.RegularPeriodConfiguration;
import org.youscope.common.task.VaryingPeriodConfiguration;
import org.youscope.plugin.changepositionjob.ChangePositionJobConfiguration;
import org.youscope.plugin.focusingjob.FocusingJobConfiguration;
import org.youscope.plugin.livemodifiablejob.LiveModifiableJob;
import org.youscope.plugin.livemodifiablejob.LiveModifiableJobConfiguration;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 * 
 */
public class MicroplateMeasurementInitializer implements MeasurementInitializer<MicroplateMeasurementConfiguration>
{

	@Override
	public void initializeMeasurement(Measurement measurement, MicroplateMeasurementConfiguration configuration, ConstructionContext jobInitializer) throws ConfigurationException, AddonException
	{
		
		String stageDeviceID = configuration.getStageDevice();		
		
		int wellTime = configuration.getTimePerWell();

		// If iteration through wells should be AFAP, put everything in one task
		MeasurementTask mainTask = null; 
		if(wellTime <= 0)
		{
			if(configuration.getPeriod() instanceof RegularPeriodConfiguration)
			{
				RegularPeriodConfiguration period = (RegularPeriodConfiguration)configuration.getPeriod();
				try
				{
					mainTask = measurement.addTask(period.getPeriod(), period.isFixedTimes(), period.getStartTime(), period.getNumExecutions());
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
			else if(configuration.getPeriod() instanceof VaryingPeriodConfiguration)
			{
				VaryingPeriodConfiguration period = (VaryingPeriodConfiguration)configuration.getPeriod();
				try
				{
					mainTask = measurement.addMultiplePeriodTask(period.getPeriods(), period.getBreakTime(), period.getStartTime(), period.getNumExecutions());
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
			else
			{
				throw new ConfigurationException("Period type is not supported.");
			}
		}

		// Iterate over all wells and positions
		Map<PositionInformation, XYAndFocusPosition> positions = configuration.getPositions();
		//boolean isMultiPos = posConf.getWellNumPositionsX() > 1 || posConf.getWellNumPositionsY() > 1;
		//boolean isWellMeasurement = !posConf.isAliasMicroplate();
		
		PathOptimizerConfiguration pathOptimizerConfiguration = configuration.getPathOptimizerConfiguration();
		List<PositionInformation> path;
		if(pathOptimizerConfiguration != null)
		{
			PathOptimizerResource pathOptimizer;
			try {
				pathOptimizer = jobInitializer.getComponentProvider().createComponent(new PositionInformation(), pathOptimizerConfiguration, PathOptimizerResource.class);
			} catch (ComponentCreationException | RemoteException e) {
				throw new AddonException("Could not create path optimizer with ID "+pathOptimizerConfiguration.getTypeIdentifier()+".", e);
			}
			try {
				SimpleMeasurementContext measurementContext = new SimpleMeasurementContext();
				pathOptimizer.initialize(measurementContext);
				path = pathOptimizer.getPath(positions);
				pathOptimizer.uninitialize(measurementContext);
			} catch (ResourceException | RemoteException e) {
				throw new AddonException("Could not calculate optimimal path using optimizer with ID "+pathOptimizerConfiguration.getTypeIdentifier()+".", e);
			}
		}
		else
		{
			path = new ArrayList<PositionInformation>(positions.keySet());
			Collections.sort(path);
		}
		
		// Iterate over all positions
		int elementNum = -1;
		
		for(PositionInformation positionInformation : path)
		{
				XYAndFocusPosition position = positions.get(positionInformation);
				elementNum++;
				if(elementNum == 0 && stageDeviceID != null)
				{
					// Add zero position to startup and shutdown settings
					try
					{
						measurement.addStartupDeviceSetting(new DeviceSetting(stageDeviceID, "PositionX", (float)position.getX()));
						measurement.addStartupDeviceSetting(new DeviceSetting(stageDeviceID, "PositionY", (float)position.getY()));
						measurement.addFinishDeviceSetting(new DeviceSetting(stageDeviceID, "PositionX", (float)position.getX()));
						measurement.addFinishDeviceSetting(new DeviceSetting(stageDeviceID, "PositionY", (float)position.getY()));
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

				String locationString = positionInformation.toString();
	
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

					if(configuration.getPeriod() instanceof RegularPeriodConfiguration)
					{
						RegularPeriodConfiguration period = (RegularPeriodConfiguration)configuration.getPeriod();
						int mainPeriod = period.getPeriod();
						boolean fixedTimes = period.isFixedTimes();
						if(mainPeriod <= 0)
						{
							mainPeriod = path.size() * wellTime;
							fixedTimes = true;
						}
						try
						{
							wellTask = measurement.addTask(mainPeriod, fixedTimes, startTime, period.getNumExecutions());
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
					else if(configuration.getPeriod() instanceof VaryingPeriodConfiguration)
					{
						VaryingPeriodConfiguration period = (VaryingPeriodConfiguration)configuration.getPeriod();
						try
						{
							wellTask = measurement.addMultiplePeriodTask(period.getPeriods(), period.getBreakTime(), startTime, period.getNumExecutions());
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
						try 
						{
							job = jobInitializer.getComponentProvider().createJob(positionInformation, CompositeJob.DEFAULT_TYPE_IDENTIFIER, CompositeJob.class);
							job.setName("Job container for " + locationString);
							wellTask.addJob(job);
						} 
						catch (ComponentCreationException e) 
						{
							throw new AddonException("Microplate measurements need the composite job plugin.",e);
						}
						catch (RemoteException e)
						{
							throw new AddonException("Could not create measurement due to remote exception.", e);
						}
						jobContainer = job;
					}
					else
					{
						StatisticsJob job;
						try 
						{
							job = jobInitializer.getComponentProvider().createJob(positionInformation, StatisticsJob.DEFAULT_TYPE_IDENTIFIER, StatisticsJob.class);
							job.setName("Job container/analyzer for " + locationString);
							job.addTableListener(jobInitializer.getMeasurementSaver().getSaveTableListener(configuration.getStatisticsFileName()));
							wellTask.addJob(job);
							
						} catch (ComponentCreationException e) {
							throw new AddonException("Microplate measurements need the statistic job plugin.",e);
						}
						catch (RemoteException e)
						{
							throw new AddonException("Could not create measurement due to remote exception.", e);
						}
							
						jobContainer = job;
					}
					if(configuration.isAllowEditsWhileRunning())
					{
						LiveModifiableJob modJob;
						try {
							modJob = jobInitializer.getComponentProvider().createJob(positionInformation, LiveModifiableJobConfiguration.TYPE_IDENTIFIER, LiveModifiableJob.class);
							modJob.setName(locationString);
							jobContainer.addJob(modJob);
						} catch (RemoteException e) {
							throw new AddonException("Could not create measurement due to remote exception.", e);
						} catch (ComponentCreationException e) {
							throw new AddonException("Microplate measurements need live-modifiable job plugin.",e);
						}
						jobContainer = modJob;
					}
					
					// Set position to well
					ChangePositionJobConfiguration positionJobConfiguration = new ChangePositionJobConfiguration();
					positionJobConfiguration.setAbsolute(true);
					positionJobConfiguration.setX(position.getX());
					positionJobConfiguration.setY(position.getY());
					positionJobConfiguration.setStageDevice(stageDeviceID);
					ChangePositionJob positionJob;
					try {
						positionJob = jobInitializer.getComponentProvider().createJob(positionInformation, positionJobConfiguration, ChangePositionJob.class);
						positionJob.setName("Moving stage to " + locationString);
						jobContainer.addJob(positionJob);

					} catch (ComponentCreationException e) {
						throw new AddonException("Microplate measurements need the change position job plugin.", e);
					}
					catch (RemoteException e)
					{
						throw new AddonException("Could not create measurement due to remote exception.", e);
					}
					
					// Set Focus
					FocusingJobConfiguration focusingJobConfiguration = null;
					if(configuration.getFocusConfiguration() != null)
					{
						focusingJobConfiguration = new FocusingJobConfiguration();
						focusingJobConfiguration.setFocusConfiguration(configuration.getFocusConfiguration());
						focusingJobConfiguration.setPosition(position.getFocus());
						focusingJobConfiguration.setRelative(false);
						FocusingJob focusingJob;
						try 
						{
							focusingJob = jobInitializer.getComponentProvider().createJob(positionInformation, focusingJobConfiguration, FocusingJob.class);
							focusingJob.setName("Focus for " + locationString);
							jobContainer.addJob(focusingJob);
						} 
						catch (ComponentCreationException e) {
							throw new AddonException("Microplate measurements need the focusing job plugin.", e);
						}
						catch (RemoteException e)
						{
							throw new AddonException("Could not create measurement due to remote exception.", e);
						}
						
					}

					// Add all other configured jobs
					JobConfiguration[] jobConfigurations = configuration.getJobs();
					for(JobConfiguration jobConfiguration : jobConfigurations)
					{
						Job job;
						try {
							job = jobInitializer.getComponentProvider().createJob(positionInformation, jobConfiguration);
							jobContainer.addJob(job);
						} catch (ComponentCreationException e) {
							throw new AddonException("Could not create child job of microplate measurement.", e);
						}
						catch (RemoteException e)
						{
							throw new AddonException("Could not create measurement due to remote exception.", e);
						}
					}
					
					if(jobContainer instanceof LiveModifiableJob)
					{
						JobConfiguration[] allConfigs = new JobConfiguration[jobConfigurations.length + (focusingJobConfiguration!=null? 2 : 1)];
						allConfigs[0] = positionJobConfiguration;
						if(focusingJobConfiguration != null)
							allConfigs[1]=focusingJobConfiguration;
						System.arraycopy(jobConfigurations, 0, allConfigs, focusingJobConfiguration == null ? 1 : 2, jobConfigurations.length);
						try {
							((LiveModifiableJob)jobContainer).setChildJobConfigurations(allConfigs);
						} catch (RemoteException | CloneNotSupportedException e) {
							throw new AddonException("Could not initialize live modifications of jobs.", e);
						}
					}
				}
				catch(MeasurementRunningException e)
				{
					throw new ConfigurationException("Could not create measurement since it is already running.", e);
				}
					
		}
	}
}