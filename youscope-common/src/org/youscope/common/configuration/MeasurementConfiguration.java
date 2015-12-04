/**
 * 
 */
package org.youscope.common.configuration;

import org.youscope.common.measurement.MeasurementSaveSettings;
import org.youscope.common.microscope.DeviceSettingDTO;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Superclass of all configurations of measurements.
 * 
 * @author Moritz Lang
 */
public abstract class MeasurementConfiguration implements Configuration
{
	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the measurementRuntime
	 */
	public int getMeasurementRuntime()
	{
		return measurementRuntime;
	}

	/**
	 * @param measurementRuntime
	 *            the measurementRuntime to set
	 */
	public void setMeasurementRuntime(int measurementRuntime)
	{
		this.measurementRuntime = measurementRuntime;
	}

	/**
	 * @return the deviseSettingsOn
	 */
	public DeviceSettingDTO[] getDeviseSettingsOn()
	{
		return deviseSettingsOn;
	}

	/**
	 * @param deviseSettingsOn
	 *            the deviseSettingsOn to set
	 */
	public void setDeviseSettingsOn(DeviceSettingDTO[] deviseSettingsOn)
	{
		this.deviseSettingsOn = deviseSettingsOn;
	}

	/**
	 * @return the deviseSettingsOff
	 */
	public DeviceSettingDTO[] getDeviseSettingsOff()
	{
		return deviseSettingsOff;
	}

	/**
	 * @param deviseSettingsOff
	 *            the deviseSettingsOff to set
	 */
	public void setDeviseSettingsOff(DeviceSettingDTO[] deviseSettingsOff)
	{
		this.deviseSettingsOff = deviseSettingsOff;
	}

	/**
	 * Serial version UID.
	 */
	private static final long		serialVersionUID	= -4103638994655960751L;

	/**
	 * The name of the measurement.
	 */
	@XStreamAlias("name")
	private String					name				= "unnamed";

	@XStreamAlias("save-settings")
	private MeasurementSaveSettings	saveSettings		= null;

	/**
	 * Runtime of the measurement in milliseconds. If time is over, measurement
	 * will be quit.
	 */
	@XStreamAlias("runtime")
	private int						measurementRuntime	= -1;

	/**
	 * Device Settings which should be activated at the beginning of the
	 * simulation
	 */
	@XStreamAlias("start-device-settings")
	private DeviceSettingDTO[]		deviseSettingsOn	= new DeviceSettingDTO[0];

	/**
	 * Device Settings which should be activated at the end of the simulation
	 */
	@XStreamAlias("end-device-settings")
	private DeviceSettingDTO[]		deviseSettingsOff	= new DeviceSettingDTO[0];

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		MeasurementConfiguration clone = (MeasurementConfiguration)super.clone();
		clone.deviseSettingsOff = new DeviceSettingDTO[deviseSettingsOff.length];
		for(int i = 0; i < deviseSettingsOff.length; i++)
		{
			clone.deviseSettingsOff[i] = deviseSettingsOff[i].clone();
		}
		clone.deviseSettingsOn = new DeviceSettingDTO[deviseSettingsOn.length];
		for(int i = 0; i < deviseSettingsOn.length; i++)
		{
			clone.deviseSettingsOn[i] = deviseSettingsOn[i].clone();
		}
		clone.saveSettings = (MeasurementSaveSettings)saveSettings.clone();

		return clone;
	}

	/**
	 * Defines how the measurement should be saved. Set to null if the measurement should not be saved.
	 * @param saveSettings Definition how the measurement should be saved.
	 */
	public void setSaveSettings(MeasurementSaveSettings saveSettings)
	{
		this.saveSettings = saveSettings;
	}

	/**
	 * Returns the definition how the measurement should be saved. Returns null if the measurement should not be saved.
	 * @return Definition how the measurement should be saved.
	 */
	public MeasurementSaveSettings getSaveSettings()
	{
		return saveSettings;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		if(deviseSettingsOff == null)
			throw new ConfigurationException("Device settings off are null.");
		if(deviseSettingsOn == null)
			throw new ConfigurationException("Device settings on are null.");
	}
}
