/**
 * 
 */
package ch.ethz.csb.youscope.shared.measurement.job;

import java.rmi.RemoteException;
import java.util.ArrayList;

import ch.ethz.csb.youscope.shared.measurement.MeasurementContext;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.microscope.Microscope;

/**
 * /**
 * Convenient adapter class to implement a job which contains other jobs.
 * Similar to {@link JobAdapter}, except that additional functionality to add and remove sub-jobs is already implemented, as well as
 * the initialization and uninitialization of these sub-jobs.
 * The sub-jobs can be accessed (e.g. the function {@link Job#executeJob(ch.ethz.csb.youscope.shared.measurement.ExecutionInformation, Microscope, MeasurementContext)}) via {@link #getJobs()}.
 * @author Moritz Lang
 */
public abstract class JobContainerAdapter extends JobAdapter implements EditableJobContainer
{

	/**
	 * Serial Version UID.
	 */
	private static final long				serialVersionUID	= -2780029256797068512L;

	private final ArrayList<Job>	jobs				= new ArrayList<Job>();

	/**
	 * Constructor.
	 * @param positionInformation The logical position where this job is executed. Must not be null.
	 * @throws RemoteException
	 */
	public JobContainerAdapter(PositionInformation positionInformation) throws RemoteException
	{
		super(positionInformation);
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
	public synchronized void addJob(Job job) throws MeasurementRunningException
	{
		assertRunning();
		synchronized(jobs)
		{
			jobs.add(job);
		}
	}

	@Override
	public synchronized void removeJob(Job job) throws MeasurementRunningException
	{
		assertRunning();
		synchronized(jobs)
		{
			jobs.remove(job);
		}
	}

	@Override
	public synchronized void clearJobs() throws MeasurementRunningException
	{
		assertRunning();
		synchronized(jobs)
		{
			jobs.clear();
		}
	}

	@Override
	public Job[] getJobs()
	{
		synchronized(jobs)
		{
			return jobs.toArray(new Job[jobs.size()]);
		}
	}

	@Override
	public int getNumJobs()
	{
		return jobs.size();
	}

	@Override
	public Job getJob(int jobIndex) throws IndexOutOfBoundsException 
	{
		if(jobIndex < 0 || jobIndex >= getNumJobs())
			throw new IndexOutOfBoundsException("Job container contains only " + Integer.toString(getNumJobs())+" job(s).");
		return jobs.get(jobIndex);
	}

	@Override
	public void insertJob(Job job, int jobIndex) throws MeasurementRunningException, IndexOutOfBoundsException 
	{
		assertRunning();
		jobs.add(jobIndex, job);
		
	}
}
