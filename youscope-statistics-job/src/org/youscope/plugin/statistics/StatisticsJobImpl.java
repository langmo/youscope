/**
 * 
 */
package org.youscope.plugin.statistics;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import org.youscope.common.measurement.ExecutionInformation;
import org.youscope.common.measurement.MeasurementContext;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.PositionInformation;
import org.youscope.common.measurement.job.Job;
import org.youscope.common.measurement.job.JobAdapter;
import org.youscope.common.measurement.job.JobContainer;
import org.youscope.common.measurement.job.JobException;
import org.youscope.common.measurement.job.JobListener;
import org.youscope.common.measurement.job.basicjobs.StatisticsJob;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.table.Table;
import org.youscope.common.table.TableDefinition;
import org.youscope.common.table.TableException;
import org.youscope.common.table.TableListener;

/**
 * @author Moritz Lang
 * 
 */
class StatisticsJobImpl extends JobAdapter implements StatisticsJob
{

	/**
	 * Serial Version UID.
	 */
	private static final long				serialVersionUID	= -1153760533381916941L;

	private final Vector<TableListener>	tableDataListeners	= new Vector<TableListener>();
	private final ArrayList<Job>		jobs				= new ArrayList<Job>();

	private final ArrayList<JobStatisticListener> jobListeners = new ArrayList<JobStatisticListener>();
	
	public StatisticsJobImpl(PositionInformation positionInformation) throws RemoteException
	{
		super(positionInformation);
	}

	@Override
	public void runJob(ExecutionInformation executionInformation, Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		synchronized(jobs)
		{
			for(Job job : jobs)
			{
				job.executeJob(executionInformation, microscope, measurementContext);
				if(Thread.interrupted())
					throw new InterruptedException();
			}
		}
		Table table = new Table(getProducedTableDefinition(), getPositionInformation(), executionInformation);
		for(JobStatisticListener listener : jobListeners)
		{
			try {
				listener.addJobRow(table);
			} catch (TableException e) {
				throw new JobException("Could not add statistics table entry for Job " + listener.jobName+".", e);
			}
		}
		
		synchronized(tableDataListeners)
		{
			for(TableListener listener : tableDataListeners)
			{
				listener.newTableProduced(table.clone());
			}
		}
		
	}

	private class JobStatisticListener extends UnicastRemoteObject implements JobListener
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -1585307547205425410L;
		private final String jobName;
		private final PositionInformation positionInformation;
		private long startTime = -1;
		private long endTime = -1;
		private ExecutionInformation executionInformation = null;
		private boolean completed = false;
		private final Job job;
		JobStatisticListener(Job job) throws RemoteException
		{
			this.job = job;
			jobName = job.getName();
			positionInformation = job.getPositionInformation();
			
		}
		public void addListener() throws RemoteException
		{
			job.addJobListener(this);
		}
		public void removeListener() throws RemoteException
		{
			job.removeJobListener(this);
		}
		@Override
		public void jobInitialized() throws RemoteException
		{
			// do nothing
		}
		@Override
		public void jobUninitialized() throws RemoteException
		{
			// do nothing
		}
		@Override
		public void jobStarted(ExecutionInformation executionInformation) throws RemoteException
		{
			startTime = (new Date()).getTime();
			this.executionInformation = executionInformation;
			completed = false;
		}
		@Override
		public void jobFinished(ExecutionInformation executionInformation) throws RemoteException
		{
			endTime = (new Date()).getTime();
			if((this.executionInformation == null && executionInformation == null) || this.executionInformation.equals(executionInformation))
			{
				completed = true;
			}
		}
		public void addJobRow(Table table) throws TableException
		{
			if(completed)
			{
				completed = false;
				table.addRow(jobName, positionInformation.toString(), executionInformation.toString(),  new Long(startTime), new Long(endTime), new Long(endTime - startTime));
			}
			return;					
		}
	}
	
	@Override
	public void initializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		super.initializeJob(microscope, measurementContext);

		// initialize child jobs
		synchronized(jobs)
		{
			for(Job job : jobs)
			{
				job.initializeJob(microscope, measurementContext);
				addJobListener(job);
			}
		}
		
	}

	private void addJobListener(Job job) throws RemoteException
	{
		JobStatisticListener listener = new JobStatisticListener(job);
		listener.addListener();
		jobListeners.add(listener);
		if(job instanceof JobContainer)
		{
			for(Job childJob : ((JobContainer)job).getJobs())
			{
				addJobListener(childJob);
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
		for(JobStatisticListener listener : jobListeners)
		{
			listener.removeListener();
		}
	}

	@Override
	public String getDefaultName()
	{
		return "Gathering Statistics";
	}

	@Override
	public void addTableListener(TableListener listener)
	{
		if(listener == null)
			return;
		synchronized(tableDataListeners)
		{
			tableDataListeners.add(listener);
		}
	}

	@Override
	public void removeTableListener(TableListener listener)
	{
		if(listener == null)
			return;
		synchronized(tableDataListeners)
		{
			tableDataListeners.remove(listener);
		}
	}

	@Override
	public synchronized void addJob(Job job) throws RemoteException, MeasurementRunningException
	{
		assertRunning();
		synchronized(jobs)
		{
			jobs.add(job);
		}
	}

	@Override
	public synchronized void removeJob(Job job) throws RemoteException, MeasurementRunningException
	{
		assertRunning();
		synchronized(jobs)
		{
			jobs.remove(job);
		}
	}

	@Override
	public synchronized void clearJobs() throws RemoteException, MeasurementRunningException
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
	public TableDefinition getProducedTableDefinition()
	{
		return StatisticsTable.getTableDefinition();
	}

	@Override
	public void insertJob(Job job, int jobIndex)
			throws RemoteException, MeasurementRunningException, IndexOutOfBoundsException {
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
