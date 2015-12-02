/**
 * 
 */
package ch.ethz.csb.youscope.addon.changepositionjob;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.shared.measurement.ExecutionInformation;
import ch.ethz.csb.youscope.shared.measurement.MeasurementContext;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.JobAdapter;
import ch.ethz.csb.youscope.shared.measurement.job.JobException;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.ChangePositionJob;
import ch.ethz.csb.youscope.shared.microscope.Microscope;
import ch.ethz.csb.youscope.shared.microscope.StageDevice;

/**
 * @author Moritz Lang 
 */
class ChangePositionJobImpl extends JobAdapter implements ChangePositionJob
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -2464707147237358900L;

	// Initialize job such that position is not changed.
	private double				x					= 0;
	private double				y					= 0;
	private boolean				absolute			= false;
	private String				stageDeviceID		= null;

	public ChangePositionJobImpl(PositionInformation positionInformation) throws RemoteException
	{
		super(positionInformation);
	}

	@Override
	public double getX()
	{
		return x;
	}

	@Override
	public double getY()
	{
		return y;
	}

	@Override
	public boolean isAbsolute()
	{
		return absolute;
	}

	@Override
	public synchronized void setPosition(double x, double y) throws MeasurementRunningException
	{
		assertRunning();
		this.x = x;
		this.y = y;
		this.absolute = true;
	}

	@Override
	public synchronized void setRelativePosition(double dx, double dy) throws RemoteException, MeasurementRunningException
	{
		assertRunning();
		this.x = dx;
		this.y = dy;
		this.absolute = false;
	}

	@Override
	public String getDefaultName()
	{
		String returnVal;
		if(absolute)
			returnVal = "Move stage to ";
		else
			returnVal = "Move stage by ";
		return returnVal + Double.toString(x) + "um/" + Double.toString(y) + "um";
	}

	@Override
	public void runJob(ExecutionInformation executionInformation, Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		try
		{
			StageDevice stageDevice;
			if(stageDeviceID == null)
				stageDevice = microscope.getStageDevice();
			else
				stageDevice = microscope.getStageDevice(stageDeviceID);
			if(absolute)
				stageDevice.setPosition(x, y);
			else
				stageDevice.setRelativePosition(x, y);
		}
		catch(Exception e)
		{
			throw new JobException("Could not set stage position.", e);
		}
	}

	@Override
	public String getStageDevice()
	{
		return stageDeviceID;
	}

	@Override
	public synchronized void setStageDevice(String deviceID) throws RemoteException, MeasurementRunningException
	{
		assertRunning();
		stageDeviceID = deviceID;
	}
}
