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
package org.youscope.plugin.onix;

import java.io.StringReader;
import java.rmi.RemoteException;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.MessageListener;
import org.youscope.common.PositionInformation;
import org.youscope.common.job.JobAdapter;
import org.youscope.common.job.JobException;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.table.Table;
import org.youscope.common.table.TableDefinition;
import org.youscope.common.table.TableException;
import org.youscope.common.util.RMIReader;

/**
 * @author langmo
 */
class OnixJobImpl extends JobAdapter implements OnixJob
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -936997010448014857L;

	private OnixAddon onix = null;
	
	private String onixProtocol = "";
	private boolean waitUntilFinished = true;
	
	private volatile Table tableToEvaluate = null;
	
	private final MessageListener messageForwarder = new MessageListener()
	{
		@Override
		public void sendMessage(String message) throws RemoteException
		{
			OnixJobImpl.this.sendMessage("Onix Message: " + message);
		}

		@Override
		public void sendErrorMessage(String message, Throwable error) throws RemoteException
		{
			OnixJobImpl.this.sendErrorMessage("Onix Error: " + message, error);
		}
	};
	
	public OnixJobImpl(PositionInformation positionInformation) throws RemoteException
	{
		super(positionInformation);
	}

	@Override
	protected String getDefaultName()
	{
		String text = "onix.";
		text += waitUntilFinished ? "eval(" : "parallel_eval(\"";
		text += onixProtocol.replace("\n", "<br />");
		text += "\")";
		
		return text;
	}

	@Override
	public void initializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		super.initializeJob(microscope, measurementContext);
		tableToEvaluate = null;
		
		if(onix == null)
		{
			onix = new OnixAddonImpl();
			onix.addMessageListener(messageForwarder);
		}
		try
		{
			onix.initialize();
		}
		catch(OnixException e)
		{
			throw new JobException("Could not initialize onix device.", e);
		}
	}
	
	@Override
	public void uninitializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		super.uninitializeJob(microscope, measurementContext);
		tableToEvaluate = null;
		if(onix != null)
		{
			try
			{
				onix.uninitialize();
			}
			catch(Exception e)
			{
				throw new JobException("Could not uninitialize onix device.", e);
			}
			onix.removeMessageListener(messageForwarder);
			onix = null;
		}
	}
	
	@Override
	public void runJob(ExecutionInformation executionInformation, Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		// Run commands we were controlled to run.
		if(tableToEvaluate != null)
		{
			try
			{
				OnixJobTableDataInterpreter.runTableData(tableToEvaluate, onix);
			}
			catch(Exception e)
			{
				throw new JobException("Error while executing consumed table data.", e);
			}
			tableToEvaluate = null;
		}
		
		if(Thread.interrupted())
			throw new InterruptedException();
		
		// Evaluate protocol.
		try
		{
			if(waitUntilFinished)
			{
				RMIReader rmiReader = new RMIReader(new StringReader(onixProtocol));
				onix.runProtocolAndWait(rmiReader);
				rmiReader.close();
			}
			else
			{
				// we do not know when the protocol is actually read, thus, we do not know when to load RMI reader.
				@SuppressWarnings("resource")
				RMIReader rmiReader = new RMIReader(new StringReader(onixProtocol));
				onix.runProtocol(rmiReader);
			}
		}
		catch(Exception e)
		{
			throw new JobException("Could not evaluate onix protocol.", e);
		}
	}

	@Override
	public void setOnixProtocol(String onixProtocol) throws ComponentRunningException
	{
		assertRunning();
		this.onixProtocol = onixProtocol == null ? "" : onixProtocol;
	}

	@Override
	public String getOnixProtocol()
	{
		return onixProtocol;
	}

	@Override
	public void setWaitUntilFinished(boolean waitUntilFinished) throws ComponentRunningException
	{
		assertRunning();
		this.waitUntilFinished = waitUntilFinished;
	}

	@Override
	public boolean isWaitUntilFinished()
	{
		return waitUntilFinished;
	}

	@Override
	public void consumeTable(Table table) throws TableException
	{
		if(table == null)
			tableToEvaluate = null;
		else tableToEvaluate = table.toTable(getConsumedTableDefinition());
	}

	@Override
	public TableDefinition getConsumedTableDefinition()
	{
		return OnixTable.getTableDefinition();
	}

	
}
