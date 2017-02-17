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
package org.youscope.server;

import org.youscope.common.job.Job;

/**
 * An element in the job execution queue.
 * @author Moritz Lang
 * 
 */
class JobExecutionQueueElement
{
	public final long			evaluationNumber;
	public final Job	job;

	public JobExecutionQueueElement(Job job, long evaluationNumber)
	{
		this.job = job;
		this.evaluationNumber = evaluationNumber;
	}
}
