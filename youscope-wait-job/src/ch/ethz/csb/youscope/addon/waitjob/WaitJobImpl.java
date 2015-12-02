/**
 * 
 */
package ch.ethz.csb.youscope.addon.waitjob;

import java.rmi.RemoteException;
import java.util.ArrayList;

import ch.ethz.csb.youscope.shared.measurement.ExecutionInformation;
import ch.ethz.csb.youscope.shared.measurement.MeasurementContext;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.measurement.job.JobAdapter;
import ch.ethz.csb.youscope.shared.measurement.job.JobException;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.WaitJob;
import ch.ethz.csb.youscope.shared.microscope.Microscope;

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
	public void setWaitTime(long waitTime) throws RemoteException, MeasurementRunningException
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
	public String getDefaultName()
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
