/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
package org.youscope.common.job;

import java.rmi.RemoteException;

import org.youscope.common.ComponentRunningException;

/**
 * A job which can contain child jobs (i.e. a non-leaf job in the measurement tree).
 * @author Moritz Lang
 *
 */
public interface CompositeJob extends Job
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
	
	/**
	 * Adds a job to the end of the container.
	 * @param job The job to be added.
	 * @throws RemoteException
	 * @throws ComponentRunningException Thrown if job could not be added, e.g. because measurement is already running.
	 * @throws JobException 
	 * 
	 */
	void addJob(Job job) throws RemoteException, ComponentRunningException, JobException;

	/**
	 * Inserts a job at the given index. The index of the job previously having the index, and all jobs having higher indices, are increased.
	 * @param job  The job to be inserted.
	 * @param jobIndex Index where the job should be inserted.
	 * @throws RemoteException 
	 * @throws ComponentRunningException Thrown if job could not be added, e.g. because measurement is already running.
	 * @throws IndexOutOfBoundsException Thrown if index is smaller than zero, or greater or equal {@link #getNumJobs()}.
	 * @throws JobException 
	 */
	void insertJob(Job job, int jobIndex) throws RemoteException, ComponentRunningException, IndexOutOfBoundsException, JobException;
	
	/**
	 * Removes the job at the given position.
	 * @param jobIndex Index of the job to be removed.
	 * @throws RemoteException
	 * @throws ComponentRunningException Thrown if job could not be removed, e.g. because measurement is already running.
	 * @throws IndexOutOfBoundsException 
	 * @throws JobException 
	 */
	void removeJob(int jobIndex) throws RemoteException, ComponentRunningException, IndexOutOfBoundsException, JobException;

	/**
	 * Removes all jobs from this container.
	 * 
	 * @throws RemoteException
	 * @throws ComponentRunningException Thrown if jobs could not be removed, e.g. because measurement is already running.
	 * @throws JobException 
	 */
	void clearJobs() throws RemoteException, ComponentRunningException, JobException;
}
