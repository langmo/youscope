/**
 * 
 */
package org.youscope.common.task;

import java.rmi.RemoteException;

import org.youscope.common.job.EditableJobContainer;

/**
 * Interface represents a task. A task is a process which--regularly or not, once or often--submits its jobs to the job execution queue, where
 * they get executed in a FIFO manner. Each measurement must have one or more tasks.
 * @author Moritz Lang
 */
public interface MeasurementTask extends EditableJobContainer
{
	/**
	 * Returns the current state of the task.
	 * 
	 * @return Current state.
	 * @throws RemoteException
	 */
	TaskState getState() throws RemoteException;
	
	/**
	 * Adds a listener which gets informed over the progress of the task.
	 * @param listener
	 * @throws RemoteException
	 */
	void addTaskListener(TaskListener listener) throws RemoteException;

	/**
	 * Removes a previously added listener.
	 * @param listener
	 * @throws RemoteException
	 */
	void removeTaskListener(TaskListener listener) throws RemoteException;
}
