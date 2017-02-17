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
package org.youscope.plugin.deviceslides;

import java.rmi.RemoteException;
import java.util.Vector;

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
import org.youscope.common.job.basicjobs.SimpleCompositeJob;
import org.youscope.common.job.basicjobs.DeviceSettingJob;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.microscope.PropertyType;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 */
public class DeviceSlidesJobAddonFactory extends ComponentAddonFactoryAdapter 
{
	private static final CustomAddonCreator<DeviceSlidesJobConfiguration, SimpleCompositeJob> CREATOR = new CustomAddonCreator<DeviceSlidesJobConfiguration, SimpleCompositeJob>()
	{

		@Override
		public SimpleCompositeJob createCustom(PositionInformation positionInformation,
				DeviceSlidesJobConfiguration configuration, ConstructionContext constructionContext)
						throws ConfigurationException, AddonException 
		{
			try
			{
				SimpleCompositeJob compositeJob;
				try {
					compositeJob = constructionContext.getComponentProvider().createJob(positionInformation, SimpleCompositeJob.DEFAULT_TYPE_IDENTIFIER, SimpleCompositeJob.class);
				} catch (ComponentCreationException e1) {
					throw new AddonException("Device slides job requires composite job plugin.", e1);
				}
				DeviceSetting[][] settings = configuration.getMultiPosDeviceSettings();
				// First check if job is valid
				if(settings.length <= 0)
					return compositeJob;
				int numSettingsPerPos = settings[0].length;
				boolean allTheSame = true;
				end: for(int i = 1; i < settings.length; i++)
				{
					// Same number of arguments?
					if(settings[i].length != numSettingsPerPos)
					{
						allTheSame = false;
						break;
					}
					// Same arguments?
					for(int j=0; j<numSettingsPerPos; j++)
					{
						if(settings[i][j].getDevice().compareTo(settings[0][j].getDevice()) != 0
								|| settings[i][j].getProperty().compareTo(settings[0][j].getProperty()) != 0
								|| settings[i][j].isAbsoluteValue() != settings[0][j].isAbsoluteValue())
						{
							allTheSame = false;
							break end;
						}
					}
				}
				if(!allTheSame)
				{
					throw new ConfigurationException("Lengths or type of device settings in multi-pos job are not the same.");
				}

				// For relative values we run into a problem: The user expect that the
				// relative value is
				// measured from the starting position,
				// but we have to provide the value relative to the last position. We
				// therefore have to keep
				// track on the last values of all relative position
				// values
				Object[] lastPositions = new Object[settings[0].length];
				PropertyType[] propertyTypes = new PropertyType[settings[0].length];
				for(int i = 0; i < lastPositions.length; i++)
				{
					// If value is given absolutely, we don't care.
					if(settings[0][i].isAbsoluteValue())
						continue;
					
					// We first assume it's an integer, because this is more restrictive, and change to float if we fail.
					propertyTypes[i]=PropertyType.PROPERTY_INTEGER;
					for(int j=0; j<settings.length; j++)
					{
						if(propertyTypes[i]==PropertyType.PROPERTY_INTEGER)
						{
							try
							{
								settings[j][i].getIntegerValue();
							}
							catch(@SuppressWarnings("unused") Exception e)
							{
								// this means it is not an integer, thus, probably a float!
								propertyTypes[i]=PropertyType.PROPERTY_FLOAT;
							}
						}
						if(propertyTypes[i]==PropertyType.PROPERTY_FLOAT)
						{
							try
							{
								settings[j][i].getFloatValue();
							}
							catch(Exception e)
							{
								// not a float and not an integer --> the setting of relative value is invalid.
								throw new ConfigurationException("Property " + settings[0][i].getDevice()+"."+settings[0][i].getProperty()+" is defined to be changed relatively. However, not all defined relative values are integers or floats.", e);
							}
						}
					}
					
					// At the beginning, the relative position is zero.
					if(propertyTypes[i] == PropertyType.PROPERTY_FLOAT)
						lastPositions[i] = new Float(0.0F);
					else if(propertyTypes[i] == PropertyType.PROPERTY_INTEGER)
						lastPositions[i] = new Integer(0);
				}

				// Add the jobs for all positions
				for(int i = 0; i < settings.length; i++)
				{
					DeviceSetting[] jobSettings = settings[i];
					DeviceSetting[] internalSettings = new DeviceSetting[jobSettings.length];
					
					// Replace relative positions
					for(int j = 0; j < jobSettings.length; j++)
					{
						DeviceSetting newSetting;
						newSetting = jobSettings[j].clone();
						if(!jobSettings[j].isAbsoluteValue())
						{
							if(propertyTypes[j] == PropertyType.PROPERTY_FLOAT)
							{
								newSetting.setRelativeValue(-(Float)lastPositions[j]);
								lastPositions[j] = jobSettings[j].getFloatValue();
							}
							else if(propertyTypes[j] == PropertyType.PROPERTY_INTEGER)
							{
								newSetting.setRelativeValue(-(Integer)lastPositions[j]);
								lastPositions[j] = jobSettings[j].getIntegerValue();
							}
						}
						internalSettings[j] = newSetting;
					}
					
					PositionInformation positionInformationChild = new PositionInformation(positionInformation, "device settings", i);

					DeviceSettingJob deviceJob;
					try {
						deviceJob = constructionContext.getComponentProvider().createJob(positionInformationChild, DeviceSettingJob.DEFAULT_TYPE_IDENTIFIER, DeviceSettingJob.class);
					} catch (ComponentCreationException e) {
						throw new AddonException("Multi positions jobs need the device job plugin.", e);
					}
					deviceJob.setDeviceSettings(internalSettings);
					try {
						compositeJob.addJob(deviceJob);
					} catch (JobException e1) {
						throw new AddonException("Could not add child job to job.", e1);
					}

					// Second, the jobs which are equal at each position.
					for(JobConfiguration childJobConfig : configuration.getJobs())
					{
						Job childJob;
						try {
							childJob = constructionContext.getComponentProvider().createJob(positionInformationChild, childJobConfig);
						} catch (ComponentCreationException e) {
							throw new AddonException("Error while creating child job.", e);
						}
						try {
							compositeJob.addJob(childJob);
						} catch (JobException e) {
							throw new AddonException("Could not add child job to job.", e);
						}
					}
				}

				// Finally, set all relative value settings to zero
				Vector<DeviceSetting> tempSettings = new Vector<DeviceSetting>();
				for(int i = 0; i < lastPositions.length; i++)
				{
					// If value is given absolutely, we don't care.
					if(lastPositions[i] == null)
						continue;
					DeviceSetting setting;
					setting = settings[0][i].clone();
					if(propertyTypes[i] == PropertyType.PROPERTY_FLOAT)
					{
						setting.setValue(-(Float)lastPositions[i]);
					}
					else if(propertyTypes[i] == PropertyType.PROPERTY_INTEGER)
					{
						setting.setValue(-(Integer)lastPositions[i]);
					}
					tempSettings.add(setting);
				}
				DeviceSettingJob deviceJob;
				try {
					deviceJob = constructionContext.getComponentProvider().createJob(positionInformation, DeviceSettingJob.DEFAULT_TYPE_IDENTIFIER, DeviceSettingJob.class);
				} catch (ComponentCreationException e) {
					throw new AddonException("Multi positions jobs need the device job plugin.", e);
				}
				deviceJob.setDeviceSettings(tempSettings.toArray(new DeviceSetting[tempSettings.size()]));
				try {
					compositeJob.addJob(deviceJob);
				} catch (JobException e) {
					throw new AddonException("Could not add child job to job.", e);
				}
				
				return compositeJob;
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
	public DeviceSlidesJobAddonFactory()
	{
		super(DeviceSlidesJobConfigurationAddon.class, CREATOR, DeviceSlidesJobConfigurationAddon.getMetadata());
	}
}
