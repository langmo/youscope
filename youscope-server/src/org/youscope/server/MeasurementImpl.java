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
package org.youscope.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.MeasurementContext;
import org.youscope.common.MessageListener;
import org.youscope.common.measurement.MeasurementException;
import org.youscope.common.measurement.MeasurementListener;
import org.youscope.common.measurement.MeasurementState;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.task.Task;
import org.youscope.server.MeasurementTaskImpl.TaskExecutor;

/**
 * @author Moritz Lang
 */
class MeasurementImpl implements TaskExecutor
{
	private final ArrayList<MeasurementListener>	measurementListeners		= new ArrayList<MeasurementListener>();

	private volatile long							maxRuntime			= -1;

	private final ArrayList<MeasurementTaskImpl>	tasks						= new ArrayList<MeasurementTaskImpl>();

	private volatile String							name						= "unnamed";

	private volatile DeviceSetting[]								startUpDeviceSettings		= new DeviceSetting[0];

	private volatile DeviceSetting[]								shutDownDeviceSettings		= new DeviceSetting[0];

	private volatile boolean						lockMicroscopeWhileRunning	= true;

	private String									userDefinedType				= "";
	
	private final MeasurementContextImpl measurementContext = new MeasurementContextImpl(this);
	
	private final HashMap<String, Serializable> initialMeasurementContextProperties = new HashMap<String, Serializable>();

	private final ArrayList<MessageListener> messageWriters = new ArrayList<MessageListener>();
	
	private final UUID uniqueIdentifier = UUID.randomUUID();
	
	private final MeasurementMetadataImpl metadata;
	
	private volatile long initialRuntime = 0;
	
	/*
	 * Tasks are first submitted into the scheduledTasksQueue. When they are due, they are moved into the 
	 * pendingTasksQueue. The measurement manager unpacks the jobs of the most pending task into the pendingJobsQueue, and processes them
	 * one by one, until the pendingJobsQueue is empty.
	 */
	private final LinkedList<JobExecutionQueueElement> pendingJobsQueue = new LinkedList<>();
	private final PriorityQueue<ScheduledTask> pendingTasksQueue = new PriorityQueue<>();
	private final DelayQueue<ScheduledTask> scheduledTasksQueue = new DelayQueue<>();
	/**
	 * Time in ms when the measurement was started, or -1 if not yet started.
	 * Should only be modified by {@link #scheduledTasksQueueExecutor}.
	 */
	private volatile long startTime = -1;
	/**
	 * Time when the measurement was paused the last time, or -1 if yet not paused.
	 * Should only be modified by {@link #scheduledTasksQueueExecutor}.
	 */
	private volatile long pauseTime = -1;
	/**
	 * Time when the measurement was stopped, or -1 if yet not stopped.
	 * Should only be modified by {@link #scheduledTasksQueueExecutor}.
	 */
	private volatile long stopTime = -1;
	
	/**
	 * Complete duration of all pauses of the measurement, in ms.
	 * Should only be modified by {@link #shutdownMeasurement(Microscope)}.
	 */
	private volatile long pauseDuration = 0;
	/**
	 * All times are given in milliseconds.
	 */
	private static final TimeUnit BASE_UNIT = TimeUnit.MILLISECONDS;
	
	/**
	 * Counter used to discriminate task scheduled at the same time in a FIFO manner.
	 */
	private volatile long nextScheduledTaskID = 0;
	private volatile Thread scheduledTasksQueueExecutor = null;
	private final ReentrantLock startStopLock = new ReentrantLock();
	
