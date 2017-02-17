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

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobException;
import org.youscope.common.job.JobListener;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.task.Task;
import org.youscope.common.task.TaskException;
import org.youscope.common.task.TaskListener;

/**
 * @author Moritz Lang
 * 
 */
class MeasurementTaskImpl extends UnicastRemoteObject implements Task
{

	/**
	 * Serial Version UID.
	 */
	private static final long					serialVersionUID		= -8056744778146696141L;

	private final long maxExecutionNumber;
	
	// All times in milleseconds
	private final boolean fixedTimes;
	private final long startTime;
	private final long[] periods;
	
	private volatile long initialExecutionNumber = 0;
	private volatile long nextExecutionNumber = initialExecutionNumber;

	private final ArrayList<Job> jobs = new ArrayList<Job>();

	private final LastJobFinishListener lastJobFinishListener = new LastJobFinishListener();
	private final FirstJobStartedListener firstJobStartedListener = new FirstJobStartedListener();
	
	private final ArrayList<TaskListener>		taskListeners			= new ArrayList<TaskListener>();
	
	private final TaskStateManager stateManager = new TaskStateManager();
	
	private volatile TaskExecutor taskExecutor = null;
	/**
	 * Current state of the task.
	 * @author Moritz Lang
	 *
	 */
	private static enum TaskState
	{
		READY,
		INITIALIZING,
		RUNNING,
		UNINITIALIZING
	}
	static interface TaskExecutor
	{
		/**
		 * Returns the current measurement runtime.
		 * @return runtime in ms.
		 */
		long getRuntime();
		/**
		 * Schedules a task.
		 * @param task task to schedule
		 * @param runtime runtime when to schedule the task, in ms.
		 */
		void scheduleTask(MeasurementTaskImpl task, long runtime);
		/**
		 * Tells the task executor that this task is finished, i.e. will not schedule itself anymore, and is not scheduled anymore. 
		 */
		void taskFinished();
	}
	private static final class TaskStateManager
	{
		
		private TaskState taskState = TaskState.READY;
				
		synchronized void assertEditable() throws ComponentRunningException
		{
			if(taskState != TaskState.READY)
				throw new ComponentRunningException();
		}
		
