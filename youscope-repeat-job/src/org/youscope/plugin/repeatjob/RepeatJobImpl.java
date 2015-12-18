/**
 * 
 */
package org.youscope.plugin.repeatjob;

import java.rmi.RemoteException;

import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.job.EditableJobContainerAdapter;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobException;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.microscope.Microscope;

/**
 * Implementation of the repeat job.
 * @author Moritz Lang
 */
class RepeatJobImpl extends EditableJobContainerAdapter implements RepeatJob
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
