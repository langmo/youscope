/**
 * 
 */
package org.youscope.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

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
import org.youscope.common.task.TaskState;

/**
 * @author Moritz Lang
 */
class MeasurementTaskImpl extends UnicastRemoteObject implements Task
{

	/**
	 * Serial Version UID.
	 */
	private static final long					serialVersionUID		= -8056744778146696141L;

	private final int									numExecutions;

	private final boolean								fixedTimes;

	private final int[]								startTimes;

	private final int									period;

	private final ArrayList<Job>			jobs					= new ArrayList<Job>();

	private volatile Timer timer = null;
	private volatile MeasurementJobQueue measurementJobQueue = null;
	private final LastJobFinishListener lastJobFinishListener = new LastJobFinishListener();
	private int	evaluationNo = 0;
	private volatile TaskState taskState = TaskState.READY;
	private final ArrayList<TaskListener>		taskListeners			= new ArrayList<TaskListener>();
	

	/**
	 * @throws RemoteException
	 */
	MeasurementTaskImpl(int period, boolean fixedTimes, int startTime, int numExecutions) throws RemoteException
	{
		this.period = period;
		this.fixedTimes = fixedTimes;
		this.startTimes = new int[] {startTime};
		this.numExecutions = numExecutions;
	}

	MeasurementTaskImpl(int period, boolean fixedTimes, int startTime) throws RemoteException
	{
		this(period, fixedTimes, startTime, -1);
	}

	MeasurementTaskImpl(int[] periods, int breakTime, int startTime) throws RemoteException
	{
		this(periods, breakTime, startTime, -1);
	}

	MeasurementTaskImpl(int[] periods, int breakTime, int startTime, int numExecutions) throws RemoteException
	{
		// Calculate main period (when sequence is run through totally).
		int totalPeriod = breakTime;
		for(int aPeriod : periods)
		{
			totalPeriod += aPeriod;
		}
		this.period = totalPeriod;
		Vector<Integer> startTimes = new Vector<Integer>();
		int start = startTime;
		for(int aPeriod : periods)
		{
			startTimes.add(start);
			start += aPeriod;
		}
		// Add a final evaluation if and only if the break time is larger zero.
		if(breakTime > 0)
			startTimes.add(start);
		this.startTimes = new int[startTimes.size()];
		for(int i = 0; i < startTimes.size(); i++)
		{
			this.startTimes[i] = startTimes.elementAt(i);
		}

		fixedTimes = true;
		this.numExecutions = numExecutions;
	}
	
	@Override
	public TaskState getState()
	{
		return taskState;
	}
	