	private final ReentrantLock pendingQueuesLock = new ReentrantLock();
	private final Condition jobsAvailable = pendingQueuesLock.newCondition();
	/**
	 * Contains a task and information when the task should be executed. Wrapper is necessary for {@link DelayQueue}.
	 * @author Moritz Lang
	 *
	 */
	private class ScheduledTask implements Delayed
	{
		/**
		 * When the task should be executed, in ms after measurement start, minus pause times.
		 */
		private final long scheduledRuntime;
		/**
		 * The task which should be executed.
		 */
		private final MeasurementTaskImpl task;
		/**
		 * Used when two scheduled tasks have the same {@link #scheduledRuntime} to first execute the task which was
		 * submitted first.
		 */
		private final long scheduledTaskID;
		/**
		 * A task scheduled at the given time after start, minus pause time.
		 * @param scheduledtime Delay when task should be executed in milliseconds.
		 */
		ScheduledTask(long scheduledRuntime, MeasurementTaskImpl task)
		{
			this.scheduledRuntime = scheduledRuntime;
			this.task = task;
			scheduledTaskID = task==null ? Long.MAX_VALUE : nextScheduledTaskID++;
		}
		@Override
		public int compareTo(Delayed otherRaw) 
		{
			if(!(otherRaw instanceof ScheduledTask))
				throw new ClassCastException("Only expecting elements of class ScheduledTask in scheduledTasksQueue.");
			ScheduledTask other = (ScheduledTask)otherRaw;
			if(scheduledRuntime < other.scheduledRuntime)
				return -1;
			else if(scheduledRuntime > other.scheduledRuntime)
				return 1;
			else if(scheduledTaskID < other.scheduledTaskID)
				return -1;
			else if(scheduledTaskID > other.scheduledTaskID)
				return 1;
			else
				return 0;
		}

		@Override
		public long getDelay(TimeUnit timeUnit) 
		{
			return timeUnit.convert(scheduledRuntime-getRuntime(), BASE_UNIT);
		}
		
	}
	
	/**
	 * Current state of the measurement, i.e. if running or not.
	 * @author Moritz Lang
	 *
	 */
	private final class RunState
	{
		private  MeasurementState measurementState = MeasurementState.READY;
		private boolean shouldStop = true;
		private boolean shouldPause = false;
		private boolean shouldBlockQueue = false;
		synchronized void stop(boolean shouldBlockQueue)
		{
			if(shouldPause)
				return;
			this.shouldStop = true;
			this.shouldBlockQueue = shouldBlockQueue;
		}
		synchronized void restart()
		{
			this.shouldPause = false;
			this.shouldStop = false;
			this.shouldBlockQueue = false;
		}
		synchronized void resume()
		{
			this.shouldPause = false;
			this.shouldBlockQueue = false;
		}
		synchronized void pause()
		{
			if(shouldStop)
				return;
			this.shouldPause = true;
			this.shouldBlockQueue = true;
		}
		synchronized boolean isShouldPause()
		{
			return shouldPause;
		}
		synchronized boolean isShouldBlockQueue()
		{
			return shouldBlockQueue;
		}
		
		synchronized void assertEditable() throws ComponentRunningException
		{
			if(!measurementState.isEditable())
				throw new ComponentRunningException();
		}
		/**
		 * Sets the state of the measurement and notifies all listeners.
		 * @param newState New state of the measurement.
		 * @throws MeasurementException Thrown if trying to change to non-error state while current state is {@link MeasurementState#ERROR}.
		 * @return true if new state is different from old.
		 */
		boolean toState(MeasurementState newState) throws MeasurementException
		{
			MeasurementState oldState;
			synchronized(this)
			{
				oldState = this.measurementState;
				// prevent error state from being accidentally cleared.
				if(oldState.isError() && !newState.isError())
					throw new MeasurementException("Cannot change state of measurement to "+newState.toString()+", because measurement is in error state.");
				this.measurementState = newState;
			}
			if(oldState != newState)
			{
				notifyMeasurementStateChanged(oldState, newState);
				return true;
			}
			return false;
		}
		
		MeasurementState getState()
		{
			return measurementState;
		}
		
		/**
		 * Sets the measurement state to {@link MeasurementState#ERROR}, notifies all listeners of the state change, notifies all listeners of the error, and returns the exception (e.g. to be used in throws).
		 * @param e Exception which occurred.
		 * @return The exception gotten as a parameter.
		 */
		<T extends Exception> T toErrorState(T e)
		{
			try {
				toState(MeasurementState.ERROR);
			} catch (@SuppressWarnings("unused") MeasurementException e1) {
				// should not happen, since changing to error state should be safe.
				// Since we are anyways trying to handle an error, do nothing.
			}
			notifyMeasurementError(e);
			return e;
		}
	
