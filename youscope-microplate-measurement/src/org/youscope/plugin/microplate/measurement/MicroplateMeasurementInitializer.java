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
package org.youscope.plugin.microplate.measurement;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentCreationException;
import org.youscope.addon.measurement.MeasurementInitializer;
import org.youscope.addon.pathoptimizer.PathOptimizerConfiguration;
import org.youscope.addon.pathoptimizer.PathOptimizerResource;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.CompositeJob;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.JobException;
import org.youscope.common.job.basicjobs.ChangePositionJob;
import org.youscope.common.job.basicjobs.SimpleCompositeJob;
import org.youscope.common.job.basicjobs.FocusingJob;
import org.youscope.common.job.basicjobs.StatisticsJob;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.SimpleMeasurementContext;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.resource.ResourceException;
import org.youscope.common.task.Task;
import org.youscope.common.task.RegularPeriodConfiguration;
import org.youscope.common.task.TaskException;
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
		Task mainTask = null; 
		if(wellTime <= 0)
		{
			if(configuration.getPeriod() instanceof RegularPeriodConfiguration)
			{
				RegularPeriodConfiguration period = (RegularPeriodConfiguration)configuration.getPeriod();
				try
				{
					mainTask = measurement.addTask(period.getPeriod(), period.isFixedTimes(), period.getStartTime(), period.getNumExecutions());
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
			else if(configuration.getPeriod() instanceof VaryingPeriodConfiguration)
			{
				VaryingPeriodConfiguration period = (VaryingPeriodConfiguration)configuration.getPeriod();
				try
				{
					mainTask = measurement.addMultiplePeriodTask(period.getPeriods(), period.getStartTime(), period.getNumExecutions());
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
			else
			{
				throw new ConfigurationException("Period type is not supported.");
			}
		}

		// Iterate over all wells and positions
		Map<PositionInformation, XYAndFocusPosition> positions = configuration.getPositions();
		
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
		Iterator<PositionInformation> iterator = path.iterator();
		while(iterator.hasNext())
		{
				PositionInformation positionInformation = iterator.next();
				XYAndFocusPosition position = positions.get(positionInformation);
				elementNum++;
				if(elementNum == 0)
				{
					try
					{
						if(configuration.getZeroPositionType() == MicroplateMeasurementConfiguration.ZeroPositionType.FIRST_WELL_TILE)
						{
							if(stageDeviceID != null)
							{
								// Add zero position to startup and shutdown settings			
								measurement.addStartupDeviceSetting(new DeviceSetting(stageDeviceID, "PositionX", (float)position.getX()));
								measurement.addStartupDeviceSetting(new DeviceSetting(stageDeviceID, "PositionY", (float)position.getY()));
								measurement.addFinishDeviceSetting(new DeviceSetting(stageDeviceID, "PositionX", (float)position.getX()));
								measurement.addFinishDeviceSetting(new DeviceSetting(stageDeviceID, "PositionY", (float)position.getY()));
							
							}
							if(configuration.getFocusConfiguration() != null && !Double.isNaN(position.getFocus()))
							{
								measurement.addStartupDeviceSetting(new DeviceSetting(configuration.getFocusConfiguration().getFocusDevice(), "Position", (float)position.getFocus()));
								measurement.addFinishDeviceSetting(new DeviceSetting(configuration.getFocusConfiguration().getFocusDevice(), "Position", (float)position.getFocus()));
							}
						}
						else if(configuration.getZeroPositionType() == MicroplateMeasurementConfiguration.ZeroPositionType.CUSTOM && configuration.getZeroPosition() != null)
						{
							XYAndFocusPosition zeroPosition = configuration.getZeroPosition();
							if(stageDeviceID != null)
							{
								// Add zero position to startup and shutdown settings			
								measurement.addStartupDeviceSetting(new DeviceSetting(stageDeviceID, "PositionX", (float)zeroPosition.getX()));
								measurement.addStartupDeviceSetting(new DeviceSetting(stageDeviceID, "PositionY", (float)zeroPosition.getY()));
								measurement.addFinishDeviceSetting(new DeviceSetting(stageDeviceID, "PositionX", (float)zeroPosition.getX()));
								measurement.addFinishDeviceSetting(new DeviceSetting(stageDeviceID, "PositionY", (float)zeroPosition.getY()));
							
							}
							if(configuration.getFocusConfiguration() != null && !Double.isNaN(zeroPosition.getFocus()))
							{
								measurement.addStartupDeviceSetting(new DeviceSetting(configuration.getFocusConfiguration().getFocusDevice(), "Position", (float)zeroPosition.getFocus()));
								measurement.addFinishDeviceSetting(new DeviceSetting(configuration.getFocusConfiguration().getFocusDevice(), "Position", (float)zeroPosition.getFocus()));
							}
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

				String locationString = positionInformation.toString();
	
				Task wellTask;
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
						catch(ComponentRunningException e)
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
							wellTask = measurement.addMultiplePeriodTask(period.getPeriods(), startTime, period.getNumExecutions());
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
					else
					{
						throw new ConfigurationException("Period type is not supported.");
					}
				}

				try
				{
					// Create a job container in which all jobs at the given position are put into.
					CompositeJob jobContainer;
					if(configuration.getStatisticsFileName() == null)
					{
						SimpleCompositeJob job;
						try 
						{
							job = jobInitializer.getComponentProvider().createJob(positionInformation, SimpleCompositeJob.DEFAULT_TYPE_IDENTIFIER, SimpleCompositeJob.class);
							job.setName("Job container for " + locationString);
							try {
								wellTask.addJob(job);
							} catch (TaskException e) {
								throw new AddonException("Could not add job to task.", e);
							}
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
							try {
								wellTask.addJob(job);
							} catch (TaskException e) {
								throw new AddonException("Could not add job to task.", e);
							}
							
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
						} catch (JobException e) {
							throw new AddonException("Could not add child job to job.", e);
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
					} catch (JobException e) {
						throw new AddonException("Could not add child job to job.", e);
					}
					
					// Set Focus
					FocusingJobConfiguration focusingJobConfiguration = null;
					if(configuration.getFocusConfiguration() != null && !Double.isNaN(position.getFocus()))
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
						} catch (JobException e) {
							throw new AddonException("Could not add child job to job.", e);
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
						} catch (JobException e) {
							throw new AddonException("Could not add child job to job.", e);
						}
					}
					
					// add go to zero position job to last job
					if(!iterator.hasNext())
					{
						
						MicroplateMeasurementConfiguration.ZeroPositionType zeroPositionType = configuration.getZeroPositionType();
						XYAndFocusPosition zeroPosition;
						if(zeroPositionType == MicroplateMeasurementConfiguration.ZeroPositionType.FIRST_WELL_TILE)
							zeroPosition = positions.get(path.get(0));
						else if(zeroPositionType == MicroplateMeasurementConfiguration.ZeroPositionType.CUSTOM)
							zeroPosition = configuration.getZeroPosition();
						else
							zeroPosition = null;
						if(zeroPosition != null)
						{
							ChangePositionJobConfiguration zeroPositionJobConfiguration = new ChangePositionJobConfiguration();
							zeroPositionJobConfiguration.setAbsolute(true);
							zeroPositionJobConfiguration.setX(zeroPosition.getX());
							zeroPositionJobConfiguration.setY(zeroPosition.getY());
							zeroPositionJobConfiguration.setStageDevice(stageDeviceID);
							ChangePositionJob zeroPositionJob;
							try {
								zeroPositionJob = jobInitializer.getComponentProvider().createJob(positionInformation, zeroPositionJobConfiguration, ChangePositionJob.class);
								zeroPositionJob.setName("Moving stage to zero position.");
								jobContainer.addJob(zeroPositionJob);
	
							} catch (ComponentCreationException e) {
								throw new AddonException("Microplate measurements need the change position job plugin.", e);
							}
							catch (RemoteException e)
							{
								throw new AddonException("Could not create measurement due to remote exception.", e);
							} catch (JobException e) {
								throw new AddonException("Could not add child job to job.", e);
							}
						}
						if(zeroPosition != null && configuration.getFocusConfiguration() != null && !Double.isNaN(zeroPosition.getFocus()))
						{
							FocusingJobConfiguration zeroFocusingJobConfiguration = new FocusingJobConfiguration();
							zeroFocusingJobConfiguration.setFocusConfiguration(configuration.getFocusConfiguration());
							zeroFocusingJobConfiguration.setPosition(zeroPosition.getFocus());
							zeroFocusingJobConfiguration.setRelative(false);
							FocusingJob zeroFocusingJob;
							try 
							{
								zeroFocusingJob = jobInitializer.getComponentProvider().createJob(positionInformation, zeroFocusingJobConfiguration, FocusingJob.class);
								zeroFocusingJob.setName("Setting focus for zero position.");
								jobContainer.addJob(zeroFocusingJob);
							} 
							catch (ComponentCreationException e) {
								throw new AddonException("Microplate measurements need the focusing job plugin.", e);
							}
							catch (RemoteException e)
							{
								throw new AddonException("Could not create measurement due to remote exception.", e);
							} catch (JobException e) {
								throw new AddonException("Could not add child job to job.", e);
							}
							
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
						} catch (RemoteException | ConfigurationException e) {
							throw new AddonException("Could not initialize live modifications of jobs.", e);
						}
					}
				}
				catch(ComponentRunningException e)
				{
					throw new AddonException("Could not create measurement since it is already running.", e);
				}
					
		}
	}
}
