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
package org.youscope.plugin.waitforuser;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.PositionInformation;
import org.youscope.common.callback.CallbackCreationException;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 */
public class WaitForUserJobAddonFactory extends ComponentAddonFactoryAdapter 
{
	private static final CustomAddonCreator<WaitForUserJobConfiguration,WaitForUserJob> CREATOR = new CustomAddonCreator<WaitForUserJobConfiguration, WaitForUserJob>()
	{

		@Override
		public WaitForUserJob createCustom(PositionInformation positionInformation,
				WaitForUserJobConfiguration configuration, ConstructionContext constructionContext)
						throws ConfigurationException, AddonException 
		{
			try
			{
				String message = configuration.getMessage();
				if(message == null)
					message = "No message";
				WaitForUserJob waitForUserJob = new WaitForUserJobImpl(positionInformation);
				try
				{
					waitForUserJob.setMessage(message);
				}
				catch(ComponentRunningException e)
				{
					throw new ConfigurationException("Newly created job already running.", e);
				}
				
				// Get callback.
				WaitForUserCallback callback;
				try
				{
					callback = constructionContext.getCallbackProvider().createCallback(WaitForUserCallback.TYPE_IDENTIFIER, WaitForUserCallback.class);
							
				}
				catch(CallbackCreationException e1)
				{
					throw new ConfigurationException("Could not create measurement callback for wait for user job.", e1);
				}
				
				// ping callback
				try
				{
					callback.pingCallback();
				}
				catch(RemoteException e)
				{
					throw new ConfigurationException("Measurement callback for wait for user job is not responding.", e);
				}
				waitForUserJob.setMeasurementCallback(callback);
				
				return waitForUserJob;
			}
			catch(RemoteException e)
			{
				throw new AddonException("Could not create job due to remote exception.", e);
			} catch (ComponentRunningException e) {
				throw new AddonException("Could not initialize newly created job since job is already running.", e);
			}
		}

		@Override
		public Class<WaitForUserJob> getComponentInterface() {
			return WaitForUserJob.class;
		}
		
	};

	/**
	 * Constructor.
	 */
	public WaitForUserJobAddonFactory()
	{
		super(WaitForUserJobConfigurationAddon.class, CREATOR, WaitForUserJobConfigurationAddon.getMetadata());
	}
}