		/**
		 * Sets the state of the task, if the current state is in one of the allowed states.
		 * If not, an error is thrown and the state is not changed.
		 * @param state New state of the measurement.
		 * @return the old state of the task, before changing.
		 */
		TaskState toState(TaskState newState, String errorMessage, TaskState... allowedStates) throws TaskException
		{
			TaskState currentState;
			synchronized(this)
			{
				currentState = this.taskState;
				for(TaskState allowedState : allowedStates)
				{
					if(currentState == allowedState)
					{
						this.taskState = newState;
						return currentState;
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
			throw new TaskException(message.toString());
		}
	}
	
	/**
	 * @throws RemoteException
	 */
	MeasurementTaskImpl(long period, boolean fixedTimes, long startTime, long numExecutions) throws RemoteException
	{
		this.periods = new long[]{period};
		this.fixedTimes = fixedTimes;
		this.startTime = startTime;
		this.maxExecutionNumber = numExecutions;
	}

	MeasurementTaskImpl(long period, boolean fixedTimes, long startTime) throws RemoteException
	{
		this(period, fixedTimes, startTime, -1);
	}

	MeasurementTaskImpl(long[] periods, long startTime) throws RemoteException
	{
		this(periods, startTime, -1);
	}

	MeasurementTaskImpl(long[] periods, long startTime, long numExecutions) throws RemoteException
	{
		this.startTime = startTime;
		this.periods = periods;
		fixedTimes = true;
		this.maxExecutionNumber = numExecutions;
	}
	
	boolean isFinished()
	{
		return maxExecutionNumber >= 0 && nextExecutionNumber >= maxExecutionNumber;
	}
	
	/**
	 * Schedules the task for execution.
	 */
	private void scheduleTask()
	{
		if(maxExecutionNumber>= 0 && nextExecutionNumber >= maxExecutionNumber)
		{
			taskExecutor.taskFinished();
			return;
		}
		long currentRuntime = taskExecutor.getRuntime();
		long majorLoop = nextExecutionNumber / periods.length;
		int minorLoop = (int) (nextExecutionNumber % periods.length);
		long scheduleTime;
		if(fixedTimes)
		{
			scheduleTime = startTime;
			for(int i=0; i<periods.length; i++)
			{
				scheduleTime += (majorLoop+(i<minorLoop ? 1 : 0))*periods[i];
			}
		}
		// two cases for fixed time = false.
		else if(nextExecutionNumber == 0)
			scheduleTime = currentRuntime + startTime;
		else 
			scheduleTime = currentRuntime + periods[(minorLoop-1+periods.length) % periods.length];
		taskExecutor.scheduleTask(this, scheduleTime);
	}
	/**
	 * Returns the jobs which should be executed, and increases the execution number.
	 * @return Jobs which should be executed.
	 */
	Collection<JobExecutionQueueElement> executeTask()
	{
		ArrayList<JobExecutionQueueElement> result = new ArrayList<>(jobs.size());
		for(Job job : jobs)
		{
			result.add(new JobExecutionQueueElement(job, nextExecutionNumber));
		}
		nextExecutionNumber++;
		return result;
	}
	
	/**
	 * Listener which gets added to the last job of the task to determine when the task has finished execution.
	 * @author Moritz Lang
	 */
	private final class LastJobFinishListener extends UnicastRemoteObject implements JobListener
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -6259221333656969538L;

		/**
		 * @throws RemoteException
		 */
		protected LastJobFinishListener() throws RemoteException
		{
			super();
		}

		@Override
		public void jobFinished(ExecutionInformation executionInformation) throws RemoteException
		{
			scheduleTask();
			notifyTaskFinished(executionInformation.getEvaluationNumber());
		}

		@Override
		public void jobInitialized() throws RemoteException
		{
			// Do nothing.
		}

		@Override
		public void jobUninitialized() throws RemoteException
		{
			// Do nothing.
		}

		@Override
		public void jobStarted(ExecutionInformation executionInformation) throws RemoteException
		{
			// Do nothing.
		}
	}
	
	/**
	 * Listener which gets added to the first job of the task to determine when the task started execution.
	 * @author Moritz Lang
	 */
	private final class FirstJobStartedListener extends UnicastRemoteObject implements JobListener
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -6159221333656969538L;

		/**
		 * @throws RemoteException
		 */
		protected FirstJobStartedListener() throws RemoteException
		{
			super();
		}

		@Override
		public void jobFinished(ExecutionInformation executionInformation) throws RemoteException
		{
			// do nothing.
		}

		@Override
		public void jobInitialized() throws RemoteException
		{
			// Do nothing.
		}

		@Override
		public void jobUninitialized() throws RemoteException
		{
			// Do nothing.
		}

		@Override
		public void jobStarted(ExecutionInformation executionInformation) throws RemoteException
		{
			notifyTaskStarted(executionInformation.getEvaluationNumber());
		}
	}
		
	@Override
	public void addTaskListener(TaskListener listener)
	{
		synchronized(taskListeners)
		{
			taskListeners.add(listener);
		}
	}

	@Override
	public void removeTaskListener(TaskListener listener)
	{
		synchronized(taskListeners)
		{
			taskListeners.remove(listener);
		}
	}
	
	/**
	 * Calls {@link TaskListener#taskStarted(long)} on all task listeners.
	 * @param executionNumber
	 */
	private void notifyTaskStarted(long executionNumber)
	{
		synchronized(taskListeners)
		{
			for (Iterator<TaskListener> iterator = taskListeners.iterator(); iterator.hasNext();) 
			{
				try
	            {
	            	iterator.next().taskStarted(executionNumber);
	            } 
				catch (RemoteException e)
	            {
	                ServerSystem.err.println("Task listener not answering. Removing him from the queue.", e);
	                iterator.remove();
	            }
	        }
		}
	}	
	
	/**
	 * Calls {@link TaskListener#taskFinished(long)} on all task listeners.
	 * @param executionNumber
	 */
	private void notifyTaskFinished(long executionNumber)
	{
		synchronized(taskListeners)
		{
			for (Iterator<TaskListener> iterator = taskListeners.iterator(); iterator.hasNext();) 
			{
				try
	            {
	            	iterator.next().taskFinished(executionNumber);
	            } 
				catch (RemoteException e)
	            {
	                ServerSystem.err.println("Task listener not answering. Removing him from the queue.", e);
	                iterator.remove();
	            }
	        }
		}
	}	