		/**
		 * Sets the state of the measurement and notifies all listeners, if the current state is in one of the allowed states.
		 * If not, an error is thrown and the state is not changed.
		 * @param state New state of the measurement.
		 * @return true if new state is different from old.
		 */
		boolean toState(MeasurementState newState, String errorMessage, MeasurementState... allowedStates) throws MeasurementException
		{
			MeasurementState currentState;
			synchronized(this)
			{
				currentState = this.measurementState;
				for(MeasurementState allowedState : allowedStates)
				{
					if(currentState == allowedState)
					{
						return toState(newState);
					}
				}
			}
			// if we are here, we were not in one of the allowed states.
			StringBuilder message = new StringBuilder(errorMessage);
			message.append(" Expected state: ");
			for(int i=0; i<allowedStates.length; i++)
			{
				if(i>0 && i==allowedStates.length-1)
					message.append(" or ");
				else if(i>0)
					message.append(", ");
				message.append(allowedStates[i].toString());
			}
			message.append(". Current state: "+currentState.toString()+".");
			throw new MeasurementException(message.toString());
		}
	}
	private final RunState runState = new RunState();
	
	/**
	 * Returns time in milliseconds after start, minus the pause time.
	 * @return run time in milliseconds.
	 */
	@Override
	public long getRuntime()
	{
		long currentTime = System.currentTimeMillis();
		long startTime = this.startTime;
		long pauseDuration = this.pauseDuration;
		long pauseTime = this.pauseTime;
		long stopTime = this.stopTime;
		return startTime<0 ? -1 : (stopTime>=0 ? initialRuntime+stopTime-startTime -pauseDuration: (pauseTime >= 0 ? initialRuntime+pauseTime - startTime-pauseDuration :  initialRuntime+currentTime - startTime-pauseDuration));
	}
	
	
	
	/**
	 * Returns the time when last time started (not resumed), or -1 if yet not started.
	 * @return Time in ms.
	 */
	public long getStartTime()
	{
		return startTime;
	}
	/**
	 * Returns the time when last time stopped, or -1 if yet not stopped.
	 * @return Time in ms.
	 */
	public long getStopTime()
	{
		return stopTime;
	}
	
	/**
	 * Measurement with infinite runtime.
	 * @throws RemoteException 
	 */
	MeasurementImpl() throws RemoteException
	{
		this(-1);
	}
	/**
	 * Measurement which stops after a certain runtime. Set runtime to negative values for infinite runtime.
	 * @param measurementRuntime time, in ms, after which measurement should stop.
	 * @throws RemoteException 
	 */
	MeasurementImpl(int measurementRuntime) throws RemoteException
	{
		this.maxRuntime = measurementRuntime;
		metadata = new MeasurementMetadataImpl();
	}
	MeasurementContext getMeasurementContext()
    {
        return measurementContext;
    }

	public UUID getUUID() 
	{
		return uniqueIdentifier;
	}
		
	public void addMessageListener(MessageListener writer)
	{
		synchronized(messageWriters)
		{
			messageWriters.add(writer);
		}
	}

	public void removeMessageListener(MessageListener writer)
	{
		synchronized(messageWriters)
		{
			messageWriters.remove(writer);
		}
	}
	
	private void sendMessage(String message)
	{
		synchronized(messageWriters)
		{
			for (Iterator<MessageListener> iterator = messageWriters.iterator(); iterator.hasNext();) 
			{
				try
	            {
	            	iterator.next().sendMessage(message);
	            } 
				catch (RemoteException e1)
	            {
	                ServerSystem.err.println("Measurement message listener not answering. Removing him from the queue.", e1);
	                iterator.remove();
	            }
	        }
		}
	}
	
	private Task addTask(MeasurementTaskImpl task) throws ComponentRunningException
	{
		synchronized(runState)
		{
			runState.assertEditable();
			tasks.add(task);
		}
		return task;
	}

	Task[] getTasks()
	{
		return tasks.toArray(new Task[tasks.size()]);
	}

	void setTypeIdentifier(String type) throws ComponentRunningException
	{
		synchronized(runState)
		{
			runState.assertEditable();
			userDefinedType = type;
		}
	}

