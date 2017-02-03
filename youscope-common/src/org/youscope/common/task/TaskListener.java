/**
 * 
 */
package org.youscope.common.task;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.EventListener;

/**
 * A listener getting notified when execution of a task starts and when it finishes.
 * @author Moritz Lang
 * 
 */
public interface TaskListener extends EventListener, Remote
{	

	/**
	 * Called when the first job of a task starts to be executed.
	 * @param executionNumber The number of times the task has already been executed, starting at zero.
	 * @throws RemoteException
	 */
	void taskStarted(long executionNumber) throws RemoteException;
	
	/**
	 * Called when the last job of a task stops to be executed.
	 * @param executionNumber The number of times the task has already been executed, starting at zero.
	 * @throws RemoteException
	 */
	void taskFinished(long executionNumber) throws RemoteException;
}
