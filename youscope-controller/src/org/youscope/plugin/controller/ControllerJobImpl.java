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
package org.youscope.plugin.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobAdapter;
import org.youscope.common.job.JobException;
import org.youscope.common.Well;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.table.ColumnDefinition;
import org.youscope.common.table.Table;
import org.youscope.common.table.TableConsumer;
import org.youscope.common.table.TableDefinition;
import org.youscope.common.table.TableException;
import org.youscope.common.table.TableListener;
import org.youscope.common.table.TableProducer;
import org.youscope.common.table.TemporaryRow;

/**
 * @author Moritz Lang
 *
 */
class ControllerJobImpl extends JobAdapter implements ControllerJob
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -343965916066788997L;

	private Job inputJob = null;
	
	private Job outputJob = null;
	
	private final HashMap<String, String> states = new HashMap<String,String>();
	
	private volatile Table lastInputTable = null;
	
	private final TableListener inputListener = new TableListener()
	{
		@Override
		public void newTableProduced(Table table) throws RemoteException {
			synchronized(ControllerJobImpl.this)
			{
				lastInputTable = table;
			}
		}
	};
	
	private final ArrayList<TableListener> tableListeners = new ArrayList<TableListener>();

	private String controllerScriptEngine = "Mozilla Rhino";
	
	private String controllerScript = "";
	
	private ScriptEngine				localEngine					= null;
	
	private final StringWriter scriptOutputListener = new StringWriter();
	
	public ControllerJobImpl(PositionInformation positionInformation) throws RemoteException
	{
		super(positionInformation);
	}

	@Override
	public Job[] getJobs() throws RemoteException
	{
		if(inputJob == null && outputJob == null)
			return new Job[0];
		else if(inputJob == null)
			return new Job[]{outputJob};
		else if(outputJob == null)
			return new Job[]{inputJob};
		else
			return new Job[]{inputJob, outputJob};
	}

	@Override
	public void removeTableListener(TableListener listener) throws RemoteException
	{
		synchronized(tableListeners)
		{
			tableListeners.remove(listener);
		}
	}

	@Override
	public void addTableListener(TableListener listener) throws RemoteException
	{
		synchronized(tableListeners)
		{
			tableListeners.add(listener);
		}
	}

	@Override
	public TableDefinition getProducedTableDefinition() throws RemoteException
	{
		ColumnDefinition<?>[] inputColumns;
		if(inputJob != null)
		{
			inputColumns = ((TableProducer)inputJob).getProducedTableDefinition().getColumnDefinitions();
		}
		else
			inputColumns = new ColumnDefinition<?>[0];
		
		ColumnDefinition<?>[] outputColumns;
		if(outputJob != null)
		{
			outputColumns = ((TableConsumer)outputJob).getConsumedTableDefinition().getColumnDefinitions();
		}
		else
			outputColumns = new ColumnDefinition<?>[0];
		
		return ControllerTable.getTableDefinition(inputColumns, outputColumns);
	}

	@Override
	public void setControllerScriptEngine(String engine) throws RemoteException, ComponentRunningException
	{
		if(engine == null)
			this.controllerScriptEngine = "Mozilla Rhino";
		else
			this.controllerScriptEngine = engine;
	}

	@Override
	public String getControllerScriptEngine() throws RemoteException
	{
		return controllerScriptEngine;
	}

	@Override
	public void setControllerScript(String controllerScript) throws RemoteException, ComponentRunningException
	{
		if(controllerScript == null)
			this.controllerScript = "";
		else
			this.controllerScript = controllerScript;
	}

	@Override
	public String getControllerScripScript() throws RemoteException
	{
		return controllerScript;
	}

	@Override
	public Job getInputJob() throws RemoteException
	{
		return inputJob;
	}

	@Override
	public void setInputJob(Job inputJob) throws NullPointerException, ComponentRunningException, IllegalArgumentException
	{
		assertRunning();
		if(inputJob == null)
			throw new NullPointerException("Input job must not be null.");
		else if(!(inputJob instanceof TableProducer))
			throw new IllegalArgumentException("Input job must implement TableProducer interface.");
		else
			this.inputJob = inputJob;
	}

	@Override
	public Job getOutputJob() throws RemoteException
	{
		return outputJob;
	}

	@Override
	public void setOutputJob(Job outputJob) throws NullPointerException, ComponentRunningException, IllegalArgumentException
	{
		if(outputJob == null)
			throw new NullPointerException("Output job must not be null.");
		else if(!(outputJob instanceof TableConsumer))
			throw new IllegalArgumentException("Output job must implement TableConsumer interface.");
		else
			this.outputJob = outputJob;
	}

	@Override
	public void runJob(ExecutionInformation executionInformation, Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		// Execute input job.
		synchronized(this)
		{
			this.lastInputTable = null;
		}
		
		inputJob.executeJob(executionInformation, microscope, measurementContext);
		// get table data from input.
		Table inputTable;
		synchronized(this)
		{
			inputTable = this.lastInputTable;
			this.lastInputTable = null;
		}
		if(inputTable == null)
			throw new JobException("Input job did not produce table used as input for controller.");	
		
		// Create new output table
		Table outputTable = new Table(((TableConsumer)outputJob).getConsumedTableDefinition(), measurementContext.getMeasurementRuntime(), getPositionInformation(), executionInformation);
		
		// Set controller algorithm variables
		ControllerCallbackImpl callback = new ControllerCallbackImpl(inputTable, outputTable, states);
		localEngine.put(ScriptEngine.FILENAME, "Controller");
		localEngine.put("evaluationNumber", executionInformation.getEvaluationNumber());
		localEngine.put("evaluationTime", outputTable.getCreationRuntime());
		PositionInformation pos = getPositionInformation();
		Well well = pos.getWell();
		if(well == null)
		{
			localEngine.put("wellX", -1);
			localEngine.put("wellY", -1);
		}
		else
		{
			localEngine.put("wellX", well.getWellX());
			localEngine.put("wellY", well.getWellY());
		}
		localEngine.put("position", pos.getPositions());
		localEngine.put("controller", callback);
		
		// Run controller algorithm.
		StringReader fileReader = null;
		BufferedReader bufferedReader = null;
		Object returnVal;
		localEngine.getContext().setWriter(scriptOutputListener);
		try
		{
			fileReader = new StringReader(controllerScript);
			bufferedReader = new BufferedReader(fileReader);
			returnVal = localEngine.eval(bufferedReader);
		}
		catch(ScriptException e)
		{
			throw new JobException("Controller algorithm evaluation failed.", e);
		}
		finally
		{
			if(fileReader != null)
				fileReader.close();
			if(bufferedReader != null)
			{
				try {
					bufferedReader.close();
				} catch (IOException e) {
					this.sendErrorMessage("Could not close file buffer.", e);
				}
			}
		}
		receiveEngineMessages();
		if(returnVal != null && returnVal.toString().length() > 0)
			sendMessage(returnVal.toString());
		
		// get controller algorithm output.
		try {
			outputTable = callback.getOutputTable();
		} catch (TableException e1) {
			throw new JobException("Table produced by controller script for output job is invalid.", e1);
		}
		
		// send table to output job.
		try
		{
			((TableConsumer)outputJob).consumeTable(outputTable.clone());
		}
		catch(TableException e)
		{
			throw new JobException("Output job of controller can not consume table produced by controller.", e);
		}
		
		// execute output job
		outputJob.executeJob(executionInformation, microscope, measurementContext);
		
		// Send data to listeners.
		// For this we have to combine input and output rows into one table.
		Table listenerTable = new Table(getProducedTableDefinition(), measurementContext.getMeasurementRuntime(), getPositionInformation(), executionInformation);
		int numRows = inputTable.getNumRows() < outputTable.getNumRows() ? outputTable.getNumRows() : inputTable.getNumRows();
		try
		{
			for(int row=0; row<numRows; row++)
			{
				TemporaryRow newRow = listenerTable.createTemporaryRow();
				if(row < inputTable.getNumRows())
				{
					for(int col = 0; col < inputTable.getNumColumns(); col++)
					{
						newRow.get(col).setValue(inputTable.getValue(row, col));
					}
				}
				if(row < outputTable.getNumRows())
				{
					for(int col = 0; col < outputTable.getNumColumns(); col++)
					{
						newRow.get(col+inputTable.getNumColumns()).setValue(outputTable.getValue(row, col));
					}
				}
				listenerTable.addRow(newRow);	
			}
		}
		catch(TableException e)
		{
			throw new JobException("Could not produce controller output table.", e);
		}
		
		synchronized(tableListeners)
		{
			for(TableListener listener : tableListeners)
			{
				listener.newTableProduced(listenerTable.clone());
			}
		}
	}

	@Override
	protected String getDefaultName()
	{
		return "Controller Job";
	}

	@Override
	public void initializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		super.initializeJob(microscope, measurementContext);

		// Initialize sub-jobs
		if(inputJob != null && inputJob instanceof TableProducer)
		{
			inputJob.initializeJob(microscope, measurementContext);
			((TableProducer)inputJob).addTableListener(inputListener);
		}
		else
		{
			throw new JobException("No input job defined.");
		}
		if(outputJob != null && outputJob instanceof TableConsumer)
			outputJob.initializeJob(microscope, measurementContext);
		else
		{
			throw new JobException("No output job defined.");
		}
		localEngine = null;
		states.clear();

		// Load script engine.
		List<ScriptEngineFactory> factories = new ScriptEngineManager(ControllerJobImpl.class.getClassLoader()).getEngineFactories();
		ScriptEngineFactory theFactory = null;
		for(ScriptEngineFactory factory : factories)
		{
			if(factory.getEngineName().compareToIgnoreCase(controllerScriptEngine)==0)
			{
				theFactory = factory;
				break;
			}
		}
		if(theFactory == null)
		{
			String message = "No local script engine with name " + controllerScriptEngine + " is registered. Registered engines:\n";
			boolean first = true;
			for(ScriptEngineFactory factory : factories)
			{
				if(first)
					first = false;
				else
					message += ", ";
				message += factory.getEngineName();
			}
			throw new JobException(message);
		}			
		
		localEngine = theFactory.getScriptEngine();
		if(localEngine == null)
			throw new JobException("Could not create local script engine with name " + controllerScriptEngine + ".");

		// Set output writer of engine
		localEngine.getContext().setWriter(scriptOutputListener);
		
		// if the script engine is Matlab, evaluate something to start the engine up
		if(theFactory.getEngineName().equals("Matlab Scripting"))
		{
			try
			{
				localEngine.eval("disp('Communication to Matlab established successfully.')");
			}
			catch(ScriptException e)
			{
				throw new JobException("Communication to Matlab could not be established.", e);
			}
		}
		
		receiveEngineMessages();			
	}
	
	@Override
	public void uninitializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		super.uninitializeJob(microscope, measurementContext);
		localEngine = null;
		if(inputJob != null)
		{
			inputJob.uninitializeJob(microscope, measurementContext);
			((TableProducer)inputJob).removeTableListener(inputListener);
		}
		if(outputJob != null)
			outputJob.uninitializeJob(microscope, measurementContext);
		
		states.clear();
	}
	
	private void receiveEngineMessages()
	{
		scriptOutputListener.flush();
		sendMessage(scriptOutputListener.toString());
		scriptOutputListener.getBuffer().setLength(0);
	}

	@Override
	public int getNumJobs() throws RemoteException {
		return (inputJob != null ? 1 : 0) + (outputJob != null ? 1 : 0); 
	}

	@Override
	public Job getJob(int jobIndex) throws RemoteException, IndexOutOfBoundsException {
		return getJobs()[jobIndex];
	}

	@Override
	public void addJob(Job job) throws RemoteException, ComponentRunningException, JobException {
		throw new JobException("Use setInputJob() or setOutputJob() to edit child jobs.");
	}

	@Override
	public void insertJob(Job job, int jobIndex)
			throws RemoteException, ComponentRunningException, IndexOutOfBoundsException, JobException {
		throw new JobException("Use setInputJob() or setOutputJob() to edit child jobs.");
	}

	@Override
	public void removeJob(int jobIndex)
			throws RemoteException, ComponentRunningException, IndexOutOfBoundsException, JobException {
		throw new JobException("Use setInputJob() or setOutputJob() to edit child jobs.");
	}

	@Override
	public void clearJobs() throws RemoteException, ComponentRunningException, JobException {
		throw new JobException("Use setInputJob() or setOutputJob() to edit child jobs.");
	}
}
