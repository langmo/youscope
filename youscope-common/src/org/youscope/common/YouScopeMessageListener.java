/**
 * 
 */
package org.youscope.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.EventListener;

/**
 * A listener which gets informed if the state of the microscope changes.
 * 
 * @author Moritz Lang
 */
public interface YouScopeMessageListener extends EventListener, Remote
{
	/**
	 * Function which gets invoked if there is a new message from the microscope.
	 * 
	 * @param message Human readable message.
	 * @param time The date/time when this message was produced.
	 * @throws RemoteException
	 */
	void consumeMessage(String message, Date time) throws RemoteException;

	/**
	 * Function which gets invoked if there is a new error from the microscope.
	 * 
	 * @param message Human readable message.
	 * @param time The date/time when this message was produced.
	 * @param exception The exception which occured.
	 * @throws RemoteException
	 */
	void consumeError(String message, Throwable exception, Date time) throws RemoteException;
}
