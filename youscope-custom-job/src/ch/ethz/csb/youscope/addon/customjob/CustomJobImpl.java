/**
 * 
 */
package ch.ethz.csb.youscope.addon.customjob;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.shared.measurement.ExecutionInformation;
import ch.ethz.csb.youscope.shared.measurement.MeasurementContext;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.measurement.job.JobContainerAdapter;
import ch.ethz.csb.youscope.shared.measurement.job.JobException;
import ch.ethz.csb.youscope.shared.microscope.Microscope;

/**
 * @author Moritz Lang
 */
class CustomJobImpl extends JobContainerAdapter implements CustomJob
{

	/**
	 * Serial Version UID.
	 */
	private static final long			serialVersionUID	= -1632043889601708085L;

	public CustomJobImpl(PositionInformation positionInformation) throws RemoteException
	{
		super(positionInformation);
	}

	@Override
	public String getDefaultName() throws RemoteException
	{
		return "Custom job";
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
