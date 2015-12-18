/**
 * 
 */
package org.youscope.plugin.compositejob;

import java.rmi.RemoteException;

import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.job.EditableJobContainerAdapter;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobException;
import org.youscope.common.job.basicjobs.CompositeJob;
import org.youscope.common.microscope.Microscope;

/**
 * @author Moritz Lang
 */
class CompositeJobImpl extends EditableJobContainerAdapter implements CompositeJob
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
