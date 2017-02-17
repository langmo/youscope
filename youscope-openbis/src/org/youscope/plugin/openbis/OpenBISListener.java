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
package org.youscope.plugin.openbis;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.EventListener;

/**
 * Listener to obtain information of the current transfer state of OpenBIS.
 * Note that the transfer is processed in several threads, such that events are not guaranteed to be fired in chronicle order
 * (i.e., a transfer finished might be fired before the last transfer progress).
 * @author Moritz Lang
 *
 */
public interface OpenBISListener extends EventListener, Remote
{
	/**
	 * Event fired when the transfer of data started.
	 * @throws RemoteException
	 */
	void transferStarted() throws RemoteException;
	
	/**
	 * Event fired when the transfer of data finished successfully.
	 * @throws RemoteException
	 */
	void transferFinished() throws RemoteException;
	
	/**
	 * Event fired when the transfer of data failed.
	 * @param e An exception indicating the reason for the failure.
	 * @throws RemoteException
	 */
	void transferFailed(Exception e) throws RemoteException;
	
	/**
	 * Fired in more or less regular periods indicating the progress of the transfer. The progress value is between zero and one,
	 * where one indicates that all data is transferred, and zero, that yet no data is transferred. If the progress is not known, -1 is send.
	 * If more information on the progress is available, this is send as a message, otherwise message is null. In general, message can be used
	 * to give more or less detailed information on the current transfer state, and can thus be several lines long.
	 * This function can e.g. be used to implement a wait bar and a text area indicating the progress.
	 * Please note that there is no guarantee that certain progress states are transmitted, i.e. there is no guarantee that the
	 * state "1.0" is transmitted ever. This function should be used for information purposes only, and the progress should not be, in any kind, used
	 * for further processing.
	 * @param progress The current state of the transfer, between 0.0 and 1.0, or -1 for unknown durations/state.
	 * @param message A human readable message associated to the progress. Can be several lines long.
	 * @throws RemoteException
	 */
	void transferProgress(float progress, String message) throws RemoteException;
}
