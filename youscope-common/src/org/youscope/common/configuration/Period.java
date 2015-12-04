/**
 * 
 */
package org.youscope.common.configuration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * @author langmo Class every possible period configuration has to extend.
 */
public abstract class Period implements Configuration
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

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

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
