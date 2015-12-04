/**
 * 
 */
package org.youscope.server;

import java.util.EventListener;

/**
 * @author Moritz Lang
 */
interface MeasurementControlListener extends EventListener
{
	/**
	 * Called when measurement wants a job to be processed.
	 * 
	 * @param job The job to be processed.
	 */
	void addJobToExecutionQueue(JobExecutionQueueElement job);

	/**
	 * Called when the measurement finished.
	 */
	void measurementFinished();
}
