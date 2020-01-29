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

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.JobConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * @author langmo
 *
 */
@XStreamAlias("oscillating-device-job")
public class OscillatingDeviceJobConfiguration implements JobConfiguration
{
	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= 4911732041177941146L;
	
	/**
	 * The identifier for this job type.
	 */
	public static final String	TYPE_IDENTIFIER	= "YouScope.OscillatingDeviceJob";
	
	@XStreamAlias("max-value")
	@XStreamAsAttribute
	private double maxValue = 1.0;
	@XStreamAlias("min-value")
	@XStreamAsAttribute
	private double minValue = 0.0;
	@XStreamAlias("period-ms")
	@XStreamAsAttribute
	private int periodLength = 60 * 1000;
	@XStreamAlias("phase")
	@XStreamAsAttribute
	private double initialPhase = 0.0;
	/**
     * Name of device.
     */
	@XStreamAlias("device")
	@XStreamAsAttribute
    private String device = "";

    /**
     * Name of device property.
     */
	@XStreamAlias("property")
	@XStreamAsAttribute
    private String property = "";
	
	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}
		
	@Override
	public String getDescription()
	{
		double amplitude = (maxValue - minValue) / 2;
		double meanValue = minValue + amplitude;
		return device + "." + property
            + "= " + Double.toString(amplitude) + " sin(2 &pi; t / "
            + Integer.toString(periodLength) + " + " + Double.toString(initialPhase)
            + ") + " + Double.toString(meanValue);
	}
	
	/**
     * @param device the device  which owns a property which should oscillate.
     */
    public void setDevice(String device)
    {
        this.device = device;
    }

    /**
     * @return the device  which owns a property which should oscillate.
     */
    public String getDevice()
    {
        return device;
    }

    /**
     * @param property the property which should oscillate.
     */
    public void setProperty(String property)
    {
        this.property = property;
    }

    /**
     * @return the property which should oscillate.
     */
    public String getProperty()
    {
        return property;
    }

	/**
	 * @param maxValue the maximal value the property should be during oscillations.
	 */
	public void setMaxValue(double maxValue)
	{
		this.maxValue = maxValue;
	}

	/**
	 * @return the maximal value the property should be during oscillations.
	 */
	public double getMaxValue()
	{
		return maxValue;
	}

	/**
	 * @param minValue the minimal value the property should be during oscillations.
	 */
	public void setMinValue(double minValue)
	{
		this.minValue = minValue;
	}

	/**
	 * @return the minimal value the property should be during oscillations.
	 */
	public double getMinValue()
	{
		return minValue;
	}

	/**
	 * Sets the period length in seconds.
	 * @param periodLength Period length in seconds.
	 */
	public void setPeriodLength(int periodLength)
	{
		this.periodLength = periodLength;
	}

	/**
	 * @return Period length in seconds.
	 */
	public int getPeriodLength()
	{
		return periodLength;
	}

	/**
	 * Sets the initial phase of the oscillations in rad.
	 * @param initialPhase Initial phase in rad.
	 */
	public void setInitialPhase(double initialPhase)
	{
		this.initialPhase = initialPhase;
	}

	/**
	 * Returns the initial phase of the oscillations in rad.
	 * @return Initial phase in rad.
	 */
	public double getInitialPhase()
	{
		return initialPhase;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		// do nothing
		
	}
}