	String getTypeIdentifier()
	{
		return userDefinedType;
	}
	/**
	 * Sets the state of the measurement to {@link MeasurementState#QUEUED}.
	 * Should only be called by {@link MeasurementManager}.
	 */
	void queueMeasurement() throws MeasurementException 
	{
		runState.toState(MeasurementState.QUEUED, "Cannot queue measurement.", MeasurementState.READY, MeasurementState.UNINITIALIZED, MeasurementState.PAUSED);
	}
	/**
	 * Sets the state of the measurement to {@link MeasurementState#READY}.
	 * Should only be called by {@link MeasurementManager}.
	 */
	void unqueueMeasurement() throws MeasurementException 
	{
		runState.toState(MeasurementState.READY, "Cannot unqueue measurement.", MeasurementState.QUEUED);
	}
	/**
	 * Interrupts the measurement, sets it to error state, and notifies all listeners.
	 * Should only be called by {@link MeasurementManager#runMeasurements()} when processing of the measurement failed.
	 * @param e
	 */
	void failMeasurement(Exception e) 
	{
		runState.toErrorState(e);
		stopMeasurement(false);
	}
	
	private void initializeMeasurement(Microscope microscope) throws MeasurementException, InterruptedException
	{
		startStopLock.lock();
		try
		{
			runState.toState(MeasurementState.INITIALIZING, "Cannot initialize measurement.", MeasurementState.READY, MeasurementState.UNINITIALIZED, MeasurementState.QUEUED);
			runState.restart();
			sendMessage("Initializing measurement...");
				
			scheduledTasksQueue.clear();
			pendingQueuesLock.lock();
			try
			{
				pendingTasksQueue.clear();
				pendingJobsQueue.clear();
				jobsAvailable.signalAll();
			}
			finally
			{
				pendingQueuesLock.unlock();
			}
			startTime = -1;
			pauseDuration = 0;
			pauseTime = -1;
			stopTime = -1;
						
			// Setup empty measurement context
	        measurementContext.clear();
	        for (Map.Entry<String, Serializable> entry : initialMeasurementContextProperties.entrySet())
	        {
	        	Serializable clone;
	        	// we want to make a copy of the serializable object. However, we cannot assume that it implements Cloneable. Thus, we simply
	        	// serialize and deserialize it, which has the same effect.
	        	
	        	ByteArrayOutputStream outStream = null;
	        	ObjectOutputStream out = null;
	        	ByteArrayInputStream inStream = null;
	        	ObjectInputStream in = null;
	        	try
	        	{
	            	outStream = new ByteArrayOutputStream();
	            	out = new ObjectOutputStream(outStream);
	                out.writeObject(entry.getValue());
	                inStream = new ByteArrayInputStream(outStream.toByteArray());
	                in = new ObjectInputStream(inStream);
	                clone = (Serializable) in.readObject();
	        	}
	        	catch(Exception e)
	        	{
	        		throw runState.toErrorState(new MeasurementException("Serialization of initial measurement context property " + entry.getKey() + " failed. Is the context property not only implementing Serializable, but also follow the rules what is allowed and what not when implementing Serializable?", e));
	        	}
	        	finally
	        	{
	        		if(outStream != null)
	        		{
	        			try {
							outStream.close();
						} catch (@SuppressWarnings("unused") IOException e) {
							// ignore close exceptions.
						}
	        		}
	        		if(out != null)
	        		{
	        			try {
							outStream.close();
						} catch (@SuppressWarnings("unused") IOException e) {
							// ignore close exceptions.
						}
	        		}
	        		if(inStream != null)
	        		{
	        			try {
							outStream.close();
						} catch (@SuppressWarnings("unused") IOException e) {
							// ignore close exceptions.
						}
	        		}
	        		if(in != null)
	        		{
	        			try {
							outStream.close();
						} catch (@SuppressWarnings("unused") IOException e) {
							// ignore close exceptions.
						}
	        		}
	        	}
	        	
	            measurementContext.setProperty(entry.getKey(), clone);
	        }
	
	
			// Process startup settings
			if(startUpDeviceSettings != null && startUpDeviceSettings.length > 0)
			{
				try
				{
					microscope.applyDeviceSettings(startUpDeviceSettings);
				}
				catch(Exception e)
				{
					throw runState.toErrorState(new MeasurementException("Could not apply measurement startup settings.", e));
				}
				// Stop if measurement got interrupted
				if(Thread.interrupted())
					throw runState.toErrorState(new InterruptedException());
			}
			// Initialize tasks and their jobs, and schedule their first execution
			for(MeasurementTaskImpl task : tasks)
			{
				try
				{
					task.initializeTask(microscope, measurementContext, this);
				}
				catch(Exception e)
				{
					throw runState.toErrorState(new MeasurementException("Could not initialize all tasks of measurement.", e));
				}
				if(Thread.interrupted())
					throw runState.toErrorState(new InterruptedException());
			}
			runState.toState(MeasurementState.INITIALIZED, "Cannot finish measurement initialization.", MeasurementState.INITIALIZING);
		}
		finally
		{
			startStopLock.unlock();
		}
		sendMessage("Finished initializing measurement.");
	}

