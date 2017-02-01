/**
 * 
 */
package org.youscope.plugin.repeatjob;

import java.rmi.RemoteException;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.job.CompositeJob;
import org.youscope.common.job.Job;


/**
 * A job which consists of other jobs, which gets executed several times when the repeat job is executed.
 * The single jobs will be run after each other in the order
 * they are created. It is guaranteed that in between two jobs no other tasks are executed by the
 * microscope.
 * 
 * @author Moritz Lang
 */
public interface RepeatJob extends Job, CompositeJob
{
	/**
	 * Returns the number of times the sub-jobs should be repeated each time the repeat-job is executed.
	 * @return number of times the job should be repeated.
	 * @throws RemoteException 
	 */
	public int getNumRepeats() throws RemoteException;

	/**
	 * Sets the number of times the sub-jobs should be repeated each time the repeat-job is executed.
	 * @param numRepeats number of times the job should be repeated. If smaller than 0, it is set to zero.
	 * @throws RemoteException 
	 * @throws ComponentRunningException 
	 */
	public void setNumRepeats(int numRepeats) throws RemoteException, ComponentRunningException;
}
