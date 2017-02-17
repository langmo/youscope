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

import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.ConfigurationException;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Superclass of all configurations of how often and when a task should be executed.
 * @author Moritz Lang 
 */
public abstract class PeriodConfiguration implements Configuration
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 2496372293408194212L;

	/**
	 * The delay in milliseconds when the first activation should take place.
	 */
	@XStreamAlias("start")
	@XStreamAsAttribute
	private int					startTime			= 0;

	/**
	 * Number of times the corresponding task should be evaluated. -1 if should evaluated infinite.
	 */
	@XStreamAlias("num-executions")
	@XStreamAsAttribute
	private int					numExecutions		= -1;

	/**
	 * @param numExecutions the numExecutions to set
	 */
	public void setNumExecutions(int numExecutions)
	{
		this.numExecutions = numExecutions;
	}

	/**
	 * @return the numExecutions
	 */
	public int getNumExecutions()
	{
		return numExecutions;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(int startTime)
	{
		this.startTime = startTime;
	}

	/**
	 * @return the startTime
	 */
	public int getStartTime()
	{
		return startTime;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		if(startTime < 0)
			throw new ConfigurationException("Start time must be bigger or equal to zero.");
		
	}
}
