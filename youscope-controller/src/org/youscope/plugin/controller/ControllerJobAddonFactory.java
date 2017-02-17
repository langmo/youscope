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
package org.youscope.plugin.controller;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.ComponentCreationException;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.Job;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 */
public class ControllerJobAddonFactory extends ComponentAddonFactoryAdapter 
{
	private static final CustomAddonCreator<ControllerJobConfiguration, ControllerJob> CREATOR = new CustomAddonCreator<ControllerJobConfiguration, ControllerJob>()
	{

		@Override
		public ControllerJob createCustom(PositionInformation positionInformation,
				ControllerJobConfiguration configuration, ConstructionContext constructionContext)
						throws ConfigurationException, AddonException 
		{
			try
			{
				ControllerJobImpl job = new ControllerJobImpl(positionInformation);
				
				job.addMessageListener(constructionContext.getLogger());
				job.setControllerScript(configuration.getControllerScript());
				job.setControllerScriptEngine(configuration.getControllerScriptEngine());
				
				if(configuration.getInputJob() != null)
				{
					Job inputJob = constructionContext.getComponentProvider().createJob(positionInformation, configuration.getInputJob());
					job.setInputJob(inputJob);
				}
				
				if(configuration.getOutputJob() != null)
				{
					Job outputJob = constructionContext.getComponentProvider().createJob(positionInformation, configuration.getOutputJob());
					job.setOutputJob(outputJob);
				}
				
				if(configuration.getControllerTableSaveName() != null)
				{
					job.addTableListener(constructionContext.getMeasurementSaver().getSaveTableListener(configuration.getControllerTableSaveName()));
				}

				return job;
				
			}
			catch(RemoteException e)
			{
				throw new AddonException("Could not create controller job due to remote exception.", e);
			} catch (ComponentCreationException e) {
				throw new AddonException("Could not create all components of controller job.", e);
			} catch (ComponentRunningException e) {
				throw new AddonException("Could not initialize newly created controller job since job is already running.", e);
			}
		}

		@Override
		public Class<ControllerJob> getComponentInterface() {
			return ControllerJob.class;
		}
		
	};

	/**
	 * Constructor.
	 */
	public ControllerJobAddonFactory()
	{
		super(ControllerJobConfigurationAddon.class, CREATOR, ControllerJobConfigurationAddon.getMetadata());
	}
}
