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
