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
package org.youscope.plugin.customjob;

import java.rmi.RemoteException;

import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.job.CompositeJobAdapter;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobException;
import org.youscope.common.microscope.Microscope;

/**
 * @author Moritz Lang
 */
class CustomJobImpl extends CompositeJobAdapter implements CustomJob
{

	/**
	 * Serial Version UID.
	 */
	private static final long			serialVersionUID	= -1632043889601708085L;

	public CustomJobImpl(PositionInformation positionInformation) throws RemoteException
	{
		super(positionInformation);
	}

	@Override
	protected String getDefaultName()
	{
		return "Custom job";
	}

	

	@Override
	public void runJob(ExecutionInformation executionInformation, Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		Job[] jobs = getJobs();
		for(Job job : jobs)
		{
			job.executeJob(executionInformation, microscope, measurementContext);
			if(Thread.interrupted())
				throw new InterruptedException();
		}
	}
}