	private class TaskTimerTask extends TimerTask
	{
		@Override
		public void run()
		{
			synchronized(MeasurementTaskImpl.this)
			{
				// If task did already stop, or did not start, yet, or is in error state, ignore commands from timers...
				if(taskState != TaskState.RUNNING && taskState != TaskState.PAUSED)
					return;
				for(Job job : jobs)
				{
					measurementJobQueue.queueJob(new JobExecutionQueueElement(job, evaluationNo));
				}
	
				evaluationNo++;
				if(numExecutions >= 0 && evaluationNo >= numExecutions)
				{
					try {
						stopTask();
					} 
					catch (TaskException e) 
					{
						// not too bad: the task is still stopped, however, cannot be started again...
						ServerSystem.err.println("Error while stopping task since its execution limit was reached.", e);
					}
				}
			}

			// Inform listeners that task executed.
			notifyTaskExecuted(evaluationNo);
		}
	}
	/**
	 * Listener which gets added for non-fixed evaluation tasks to the last job of the task to determine when the job was finished execution and, thus, when to schedule it again.
	 * @author Moritz Lang
	 *
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
			// Called when fixedTimes = false when last job of task was executed.
			synchronized(MeasurementTaskImpl.this)
			{
				if(taskState != TaskState.RUNNING)
					return;
				timer.schedule(new TaskTimerTask(), period);
			}
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
	
	synchronized void startTask(MeasurementJobQueue measurementJobQueue) throws TaskException
	{
		assertCorrectState("Cannot start task.", TaskState.INITIALIZED);
		evaluationNo = 0;
		this.measurementJobQueue = measurementJobQueue;
		
		if(getNumJobs() <= 0)
		{
			toState(TaskState.RUNNING);
			ServerSystem.out.println("Task will finish immedeately since it is empty.");
			toState(TaskState.STOPPED);
			return;
		}
		timer = new Timer("Measurement Task", true);
			
		if(fixedTimes)
		{
			for(int i = 0; i < startTimes.length; i++)
			{
				timer.scheduleAtFixedRate(new TaskTimerTask(), startTimes[i], period);
			}
		}
		else
		{
			// Add a finish listener to the last job in the task, which schedules the next execution of the task when the last job of the task is finished.
			try {
				getJob(getNumJobs()-1).addJobListener(lastJobFinishListener);
			} 
			catch (Exception e) 
			{
				throw toErrorState(new TaskException("Tried to start task, but could not add listener to last job of task to signal when job execution finished, and, thus, when to schedule the next execution of the task.", e));
			} 
			timer.schedule(new TaskTimerTask(), startTimes[0]);
		}
		toState(TaskState.RUNNING);
	}
	
	synchronized void stopTask() throws TaskException
	{
		assertCorrectState("Cannot stop task.", TaskState.STOPPED, TaskState.INITIALIZED, TaskState.RUNNING, TaskState.PAUSED);
		if(taskState == TaskState.STOPPED)
		{
			// do nothing, already finished!
			return;
		}
		else if(taskState == TaskState.INITIALIZED)
		{
			// finish the task without starting it. Well, just switch states...
			toState(TaskState.STOPPED);
			return;
		}
		else
		{
			if(timer!=null)
			{
				timer.cancel();
				timer = null;
			}
			if(!fixedTimes && getNumJobs() > 0)
			{
				// remove the finish listener from last job.
				try {
					getJob(getNumJobs()-1).removeJobListener(lastJobFinishListener);
				} 
				catch (Exception e) 
				{
					throw toErrorState(new TaskException("Tried to stop task, but could not remove listener from last job of task signaling when job execution finished, and, thus, when to schedule the next execution of the task.", e));
				} 
			}
			toState(TaskState.STOPPED);
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
	 * Calls {@link TaskListener#taskError(Exception)} on all task listeners.
	 */
	private void notifyTaskError(Exception e)
    {
		synchronized(taskListeners)
		{
			for (Iterator<TaskListener> iterator = taskListeners.iterator(); iterator.hasNext();) 
			{
				try
	            {
	            	iterator.next().taskError(e);
	            } 
				catch (RemoteException e1)
	            {
	                ServerSystem.err.println("Task listener not answering. Removing him from the queue.", e1);
	                iterator.remove();
	            }
	        }
		}
    }
	/**
	 * Calls {@link TaskListener#taskStateChanged(TaskState, TaskState)} on all task listeners.
	 */
	private void notifyTaskStateChanged(TaskState oldState, TaskState newState)
    {
		synchronized(taskListeners)
		{
			for (Iterator<TaskListener> iterator = taskListeners.iterator(); iterator.hasNext();) 
			{
				try
	            {
	            	iterator.next().taskStateChanged(oldState, newState);
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
	 * Calls {@link TaskListener#taskExecuted(int)} on all task listeners.
	 * @param executionNumber
	 */
	private void notifyTaskExecuted(int executionNumber)
	{
		synchronized(taskListeners)
		{
			for (Iterator<TaskListener> iterator = taskListeners.iterator(); iterator.hasNext();) 
			{
				try
	            {
	            	iterator.next().taskExecuted(executionNumber);
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
	 * Sets the state of the task and notifies all listeners.
	 * @param state New state of the task.
	 */
	private void toState(TaskState state)
	{
		TaskState oldState;
		synchronized(this)
		{
			oldState = this.taskState;
			this.taskState = state;
		}
		notifyTaskStateChanged(oldState, state);
	}
	/**
	 * Sets the task state to {@link TaskState#ERROR}, notifies all listeners of the state change, notifies all listeners of the error, and returns the exception (e.g. to be used in throws).
	 * @param e Exception which occurred.
	 * @return The exception gotten as a parameter.
	 */
	private <T extends Exception> T toErrorState(T e)
	{
		toState(TaskState.ERROR);
		notifyTaskError(e);
		return e;
	}
	
	private void assertCorrectState(String errorMessage, TaskState... allowedStates) throws TaskException
	{
		TaskState state = this.taskState;
		for(TaskState allowedState : allowedStates)
		{
			if(state == allowedState)
				return;
		}
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
		message.append(". Current state: "+state.toString()+".");
		throw new TaskException(message.toString());
	}
	
	private void assertEditable() throws ComponentRunningException
	{
		TaskState state = taskState;
		if(state != TaskState.READY && state != TaskState.UNINITIALIZED)
			throw new ComponentRunningException();
	}

	synchronized void initializeTask(Microscope microscope, MeasurementContext measurementContext) throws TaskException, InterruptedException
	{
		assertCorrectState("Cannot initialize task.", TaskState.READY, TaskState.UNINITIALIZED);
		toState(TaskState.INITIALIZING);
		// Initialize jobs
		for(Job job : jobs)
		{
			try {
				job.initializeJob(microscope, measurementContext);
			} 
			catch (JobException | RemoteException e) 
			{
				throw toErrorState(new TaskException("Failed to initialize a job of the task.", e));
			}
		}
		toState(TaskState.INITIALIZED);
	}

	synchronized void uninitializeTask(Microscope microscope, MeasurementContext measurementContext) throws TaskException, InterruptedException
	{
		assertCorrectState("Cannot uninitialize task", TaskState.INITIALIZED, TaskState.READY, TaskState.STOPPED, TaskState.UNINITIALIZED);
		if(taskState == TaskState.READY || taskState == TaskState.UNINITIALIZED)
			return;
		toState(TaskState.UNINITIALIZING);
		// Uninitialize jobs
		for(Job job : jobs)
		{
			try {
				job.uninitializeJob(microscope, measurementContext);
			} 
			catch (JobException | RemoteException e) 
			{
				throw toErrorState(new TaskException("Failed to uninitialize a job of the task.", e));
			}
		}
		toState(TaskState.UNINITIALIZED);
	}	

	@Override
	public synchronized void addJob(Job job) throws ComponentRunningException
	{
		assertEditable();
		jobs.add(job);
	}

	@Override
	public synchronized void removeJob(int jobIndex) throws ComponentRunningException, IndexOutOfBoundsException
	{
		assertEditable();
		jobs.remove(jobIndex);
	}

	@Override
	public synchronized void clearJobs() throws ComponentRunningException
	{
		assertEditable();
		jobs.clear();
	}

	@Override
	public Job[] getJobs()
	{
		ArrayList<?> jobs = this.jobs;
		return jobs.toArray(new Job[jobs.size()]);
	}

	@Override
	public synchronized void insertJob(Job job, int jobIndex)
			throws ComponentRunningException, IndexOutOfBoundsException {
		assertEditable();
		jobs.add(jobIndex, job);
	}

	@Override
	public int getNumJobs() 
	{
		return jobs.size();
	}

	@Override
	public Job getJob(int jobIndex) throws IndexOutOfBoundsException {
		return jobs.get(jobIndex);
	}
}
