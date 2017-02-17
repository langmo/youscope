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
package org.youscope.plugin.devicejob;

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.basicjobs.DeviceSettingJob;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.table.TableConsumerConfiguration;
import org.youscope.common.table.TableDefinition;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This job/task changes device settings.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("simple-device-settings-job")
public class DeviceJobConfiguration implements JobConfiguration, TableConsumerConfiguration
{

	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= 1615857170050877311L;

	/**
	 * The device settings which should be applied when the microscope switches to the ON-state.
	 */
	@XStreamAlias("device-settings")
	private DeviceSetting[]	deviceSettings	= new DeviceSetting[0];

	@Override
	public String getDescription()
	{
		String description = "";
		for(DeviceSetting setting : getDeviceSettings())
		{
			description += "<p>";
			if(setting.isAbsoluteValue())
				description += setting.getDevice() + "." + setting.getProperty() + " = " + setting.getStringValue();
			else
				description += setting.getDevice() + "." + setting.getProperty() + " += " + setting.getStringValue();
			description += "</p>";
		}
		if(description.length() <= 0)
			description = "<p>Empty Device Job</p>";
		return description;
	}

	/**
	 * Sets the device settings which should be activated by the job.
	 * @param deviceSettings Device settings which should be set when job is activated.
	 * @throws NullPointerException Thrown if deviceSettings is null, or element of deviceSettings is null.
	 */
	public void setDeviceSettings(DeviceSetting... deviceSettings) throws NullPointerException
	{
		if(deviceSettings == null)
			throw new NullPointerException();
		this.deviceSettings = new DeviceSetting[deviceSettings.length];
		for(int i=0;i<deviceSettings.length; i++)
		{
			if(deviceSettings[i] == null)
				throw new NullPointerException();
			this.deviceSettings[i] =deviceSettings[i].clone();
		}
	}

	/**
	 * Returns the device settings which should be activated by the job.
	 * @return settings which should be set when job is activated.
	 */
	public DeviceSetting[] getDeviceSettings()
	{
		DeviceSetting[] returnVal = new DeviceSetting[deviceSettings.length];
		for(int i=0;i<deviceSettings.length; i++)
		{
			returnVal[i] =deviceSettings[i].clone();
		}
		return returnVal;
	}

	@Override
	public String getTypeIdentifier()
	{
		return DeviceSettingJob.DEFAULT_TYPE_IDENTIFIER;
	}

	@Override
	public TableDefinition getConsumedTableDefinition() {
		return DeviceTable.getTableDefinition();
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		if(deviceSettings == null)
			throw new ConfigurationException("Device settings are null.");
	}
}
