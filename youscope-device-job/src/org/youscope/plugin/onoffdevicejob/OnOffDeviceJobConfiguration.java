/**
 * 
 */
package org.youscope.plugin.onoffdevicejob;

import org.youscope.common.configuration.JobConfiguration;
import org.youscope.common.microscope.DeviceSettingDTO;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This job/task changes a device setting in regular intervals.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("device-settings-job")
public class OnOffDeviceJobConfiguration extends JobConfiguration
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
	private DeviceSettingDTO[]	deviceSettingsOn	= new DeviceSettingDTO[0];

	/**
	 * The device settings which should be applied when the microscope switches to the OFF-state.
	 */
	@XStreamAlias("device-settings-off")
	private DeviceSettingDTO[]	deviceSettingsOff	= new DeviceSettingDTO[0];

	@Override
	public String getDescription()
	{
		String description = "";
		for(DeviceSettingDTO setting : getDeviceSettingsOn())
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
		for(DeviceSettingDTO setting : getDeviceSettingsOff())
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

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		OnOffDeviceJobConfiguration clone = (OnOffDeviceJobConfiguration)super.clone();
		clone.setDeviceSettingsOn(new DeviceSettingDTO[getDeviceSettingsOn().length]);
		clone.setDeviceSettingsOff(new DeviceSettingDTO[getDeviceSettingsOff().length]);
		for(int i = 0; i < getDeviceSettingsOn().length; i++)
		{
			clone.getDeviceSettingsOn()[i] = getDeviceSettingsOn()[i].clone();
		}
		for(int i = 0; i < getDeviceSettingsOff().length; i++)
		{
			clone.getDeviceSettingsOff()[i] = getDeviceSettingsOff()[i].clone();
		}
		return clone; 
	}

	/**
	 * @param deviceSettingsOn the deviceSettingsOn to set
	 */
	public void setDeviceSettingsOn(DeviceSettingDTO[] deviceSettingsOn)
	{
		if(deviceSettingsOn == null)
			throw new NullPointerException();
		this.deviceSettingsOn = deviceSettingsOn;
	}

	/**
	 * @return the deviceSettingsOn
	 */
	public DeviceSettingDTO[] getDeviceSettingsOn()
	{
		return deviceSettingsOn;
	}

	/**
	 * @param deviceSettingsOff the deviceSettingsOff to set
	 */
	public void setDeviceSettingsOff(DeviceSettingDTO[] deviceSettingsOff)
	{
		if(deviceSettingsOff == null)
			throw new NullPointerException();
		this.deviceSettingsOff = deviceSettingsOff;
	}

	/**
	 * @return the deviceSettingsOff
	 */
	public DeviceSettingDTO[] getDeviceSettingsOff()
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
	public static final String	TYPE_IDENTIFIER	= "CSB::DeviceJob";

	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}
}
