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
package org.youscope.plugin.waitjob;

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.basicjobs.WaitJob;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This job/task simply pauses the execution for a given time.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("wait-job")
public class WaitJobConfiguration implements JobConfiguration
{

	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= 1615857170050111320L;

	/**
	 * The wait time in milliseconds
	 */
	@XStreamAlias("wait-ms")
	private long				waitTime			= 0;
	
	@Override
	public String getDescription()
	{
		String description = "<p>wait("+Long.toString(waitTime)+"ms)</p>";
		return description;
	}

	/**
	 * Returns the wait time in ms.
	 * @return Wait time in ms.
	 */
	public long getWaitTime()
	{
		return waitTime;
	}
	
	/**
	 * Sets the wait time in ms. Must be larger or equal 0.
	 * @param waitTime Wait time in ms.
	 */
	public void setWaitTime(long waitTime)
	{
		this.waitTime = waitTime > 0 ? waitTime : 0;
	}
	
	@Override
	public String getTypeIdentifier()
	{
		return WaitJob.DEFAULT_TYPE_IDENTIFIER;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
	
		if(waitTime < 0)
			throw new ConfigurationException("Wait time must be bigger or equal to zero.");
	}
}
