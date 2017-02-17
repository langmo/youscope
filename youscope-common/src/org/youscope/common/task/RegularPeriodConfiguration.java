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
package org.youscope.common.task;

import org.youscope.common.configuration.ConfigurationException;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;

/**
 * Configurations of how often and when a task should be executed. The task is executed, after an initial delay, regularly every {@link #getPeriod()} ms.
 * However, this period can be measured in two ways: (i) The period should be as precisely as possible, such that the duration between two executions of the
 * jobs of the same task has mean {@link #getPeriod()} and minimal variance. Or, (ii), the period can be measured between the end of the execution of the last
 * job of the task in the previous iteration, and the beginning of the execution of the first task of the next execution. The latter allows to define periods way
 * smaller than the microscope is able to execute the jobs of the task, effectively implementing tasks which execute as fast as possible, or tasks which
 * execution times can be delayed if the microscope is occupied with tasks which cannot be delayed.
 * @author Moritz Lang
 */
@XStreamAlias("regular-period")
public class RegularPeriodConfiguration extends PeriodConfiguration
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 1308127813101097630L;

	/**
	 * The period time in milliseconds.
	 */
	@XStreamAlias("period")
	@XStreamAsAttribute
	private int					period				= 10000;

	/**
	 * TRUE if starts of executions should be exactly <period> ms away from each other. FALSE if the
	 * end of one execution should be <period> ms away from the start of the next one.
	 */
	@XStreamAlias("fixed")
	@XStreamAsAttribute
	@XStreamConverter(value = BooleanConverter.class, booleans = {false}, strings = {"yes", "no"})
	private boolean				fixedTimes			= false;

	@Override
	public String getTypeIdentifier() 
	{
		return "YouScope.RegularPeriod";
	}
	
	/**
	 * @param fixedTimes the fixedTimes to set
	 */
	public void setFixedTimes(boolean fixedTimes)
	{
		this.fixedTimes = fixedTimes;
	}

	/**
	 * @return the fixedTimes
	 */
	public boolean isFixedTimes()
	{
		return fixedTimes;
	}

	/**
	 * @param period the period to set
	 */
	public void setPeriod(int period)
	{
		this.period = period;
	}

	/**
	 * @return the period
	 */
	public int getPeriod()
	{
		return period;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException 
	{
		super.checkConfiguration();
		if(period < 0)
			throw new ConfigurationException("Period length must be bigger or equal to zero.");
	}
}
