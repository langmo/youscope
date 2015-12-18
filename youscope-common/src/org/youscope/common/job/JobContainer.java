/**
 * 
 */
package org.youscope.common.job;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * A job container is any measurement component which contains jobs.
 * @author Moritz Lang
 */
public interface JobContainer extends Remote
{
	/**
	 * Returns a list of all jobs contained in this container.
	 * 
	 * @return List of jobs.
	 * @throws RemoteException
	 */
	Job[] getJobs() throws RemoteException;
	
	/**
	 * Returns the number of jobs in this container.
	 * @return number of jobs in this container.
	 * @throws RemoteException
	 */
	int getNumJobs() throws RemoteException;
	
	/**
	 * Returns the job at the given index in this container.
	 * @param jobIndex Index of job in the container.
	 * @return the job at the given index.
	 * @throws RemoteException
	 * @throws IndexOutOfBoundsException Thrown if jobIndex is smaller than zero, or greater or equal to {@link #getNumJobs()}.
	 */
	Job getJob(int jobIndex) throws RemoteException, IndexOutOfBoundsException;
}
