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
package org.youscope.plugin.waitsincelastaction;

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.JobConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This job stops the time since it was executed in any well, and waits until the time difference since the last execution
 * passes a threshold.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("wait-since-last-action-job")
public class WaitSinceLastActionJobConfiguration implements JobConfiguration
{

	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= 1115857170050111320L;

	/**
	 * The wait time in milliseconds between actions.
	 */
	@XStreamAlias("wait-ms")
	private long				waitTime			= 0;
	
	/**
	 * The wait time in milliseconds of the first action.
	 */
	@XStreamAlias("initial-wait-ms")
	private long				initialWaitTime			= 0;
	
	/**
	 * The ID of this action. Different IDs allow to wait for different actions in the same measurement.
	 */
	@XStreamAlias("resetAfterIteration")
	private boolean				resetAfterIteration			= true;
	
	/**
	 * The ID of this action. Different IDs allow to wait for different actions in the same measurement.
	 */
	@XStreamAlias("action-id")
	private int				actionID			= 1;
	
	/**
	 * Returns the initial wait time in ms.
	 * @return initial wait time in ms.
	 */
	public long getInitialWaitTime() {
		return initialWaitTime;
	}

	/**
	 * Sets the initial wait time in ms.
	 * @param initialWaitTime the wait time at the first execution
	 */
	public void setInitialWaitTime(long initialWaitTime) {
		this.initialWaitTime = initialWaitTime;
	}

	/**
	 * Returns true if the wait timer is reset after a complete iteration.
	 * @return True if wait timer is reset.
	 */
	public boolean isResetAfterIteration() {
		return resetAfterIteration;
	}

	/**
	 * Set to true to reset the wait timer after each iteration.
	 * @param resetAfterIteration true to reset wait timer.
	 */
	public void setResetAfterIteration(boolean resetAfterIteration) {
		this.resetAfterIteration = resetAfterIteration;
	}

	/**
	 * Returns the ID of this action. The job waits with respect to the execution time of the last action with the same ID.
	 * @return ID of action.
	 */
	public int getActionID() {
		return actionID;
	}

	/**
	 * Sets the ID of this action. The job waits with respect to the execution time of the last action with the same ID.
	 * @param actionID ID of action.
	 */
	public void setActionID(int actionID) {
		this.actionID = actionID;
	}
	
	@Override
	public String getDescription()
	{
		String description = "<p>wait("+Long.toString(waitTime)+"-lastTime ms)<br />lastTime = currentTime()</p>";
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
	
	/**
	 * The identifier for this job type.
	 */
	public static final String	TYPE_IDENTIFIER	= "YouScope.WaitSinceLastActionJob";
	
	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		if(waitTime < 0 || initialWaitTime < 0)
			throw new ConfigurationException("Wait time must be bigger or equal to zero.");
	}
}
