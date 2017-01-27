/**
 * 
 */
package org.youscope.server;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A queue of all jobs of a measurement which should be evaluated first in first out.
 * @author Moritz Lang
 */
class MeasurementJobQueue
{
	/**
	 * Queue for the jobs. Jobs are added by the tasked and pulled by the measurement manager first
	 * in first out.
	 */
	private final ConcurrentLinkedQueue<JobExecutionQueueElement>	jobQueue					= new ConcurrentLinkedQueue<JobExecutionQueueElement>();

	boolean isJobQueueEmpty()
	{
		return jobQueue.isEmpty();
	}
	
	JobExecutionQueueElement unqueueJob()
	{
		synchronized(jobQueue)
		{
			return jobQueue.poll();
		}
	}
	void queueJob(JobExecutionQueueElement job)
	{
		synchronized(jobQueue)
		{
			jobQueue.add(job);
		}
	}
	void clearJobQueue()
	{
		synchronized(jobQueue)
		{
			jobQueue.clear();
		}
	}
}
