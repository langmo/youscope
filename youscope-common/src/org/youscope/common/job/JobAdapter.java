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
package org.youscope.common.job;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.MessageListener;
import org.youscope.common.PositionInformation;
import org.youscope.common.microscope.Microscope;

/**
 * Convenient adapter class to implement a job. Basic required functionality (evaluation counting, job listener notifications, ...) of a job is already implemented.
 * @author Moritz Lang
 */
public abstract class JobAdapter extends UnicastRemoteObject implements Job
{
	/**
	 * Serial Version UID
	 */
	private static final long			serialVersionUID	= 8203311968197924740L;

	private final Vector<JobListener>	jobListeners		= new Vector<JobListener>();

	private final ArrayList<MessageListener>	messageListeners	= new ArrayList<MessageListener>();

	private final PositionInformation	positionInformation;

	private volatile boolean			isRunning			= false;
	private volatile String				jobName				= null;

	private final UUID uniqueID = UUID.randomUUID();
	
	/**
	 * Constructor.
	 * @param positionInformation The logical position where this job is executed. Must not be null.
	 * @throws RemoteException
	 */
	public JobAdapter(PositionInformation positionInformation) throws RemoteException
	{
		if(positionInformation == null)
			throw new IllegalArgumentException("Position information must not be null.");

		this.positionInformation = positionInformation;
	}

	/**
	 * Convenient method to throw an exception if a parameter of a job should be changed, but the job is currently executed.
	 * Should be called at the beginning of every function which is manipulating the parameters of the job (i.e. all "set" methods).
	 * @throws JobRunningException
	 */
	protected synchronized void assertRunning() throws ComponentRunningException
	{
		if(isRunning())
			throw new ComponentRunningException();
	}

	/**
	 * Returns true if the measurement the job belongs to is currently executed.
	 * @return True if the job is currently executed.
	 */
	public boolean isRunning()
	{
		return isRunning;
	}

	private synchronized void setRunning(boolean isRunning)
	{
		this.isRunning = isRunning;
	}

	private final void startJob(ExecutionInformation executionInformation) throws RemoteException
	{
		synchronized(jobListeners)
		{
			for(JobListener listener : jobListeners)
			{
				listener.jobStarted(executionInformation);
			}
		}
	}

	/**
	 * The standard implementation of this function automatically takes care for evaluation counting and the notification of listeners.
	 * Do not overwrite this function, but runJob() instead (which is called by this implementation)
	 */
	@Override
	public void executeJob(ExecutionInformation executionInformation, Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		startJob(executionInformation);
		runJob(executionInformation, microscope, measurementContext);
		endJob(executionInformation);
	}

	private final void endJob(ExecutionInformation executionInformation) throws RemoteException
	{
		synchronized(jobListeners)
		{
			for(JobListener listener : jobListeners)
			{
				listener.jobFinished(executionInformation);
			}
		}
	}

	/**
	 * Convenient function which gets called by executeJob(). Does not have to take care for execution counting and notification of job listeners.
	 * Overwrite this method instead of executeJob.
	 * @param executionInformation Information about the number of times, and the loops, in which this job is executed.
	 * @param microscope The microscope on which this job should be executed.
	 * @param measurementContext the context of the measurement, allowing to transfer data between measurement components and similar.
	 * @throws JobException
	 * @throws InterruptedException
	 * @throws RemoteException
	 */
	public abstract void runJob(ExecutionInformation executionInformation, Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException;

	@Override
	public PositionInformation getPositionInformation()
	{
		return positionInformation;
	}

	@Override
	public void addJobListener(JobListener listener)
	{
		if(listener == null)
			return;
		synchronized(jobListeners)
		{
			jobListeners.add(listener);
		}
	}

	@Override
	public void removeJobListener(JobListener listener)
	{
		if(listener == null)
			return;
		synchronized(jobListeners)
		{
			jobListeners.remove(listener);
		}
	}

	@Override
	public void addMessageListener(MessageListener writer)
	{
		if(writer == null)
			return;
		synchronized(messageListeners)
		{
			messageListeners.add(writer);
		}
	}

	@Override
	public void removeMessageListener(MessageListener writer)
	{
		if(writer == null)
			return;
		synchronized(messageListeners)
		{
			messageListeners.remove(writer);
		}
	}

	/**
	 * Sends the given message to all output writers.
	 * @param message Message to send
	 * @throws RemoteException
	 */
	protected void sendMessage(String message)
	{
		synchronized(messageListeners)
		{
			for (Iterator<MessageListener> iterator = messageListeners.iterator(); iterator.hasNext(); ) 
			{
				MessageListener writer = iterator.next();
				try 
				{
					writer.sendMessage(message);
				} 
				catch (@SuppressWarnings("unused") RemoteException e) 
				{
					iterator.remove();
				}
			}
		}
	}
	
	protected void sendErrorMessage(String message, Throwable error)
	{
		synchronized(messageListeners)
		{
			for (Iterator<MessageListener> iterator = messageListeners.iterator(); iterator.hasNext(); ) 
			{
				MessageListener writer = iterator.next();
				try 
				{
					writer.sendErrorMessage(message, error);
				} 
				catch (@SuppressWarnings("unused") RemoteException e) 
				{
					iterator.remove();
				}
			}
		}
	}

	/**
	 * When overwriting this function, the base implementation should be called at the beginning, i.e. super.initializeJob(...).
	 */
	@Override
	public void initializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		synchronized(jobListeners)
		{
			for(JobListener listener : jobListeners)
			{
				listener.jobInitialized();
			}
		}
		setRunning(true);
	}

	/**
	 * When overwriting this function, the base implementation should be called at the beginning, i.e. super.initializeJob(...).
	 */
	@Override
	public void uninitializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		setRunning(false);
		synchronized(jobListeners)
		{
			for(JobListener listener : jobListeners)
			{
				listener.jobUninitialized();
			}
		}
	}

	@Override
	public String getName()
	{
		if(jobName == null)
			return getDefaultName();
		return jobName;
	}

	@Override
	public synchronized void setName(String jobName) throws ComponentRunningException
	{
		assertRunning();
		this.jobName = jobName;
	}

	/**
	 * Should return a default name for the job type which gets returned by getJobName() if no job name was set explicitly by setJobName.
	 * @return Default job name. Should be short, but however informative.
	 * @throws RemoteException
	 */
	protected abstract String getDefaultName();

	@Override
	public UUID getUUID() 
	{
		return uniqueID;
	}
}
