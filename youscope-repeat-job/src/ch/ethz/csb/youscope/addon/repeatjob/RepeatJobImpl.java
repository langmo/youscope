/**
 * 
 */
package ch.ethz.csb.youscope.addon.repeatjob;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.shared.measurement.ExecutionInformation;
import ch.ethz.csb.youscope.shared.measurement.MeasurementContext;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.measurement.job.JobContainerAdapter;
import ch.ethz.csb.youscope.shared.measurement.job.JobException;
import ch.ethz.csb.youscope.shared.microscope.Microscope;

/**
 * Implementation of the repeat job.
 * @author Moritz Lang
 */
class RepeatJobImpl extends JobContainerAdapter implements RepeatJob
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
	public String getDefaultName() throws RemoteException
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
			text += job.getName();
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
	public void setNumRepeats(int numRepeats) throws MeasurementRunningException
	{
		assertRunning();
		this.numRepeats = numRepeats > 0 ? numRepeats : 0;
	}
}