	private void uninitializeMeasurement(Microscope microscope) throws MeasurementException, InterruptedException
	{
		startStopLock.lock();
		try
		{
			boolean alreadyUninitialized = !runState.toState(MeasurementState.UNINITIALIZING, "Cannot uninitialize measurement.", MeasurementState.STOPPED, MeasurementState.INITIALIZED, MeasurementState.PAUSED, MeasurementState.UNINITIALIZED);
			if(alreadyUninitialized)
				return;
			sendMessage("Uninitializing measurement...");
	
			scheduledTasksQueue.clear();
			pendingQueuesLock.lock();
			try
			{
				pendingTasksQueue.clear();
				pendingJobsQueue.clear();
				jobsAvailable.signalAll();
			}
			finally
			{
				pendingQueuesLock.unlock();
			}
			
			// Uninitialize tasks and their jobs
			for(MeasurementTaskImpl task : tasks)
			{
				try
				{
					task.uninitializeTask(microscope, measurementContext);
				}
				catch(Exception e)
				{
					throw runState.toErrorState(new MeasurementException("Could not uninitialize all tasks.", e));
				}
				if(Thread.interrupted())
					throw runState.toErrorState(new InterruptedException());
			}
	
			// Process shutdown settings
			if(shutDownDeviceSettings != null && shutDownDeviceSettings.length > 0)
			{
				try
				{
					microscope.applyDeviceSettings(shutDownDeviceSettings);
				}
				catch(Exception e)
				{
					throw runState.toErrorState(new MeasurementException("Could not apply all measurement shutdown device settings.", e));
				}
				// Stop if measurement got interrupted
				if(Thread.interrupted())
					throw runState.toErrorState(new InterruptedException());
			}
	
			runState.toState(MeasurementState.UNINITIALIZED, "Cannot finish uninitialization.", MeasurementState.UNINITIALIZING);
			sendMessage("Finished uninitializing measurement.");
		}
		
		finally
		{
			startStopLock.unlock();
		}
	}
	
	/**
	 * This function starts or resumes the measurement. It should only be called by {@link MeasurementManager#runMeasurements()}.
	 * 
	 * @throws InterruptedException 
	 * @throws ComponentRunningException
	 * @throws RemoteException
	 */
	void startupMeasurement(Microscope microscope) throws MeasurementException, InterruptedException
	{
		startStopLock.lock();
		try
		{
			if(runState.isShouldPause())
			{
				runState.toState(MeasurementState.INITIALIZED, "Cannot resume measurement.", MeasurementState.PAUSED, MeasurementState.QUEUED);
				runState.resume();
			}
			else
			{
				initializeMeasurement(microscope);
			}
			runMeasurement();
		}
		finally
		{
			startStopLock.unlock();
		}
	}
	/**
	 * This function runs the uninitialization of the measurement, or methods to pause it. 
	 * It should only be called by {@link MeasurementManager#runMeasurements()} after the measurement has stopped or paused.
	 * 
	 * @throws InterruptedException 
	 * @throws ComponentRunningException
	 * @throws RemoteException
	 */
	void shutdownMeasurement(Microscope microscope) throws MeasurementException, InterruptedException
	{
		long currentTime = System.currentTimeMillis();
		startStopLock.lock();
		try
		{
			if(runState.isShouldPause())
			{
				runState.toState(MeasurementState.PAUSED, "Cannot pause measurement.", MeasurementState.PAUSING);
				pauseTime = currentTime;
			}
			else
			{
				runState.toState(MeasurementState.STOPPED, "Cannot pause measurement.", MeasurementState.STOPPING);
				stopTime = currentTime;
				uninitializeMeasurement(microscope);
			}
		}
		finally
		{
			startStopLock.unlock();
		}
	}
	
