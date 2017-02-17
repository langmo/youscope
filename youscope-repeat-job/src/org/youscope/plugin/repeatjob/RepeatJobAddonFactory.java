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
package org.youscope.plugin.repeatjob;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.ComponentCreationException;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.JobException;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 */
public class RepeatJobAddonFactory extends ComponentAddonFactoryAdapter 
{
	private static final CustomAddonCreator<RepeatJobConfiguration, RepeatJob> CREATOR = new CustomAddonCreator<RepeatJobConfiguration, RepeatJob>()
	{

		@Override
		public RepeatJob createCustom(PositionInformation positionInformation,
				RepeatJobConfiguration configuration, ConstructionContext constructionContext)
						throws ConfigurationException, AddonException 
		{
			try
			{
				RepeatJob job = new RepeatJobImpl(positionInformation);
				try
				{
					job.setNumRepeats(configuration.getNumRepeats());
				}
				catch(ComponentRunningException e1)
				{
					throw new AddonException("Newly created job already running.", e1);
				}
				for(JobConfiguration childJobConfig : configuration.getJobs())
				{
					Job childJob;
					try {
						childJob = constructionContext.getComponentProvider().createJob(positionInformation, childJobConfig);
					} catch (ComponentCreationException e1) {
						throw new AddonException("Could not create child job.", e1);
					}
					try {
						job.addJob(childJob);
					} catch (JobException e) {
						throw new AddonException("Could not add child job to job.", e);
					}
				}
				return job;
				
			}
			catch(RemoteException e)
			{
				throw new AddonException("Could not create job due to remote exception.", e);
			} catch (ComponentRunningException e) {
				throw new AddonException("Could not initialize newly created job since job is already running.", e);
			}
		}

		@Override
		public Class<RepeatJob> getComponentInterface() {
			return RepeatJob.class;
		}
		
	};

	/**
	 * Constructor.
	 */
	public RepeatJobAddonFactory()
	{
		super(RepeatJobConfigurationAddon.class, CREATOR, RepeatJobConfigurationAddon.getMetadata());
	}
}
