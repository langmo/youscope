/**
 * 
 */
package org.youscope.plugin.waitforuser;

import java.rmi.RemoteException;

import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.job.Job;


/**
 * @author langmo
 */
public interface WaitForUserJob extends Job
{

	/**
	 * Returns the message displayed to the user.
	 * 
	 * @return message to be displayed.
	 * @throws RemoteException
	 */
	String getMessage() throws RemoteException;

	/**
	 * Sets the message to be displayed to the user.
	 * 
	 * @param message Message to be displayed.
	 * @throws RemoteException
	 * @throws MeasurementRunningException 
	 */
	void setMessage(String message) throws RemoteException,	MeasurementRunningException;
	
	/**
	 * Sets the callback to the client used by this job. Must be set before initialization.
	 * @param callback Callback to be used to wait for the user.
	 * @throws RemoteException
	 * @throws MeasurementRunningException 
	 */
	void setMeasurementCallback(WaitForUserCallback callback) throws RemoteException, MeasurementRunningException;
}
