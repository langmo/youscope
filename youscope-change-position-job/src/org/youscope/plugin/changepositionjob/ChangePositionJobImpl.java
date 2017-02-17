/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.youscope.plugin.changepositionjob;

import java.rmi.RemoteException;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.job.JobAdapter;
import org.youscope.common.job.JobException;
import org.youscope.common.job.basicjobs.ChangePositionJob;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.microscope.StageDevice;

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
	public synchronized void setPosition(double x, double y) throws ComponentRunningException
	{
		assertRunning();
		this.x = x;
		this.y = y;
		this.absolute = true;
	}

	@Override
	public synchronized void setRelativePosition(double dx, double dy) throws RemoteException, ComponentRunningException
	{
		assertRunning();
		this.x = dx;
		this.y = dy;
		this.absolute = false;
	}

	@Override
	protected String getDefaultName()
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
	public synchronized void setStageDevice(String deviceID) throws RemoteException, ComponentRunningException
	{
		assertRunning();
		stageDeviceID = deviceID;
	}
}
