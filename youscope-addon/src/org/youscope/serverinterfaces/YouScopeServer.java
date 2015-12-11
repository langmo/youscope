/**
 * 
 */
package org.youscope.serverinterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

import org.youscope.common.MessageListener;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.callback.CallbackProvider;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.microscope.MicroscopeStateListener;

/**
 * The main interface of the YouScope server, which controls the microscope, executes measurements, and similar.
 * 
 * @author Moritz Lang
 */
public interface YouScopeServer extends Remote
{
	/**
	 * Returns the properties of the server.
	 * 
	 * @return Properties of the server.
	 * @throws RemoteException
	 */
	YouScopeServerProperties getProperties() throws RemoteException;

	/**
	 * Returns a provider for the construction of new measurements.
	 * 
	 * @return Factory for the construction of new measurements.
	 * @throws RemoteException
	 */
	MeasurementProvider getMeasurementProvider() throws RemoteException;

	/**
	 * Returns a provider for the construction of measurement components, e.g. jobs, resources, and similar.
	 * @param measurementUUID Unique identifier of measurement to which the newly created components belong.
	 * 
	 * @param callbackProvider A provider for callback interfaces. Can be null.
	 * @return Provider for the construction of new measurement components.
	 * @throws RemoteException
	 */
	ComponentProvider getComponentProvider(UUID measurementUUID, CallbackProvider callbackProvider) throws RemoteException;

	/**
	 * Returns an object allowing the access to the microscope.
	 * 
	 * @return Microscope object.
	 * @throws RemoteException
	 */
	Microscope getMicroscope() throws RemoteException;

	/**
	 * Adds a listener which detects new messages from the server/microscope.
	 * 
	 * @param listener Listener which detects new messages.
	 * @throws RemoteException
	 */
	void addMessageListener(MessageListener listener) throws RemoteException;

	/**
	 * Removes a previously added listener.
	 * 
	 * @param listener the listener.
	 * @throws RemoteException
	 */
	void removeMessageListener(MessageListener listener) throws RemoteException;

	/**
	 * Adds a listener which detects state changes from the server/microscope.
	 * 
	 * @param listener Listener which detects state changes.
	 * @throws RemoteException
	 */
	void addStateListener(MicroscopeStateListener listener) throws RemoteException;

	/**
	 * Removes a previously added listener.
	 * 
	 * @param listener the listener.
	 * @throws RemoteException
	 */
	void removeStateListener(MicroscopeStateListener listener) throws RemoteException;

	/**
	 * Returns the currently running measurement or NULL, if none is running.
	 * 
	 * @return Currently running measurement
	 * @throws RemoteException
	 */
	Measurement getCurrentMeasurement() throws RemoteException;

	/**
	 * Returns the currently queued measurements. Remark: The current measurement is not included.
	 * 
	 * @return Measurement queue.
	 * @throws RemoteException
	 */
	Measurement[] getMeasurementQueue() throws RemoteException;

	/**
	 * Stops the XY stage of the microscope and prevents ASAP any other thread to obtain access to
	 * the microscope.
	 * 
	 * @throws RemoteException
	 */
	void emergencyStop() throws RemoteException;

	/**
	 * Resets the emergency-stop state, such that microscope can be accessed again.
	 * 
	 * @throws RemoteException
	 */
	void resetEmergencyStop() throws RemoteException;

	/**
	 * Returns true if microscope is currently in the emergency-stop state.
	 * 
	 * @return TRUE if emergency-stopped.
	 * @throws RemoteException
	 */
	boolean isEmergencyStopped() throws RemoteException;
}
