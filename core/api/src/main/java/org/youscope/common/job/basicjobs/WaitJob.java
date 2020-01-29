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
package org.youscope.common.job.basicjobs;

import java.rmi.RemoteException;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.job.CompositeJob;
import org.youscope.common.job.Job;

/**
 * A job for waiting a given time. If this job has child job, it executes the child jobs first and then waits the given time minus the
 * execution time of the child jobs. This job can thus be used to guarantee a minimum execution time of one or more jobs.
 * @author Moritz Lang
 * 
 */
public interface WaitJob extends Job, CompositeJob
{
	/**
	 * The type identifier of the default implementation of this job. 
	 * Basic jobs are considered such essential to YouScope
	 * such that their interfaces are made part of the shared library. However, their implementation are not, and there
	 * might be several addons providing (different) implementations of this job. Most of these implementations, however, are specific
	 * for a given application and not general. The addon exposing this identifier should be general, that is, every other
	 * part of YouScope accessing this job over the default identifier is expecting the job to behave in the general way.
	 * Only one implementation (addon) should expose the default identifier. Typically, this implementation is already part of YouScope,
	 * such that implementing this addon is not necessary. However, there might be cases when the default implementation should be overwritten,
	 * which is why the interface, but not the implementation is part of YouScope's core elements. In this case, the default implementation
	 * already part of YouScope should be removed (i.e. the corresponding default plug-in deleted).
	 * 
	 */
	public static final String	DEFAULT_TYPE_IDENTIFIER	= "YouScope.WaitJob";
	
	/**
	 * Returns the time delay/sleeping period in ms when this job runs.
	 * 
	 * @return Delay when this job runs in ms.
	 * @throws RemoteException
	 */
	long getWaitTime() throws RemoteException;

	/**
	 * Sets the time delay in ms.
	 * 
	 * @param waitTime Delay in ms.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void setWaitTime(long waitTime) throws RemoteException, ComponentRunningException;

}
