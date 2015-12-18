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
	 * Called when task started to operate, i.e. to send its jobs--possibly regularly--to the job execution queue.
	 * @throws RemoteException
	 */
	void taskStarted() throws RemoteException;

	/**
	 * Called when the task finished to operate, i.e. won't send any jobs to the job queue anymore.
	 * @throws RemoteException
	 */
	void taskFinished() throws RemoteException;

	/**
	 * Called when the task did send its jobs to the job queue.
	 * @param submissionNumber The number of times the jobs have been send since the task started, starting at zero.
	 * @throws RemoteException
	 */
	void jobsSubmitted(int submissionNumber) throws RemoteException;
}
