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
package org.youscope.plugin.simplemeasurement;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentCreationException;
import org.youscope.addon.measurement.MeasurementInitializer;
import org.youscope.common.ComponentException;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.CompositeJob;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.basicjobs.SimpleCompositeJob;
import org.youscope.common.job.basicjobs.StatisticsJob;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.task.Task;
import org.youscope.common.task.RegularPeriodConfiguration;
import org.youscope.common.task.VaryingPeriodConfiguration;
import org.youscope.plugin.livemodifiablejob.LiveModifiableJob;
import org.youscope.plugin.livemodifiablejob.LiveModifiableJobConfiguration;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author langmo
 * 
 */
class SimpleMeasurementInitializer implements MeasurementInitializer<SimpleMeasurementConfiguration>
{

	@Override
	public void initializeMeasurement(Measurement measurement, SimpleMeasurementConfiguration configuration, ConstructionContext jobInitializer) throws ConfigurationException, AddonException
	{
		Task mainTask;
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

		PositionInformation positionInformation = new PositionInformation(null);
		
		try
		{
			// Create a job container in which all jobs are put into.
			CompositeJob jobContainer;
			if(configuration.getStatisticsFileName() == null)
			{
				SimpleCompositeJob job;
				try 
				{
					job = jobInitializer.getComponentProvider().createJob(positionInformation, SimpleCompositeJob.DEFAULT_TYPE_IDENTIFIER, SimpleCompositeJob.class);
					job.setName("Job container for simple measurement imaging protocol");
					mainTask.addJob(job);	
				} 
				catch (ComponentCreationException e) {
					throw new AddonException("Microplate measurements need the composite job plugin.", e);
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
				try {
					job = jobInitializer.getComponentProvider().createJob(positionInformation, StatisticsJob.DEFAULT_TYPE_IDENTIFIER, StatisticsJob.class);
					job.setName("Job container/analyzer for simple measurement imaging protocol");
					job.addTableListener(jobInitializer.getMeasurementSaver().getSaveTableListener(configuration.getStatisticsFileName()));
					job.addMessageListener(jobInitializer.getLogger());
					mainTask.addJob(job);
				} 
				catch (ComponentCreationException e) 
				{
					throw new AddonException("Microplate measurements need the statistic job plugin.", e);
				}
				catch (RemoteException e)
				{
					throw new AddonException("Could not create measurement due to remote exception.", e);
				}
					
				jobContainer = job;
			}
			JobConfiguration[] jobConfigurations = configuration.getJobs();
			if(configuration.isAllowEditsWhileRunning())
			{
				LiveModifiableJob modJob;
				try {
					modJob = jobInitializer.getComponentProvider().createJob(positionInformation, LiveModifiableJobConfiguration.TYPE_IDENTIFIER, LiveModifiableJob.class);
					
					JobConfiguration[] allConfigs = new JobConfiguration[jobConfigurations.length];
					System.arraycopy(jobConfigurations, 0, allConfigs, 0, jobConfigurations.length);
					try {
						modJob.setChildJobConfigurations(allConfigs);
					} catch (RemoteException | ConfigurationException e) {
						throw new AddonException("Could not initialize live modifications of jobs.", e);
					}
					
					jobContainer.addJob(modJob);
				} catch (RemoteException e) {
					throw new AddonException("Could not create measurement due to remote exception.", e);
				} catch (ComponentCreationException e) {
					throw new AddonException("Microplate measurements need live-modifiable job plugin.",e);
				}
				jobContainer = modJob;
			}
			
			// Add all jobs
			for(JobConfiguration jobConfiguration : jobConfigurations)
			{
				Job job;
				try {
					job = jobInitializer.getComponentProvider().createJob(positionInformation, jobConfiguration);
					jobContainer.addJob(job);
				} catch (ComponentCreationException e) {
					throw new AddonException("Could not create child job.", e);
				}
				catch (RemoteException e)
				{
					throw new AddonException("Could not create measurement due to remote exception.", e);
				}
			}
		}
		catch(ComponentException e)
		{
			throw new AddonException("Could not create measurement since it is already running.", e);
		}
			
	}
}
