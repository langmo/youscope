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
/**
 * 
 */
package org.youscope.common.task;

import java.rmi.RemoteException;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.job.Job;

/**
 * Interface represents a task. A task is a process which--regularly or not, once or often--submits its jobs to the job execution queue, where
 * they get executed in a FIFO manner. Each measurement must have one or more tasks.
 * @author Moritz Lang
 */
public interface Task
{
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
	
	/**
	 * Adds a job to the end of the job list.
	 * @param job The job to be added.
	 * @throws RemoteException
	 * @throws ComponentRunningException Thrown if job could not be added, e.g. because measurement is already running.
	 * @throws TaskException 
	 * 
	 */
	void addJob(Job job) throws RemoteException, ComponentRunningException, TaskException;

	/**
	 * Inserts a job at the given index. The index of the job previously having the index, and all jobs having higher indices, are increased.
	 * @param job  The job to be inserted.
	 * @param jobIndex Index where the job should be inserted.
	 * @throws RemoteException 
	 * @throws ComponentRunningException Thrown if job could not be added, e.g. because measurement is already running.
	 * @throws TaskException 
	 * @throws IndexOutOfBoundsException Thrown if index is smaller than zero, or greater or equal {@link #getNumJobs()}.
	 */
	void insertJob(Job job, int jobIndex) throws RemoteException, ComponentRunningException, TaskException, IndexOutOfBoundsException;
	
	/**
	 * Removes the job at the given index.
	 * @param jobIndex Index of the job which should be removed.
	 * @throws RemoteException
	 * @throws ComponentRunningException Thrown if job could not be removed, e.g. because measurement is already running.
	 * @throws TaskException 
	 * @throws IndexOutOfBoundsException 
	 */
	void removeJob(int jobIndex) throws RemoteException, ComponentRunningException, TaskException, IndexOutOfBoundsException;

	/**
	 * Removes all jobs from this task.
	 * 
	 * @throws RemoteException
	 * @throws ComponentRunningException Thrown if jobs could not be removed, e.g. because measurement is already running.
	 * @throws TaskException 
	 */
	void clearJobs() throws RemoteException, ComponentRunningException, TaskException;
	
	/**
	 * Returns a list of all jobs contained in this task.
	 * 
	 * @return List of jobs.
	 * @throws RemoteException
	 */
	Job[] getJobs() throws RemoteException;
	
	/**
	 * Returns the number of jobs in this task.
	 * @return number of jobs in this container.
	 * @throws RemoteException
	 */
	int getNumJobs() throws RemoteException;
	
	/**
	 * Returns the job at the given index in this task.
	 * @param jobIndex Index of job in the container.
	 * @return the job at the given index.
	 * @throws RemoteException
	 * @throws IndexOutOfBoundsException Thrown if jobIndex is smaller than zero, or greater or equal to {@link #getNumJobs()}.
	 */
	Job getJob(int jobIndex) throws RemoteException, IndexOutOfBoundsException;
	
	/**
	 * Sets the initial execution number of this task. Typically, this is always zero. Negative numbers are not allowed.
	 * @param executionNumber initial execution number of task
	 * @throws ComponentRunningException
	 * @throws RemoteException
	 * @throws IllegalArgumentException 
	 */
	void setInitialExecutionNumber(long executionNumber) throws ComponentRunningException, RemoteException, IllegalArgumentException;
	/**
	 * Returns the initial execution number of this task. Typically, this is always zero. Negative numbers are not allowed.
	 * @return initial execution number of task.
	 * @throws RemoteException 
	
	 */
	long getInitialExecutionNumber() throws RemoteException;
}
