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
package org.youscope.plugin.waitsincelastaction;

import java.rmi.RemoteException;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.job.JobAdapter;
import org.youscope.common.job.JobException;
import org.youscope.common.microscope.Microscope;


/**
 * @author Moritz Lang 
 */
class WaitSinceLastActionJobImpl  extends JobAdapter implements WaitSinceLastActionJob
{
	/**
	 * SerializableVersion UID.
	 */
	private static final long	serialVersionUID	= 8122229758338178084L;
	
	private long				waitTime			= 0;
	
	private long				initialWaitTime			= 0;
	
	private boolean				resetAfterIteration			= true;
	
	private int				actionID			= 1;

	public WaitSinceLastActionJobImpl(PositionInformation positionInformation) throws RemoteException
	{
		super(positionInformation);
	}

	

	@Override
	public void runJob(ExecutionInformation executionInformation,  Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		long lastExecution = measurementContext.getProperty(getLastExecutionPropertyName(), Long.class);
		
		long currentIteration = executionInformation.getEvaluationNumber();
		long lastIteration = measurementContext.getProperty(getLastIterationPropertyName(), Long.class);
		boolean initialIteration = lastExecution < 0 || lastIteration < 0 || (resetAfterIteration && lastIteration != currentIteration);
		if(initialIteration)
		{
			Thread.sleep(initialWaitTime);
		}
		else
		{
			long sleepMs = waitTime-(System.currentTimeMillis()-lastExecution);
			if(sleepMs > 0)
				Thread.sleep(sleepMs);
		}
		measurementContext.setProperty(getLastExecutionPropertyName(), new Long(System.currentTimeMillis()));
		measurementContext.setProperty(getLastIterationPropertyName(), new Long(currentIteration));
	}

	@Override
	protected String getDefaultName()
	{
		return "Wait since last iteration";
	}

	@Override
	public void initializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		super.initializeJob(microscope, measurementContext);
		resetTimer(measurementContext);
	}

	private String getLastExecutionPropertyName()
	{
		return "WaitSinceLastActionJobImpl.action"+Long.toString(actionID)+".lastExecution";
	}
	private String getLastIterationPropertyName()
	{
		return "WaitSinceLastActionJobImpl.action"+Long.toString(actionID)+".lastIteration";
	}
	
	private void resetTimer(MeasurementContext measurementContext) throws RemoteException
	{
		measurementContext.setProperty(getLastExecutionPropertyName(), new Long(-1));
		measurementContext.setProperty(getLastIterationPropertyName(), new Long(-1));
	}


	@Override
	public void uninitializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		super.uninitializeJob(microscope, measurementContext);
		resetTimer(measurementContext);
	}



	@Override
	public long getWaitTime() throws RemoteException {
		return waitTime;
	}



	@Override
	public void setWaitTime(long waitTime) throws RemoteException, ComponentRunningException {
		assertRunning();
		this.waitTime = waitTime > 0 ? waitTime : 0;
		
	}



	@Override
	public long getInitialWaitTime() throws RemoteException {
		return initialWaitTime;
	}



	@Override
	public void setInitialWaitTime(long initialWaitTime) throws RemoteException, ComponentRunningException {
		assertRunning();
		this.initialWaitTime = initialWaitTime > 0 ? initialWaitTime : 0;
	}



	@Override
	public boolean isResetAfterIteration() throws RemoteException {
		return resetAfterIteration;
	}



	@Override
	public void setResetAfterIteration(boolean resetAfterIteration)
			throws RemoteException, ComponentRunningException {
		assertRunning();
		this.resetAfterIteration = resetAfterIteration;
	}



	@Override
	public int getActionID() throws RemoteException {
		return actionID;
	}



	@Override
	public void setActionID(int actionID) throws RemoteException, ComponentRunningException {
		assertRunning();
		this.actionID = actionID;
	}
}