	private void runMeasurement() throws MeasurementException
	{
		startStopLock.lock();
		try
		{
			runState.toState(MeasurementState.RUNNING, "Cannot start/run measurement.", MeasurementState.INITIALIZED, MeasurementState.PAUSED);
			sendMessage("Starting measurement...");
		
			if(scheduledTasksQueueExecutor != null)
				throw new MeasurementException("Measurement Runner already/still running, even though it shouldn't given the current measurement state.");
			scheduledTasksQueueExecutor = new Thread(new Runnable()
			{
				@Override
				public void run() 
				{
					runMeasurementInternally();
				}
		
			}, "Measurement Runner");
			scheduledTasksQueueExecutor.setDaemon(true);
			scheduledTasksQueueExecutor.start();
		}
		finally
		{
			startStopLock.unlock();
		}
	}
	
	private void runMeasurementInternally()
	{
		if(maxRuntime >= 0)
		{
			// schedule a special task to stop measurement.
			scheduledTasksQueue.add(new ScheduledTask(maxRuntime, null));
		}
		
		long currentTime = System.currentTimeMillis();
		if(startTime < 0)
		{
			// First run, not yet paused.
			startTime = currentTime;
			pauseDuration = 0;
			pauseTime = -1;
			stopTime = -1;
		}
		else
		{
			// We paused the measurement. Let's continue it.
			pauseDuration += currentTime - pauseTime;
			pauseTime = -1;
			stopTime = -1;
		}
		while(true)
		{
			try 
			{
				ScheduledTask scheduledTask = scheduledTasksQueue.take();
				if(scheduledTask != null)
				{
					// the null task is a special task, intended to signal that the measurement should stop.
					if(scheduledTask.task == null)
						stopMeasurement(true);
					pendingQueuesLock.lock();
					try
					{
						pendingTasksQueue.add(scheduledTask);
						jobsAvailable.signalAll();
					}
					finally
					{
						pendingQueuesLock.unlock();
					}
				}
				if(Thread.interrupted())
					throw new InterruptedException();
			} 
			catch (@SuppressWarnings("unused") InterruptedException e) 
			{
				currentTime = System.currentTimeMillis();
				// gets interrupted if it should go to pause of stop mode.
				// do some cleanup and then finish.
				
				boolean shouldPause;
				startStopLock.lock();
				try
				{
					// otherwise: should stop!
					shouldPause = runState.isShouldPause();
					try
					{
						if(shouldPause)
						{
							runState.toState(MeasurementState.PAUSING, "Cannot pause measurement.", MeasurementState.RUNNING);
							sendMessage("Pausing measurement...");
						}
						else
						{
							runState.toState(MeasurementState.STOPPING, "Cannot stop measurement.", MeasurementState.RUNNING);
							sendMessage("Stopping measurement...");
						}
					}
					catch(MeasurementException e2)
					{
						// we are probably anyways in error state, but set it again to be on the safe side...
						runState.toErrorState(e2);
					}
					
					pendingQueuesLock.lock();
					try
					{
						scheduledTasksQueueExecutor = null;
						jobsAvailable.signalAll();
					}
					finally
					{
						pendingQueuesLock.unlock();
					}
				}
				finally
				{
					startStopLock.unlock();
				}
				if(shouldPause)
					sendMessage("Paused measurement.");
				else
					sendMessage("Stopped measurement.");
				return;
			}
		}
	}
	
	public JobExecutionQueueElement waitForNextJob() throws InterruptedException
	{
		pendingQueuesLock.lock();
		try
		{
			while(true)
			{
				if(!runState.isShouldBlockQueue())
				{
					JobExecutionQueueElement job= pendingJobsQueue.poll();
					if(job != null)
						return job;
					ScheduledTask nextScheduledTask = pendingTasksQueue.poll();
					if(nextScheduledTask != null)
					{
						pendingJobsQueue.addAll(nextScheduledTask.task.executeTask());
						job= pendingJobsQueue.poll();
						if(job != null)
							return job;
					}
				}
				// if the thread has finished, no new jobs will arrive... 
				if(scheduledTasksQueueExecutor == null)
				{
					return null;
				}
				jobsAvailable.await();
			}
		}
		finally
		{
			pendingQueuesLock.unlock();
		}
	}

