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
package org.youscope.plugin.onoffdevicejob;

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.microscope.DeviceSetting;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This job/task changes a device setting in regular intervals.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("device-settings-job")
public class OnOffDeviceJobConfiguration implements JobConfiguration
{

	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= 1615857170050877320L;

	/**
	 * The exposure time in milliseconds in between the switches between the ON-settings and the
	 * OFF-settings.
	 */
	@XStreamAlias("exposure-ms")
	private double				exposure			= 0.0;

	/**
	 * The device settings which should be applied when the microscope switches to the ON-state.
	 */
	@XStreamAlias("device-settings-on")
	private DeviceSetting[]	deviceSettingsOn	= new DeviceSetting[0];

	/**
	 * The device settings which should be applied when the microscope switches to the OFF-state.
	 */
	@XStreamAlias("device-settings-off")
	private DeviceSetting[]	deviceSettingsOff	= new DeviceSetting[0];

	@Override
	public String getDescription()
	{
		String description = "";
		for(DeviceSetting setting : getDeviceSettingsOn())
		{
			description += "<p>";
			if(setting.isAbsoluteValue())
				description += setting.getDevice() + "." + setting.getProperty() + " = " + setting.getStringValue();
			else
				description += setting.getDevice() + "." + setting.getProperty() + " += " + setting.getStringValue();
			description += "</p>";
		}
		if(getExposure() > 0.0)
		{
			description += "<p>wait(" + Double.toString(getExposure()) + "ms)</p>";
		}
		for(DeviceSetting setting : getDeviceSettingsOff())
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
	 * @param deviceSettingsOn the deviceSettingsOn to set
	 */
	public void setDeviceSettingsOn(DeviceSetting[] deviceSettingsOn)
	{
		if(deviceSettingsOn == null)
			throw new NullPointerException();
		this.deviceSettingsOn = deviceSettingsOn;
	}

	/**
	 * @return the deviceSettingsOn
	 */
	public DeviceSetting[] getDeviceSettingsOn()
	{
		return deviceSettingsOn;
	}

	/**
	 * @param deviceSettingsOff the deviceSettingsOff to set
	 */
	public void setDeviceSettingsOff(DeviceSetting[] deviceSettingsOff)
	{
		if(deviceSettingsOff == null)
			throw new NullPointerException();
		this.deviceSettingsOff = deviceSettingsOff;
	}

	/**
	 * @return the deviceSettingsOff
	 */
	public DeviceSetting[] getDeviceSettingsOff()
	{
		return deviceSettingsOff;
	}

	/**
	 * @param exposure the exposure to set
	 */
	public void setExposure(double exposure)
	{
		this.exposure = exposure;
	}

	/**
	 * @return the exposure
	 */
	public double getExposure()
	{
		return exposure;
	}

	/**
	 * The identifier for this job type.
	 */
	public static final String	TYPE_IDENTIFIER	= "YouScope.DeviceJob";

	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		// do nothing
		
	}
}
