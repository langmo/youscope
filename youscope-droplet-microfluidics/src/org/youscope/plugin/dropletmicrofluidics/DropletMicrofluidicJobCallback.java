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
package org.youscope.plugin.dropletmicrofluidics;

import java.rmi.RemoteException;

import org.youscope.common.ExecutionInformation;
import org.youscope.common.callback.Callback;
import org.youscope.common.callback.CallbackException;
import org.youscope.common.table.Table;

/**
 * Callback providing an UI for droplet based microfluidic measurements.
 * 
 * @author Moritz Lang
 */
public interface DropletMicrofluidicJobCallback extends Callback
{
	
	/**
	 * Type identifier for callback.
	 */
	public static final String	TYPE_IDENTIFIER	= "YouScope.DropletBasedMicrofluidicsJob.Callback";
    /**
     * Sends the table produced by a droplet based microfluidic job (see {@link DropletMicrofluidicTable#getTableDefinition()} to the callback for visualization.
     * @param executionInformation Execution information of droplet based microfluidics job.
     * @param table table containing droplet data.
     * @throws RemoteException
     * @throws CallbackException 
     */
    public void dropletMeasured(ExecutionInformation executionInformation, Table table) throws RemoteException, CallbackException;
}