	void stopMeasurement(boolean processJobQueue)
	{
		runState.stop(!processJobQueue);
		Thread scheduledTasksQueueExecutor = this.scheduledTasksQueueExecutor;
		if(scheduledTasksQueueExecutor != null)
			scheduledTasksQueueExecutor.interrupt();
	}
	
	void pauseMeasurement()
	{
		runState.pause();
		Thread scheduledTasksQueueExecutor = this.scheduledTasksQueueExecutor;
		if(scheduledTasksQueueExecutor != null)
			scheduledTasksQueueExecutor.interrupt();
	}
	
	/**
	 * Calls {@link MeasurementListener#measurementError(Exception)} on all measurement listeners.
	 */
	private void notifyMeasurementError(Exception e)
    {
		synchronized(measurementListeners)
		{
			for (Iterator<MeasurementListener> iterator = measurementListeners.iterator(); iterator.hasNext();) 
			{
				try
	            {
	            	iterator.next().measurementError(e);
	            } 
				catch (RemoteException e1)
	            {
	                ServerSystem.err.println("Measurement listener not answering. Removing him from the queue.", e1);
	                iterator.remove();
	            }
	        }
		}
    }
	/**
	 * Calls {@link MeasurementListener#measurementStateChanged(MeasurementState, MeasurementState)} on all measurement listeners.
	 */
	private void notifyMeasurementStateChanged(MeasurementState oldState, MeasurementState newState)
    {
		synchronized(measurementListeners)
		{
			for (Iterator<MeasurementListener> iterator = measurementListeners.iterator(); iterator.hasNext();) 
			{
				try
	            {
	            	iterator.next().measurementStateChanged(oldState, newState);
	            } 
				catch (RemoteException e)
	            {
	                ServerSystem.err.println("Measurement listener not answering. Removing him from the queue.", e);
	                iterator.remove();
	            }
	        }
		}
    }
	
	/**
	 * Calls {@link MeasurementListener#measurementStructureModified()} on all measurement listeners.
	 */
	void notifyMeasurementStructureModified()
    {
		synchronized(measurementListeners)
		{
			for (Iterator<MeasurementListener> iterator = measurementListeners.iterator(); iterator.hasNext();) 
			{
				try
	            {
	            	iterator.next().measurementStructureModified();
	            } 
				catch (RemoteException e)
	            {
	                ServerSystem.err.println("Measurement listener not answering. Removing him from the queue.", e);
	                iterator.remove();
	            }
	        }
		}
    }
	

	void setStartupDeviceSettings(DeviceSetting[] settings) throws ComponentRunningException
	{
		
		DeviceSetting[] copy = new DeviceSetting[settings.length];
		for(int i=0; i<settings.length; i++)
		{
			copy[i] = new DeviceSetting(settings[i]);
		}
		
		synchronized(runState)
		{
			runState.assertEditable();
			startUpDeviceSettings = copy;
		}
	}

	void addStartupDeviceSetting(DeviceSetting setting) throws ComponentRunningException
	{
		DeviceSetting[] newSettings = new DeviceSetting[startUpDeviceSettings.length + 1];
		System.arraycopy(startUpDeviceSettings, 0, newSettings, 0, startUpDeviceSettings.length);
		newSettings[startUpDeviceSettings.length] = new DeviceSetting(setting);
		
		synchronized(runState)
		{
			runState.assertEditable();
			startUpDeviceSettings = newSettings;
		}
	}

	void setFinishDeviceSettings(DeviceSetting[] settings) throws ComponentRunningException
	{
		DeviceSetting[] copy = new DeviceSetting[settings.length];
		for(int i=0; i<settings.length; i++)
		{
			copy[i] = new DeviceSetting(settings[i]);
		}
		synchronized(runState)
		{
			runState.assertEditable();
			shutDownDeviceSettings = copy;
		}
	}