	/**
	 * Schould only be called by {@link MeasurementImpl}.
	 * @param microscope
	 * @param measurementContext
	 * @throws TaskException
	 * @throws InterruptedException
	 */
	void initializeTask(Microscope microscope, MeasurementContext measurementContext, TaskExecutor taskExecutor) throws TaskException, InterruptedException
	{
		stateManager.toState(TaskState.INITIALIZING, "Cannot initialize task,", TaskState.READY);
		nextExecutionNumber = initialExecutionNumber;
		this.taskExecutor = taskExecutor;
		scheduleTask();
		// Initialize jobs
		for(Job job : jobs)
		{
			try {
				job.initializeJob(microscope, measurementContext);
			} 
			catch (JobException | RemoteException e) 
			{
				throw new TaskException("Failed to initialize a job of the task.", e);
			}
		}
		if(jobs.size() > 0)
		{
			// Add listeners to the first and last job in the task to determine when task execution started and finished.
			try 
			{
				jobs.get(0).addJobListener(firstJobStartedListener);
			} 
			catch (Exception e) 
			{
				throw new TaskException("Could not add listener to first job of the task.", e);
			}
			try 
			{
				jobs.get(jobs.size()-1).addJobListener(lastJobFinishListener);
			} 
			catch (Exception e) 
			{
				throw new TaskException("Could not add listener to last job of the task.", e);
			}
		}
		stateManager.toState(TaskState.RUNNING, "Cannot finish task initialization,", TaskState.INITIALIZING);
	}

	void uninitializeTask(Microscope microscope, MeasurementContext measurementContext) throws TaskException, InterruptedException
	{
		TaskState oldState = stateManager.toState(TaskState.UNINITIALIZING, "Cannot finish task initialization,", TaskState.RUNNING, TaskState.READY);
		if(oldState == TaskState.READY)
			return;
		this.taskExecutor = null;
		// Uninitialize jobs
		for(Job job : jobs)
		{
			try {
				job.uninitializeJob(microscope, measurementContext);
			} 
			catch (JobException | RemoteException e) 
			{
				throw new TaskException("Failed to uninitialize a job of the task.", e);
			}
		}
		if(jobs.size() > 0)
		{
			// Remove listeners from the first and last job in the task added to determine when task execution started and finished.
			try 
			{
				jobs.get(0).removeJobListener(firstJobStartedListener);
			} 
			catch (Exception e) 
			{
				throw new TaskException("Could not remove listener from first job of the task.", e);
			}
			try 
			{
				jobs.get(jobs.size()-1).removeJobListener(lastJobFinishListener);
			} 
			catch (Exception e) 
			{
				throw new TaskException("Could not remove listener from last job of the task.", e);
			}
		}
		stateManager.toState(TaskState.READY, "Cannot finish task initialization,", TaskState.UNINITIALIZING);
	}	

	@Override
	public void addJob(Job job) throws ComponentRunningException
	{
		synchronized(stateManager)
		{
			stateManager.assertEditable();
			jobs.add(job);
		}
	}

	@Override
	public void removeJob(int jobIndex) throws ComponentRunningException, IndexOutOfBoundsException
	{
		synchronized(stateManager)
		{
			stateManager.assertEditable();
			jobs.remove(jobIndex);
		}
	}

	@Override
	public void clearJobs() throws ComponentRunningException
	{
		synchronized(stateManager)
		{
			stateManager.assertEditable();
			jobs.clear();
		}
	}

	@Override
	public Job[] getJobs()
	{
		synchronized(stateManager)
		{
			return jobs.toArray(new Job[jobs.size()]);
		}
	}

	@Override
	public void insertJob(Job job, int jobIndex)
			throws ComponentRunningException, IndexOutOfBoundsException 
	{
		synchronized(stateManager)
		{
			stateManager.assertEditable();
			jobs.add(jobIndex, job);
		}
	}

	@Override
	public int getNumJobs() 
	{
		synchronized(stateManager)
		{
			return jobs.size();
		}
	}

	@Override
	public Job getJob(int jobIndex) throws IndexOutOfBoundsException 
	{
		synchronized(stateManager)
		{
			return jobs.get(jobIndex);
		}
	}

	@Override
	public void setInitialExecutionNumber(long executionNumber) throws ComponentRunningException, IllegalArgumentException 
	{
		if(executionNumber < 0)
			throw new IllegalArgumentException("Initial execution number must be greater or equal to zero.");
		synchronized(stateManager)
		{
			stateManager.assertEditable();
			initialExecutionNumber = executionNumber;
		}
	}

	@Override
	public long getInitialExecutionNumber()
	{
		return initialExecutionNumber;
	}
}
