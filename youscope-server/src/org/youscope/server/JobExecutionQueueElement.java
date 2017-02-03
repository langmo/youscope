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
