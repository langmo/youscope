/**
 * 
 */
package ch.ethz.csb.youscope.addon.compositejob;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.shared.measurement.ExecutionInformation;
import ch.ethz.csb.youscope.shared.measurement.MeasurementContext;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.measurement.job.JobContainerAdapter;
import ch.ethz.csb.youscope.shared.measurement.job.JobException;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.CompositeJob;
import ch.ethz.csb.youscope.shared.microscope.Microscope;

/**
 * @author Moritz Lang
 */
class CompositeJobImpl extends JobContainerAdapter implements CompositeJob
{

	/**
	 * Serial Version UID.
	 */
	private static final long			serialVersionUID	= -1632043889601708085L;

	public CompositeJobImpl(PositionInformation positionInformation) throws RemoteException
	{
		super(positionInformation);
	}

	@Override
	public String getDefaultName() throws RemoteException
	{
		String text = "Job container(";
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
		for(Job job : jobs)
		{
			job.executeJob(executionInformation, microscope, measurementContext);
			if(Thread.interrupted())
				throw new InterruptedException();
		}
	}
}
