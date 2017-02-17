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
package org.youscope.plugin.waitjob;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobAdapter;
import org.youscope.common.job.JobException;
import org.youscope.common.job.basicjobs.WaitJob;
import org.youscope.common.microscope.Microscope;

/**
 * @author Moritz Lang
 * 
 */
class WaitJobImpl extends JobAdapter implements WaitJob
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 412477219773598525L;
	private long				waitTime			= 0;
	
	private final ArrayList<Job>	jobs				= new ArrayList<Job>();

	public WaitJobImpl(PositionInformation positionInformation) throws RemoteException
	{
		super(positionInformation);
	}

	@Override
	public long getWaitTime() throws RemoteException
	{
		return waitTime;
	}

	@Override
	public void setWaitTime(long waitTime) throws RemoteException, ComponentRunningException
	{
		this.waitTime = waitTime > 0 ? waitTime : 0;
	}

	@Override
	public void runJob(ExecutionInformation executionInformation, Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		long wait = waitTime;
		synchronized(jobs)
		{
			if(!jobs.isEmpty())
			{
				long startSubJobs = System.currentTimeMillis();
				for(int i = 0; i < jobs.size(); i++)
				{
					jobs.get(i).executeJob(executionInformation, microscope, measurementContext);
					if(Thread.interrupted())
						throw new InterruptedException();
				}
				wait -= System.currentTimeMillis()-startSubJobs;
			}
		}
			
		if(wait > 0)
		{
			Thread.sleep(wait);
		}
	}

	@Override
	protected String getDefaultName()
	{
		return "Wait job";
	}
	
	@Override
	public void initializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		super.initializeJob(microscope, measurementContext);

		synchronized(jobs)
		{
			for(Job job : jobs)
			{
				job.initializeJob(microscope, measurementContext);
			}
		}
	}

	@Override
	public void uninitializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		super.uninitializeJob(microscope, measurementContext);

		synchronized(jobs)
		{
			for(Job job : jobs)
			{
				job.uninitializeJob(microscope, measurementContext);
			}
		}
	}

	@Override
	public synchronized void addJob(Job job) throws RemoteException, ComponentRunningException
	{
		assertRunning();
		synchronized(jobs)
		{
			jobs.add(job);
		}
	}

	@Override
	public synchronized void removeJob(int jobIndex) throws RemoteException, ComponentRunningException, IndexOutOfBoundsException
	{
		assertRunning();
		synchronized(jobs)
		{
			jobs.remove(jobIndex);
		}
	}

	@Override
	public synchronized void clearJobs() throws RemoteException, ComponentRunningException
	{
		assertRunning();
		synchronized(jobs)
		{
			jobs.clear();
		}
	}

	@Override
	public Job[] getJobs() throws RemoteException
	{
		synchronized(jobs)
		{
			return jobs.toArray(new Job[jobs.size()]);
		}
	}

	@Override
	public void insertJob(Job job, int jobIndex)
			throws RemoteException, ComponentRunningException, IndexOutOfBoundsException {
		jobs.add(jobIndex, job);
	}

	@Override
	public int getNumJobs() throws RemoteException {
		return jobs.size();
	}

	@Override
	public Job getJob(int jobIndex) throws RemoteException, IndexOutOfBoundsException {
		return jobs.get(jobIndex);
	}
}
