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
package org.youscope.plugin.microplate.job;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.ComponentCreationException;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.addon.pathoptimizer.PathOptimizerConfiguration;
import org.youscope.addon.pathoptimizer.PathOptimizerResource;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.JobException;
import org.youscope.common.job.basicjobs.ChangePositionJob;
import org.youscope.common.job.basicjobs.SimpleCompositeJob;
import org.youscope.common.job.basicjobs.FocusingJob;
import org.youscope.common.measurement.SimpleMeasurementContext;
import org.youscope.common.resource.ResourceException;
import org.youscope.plugin.microplate.measurement.XYAndFocusPosition;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 */
public class MicroplateJobAddonFactory extends ComponentAddonFactoryAdapter 
{
	private static final CustomAddonCreator<MicroplateJobConfiguration, SimpleCompositeJob> CREATOR = new CustomAddonCreator<MicroplateJobConfiguration, SimpleCompositeJob>()
	{

		@Override
		public SimpleCompositeJob createCustom(PositionInformation mainPositionInformation,
				MicroplateJobConfiguration configuration, ConstructionContext constructionContext)
						throws ConfigurationException, AddonException 
		{
			try
			{
				String stageDeviceID = configuration.getStageDevice();		
				
				SimpleCompositeJob microplateJob;
				try {
					microplateJob = constructionContext.getComponentProvider().createJob(mainPositionInformation, SimpleCompositeJob.DEFAULT_TYPE_IDENTIFIER, SimpleCompositeJob.class);
				} catch (ComponentCreationException e3) {
					throw new AddonException("Microplate measurements need the composite job plugin.", e3);
				}
				

				// Iterate over all wells and positions
				Map<PositionInformation, XYAndFocusPosition> positions = configuration.getPositions();
				
				PathOptimizerConfiguration pathOptimizerConfiguration = configuration.getPathOptimizerConfiguration();
				List<PositionInformation> path;
				if(pathOptimizerConfiguration != null)
				{
					PathOptimizerResource pathOptimizer;
					try {
						pathOptimizer = constructionContext.getComponentProvider().createComponent(new PositionInformation(), pathOptimizerConfiguration, PathOptimizerResource.class);
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
				Iterator<PositionInformation> iterator = path.iterator();
				while(iterator.hasNext())
				{
						PositionInformation positionInformation = iterator.next();
						XYAndFocusPosition position = positions.get(positionInformation);
				
						String locationString = positionInformation.toString();
						
						// Create a job container in which all jobs at the given position are put into.
						SimpleCompositeJob jobContainer;
						try {
							jobContainer = constructionContext.getComponentProvider().createJob(positionInformation, SimpleCompositeJob.DEFAULT_TYPE_IDENTIFIER, SimpleCompositeJob.class);
						} catch (ComponentCreationException e2) {
							throw new AddonException("Microplate measurements need the composite job plugin.",e2);
						}
						
						jobContainer.setName("Job container for " + locationString);
						try {
							microplateJob.addJob(jobContainer);
						} catch (JobException e2) {
							throw new AddonException("Could not add child job to job.", e2);
						}
						
						// Set position to well
						ChangePositionJob positionJob;
						try {
							positionJob = constructionContext.getComponentProvider().createJob(positionInformation, ChangePositionJob.DEFAULT_TYPE_IDENTIFIER, ChangePositionJob.class);
						} catch (ComponentCreationException e1) {
							throw new AddonException("Microplate measurements need the change position job plugin.", e1);
						}
						positionJob.setPosition(position.getX(), position.getY());
						positionJob.setStageDevice(stageDeviceID);
						positionJob.setName("Moving stage to " + locationString);
						try {
							jobContainer.addJob(positionJob);
						} catch (JobException e1) {
							throw new AddonException("Could not add child job to job.", e1);
						}

						// Set Focus
						if(configuration.getFocusConfiguration() != null)
						{
							FocusingJob focusingJob;
							try {
								focusingJob = constructionContext.getComponentProvider().createJob(positionInformation, FocusingJob.DEFAULT_TYPE_IDENTIFIER, FocusingJob.class);
							} catch (ComponentCreationException e) {
								throw new AddonException("Microplate measurements need the focussing job plugin.", e);
							}
							focusingJob.setFocusAdjustmentTime(configuration.getFocusConfiguration().getAdjustmentTime());
							focusingJob.setPosition(position.getFocus(), false);
							focusingJob.setFocusDevice(configuration.getFocusConfiguration().getFocusDevice());
							focusingJob.setName("Setting focus for " + locationString);
							try {
								jobContainer.addJob(focusingJob);
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
								job = constructionContext.getComponentProvider().createJob(positionInformation, jobConfiguration);
							} catch (ComponentCreationException e) {
								throw new AddonException("Could not create child job.", e);
							}
							try {
								jobContainer.addJob(job);
							} catch (JobException e) {
								throw new AddonException("Could not add child job to job.", e);
							}
						}
						
							
				}
				return microplateJob;
				
			}
			catch(RemoteException e)
			{
				throw new AddonException("Could not create job due to remote exception.", e);
			} catch (ComponentRunningException e) {
				throw new AddonException("Could not initialize newly created job since job is already running.", e);
			}
		}

		@Override
		public Class<SimpleCompositeJob> getComponentInterface() {
			return SimpleCompositeJob.class;
		}
		
	};

	/**
	 * Constructor.
	 */
	public MicroplateJobAddonFactory()
	{
		super(MicroplateJobAddonUI.class, CREATOR, MicroplateJobAddonUI.getMetadata());
	}
}