	void addFinishDeviceSetting(DeviceSetting setting) throws ComponentRunningException
	{
		DeviceSetting[] newSettings = new DeviceSetting[shutDownDeviceSettings.length + 1];
		System.arraycopy(shutDownDeviceSettings, 0, newSettings, 0, shutDownDeviceSettings.length);
		newSettings[shutDownDeviceSettings.length] = new DeviceSetting(setting);
		synchronized(runState)
		{
			runState.assertEditable();
			shutDownDeviceSettings = newSettings;
		}
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name) throws ComponentRunningException
	{
		if(name == null)
			throw new NullPointerException();
		synchronized(runState)
		{
			runState.assertEditable();
			this.name = name;
		}
	}

	public void removeMeasurementListener(MeasurementListener listener)
	{
		synchronized(measurementListeners)
		{
			measurementListeners.remove(listener);
		}
	}

	public void addMeasurementListener(MeasurementListener listener)
	{
		synchronized(measurementListeners)
		{
			measurementListeners.add(listener);
		}
	}

	public void setMaxRuntime(long measurementRuntime) throws ComponentRunningException
	{
		synchronized(runState)
		{
			runState.assertEditable();
			this.maxRuntime = measurementRuntime;
		}
	}

	public long getMaxRuntime()
	{
		return maxRuntime;
	}

	public void setLockMicroscopeWhileRunning(boolean lock) throws ComponentRunningException
	{
		synchronized(runState)
		{
			runState.assertEditable();
			lockMicroscopeWhileRunning = lock;
		}
	}

	public boolean isLockMicroscopeWhileRunning()
	{
		return lockMicroscopeWhileRunning;
	}

	public MeasurementState getState()
	{
		return runState.getState();
	}

	Task addTask(long period, boolean fixedTimes, long startTime) throws RemoteException, ComponentRunningException
	{
		synchronized(runState)
		{
			runState.assertEditable();
			return addTask(period, fixedTimes, startTime, -1);
		}
	}

	Task addTask(long period, boolean fixedTimes, long startTime, long numExecutions) throws RemoteException, ComponentRunningException
	{
		MeasurementTaskImpl measurementTask = new MeasurementTaskImpl(period, fixedTimes, startTime, numExecutions);
		synchronized(runState)
		{
			runState.assertEditable();
			return addTask(measurementTask);
		}
	}

	Task addMultiplePeriodTask(long[] periods, long startTime) throws RemoteException, ComponentRunningException
	{
		synchronized(runState)
		{
			runState.assertEditable();
			return addMultiplePeriodTask(periods, startTime, -1);
		}
	}

	Task addMultiplePeriodTask(long[] periods, long startTime, long numExecutions) throws RemoteException, ComponentRunningException
	{
		MeasurementTaskImpl measurementTask = new MeasurementTaskImpl(periods, startTime, numExecutions);
		
		synchronized(runState)
		{
			runState.assertEditable();
			return addTask(measurementTask);
		}
	}

	public void setInitialMeasurementContextProperty(String identifier, Serializable property) throws ComponentRunningException
    {
		synchronized(runState)
		{
			runState.assertEditable();
			initialMeasurementContextProperties.put(identifier, property);
		}
    }

	@Override
	public void scheduleTask(MeasurementTaskImpl task, long runtime) 
	{
		scheduledTasksQueue.put(new ScheduledTask(runtime, task));
		
	}
	
	public void setInitialRuntime(long initialRuntime) throws ComponentRunningException, IllegalArgumentException
	{
		if(initialRuntime < 0)
			throw new IllegalArgumentException("Initial runtime must be greater or equal to zero.");
		synchronized(runState)
		{
			runState.assertEditable();
			this.initialRuntime = initialRuntime;
		}
	}
	public long getInitialRuntime()
	{
		return initialRuntime;
	}

	@Override
	public void taskFinished() 
	{
		for(MeasurementTaskImpl task : tasks)
		{
			if(!task.isFinished())
				return;
		}
		stopMeasurement(true);
	}

	public long getPauseTime() {
		return pauseTime;
	}

	public long getPauseDuration() {
		return pauseDuration;
	}

	public MeasurementMetadataImpl getMetadata() {
		return metadata;
	}
}
