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
package org.youscope.plugin.shareexecution;

import java.rmi.RemoteException;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.job.CompositeJobAdapter;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobException;
import org.youscope.common.microscope.Microscope;

/**
 * Implementation of the share execution job.
 * @author Moritz Lang
 */
class ShareExecutionJobImpl extends CompositeJobAdapter implements ShareExecutionJob
{

	/**
	 * Serial Version UID.
	 */
	private static final long			serialVersionUID	= -1632043889601708085L;

	private int numShare = 1;
	
	private int shareID = 1;
	
	private boolean separateForEachWell = false;
	
	public ShareExecutionJobImpl(PositionInformation positionInformation) throws RemoteException
	{
		super(positionInformation);
	}

	@Override
	protected String getDefaultName()
	{
		String text = "Share Execution Job(share=" + Integer.toString(numShare) + ")";
		return text;
	}

	

	@Override
	public void runJob(ExecutionInformation executionInformation, Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		long currentIteration = executionInformation.getEvaluationNumber();
		long lastIteration = measurementContext.getProperty(getLastIterationPropertyName(), Long.class);
		long lastID = measurementContext.getProperty(getLastIDPropertyName(), Long.class);
		long numIDs = measurementContext.getProperty(getNumIDsPropertyName(), Long.class);
		long currentID;
		if(lastIteration != currentIteration)
		{
			currentID = 0;
			measurementContext.setProperty(getLastIterationPropertyName(), currentIteration);
			if(currentIteration != 0)
			{
				numIDs = lastID+1;
				measurementContext.setProperty(getNumIDsPropertyName(), numIDs);
			}
			else
			{
				numIDs = Long.MAX_VALUE;
				measurementContext.setProperty(getNumIDsPropertyName(), numIDs);
			}
		}
		else
			currentID = lastID+1;
		
		long id0 = ((currentIteration%numIDs)*numShare)%numIDs;
		if((currentID >= id0 && currentID < id0+numShare) 
				|| (id0+numShare>numIDs && currentID < (id0+numShare)%numIDs))
		{
			Job[] jobs = getJobs();
			for(Job job : jobs)
			{
				job.executeJob(executionInformation, microscope, measurementContext);
				if(Thread.interrupted())
					throw new InterruptedException();
			}
		}
		measurementContext.setProperty(getLastIDPropertyName(), currentID);
		
	}
	
	private String getPropertyBaseName()
	{
		if(separateForEachWell && getPositionInformation().getWell() != null)
			return "ShareExecutionJob.action"+Long.toString(shareID)+".well"+getPositionInformation().getWell().toString();
		return "ShareExecutionJob.action"+Long.toString(shareID);
	}
	private String getLastIterationPropertyName()
	{
		return getPropertyBaseName()+".lastIteration";
	}
	private String getLastIDPropertyName()
	{
		return getPropertyBaseName()+".lastID";
	}
	private String getNumIDsPropertyName()
	{
		return getPropertyBaseName()+".numIDs";
	}
	private void resetJob(MeasurementContext measurementContext) throws RemoteException
	{
		measurementContext.setProperty(getLastIterationPropertyName(), new Long(-1));
		measurementContext.setProperty(getLastIDPropertyName(), new Long(-1));
		measurementContext.setProperty(getNumIDsPropertyName(), new Long(Long.MAX_VALUE));
	}
	@Override
	public void uninitializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		resetJob(measurementContext);
		super.uninitializeJob(microscope, measurementContext);
	}
	@Override
	public void initializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		super.initializeJob(microscope, measurementContext);
		resetJob(measurementContext);
	}

	@Override
	public int getNumShare() throws RemoteException {
		return numShare;
	}

	@Override
	public void setNumShare(int numShare) throws RemoteException, ComponentRunningException {
		assertRunning();
		this.numShare = numShare > 0 ? numShare : 0;
	}

	@Override
	public int getShareID() throws RemoteException {
		return shareID;
	}

	@Override
	public void setShareID(int shareID) throws RemoteException, ComponentRunningException {
		assertRunning();
		this.shareID = shareID;
	}

	@Override
	public boolean isSeparateForEachWell() throws RemoteException {
		return separateForEachWell;
	}

	@Override
	public void setSeparateForEachWell(boolean separateForEachWell)
			throws RemoteException, ComponentRunningException {
		assertRunning();
		this.separateForEachWell = separateForEachWell;
	}
}
