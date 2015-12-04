package org.youscope.common.measurement;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.EventListener;

/**
 * A listener which gets notified about the progress of a measurement.
 * 
 * @author Moritz Lang
 */
public interface MeasurementListener extends EventListener, Remote
{
	/**
	 * Function gets invoked when the measurement starts execution.
	 * 
	 * @throws RemoteException
	 */
	void measurementStarted() throws RemoteException;

	/**
	 * Function gets invoked when the measurement is initializing.
	 * @throws RemoteException
	 */
	void measurementInitializing() throws RemoteException;

	/**
	 * Function gets invoked when the measurement is uninitializing.
	 * @throws RemoteException
	 */
	void measurementUninitializing() throws RemoteException;

	/**
     * Function gets invoked when the structure of a measurement is modified during the execution of the measurement.
     * The structure is e.g. modified if sub-jobs are added or removed.
     * 
     * @throws RemoteException
     */
    void measurementStructureModified() throws RemoteException;
    
	/**
	 * Function gets invoked when the measurement has stoped execution.
	 * 
	 * @throws RemoteException
	 */
	void measurementFinished() throws RemoteException;

	/**
	 * Function gets invoked when the measurement is queued and will be activated as soon as the
	 * microscope is free.
	 * 
	 * @throws RemoteException
	 */
	void measurementQueued() throws RemoteException;

	/**
	 * Function is invoked when measurement is removed from the queue without being stated
	 * 
	 * @throws RemoteException
	 */
	void measurementUnqueued() throws RemoteException;

	/**
	 * Function gets invoked if error occurs during the measurement.
	 * 
	 * @param e Object describing the error.
	 * @throws RemoteException
	 */
	void errorOccured(Exception e) throws RemoteException;
}