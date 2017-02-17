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
package org.youscope.plugin.focusingjob;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.basicjobs.FocusingJob;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 */
public class FocusingJobAddonFactory extends ComponentAddonFactoryAdapter 
{
	/**
	 * Constructor.
	 */
	public FocusingJobAddonFactory()
	{
		super(FocusingJob.DEFAULT_TYPE_IDENTIFIER, FocusingJobConfiguration.class, CREATOR); 
	}
	 
	private static final CustomAddonCreator<FocusingJobConfiguration, FocusingJob> CREATOR = new CustomAddonCreator<FocusingJobConfiguration,FocusingJob>()
	{
		@Override
		public FocusingJob createCustom(PositionInformation positionInformation, FocusingJobConfiguration configuration,
				ConstructionContext constructionContext) throws ConfigurationException, AddonException 
		{
			FocusingJob job;
			try
			{
				job = new FocusingJobImpl(positionInformation);
				job.setPosition(configuration.getPosition(), configuration.isRelative());
				if(configuration.getFocusConfiguration() != null)
				{
					
					job.setFocusDevice(configuration.getFocusConfiguration().getFocusDevice());
					job.setFocusAdjustmentTime(configuration.getFocusConfiguration().getAdjustmentTime());
				}
				else
				{
					job.setFocusDevice(null);
					job.setFocusAdjustmentTime(0); 
				}
			}
			catch(ComponentRunningException e)
			{
				throw new AddonException("Could not create focusing job, since newly created job is already running.", e);
			} catch (RemoteException e1) {
				throw new AddonException("Could not create focusing job, due to remote exception.", e1);
			}
			return job;
		}

		@Override
		public Class<FocusingJob> getComponentInterface() {
			return FocusingJob.class;
		}
	};
}
