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
package org.youscope.plugin.livemodifiablejob;

import java.rmi.RemoteException;

import org.youscope.common.callback.Callback;
import org.youscope.common.callback.CallbackException;

/**
 * Callback for registering jobs which can be live modified.
 * 
 * @author Moritz Lang
 */
public interface LiveModifiableJobCallback extends Callback
{
	
	/**
	 * type identifier of callback.
	 */
	public static final String TYPE_IDENTIFIER = "YouScope.LiveModifiableJob.Callback";
    /**
     * Registers a job to be modifiable by the UI.
     * 
     * @param job job to be registered
     * @throws RemoteException
     * @throws CallbackException 
     */
    public void registerJob(LiveModifiableJob job) throws RemoteException, CallbackException;
    
    /**
     * Registers a job to be modifiable by the UI.
     * 
     * @param job job to be unregistered
     * @throws RemoteException
     * @throws CallbackException 
     */
    public void unregisterJob(LiveModifiableJob job) throws RemoteException, CallbackException;
}
