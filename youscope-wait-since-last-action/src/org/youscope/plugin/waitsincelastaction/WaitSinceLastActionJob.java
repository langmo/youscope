/**
 * 
 */
package org.youscope.plugin.waitsincelastaction;

import java.rmi.RemoteException;

import org.youscope.common.job.Job;
import org.youscope.common.measurement.MeasurementRunningException;


/**
 * This job stops the time since it was executed in any well, and waits until the time difference since the last execution
 * passes a threshold.
 * 
 * @author Moritz Lang
 */
public interface WaitSinceLastActionJob extends Job
{
	/**
	 * Returns the wait time in ms.
	 * @return Wait time in ms.
	 * @throws RemoteException 
	 */
	public long getWaitTime() throws RemoteException;
		
	/**
	 * Sets the wait time in ms. Must be larger or equal 0.
	 * @param waitTime Wait time in ms.
	 * @throws RemoteException 
	 * @throws MeasurementRunningException 
	 */
	public void setWaitTime(long waitTime) throws RemoteException, MeasurementRunningException;
	
	
	/**
	 * Returns the initial wait time in ms.
	 * @return initial wait time in ms.
	 * @throws RemoteException 
	 */
	public long getInitialWaitTime() throws RemoteException;
	

	/**
	 * Sets the initial wait time in ms.
	 * @param initialWaitTime the wait time at the first execution
	 * @throws RemoteException 
	 * @throws MeasurementRunningException 
	 */
	public void setInitialWaitTime(long initialWaitTime) throws RemoteException, MeasurementRunningException;
	

	/**
	 * Returns true if the wait timer is reset after a complete iteration.
	 * @return True if wait timer is reset.
	 * @throws RemoteException 
	 */
	public boolean isResetAfterIteration() throws RemoteException;
	

	/**
	 * Set to true to reset the wait timer after each iteration.
	 * @param resetAfterIteration true to reset wait timer.
	 * @throws RemoteException 
	 * @throws MeasurementRunningException 
	 */
	public void setResetAfterIteration(boolean resetAfterIteration) throws RemoteException, MeasurementRunningException;
	

	/**
	 * Returns the ID of this action. The job waits with respect to the execution time of the last action with the same ID.
	 * @return ID of action.
	 * @throws RemoteException 
	 */
	public int getActionID()  throws RemoteException;
	

	/**
	 * Sets the ID of this action. The job waits with respect to the execution time of the last action with the same ID.
	 * @param actionID ID of action.
	 * @throws RemoteException 
	 * @throws MeasurementRunningException 
	 */
	public void setActionID(int actionID) throws RemoteException, MeasurementRunningException;
	
}
