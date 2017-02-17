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
package org.youscope.plugin.repeatjob;

import java.rmi.RemoteException;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.job.CompositeJobAdapter;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobException;
import org.youscope.common.microscope.Microscope;

/**
 * Implementation of the repeat job.
 * @author Moritz Lang
 */
class RepeatJobImpl extends CompositeJobAdapter implements RepeatJob
{

	/**
	 * Serial Version UID.
	 */
	private static final long			serialVersionUID	= -1632043889601708085L;

	private int numRepeats = 1;
	
	public RepeatJobImpl(PositionInformation positionInformation) throws RemoteException
	{
		super(positionInformation);
	}

	@Override
	protected String getDefaultName()
	{
		String text = "Repeat Job(" + Integer.toString(numRepeats) + "times: ";
		Job[] jobs = getJobs();
		boolean first = true;
		for(Job job : jobs)
		{
			if(first)
				first = false;
			else
				text += "; ";
			try {
				text += job.getName();
			} catch (@SuppressWarnings("unused") RemoteException e) {
				text+="UNKNOWN";
			}
		}
		return text + ")";
	}

	

	@Override
	public void runJob(ExecutionInformation executionInformation, Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		Job[] jobs = getJobs();
		for(int k = 0; k < numRepeats; k++)
		{
			ExecutionInformation subExecution = new ExecutionInformation(executionInformation, k);
			for(Job job : jobs)
			{
				job.executeJob(subExecution, microscope, measurementContext);
				if(Thread.interrupted())
					throw new InterruptedException();
			}
		}
	}

	@Override
	public int getNumRepeats()
	{
		return numRepeats;
	}

	@Override
	public void setNumRepeats(int numRepeats) throws ComponentRunningException
	{
		assertRunning();
		this.numRepeats = numRepeats > 0 ? numRepeats : 0;
	}
}
