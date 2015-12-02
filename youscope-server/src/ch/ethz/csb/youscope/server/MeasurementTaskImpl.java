/**
 * 
 */
package ch.ethz.csb.youscope.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import ch.ethz.csb.youscope.shared.measurement.ExecutionInformation;
import ch.ethz.csb.youscope.shared.measurement.MeasurementContext;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.measurement.job.JobException;
import ch.ethz.csb.youscope.shared.measurement.job.JobListener;
import ch.ethz.csb.youscope.shared.measurement.task.MeasurementTask;
import ch.ethz.csb.youscope.shared.measurement.task.TaskListener;
import ch.ethz.csb.youscope.shared.microscope.Microscope;

/**
 * @author langmo
 */
class MeasurementTaskImpl extends UnicastRemoteObject implements MeasurementTask
{

	/**
	 * Serial Version UID.
	 */
	private static final long					serialVersionUID		= -8056744778146696141L;

	private Timer								timer					= new Timer();
	public Date									measurementStartTime	= null;

	private int									evaluationNo			= 0;

	private int									numExecutions			= -1;

	private volatile MeasurementControlListener	controlListener			= null;

	private final ArrayList<TaskListener>		taskListeners			= new ArrayList<TaskListener>();

	private boolean								fixedTimes;

	private int[]								startTimes;

	private int									period;

	private final ArrayList<Job>			jobs					= new ArrayList<Job>();

	private volatile boolean					isRunning				= false;

	private final class JobFinishListener extends UnicastRemoteObject implements JobListener
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -6259221333656969538L;

		/**
		 * @throws RemoteException
		 */
		protected JobFinishListener() throws RemoteException
		{
			super();
		}

		@Override
		public void jobFinished(ExecutionInformation executionInformation) throws RemoteException
		{
			synchronized(MeasurementTaskImpl.this)
			{
				if(!isRunning())
					return;
				timer.schedule(new ExecuteJobsTimerTask(), period);
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

	private final JobListener	jobFinishListener	= new JobFinishListener();

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
		period = breakTime;
		for(int aPeriod : periods)
		{
			period += aPeriod;
		}
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

	private void assertRunning() throws MeasurementRunningException
	{
		if(isRunning())
			throw new MeasurementRunningException();
	}

	@Override
	public boolean isRunning()
	{
		return isRunning;
	}

	private synchronized void setRunning(boolean isRunning)
	{
		this.isRunning = isRunning;
	}

	private class ExecuteJobsTimerTask extends TimerTask
	{
		@Override
		public void run()
		{
			executeJobs();
		}
	}

	public synchronized void initializeTask(Microscope microscope, MeasurementContext measurementContext) throws MeasurementRunningException, JobException, RemoteException, InterruptedException
	{
		assertRunning();
		setRunning(true);

		evaluationNo = 0;

		// Initialize jobs
		for(Job job : jobs)
		{
			job.initializeJob(microscope, measurementContext);
		}

		if(!fixedTimes && jobs.size() > 0)
		{
			// Add a finish listener to the last task, so that this class'
			// actionPerformed method
			// gets called
			// when execution of all jobs of this task is completed.
			jobs.get(jobs.size() - 1).addJobListener(jobFinishListener);
		}
	}

	public synchronized void uninitializeTask(Microscope microscope, MeasurementContext measurementContext) throws RemoteException, JobException, InterruptedException
	{
		setRunning(false);
		if(!fixedTimes && jobs.size() > 0)
		{
			// Add a finish listener to the last task, so that this class'
			// actionPerformed method
			// gets called
			// when execution of all jobs of this task is completed.
			jobs.get(jobs.size() - 1).removeJobListener(jobFinishListener);
		}
		for(Job job : jobs)
		{
			job.uninitializeJob(microscope, measurementContext);
		}
	}

	public synchronized void startTask(MeasurementControlListener controlListener, Date measurementStartTime)
	{
		this.measurementStartTime = measurementStartTime;
		if(jobs.size() <= 0)
		{
			// Task should execute nothing, so we don't even start it.
			// We however inform the listeners in the right order.
			taskStarted();
			ServerSystem.out.println("Task was finished immedeately since it was empty.");
			taskFinished();
			return;
		}

		timer = new Timer("Measurement Task", true);
		this.controlListener = controlListener;

		if(fixedTimes)
		{
			for(int i = 0; i < startTimes.length; i++)
			{
				timer.scheduleAtFixedRate(new ExecuteJobsTimerTask(), startTimes[i], period);
			}
		}
		else
		{
			timer.schedule(new ExecuteJobsTimerTask(), startTimes[0]);
		}

		// Inform listeners that task started.
		taskStarted();
	}

	private void taskStarted()
	{
		synchronized(taskListeners)
		{
			for(int i = 0; i < taskListeners.size(); i++)
			{
				try
				{
					taskListeners.get(i).taskStarted();
				}
				catch(RemoteException e)
				{
					// Remove task listener and proceed.
					taskListeners.remove(i);
					i--;
					ServerSystem.err.println("Removed task listener since it was not reacting.", e);
				}
			}
		}
	}

	private void taskEvaluated(int submissionNumber)
	{
		synchronized(taskListeners)
		{
			for(int i = 0; i < taskListeners.size(); i++)
			{
				try
				{
					taskListeners.get(i).jobsSubmitted(submissionNumber);
				}
				catch(RemoteException e)
				{
					// Remove task listener and proceed.
					taskListeners.remove(i);
					i--;
					ServerSystem.err.println("Removed task listener since it was not reacting.", e);
				}
			}
		}
	}

	private void taskFinished()
	{
		synchronized(taskListeners)
		{
			for(int i = 0; i < taskListeners.size(); i++)
			{
				try
				{
					taskListeners.get(i).taskFinished();
				}
				catch(RemoteException e)
				{
					// Remove task listener and proceed.
					taskListeners.remove(i);
					i--;
					ServerSystem.err.println("Removed task listener since it was not reacting.", e);
				}
			}
		}
	}

	public synchronized void cancelTask()
	{
		timer.cancel();
		setRunning(false);

		taskFinished();
	}

	/**
	 * Called by timers.
	 */
	private synchronized void executeJobs()
	{
		// If task did already stop, ignore commands from timers...
		if(!isRunning())
			return;
		if(controlListener != null)
		{
			for(Job job : jobs)
			{
				controlListener.addJobToExecutionQueue(new JobExecutionQueueElement(job, evaluationNo, measurementStartTime.getTime()));
			}
		}
		// Inform listeners that task started.
		taskEvaluated(evaluationNo);

		evaluationNo++;
		if(numExecutions >= 0 && evaluationNo >= numExecutions)
		{
			cancelTask();
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

	@Override
	public synchronized void addJob(Job job) throws MeasurementRunningException
	{
		assertRunning();
		jobs.add(job);
	}

	@Override
	public synchronized void removeJob(Job job) throws MeasurementRunningException
	{
		assertRunning();
		jobs.remove(job);
	}

	@Override
	public synchronized void clearJobs() throws MeasurementRunningException
	{
		assertRunning();
		jobs.clear();
	}

	@Override
	public Job[] getJobs() throws RemoteException
	{
		return jobs.toArray(new Job[jobs.size()]);
	}

	@Override
	public void insertJob(Job job, int jobIndex)
			throws RemoteException, MeasurementRunningException, IndexOutOfBoundsException {
		assertRunning();
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
