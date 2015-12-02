package ch.ethz.csb.youscope.shared.measurement.job;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;

/**
 * A job container allowing to add or remove jobs.
 * @author Moritz Lang
 *
 */
public interface EditableJobContainer extends JobContainer 
{

	/**
	 * Adds a job to the end of the container.
	 * @param job The job to be added.
	 * @throws RemoteException
	 * @throws MeasurementRunningException Thrown if job could not be added, e.g. because measurement is already running.
	 * 
	 */
	void addJob(Job job) throws RemoteException, MeasurementRunningException;

	/**
	 * Inserts a job at the given index. The index of the job previously having the index, and all jobs having higher indices, are increased.
	 * @param job  The job to be inserted.
	 * @param jobIndex Index where the job should be inserted.
	 * @throws RemoteException 
	 * @throws MeasurementRunningException Thrown if job could not be added, e.g. because measurement is already running.
	 * @throws IndexOutOfBoundsException Thrown if index is smaller than zero, or greater or equal {@link #getNumJobs()}.
	 */
	void insertJob(Job job, int jobIndex) throws RemoteException, MeasurementRunningException, IndexOutOfBoundsException;
	
	/**
	 * Removes a previously added job.
	 * @param job The job to be removed.
	 * @throws RemoteException
	 * @throws MeasurementRunningException Thrown if job could not be removed, e.g. because measurement is already running.
	 */
	void removeJob(Job job) throws RemoteException, MeasurementRunningException;

	/**
	 * Removes all jobs from this container.
	 * 
	 * @throws RemoteException
	 * @throws MeasurementRunningException Thrown if jobs could not be removed, e.g. because measurement is already running.
	 */
	void clearJobs() throws RemoteException, MeasurementRunningException;
}
