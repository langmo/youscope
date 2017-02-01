/**
 * 
 */
package org.youscope.common.microscope;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.EventListener;

/**
 * Listener for the state of measurement processing.
 * @author Moritz Lang
 */
public interface MeasurementProcessingListener extends EventListener, Remote
{
    /**
     * Called when the queue of measurements changed.
     * 
     * @throws RemoteException
     */
    void measurementQueueChanged() throws RemoteException;

    /**
     * Called when the current measurement changed.
     * 
     * @throws RemoteException
     */
    void currentMeasurementChanged() throws RemoteException;

    /**
     * Called when measurement processing stopped.
     * @throws RemoteException 
     */
	void measurementProcessingStopped() throws RemoteException;
}
