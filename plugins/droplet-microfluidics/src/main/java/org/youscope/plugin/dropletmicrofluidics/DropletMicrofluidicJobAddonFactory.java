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
package org.youscope.plugin.dropletmicrofluidics;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.ComponentCreationException;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.addon.dropletmicrofluidics.DropletControllerConfiguration;
import org.youscope.addon.dropletmicrofluidics.DropletControllerResource;
import org.youscope.addon.dropletmicrofluidics.DropletObserverConfiguration;
import org.youscope.addon.dropletmicrofluidics.DropletObserverResource;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.PositionInformation;
import org.youscope.common.callback.CallbackCreationException;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.plugin.autofocus.AutoFocusJob;
import org.youscope.plugin.autofocus.AutoFocusJobConfiguration;
import org.youscope.plugin.nemesys.NemesysJob;
import org.youscope.plugin.nemesys.NemesysJobConfiguration;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 */
public class DropletMicrofluidicJobAddonFactory extends ComponentAddonFactoryAdapter 
{
	private static final CustomAddonCreator<DropletMicrofluidicJobConfiguration, DropletMicrofluidicJob> CREATOR = new CustomAddonCreator<DropletMicrofluidicJobConfiguration, DropletMicrofluidicJob>()
	{

		@Override
		public DropletMicrofluidicJob createCustom(PositionInformation positionInformation,
				DropletMicrofluidicJobConfiguration configuration, ConstructionContext constructionContext)
						throws ConfigurationException, AddonException 
		{
			try
			{
				DropletMicrofluidicJobImpl job = new DropletMicrofluidicJobImpl(positionInformation);
				job.setMicrofluidicChipID(configuration.getMicrofluidicChipID());
				job.setConnectedSyringes(configuration.getConnectedSyringes());
				
				AutoFocusJobConfiguration autofocusConfiguration = configuration.getAutofocusConfiguration();
				if(autofocusConfiguration == null)
					throw new ConfigurationException("No autofocus configuration set.");
				job.setInputJob(constructionContext.getComponentProvider().createJob(positionInformation, autofocusConfiguration, AutoFocusJob.class));				
				
				NemesysJob nemesysJob = constructionContext.getComponentProvider().createJob(positionInformation, NemesysJobConfiguration.TYPE_IDENTIFIER, NemesysJob.class);
				nemesysJob.setNemesysDeviceName(configuration.getNemesysDevice());
				job.setOutputJob(nemesysJob);
				
				DropletControllerConfiguration controllerConfiguration = configuration.getControllerConfiguration();
				if(controllerConfiguration == null)
					throw new ConfigurationException("No controller configuration set.");
				job.setController(constructionContext.getComponentProvider().createComponent(positionInformation, controllerConfiguration, DropletControllerResource.class));
				
				DropletObserverConfiguration observerConfiguration = configuration.getObserverConfiguration();
				if(observerConfiguration == null)
					throw new ConfigurationException("No observer configuration set.");
				job.setObserver(constructionContext.getComponentProvider().createComponent(positionInformation, observerConfiguration, DropletObserverResource.class));
				
				if(configuration.getDropletTableSaveName() != null)
				{
					job.addTableListener(constructionContext.getMeasurementSaver().getSaveTableListener(configuration.getDropletTableSaveName()));
				}
				
				DropletMicrofluidicJobCallback callback;
				try {
					callback = constructionContext.getCallbackProvider().createCallback(DropletMicrofluidicJobCallback.TYPE_IDENTIFIER, DropletMicrofluidicJobCallback.class);
				} catch (CallbackCreationException e) {
					throw new AddonException("Could not construct visual callback for job.", e);
				}
				job.setCallback(callback);
				
				return job;
				
			}
			catch(RemoteException e)
			{
				throw new AddonException("Could not create droplet based microfluidics job due to remote exception.", e);
			} catch (ComponentCreationException e) {
				throw new AddonException("Could not create all components of droplet based microfluidics job.", e);
			} catch (ComponentRunningException e) {
				throw new AddonException("Could not initialize newly created droplet based microfluidics job since job is already running.", e);
			}
		}

		@Override
		public Class<DropletMicrofluidicJob> getComponentInterface() {
			return DropletMicrofluidicJob.class;
		}
		
	};

	/**
	 * Constructor.
	 */
	public DropletMicrofluidicJobAddonFactory()
	{
		super(DropletMicrofluidicJobConfigurationAddon.class, CREATOR, DropletMicrofluidicJobConfigurationAddon.getMetadata());
	}
}
