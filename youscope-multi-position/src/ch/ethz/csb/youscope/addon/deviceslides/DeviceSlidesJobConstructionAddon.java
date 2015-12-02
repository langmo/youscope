/**
 * 
 */
package ch.ethz.csb.youscope.addon.deviceslides;

import java.rmi.RemoteException;
import java.util.Vector;

import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddon;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;
import ch.ethz.csb.youscope.shared.measurement.ComponentCreationException;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.measurement.job.JobCreationException;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.CompositeJob;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.DeviceSettingJob;
import ch.ethz.csb.youscope.shared.microscope.PropertyType;
import ch.ethz.csb.youscope.shared.microscope.DeviceSettingDTO;

/**
 * @author langmo
 * 
 */
class DeviceSlidesJobConstructionAddon implements JobConstructionAddon
{

	@Override
	public Job createJob(JobConfiguration generalJobConfiguration, ConstructionContext initializer, PositionInformation positionInformation) throws RemoteException, ConfigurationException, JobCreationException
	{
		if(generalJobConfiguration instanceof DeviceSlidesJobConfigurationDTO)
		{
			DeviceSlidesJobConfigurationDTO jobConfiguration = (DeviceSlidesJobConfigurationDTO)generalJobConfiguration;
			CompositeJob job;
			try
			{
				job = initializer.getComponentProvider().createJob(positionInformation, CompositeJob.DEFAULT_TYPE_IDENTIFIER, CompositeJob.class);
				inititateMultiPosJob(jobConfiguration, job, initializer, positionInformation);
			}
			catch(Exception e)
			{
				throw new JobCreationException("Could not create job.", e);
			}
			return job;
		}
		throw new JobCreationException("Configuration is not supported by this addon.");
	}

	private static void inititateMultiPosJob(DeviceSlidesJobConfigurationDTO multiPosJobConfiguration, CompositeJob compositeJob, ConstructionContext initializer, PositionInformation positionInformation) throws RemoteException, MeasurementRunningException, ConfigurationException, JobCreationException
	{
		DeviceSettingDTO[][] settings = multiPosJobConfiguration.getMultiPosDeviceSettings();
		// First check if job is valid
		if(settings.length <= 0)
			return;
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
			DeviceSettingDTO[] jobSettings = settings[i];
			DeviceSettingDTO[] internalSettings = new DeviceSettingDTO[jobSettings.length];
			
			// Replace relative positions
			for(int j = 0; j < jobSettings.length; j++)
			{
				DeviceSettingDTO newSetting;
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
				deviceJob = initializer.getComponentProvider().createJob(positionInformationChild, DeviceSettingJob.DEFAULT_TYPE_IDENTIFIER, DeviceSettingJob.class);
			} catch (ComponentCreationException e) {
				throw new JobCreationException("Multi positions jobs need the device job plugin.", e);
			}
			deviceJob.setDeviceSettings(internalSettings);
			compositeJob.addJob(deviceJob);

			// Second, the jobs which are equal at each position.
			for(JobConfiguration childJobConfig : multiPosJobConfiguration.getJobs())
			{
				Job childJob;
				try {
					childJob = initializer.getComponentProvider().createJob(positionInformationChild, childJobConfig);
				} catch (ComponentCreationException e) {
					throw new JobCreationException("Error while creating child job.", e);
				}
				compositeJob.addJob(childJob);
			}
		}

		// Finally, set all relative value settings to zero
		Vector<DeviceSettingDTO> tempSettings = new Vector<DeviceSettingDTO>();
		for(int i = 0; i < lastPositions.length; i++)
		{
			// If value is given absolutely, we don't care.
			if(lastPositions[i] == null)
				continue;
			DeviceSettingDTO setting;
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
			deviceJob = initializer.getComponentProvider().createJob(positionInformation, DeviceSettingJob.DEFAULT_TYPE_IDENTIFIER, DeviceSettingJob.class);
		} catch (ComponentCreationException e) {
			throw new JobCreationException("Multi positions jobs need the device job plugin.", e);
		}
		deviceJob.setDeviceSettings(tempSettings.toArray(new DeviceSettingDTO[tempSettings.size()]));
		compositeJob.addJob(deviceJob);
	}
}
