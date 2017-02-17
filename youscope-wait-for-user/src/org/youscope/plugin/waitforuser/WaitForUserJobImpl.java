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
package org.youscope.plugin.waitforuser;

import java.rmi.RemoteException;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.callback.CallbackException;
import org.youscope.common.job.JobAdapter;
import org.youscope.common.job.JobException;
import org.youscope.common.microscope.Microscope;

/**
 * @author langmo 
 */
class WaitForUserJobImpl  extends JobAdapter implements WaitForUserJob
{
	/**
	 * SerializableVersion UID.
	 */
	private static final long	serialVersionUID	= 8128119758338178084L;

	private String message = "No message.";
	private WaitForUserCallback callback;

	public WaitForUserJobImpl(PositionInformation positionInformation) throws RemoteException
	{
		super(positionInformation);
	}

	

	@Override
	public void runJob(ExecutionInformation executionInformation,  Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		callback.waitForUser(message);
	}

	@Override
	protected String getDefaultName()
	{
		return "Wait for user";
	}

	@Override
	public String getMessage() throws RemoteException
	{
		return message;
	}

	@Override
	public void setMessage(String message) throws RemoteException, ComponentRunningException
	{
		assertRunning();
		if(message != null)
			this.message = message;
	}

	@Override
	public void setMeasurementCallback(WaitForUserCallback callback) throws RemoteException, ComponentRunningException
	{
		assertRunning();
		this.callback = callback;
	}

	@Override
	public void initializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		super.initializeJob(microscope, measurementContext);
		// Check if callback defined.
		if(callback == null)
			throw new JobException("Measurement callback is null, thus cannot wait for user.");
		// Initialize callback.
		try
		{
			callback.initializeCallback();
		}
		catch(CallbackException e)
		{
			throw new JobException("Measurement callback did throw an error while initialization.", e);
		}
	}



	@Override
	public void uninitializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		super.uninitializeJob(microscope, measurementContext);
		
		if(callback != null)
		{
			try
			{
				callback.uninitializeCallback();
			}
			catch(CallbackException e)
			{
				throw new JobException("Measurement callback did throw an error while uninitialization.", e);
			}
		}	
	}
}
