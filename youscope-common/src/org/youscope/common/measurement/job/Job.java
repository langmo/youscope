/**
 * 
 */
package org.youscope.common.measurement.job;

import java.rmi.RemoteException;

import org.youscope.common.measurement.Component;
import org.youscope.common.measurement.ExecutionInformation;
import org.youscope.common.measurement.MeasurementContext;
import org.youscope.common.microscope.Microscope;

/**
 * Base class of all jobs.
 * 
 * @author Moritz Lang
 */
public interface Job extends Component
{
	/**
	 * Adds a listener which should e.g. be invoked after each evaluation of the job (at the end of the implementation of runJob().
	 * @param listener The listener to add.
	 * @throws RemoteException
	 */
	public void addJobListener(JobListener listener) throws RemoteException;

	/**
	 * Removes a previously added listener.
	 * @param listener The listener to be removed.
	 * @throws RemoteException
	 */
	public void removeJobListener(JobListener listener) throws RemoteException;

	/**
	 * Function which is called at every evaluation of the job. If this job contains child jobs, it is responsible to call
	 * their respective executeJob() methods.
	 * Should not be called directly by client applications.
	 * @param executionInformation Information about how many times the job has been executed, and if it is executed in a loop.
	 * @param microscope Interface to control and get information of the microscope.
	 * @param measurementContext the context of the measurement, allowing to transfer data between measurement components and similar.
	 * @throws JobException
	 * @throws InterruptedException
	 * @throws RemoteException
	 */
	public void executeJob(ExecutionInformation executionInformation, Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException;

	/**
	 * Called before the first evaluation of a job in a measurement. If this job contains child jobs, it is responsible to call
	 * their respective initializeJob() methods.
	 * Should not be called directly by client applications.
	 * @param microscope Interface to control and get information of the microscope.
	 * @param measurementContext the context of the measurement, allowing to transfer data between measurement components and similar.
	 * @throws JobException
	 * @throws InterruptedException
	 * @throws RemoteException
	 */
	public void initializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException;

	/**
	 * Called before the first evaluation of a job in a measurement. If this job contains child jobs, it is responsible to call
	 * their respective uninitializeJob() methods.
	 * Should not be called directly by client applications.
	 * There is no guarantee that this method is called. Usually it is called when the measurement stops regularly, e.g. by timing out or when the user
	 * invokes "stop measurement". However, it is not invoked if the measurement is interrupted or similar.
	 * @param microscope Interface to control and get information of the microscope.
	 * @param measurementContext the context of the measurement, allowing to transfer data between measurement components and similar.
	 * @throws JobException
	 * @throws InterruptedException
	 * @throws RemoteException
	 */
	public void uninitializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException;
}
