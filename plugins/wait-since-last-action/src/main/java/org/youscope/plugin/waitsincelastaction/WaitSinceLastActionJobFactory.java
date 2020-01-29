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
package org.youscope.plugin.waitsincelastaction;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 */
public class WaitSinceLastActionJobFactory extends ComponentAddonFactoryAdapter 
{
	/**
	 * Constructor.
	 */
	public WaitSinceLastActionJobFactory()
	{
		addAddon(WaitSinceLastActionJobConfigurationAddon.class, CREATOR_WAIT, WaitSinceLastActionJobConfigurationAddon.getMetadata());
	}
	
	private static final CustomAddonCreator<WaitSinceLastActionJobConfiguration, WaitSinceLastActionJob> CREATOR_WAIT = new CustomAddonCreator<WaitSinceLastActionJobConfiguration,WaitSinceLastActionJob>()
	{
		@Override
		public WaitSinceLastActionJob createCustom(PositionInformation positionInformation, WaitSinceLastActionJobConfiguration configuration,
				ConstructionContext constructionContext) throws ConfigurationException, AddonException 
		{
			WaitSinceLastActionJob job;
			try
			{
				job = new WaitSinceLastActionJobImpl(positionInformation);
				job.setWaitTime(configuration.getWaitTime());
				job.setInitialWaitTime(configuration.getInitialWaitTime());
				job.setResetAfterIteration(configuration.isResetAfterIteration());
				job.setActionID(configuration.getActionID());
			}
			catch(ComponentRunningException e)
			{
				throw new AddonException("Could not create wait since last action job since newly created job already running.", e);
			} catch (RemoteException e) {
				throw new AddonException("Could not create wait since last action  job due to remote exception.", e);
			}
			return job;
		}

		@Override
		public Class<WaitSinceLastActionJob> getComponentInterface() {
			return WaitSinceLastActionJob.class;
		}
	};
}
