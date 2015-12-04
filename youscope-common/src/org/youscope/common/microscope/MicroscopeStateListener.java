/**
 * 
 */
package org.youscope.common.microscope;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.EventListener;

/**
 * @author langmo
 */
public interface MicroscopeStateListener extends EventListener, Remote
{
    /**
     * Thrown when the queue of measurements changed.
     * 
     * @throws RemoteException
     */
    void measurementQueueChanged() throws RemoteException;

    /**
     * Thrown when the current measurement changed.
     * 
     * @throws RemoteException
     */
    void currentMeasurementChanged() throws RemoteException;
}
