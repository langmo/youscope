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
package org.youscope.plugin.oscillatinginput;

import java.net.URL;
import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.ComponentCreationException;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.JobException;
import org.youscope.common.job.basicjobs.DeviceSettingJob;
import org.youscope.common.job.basicjobs.ScriptingJob;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 */
public class OscillatingDeviceJobAddonFactory extends ComponentAddonFactoryAdapter 
{
	private static final CustomAddonCreator<OscillatingDeviceJobConfiguration, ScriptingJob> CREATOR = new CustomAddonCreator<OscillatingDeviceJobConfiguration, ScriptingJob>()
	{

		@Override
		public ScriptingJob createCustom(PositionInformation positionInformation,
				OscillatingDeviceJobConfiguration configuration, ConstructionContext constructionContext)
						throws ConfigurationException, AddonException 
		{
			try
			{
				// Create job.
				ScriptingJob job;
				try {
					job = constructionContext.getComponentProvider().createJob(positionInformation, ScriptingJob.DEFAULT_TYPE_IDENTIFIER, ScriptingJob.class);
				} catch (ComponentCreationException e1) {
					throw new AddonException("Oscillating device jobs need the scripting job plugin.", e1);
				}
				
				// Set script file
				URL scriptURL = getClass().getClassLoader().getResource("org/youscope/plugin/oscillatinginput/oscillating_device.js");
				if(scriptURL == null)
					throw new ConfigurationException("Could not load script file as resource. Check consistency of JAR file.");
				try
				{
					job.setScriptFile(scriptURL);
				}
				catch(Exception e)
				{
					throw new ConfigurationException("Could not load script file as resource. Syntax of script file "+ scriptURL.toString() + " invalid.", e);
				}
				
				// Initialize parameters of script file
				job.putVariable("device", configuration.getDevice());
				job.putVariable("property", configuration.getProperty());
				job.putVariable("minValue", configuration.getMinValue());
				job.putVariable("maxValue", configuration.getMaxValue());
				job.putVariable("periodLength", configuration.getPeriodLength());
				job.putVariable("initialPhase", configuration.getInitialPhase());
		
				// Set engine to default JavaScript engine.
				job.setScriptEngine("Mozilla Rhino");
				
				// Add sub job to change position.
				DeviceSettingJob devJob;
				try {
					devJob = constructionContext.getComponentProvider().createJob(positionInformation, DeviceSettingJob.DEFAULT_TYPE_IDENTIFIER, DeviceSettingJob.class);
				} catch (ComponentCreationException e) {
					throw new AddonException("Oscillating device jobs need the device setting job plugin.", e);
				}
				try {
					job.addJob(devJob);
				} catch (JobException e) {
					throw new AddonException("Could not add child job to job.", e);
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
		public Class<ScriptingJob> getComponentInterface() {
			return ScriptingJob.class;
		}
		
	};

	/**
	 * Constructor.
	 */
	public OscillatingDeviceJobAddonFactory()
	{
		super(OscillatingDeviceJobConfigurationAddon.class, CREATOR, OscillatingDeviceJobConfigurationAddon.getMetadata());
	}
}
