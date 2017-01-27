/**
 * 
 */
package org.youscope.common.task;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.EventListener;

/**
 * @author Moritz Lang
 * 
 */
public interface TaskListener extends EventListener, Remote
{
	/**
	 * Function gets invoked when state of task changed.
	 * @param oldState Old state before the change.
	 * @param newState New state.
	 * @throws RemoteException
	 */
	void taskStateChanged(TaskState oldState, TaskState newState) throws RemoteException;
	
	/**
	 * Function gets invoked if error occurs during the execution of the task. 
	 * 
	 * @param e Object describing the error.
	 * @throws RemoteException
	 */
	void taskError(Exception e) throws RemoteException;

	/**
	 * Called when the task was executed, i.e. when it scheduled all its jobs for execution.
	 * @param executionNumber The number of times the jobs have been send since the task started, starting at zero.
	 * @throws RemoteException
	 */
	void taskExecuted(int executionNumber) throws RemoteException;
}
