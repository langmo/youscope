/**
 * 
 */
package org.youscope.server;

import java.util.ArrayList;
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
	private volatile boolean blocked = false;
	private final ArrayList<JobQueueListener> jobQueueListeners = new ArrayList<>(1);
	boolean isEmpty()
	{
		synchronized(jobQueue)
		{
			return jobQueue.isEmpty();
		}
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
			if(!blocked)
				jobQueue.add(job);
		}
		synchronized(jobQueueListeners)
		{
			for(JobQueueListener listener : jobQueueListeners)
			{
				listener.jobQueued();
			}
		}
	}
	void clearAndBlock()
	{
		synchronized(jobQueue)
		{
			blocked = true;
			jobQueue.clear();
		}
	}
	void addJobQueueListener(JobQueueListener listener)
	{
		synchronized(jobQueueListeners)
		{
			jobQueueListeners.add(listener);
		}
	}
	void removeJobQueueListener(JobQueueListener listener)
	{
		synchronized(jobQueueListeners)
		{
			jobQueueListeners.remove(listener);
		}
	}
	
	static interface JobQueueListener
	{
		void jobQueued();
	}
}
