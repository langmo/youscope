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
package org.youscope.common.microscope;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;

/**
 * Setting for a device property.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("device-setting")
public class DeviceSetting implements Cloneable, Serializable
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -3371722843226203139L;

	/**
	 * Name of device.
	 */
	@XStreamAsAttribute
	private String				device				= "";

	/**
	 * Name of device property.
	 */
	@XStreamAsAttribute
	private String				property			= "";

	/**
	 * Value to which the device property should be set.
	 */
	@XStreamAsAttribute
	private String				value				= "";

	/**
	 * True if device should be set to the absolute value <value>, false if it should be set to
	 * lastValue + <value>. FALSE is only valid if property type is float or integer and ignored
	 * otherwise!
	 */
	@XStreamAsAttribute
	@XStreamAlias("relative")
	@XStreamConverter(value = BooleanConverter.class, booleans = {false}, strings = {"no", "yes"})
	private boolean				absoluteValue		= true;

	/**
	 * Default constructor.
	 */
	public DeviceSetting()
	{
		// Do nothing.
	}

	/**
	 * Constructor already initializing the setting.
	 * @param device the device to set.
	 * @param property the property to set.
	 * @param value the value to set.
	 */
	public DeviceSetting(String device, String property, String value)
	{
		setDeviceProperty(device, property);
		setValue(value);
	}

	/**
	 * Constructor already initializing the setting.
	 * @param device the device to set.
	 * @param property the property to set.
	 * @param value the value to set.
	 */
	public DeviceSetting(String device, String property, int value)
	{
		setDeviceProperty(device, property);
		setValue(value);
	}

	/**
	 * Constructor already initializing the setting.
	 * @param device the device to set.
	 * @param property the property to set.
	 * @param value the value to set.
	 */
	public DeviceSetting(String device, String property, float value)
	{
		setDeviceProperty(device, property);
		setValue(value);
	}

	/**
	 * Clone constructor.
	 * @param deviceSetting The device setting to clone.
	 */
	public DeviceSetting(DeviceSetting deviceSetting)
	{
		setDeviceProperty(deviceSetting.getDevice(), deviceSetting.getProperty());
		setAbsoluteValue(deviceSetting.isAbsoluteValue());
		setValue(deviceSetting.getStringValue());
	}

	@Override
	public DeviceSetting clone()
	{
		try {
			return (DeviceSetting) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Clone not supported.", e); // won't happen.
		}
	}

	@Override
	public String toString()
	{
		return getDevice() + "." + getProperty() + (isAbsoluteValue() ? " = " : " += ") + getStringValue();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (absoluteValue ? 1231 : 1237);
		result = prime * result + ((device == null) ? 0 : device.hashCode());
		result = prime * result + ((property == null) ? 0 : property.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeviceSetting other = (DeviceSetting) obj;
		if (absoluteValue != other.absoluteValue)
			return false;
		if (device == null) {
			if (other.device != null)
				return false;
		} else if (!device.equals(other.device))
			return false;
		if (property == null) {
			if (other.property != null)
				return false;
		} else if (!property.equals(other.property))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	/**
	 * Sets the device property which this setting represents.
	 * @param device the device to set.
	 * @param property the property to set.
	 */
	public void setDeviceProperty(String device, String property)
	{
		this.device = new String(device);
		this.property = new String(property);
	}

	/**
	 * Returns the device of which a property should be changed.
	 * @return the device.
	 */
	public String getDevice()
	{
		return device;
	}

	/**
	 * Returns the property of the device which should be changed.
	 * @return the property.
	 */
	public String getProperty()
	{
		return property;
	}

	/**
	 * Sets the current value of the setting.
	 * @param value the value to set.
	 */
	public void setValue(String value)
	{
		this.value = new String(value);
	}

	/**
	 * Sets the current value of the setting.
	 * @param value the value to set.
	 */
	public void setValue(int value)
	{
		this.value = Integer.toString(value);
	}

	/**
	 * Sets the current value of the setting.
	 * @param value the value to set.
	 */
	public void setValue(float value)
	{
		this.value = Float.toString(value);
	}

	/**
	 * Sets the current value of the setting relative to its previous value.
	 * Same as setValue(getFloatValue() + offset)
	 * @param offset the offset to set.
	 * @throws NumberFormatException
	 */
	public void setRelativeValue(float offset) throws NumberFormatException
	{
		setValue(getFloatValue() + offset);
	}

	/**
	 * Sets the current value of the setting relative to its previous value.
	 * Same as setValue(getIntegerValue() + offset)
	 * @param offset the offset to set.
	 * @throws NumberFormatException
	 */
	public void setRelativeValue(int offset) throws NumberFormatException
	{
		setValue(getIntegerValue() + offset);
	}

	/**
	 * Returns the value of the setting as a string.
	 * @return The current value as a string.
	 */
	public String getStringValue()
	{
		return value;
	}

	/**
	 * Returns the value of the setting as an integer.
	 * @return The current value as an integer.
	 * @throws NumberFormatException
	 */
	public int getIntegerValue() throws NumberFormatException
	{
		return Integer.parseInt(getStringValue());
	}

	/**
	 * Returns the value of the setting as a float.
	 * @return The current value as a float.
	 * @throws NumberFormatException
	 */
	public float getFloatValue() throws NumberFormatException
	{
		return Float.parseFloat(getStringValue());
	}

	/**
	 * Set to true if the property should be set to the given value,
	 * and to false if it should be changed for an offset relative to the current value.
	 * @param absoluteValue true if the property should be changed absolutely.
	 */
	public void setAbsoluteValue(boolean absoluteValue)
	{
		this.absoluteValue = absoluteValue;
	}

	/**
	 * Returns true if the property should be set to the given value,
	 * and false if it should be changed for an offset relative to the current value.
	 * @return true if the property should be changed absolutely.
	 */
	public boolean isAbsoluteValue()
	{
		return absoluteValue;
	}
}
