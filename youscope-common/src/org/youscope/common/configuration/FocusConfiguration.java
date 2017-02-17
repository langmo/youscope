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
package org.youscope.common.configuration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Configuration of a focus device (with focus adjustment time).
 * @author Moritz Lang
 * 
 */
@XStreamAlias("focus-configuration")
public final class FocusConfiguration implements Configuration
{
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + adjustmentTime;
		result = prime * result + ((focusDevice == null) ? 0 : focusDevice.hashCode());
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
		FocusConfiguration other = (FocusConfiguration) obj;
		if (adjustmentTime != other.adjustmentTime)
			return false;
		if (focusDevice == null) {
			if (other.focusDevice != null)
				return false;
		} else if (!focusDevice.equals(other.focusDevice))
			return false;
		return true;
	}

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -2291722102827042413L;
	/**
	 * Adjustment time in milliseconds.
	 */
	@XStreamAlias("adjustment-time-ms")
	@XStreamAsAttribute
	private int					adjustmentTime;

	@XStreamAlias("focus-device")
	@XStreamAsAttribute
	private String				focusDevice;
	
	/**
	 * Default constructor.
	 * Sets focus device to null (i.e. default focus), and adjustment time to 0ms.
	 */
	public FocusConfiguration()
	{
		this.adjustmentTime = 0;
		this.focusDevice = null;
	}
	
	/**
	 * Constructor. Sets focus adjustment time to 0ms.
	 * @param focusDevice The focus device to use (null for default focus device).
	 */
	public FocusConfiguration(String focusDevice)
	{
		this.adjustmentTime = 0;
		this.focusDevice = focusDevice;
	}
	
	/**
	 * Constructor.
	 * @param focusDevice The focus device to use (null for default focus device).
	 * @param adjustmentTime Time in ms after a change of the focus position can be assumed to have taken effect.
	 */
	public FocusConfiguration(String focusDevice, int adjustmentTime)
	{
		this.focusDevice = focusDevice;
		this.adjustmentTime = adjustmentTime < 0 ? 0: adjustmentTime;
	}
	
	/**
	 * Copy constructor.
	 * @param configuration Focus configuration to copy. If null, same as <code>FocusConfiguration()</code>.
	 */
	public FocusConfiguration(FocusConfiguration configuration)
	{
		this.focusDevice = configuration == null ? null : configuration.getFocusDevice();
		this.adjustmentTime = configuration== null ? 0 : configuration.getAdjustmentTime();
	}
	
	/**
	 * @param adjustmentTime
	 *            the adjustmentTime to set
	 */
	public void setAdjustmentTime(int adjustmentTime)
	{
		this.adjustmentTime = adjustmentTime;
	}
	
	/**
	 * @return The focus device.
	 */
	public String getFocusDevice()
	{
		return focusDevice;
	}
	
	@Override
	public String getTypeIdentifier() 
	{
		return "YouScope.Focus";
	}

	/**
	 * @param focusDevice
	 *            The focus device.
	 */
	public void setFocusDevice(String focusDevice)
	{
		this.focusDevice = focusDevice;
	}

	/**
	 * @return the adjustmentTime
	 */
	public int getAdjustmentTime()
	{
		return adjustmentTime;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		if(adjustmentTime < 0)
			throw new ConfigurationException("Focus adjustment time must be bigger or equal to zero.");
	}
}
