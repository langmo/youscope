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
package org.youscope.plugin.fluigent;

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
import org.youscope.common.job.JobAdapter;
import org.youscope.common.job.JobException;
import org.youscope.common.microscope.Device;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.table.RowView;
import org.youscope.common.table.Table;
import org.youscope.common.table.TableDefinition;
import org.youscope.common.table.TableException;
import org.youscope.common.table.TableListener;

/**
 * @author Moritz Lang
 *
 */
class FluigentJobImpl extends JobAdapter implements FluigentJob, FluigentScriptCallback
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -341115116066788997L;
	
	private final HashMap<String, String> states = new HashMap<String,String>();
	private String scriptEngine = "Oracle Nashorn";
	
	private String script = "";
	
	private ScriptEngine				localEngine					= null;
	
	private final StringWriter scriptOutputListener = new StringWriter();
	
	private String fluigentDeviceName = null;
	
	private Device fluigentDevice = null;
	
	private TimingScriptEngine timingTable = null;
	
	private volatile Table tableToEvaluate = null;
	
	private int numFlowRateUnits = 0;
	
	private final ArrayList<TableListener> tableDataListeners = new ArrayList<TableListener>();
	
	public FluigentJobImpl(PositionInformation positionInformation) throws RemoteException
	{
		super(positionInformation);
	}
	@Override
	public void setScriptEngine(String engine) throws RemoteException, ComponentRunningException
	{
		if(engine == null)
			this.scriptEngine = "Oracle Nashorn";
		else
			this.scriptEngine = engine;
	}

	@Override
	public String getScriptEngine() throws RemoteException
	{
		return scriptEngine;
	}

	@Override
	public void setScript(String script) throws RemoteException, ComponentRunningException
	{
		if(script == null)
			this.script = "";
		else
			this.script = script;
	}

	@Override
	public String getScript() throws RemoteException
	{
		return script;
	}

	@Override
	public void runJob(ExecutionInformation executionInformation, Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		// Execute input job.
		if(timingTable != null)
		{
			SyringeTableRow timing = timingTable.getActiveSettings(measurementContext.getMeasurementRuntime());
			if(timing == null)
			{
				// do nothing.
			}
			else if(timing.flowRates.length != numFlowRateUnits)
			{
				throw new JobException("Fluigent timing protocol has different number of dosing units than are currently connected.");
			}
			else
			{
				for(int i=0; i<numFlowRateUnits; i++)
				{
					try
					{
						this.setFlowRate(i, timing.flowRates[i]);
					}
					catch(ResourceException e)
					{
						throw new JobException("Error while setting flow rate by protocol.", e);
					}
				}
			}
			
		}
		else
		{
			// Set controller algorithm variables
			localEngine.put("evaluationNumber", executionInformation.getEvaluationNumber());
			localEngine.put("evaluationTime", measurementContext.getMeasurementRuntime());
			localEngine.put("fluigent", this);
			
			// Run controller algorithm.
			StringReader fileReader = null;
			BufferedReader bufferedReader = null;
			Object returnVal;
			localEngine.getContext().setWriter(scriptOutputListener);
			try
			{
				fileReader = new StringReader(script);
				bufferedReader = new BufferedReader(fileReader);
				returnVal = localEngine.eval(bufferedReader);
			}
			catch(ScriptException e)
			{
				throw new JobException("Fluigent syringe job failed due to error in control script.", e);
			}
			finally
			{
				if(fileReader != null)
					fileReader.close();
				if(bufferedReader != null)
				{
					try {
						bufferedReader.close();
					} catch (@SuppressWarnings("unused") IOException e) {
						// do nothing.
					}
				}
			}
			receiveEngineMessages();
			if(returnVal != null && returnVal.toString().length() > 0)
				sendMessage(returnVal.toString());
		}
		
		// execute control input
		Table tableToEvaluate = this.tableToEvaluate;
		this.tableToEvaluate = null;
		if(tableToEvaluate != null)
		{
			for(RowView rowView : tableToEvaluate)
			{
				try
				{
					setFlowRate(rowView.getValue(0, Integer.class).intValue(), 
							rowView.getValue(1, Double.class).doubleValue());
				}
				catch(Exception e)
				{
					throw new JobException("Error while interpreting control inputs.", e);
				} 
			}
		}
		
		// send state information to listeners.
		// make local copy
		TableListener[] listeners = tableDataListeners.toArray(new TableListener[tableDataListeners.size()]);
		if(listeners.length > 0)
		{
			Table monitorTable = new Table(getProducedTableDefinition(), measurementContext.getMeasurementRuntime(), getPositionInformation(), executionInformation);
			for(int i=0; i<numFlowRateUnits; i++)
			{
				try
				{
					monitorTable.addRow(new Integer(i), new Double(getFlowRate(i)));
				}
				catch(ResourceException e)
				{
					throw new JobException("Could not obtain current flow rate of flow unit " + Integer.toString(i+1) + ".", e);
				} catch (TableException e) {
					throw new JobException("Could not save current flow rate of flow unit " + Integer.toString(i+1) + " into output table.", e);
				}
			}
			for(TableListener listener : listeners)
			{
				listener.newTableProduced(monitorTable.clone());
			}
		}
	}

	@Override
	protected String getDefaultName()
	{
		return "Fluigent Job";
	}

	@Override
	public void initializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		super.initializeJob(microscope, measurementContext);
		tableToEvaluate = null;
		localEngine = null;
		states.clear();
		fluigentDevice = null;
		numFlowRateUnits = 0;
		
		if(fluigentDeviceName == null)
			throw new JobException("Name of Fluigent device not specified", new NullPointerException());
		
		
		try
		{
			fluigentDevice = microscope.getDevice(fluigentDeviceName);
		}
		catch(DeviceException e)
		{
			throw new JobException("Fluigent device could not be found.", e);
		}
		if(!fluigentDevice.getDriverID().equals("Fluigent") || !fluigentDevice.getLibraryID().equals("FluigentPump"))
		{
			throw new JobException("Device with ID "+fluigentDeviceName+" is not a Fluigent device driver.");
		}
		
		try
		{
			numFlowRateUnits = Integer.parseInt(fluigentDevice.getProperty("numFlowUnits").getValue());
		}
		catch(NumberFormatException e)
		{
			throw new JobException("Could not parse number of flow units.", e);
		}
		catch(MicroscopeException e)
		{
			throw new JobException("Could not obtain number of flow units.", e);
		}
		catch(DeviceException e)
		{
			throw new JobException("Could not obtain number of flow units.", e);
		}
		
		// Load script engine.
		if(FluigentJobConfiguration.SCRIPT_ENGINE_TIMETABLE.equals(scriptEngine))
		{
			try
			{
				timingTable = new TimingScriptEngine(script);
			}
			catch(ResourceException e)
			{
				throw new JobException("Error while initializing Fluigent protocol.", e);
			}
		}
		else
		{
			List<ScriptEngineFactory> factories = new ScriptEngineManager(FluigentJobImpl.class.getClassLoader()).getEngineFactories();
			ScriptEngineFactory theFactory = null;
			for(ScriptEngineFactory factory : factories)
			{
				if(factory.getEngineName().compareToIgnoreCase(scriptEngine)==0)
				{
					theFactory = factory;
					break;
				}
			}
			if(theFactory == null)
			{
				String message = "No local script engine with name " + scriptEngine + " is registered. Registered engines:\n";
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
				throw new JobException("Could not create local script engine with name " + scriptEngine + ".");
	
			// Set output writer of engine
			localEngine.getContext().setWriter(scriptOutputListener);
			receiveEngineMessages();			
		}
	}
	
	@Override
	public void uninitializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		super.uninitializeJob(microscope, measurementContext);
		localEngine = null;
		tableToEvaluate = null;
		fluigentDevice = null;
		numFlowRateUnits = 0;
		states.clear();
	}
	
	private void receiveEngineMessages()
	{
		scriptOutputListener.flush();
		sendMessage(scriptOutputListener.toString());
		scriptOutputListener.getBuffer().setLength(0);
	}
	@Override
	public void consumeTable(Table table) throws TableException
	{
		if(table == null)
			tableToEvaluate = null;
		else
			tableToEvaluate = table.toTable(getConsumedTableDefinition());
	}
	@Override
	public TableDefinition getConsumedTableDefinition()
	{
		return FluigentControlTable.getTableDefinition();
	}
	
	@Override
	public void setFlowRate(int flowUnit, double flowRate) throws RemoteException, ResourceException, InterruptedException
	{
		if(flowUnit < 0 || flowUnit >= numFlowRateUnits)
			throw new ResourceException("The parameter flowUnit must be bigger or equal to 0 and smaller than " + Integer.toString(numFlowRateUnits) + " (" + Integer.toString(numFlowRateUnits) + " flow units connected to Fluigent device)");
		try
		{
			fluigentDevice.getProperty("flowUnit" + Integer.toString(flowUnit+1) + ".flowRateSetPoint").setValue(Double.toString(flowRate));
		}
		catch(MicroscopeException e)
		{
			throw new ResourceException("Could not set flow rate.", e);
		}
		catch(DeviceException e)
		{
			throw new ResourceException("Could not set flow rate.", e);
		}
		catch(MicroscopeLockedException e)
		{
			throw new ResourceException("Could not set flow rate.", e);
		}
	}
	
	@Override
	public double getFlowRate(int flowUnit) throws RemoteException, ResourceException, InterruptedException
	{
		if(flowUnit < 0 || flowUnit >= numFlowRateUnits)
			throw new ResourceException("The parameter flowUnit must be bigger or equal to 0 and smaller than " + Integer.toString(numFlowRateUnits) + " (" + Integer.toString(numFlowRateUnits) + " flow units connected to Fluigent device)");
		try
		{
			return Double.parseDouble(fluigentDevice.getProperty("flowUnit" + Integer.toString(flowUnit+1) + ".currentFlowRate").getValue());
		}
		catch(NumberFormatException e)
		{
			throw new ResourceException("Could not parse flow rate.", e);
		}
		catch(MicroscopeException e)
		{
			throw new ResourceException("Could not obtain flow rate.", e);
		}
		catch(DeviceException e)
		{
			throw new ResourceException("Could not obtain flow rate.", e);
		}
	}
	public double getPressure(int flowUnit) throws RemoteException, ResourceException, InterruptedException
	{
		if(flowUnit < 0 || flowUnit >= numFlowRateUnits)
			throw new ResourceException("The parameter flowUnit must be bigger or equal to 0 and smaller than " + Integer.toString(numFlowRateUnits) + " (" + Integer.toString(numFlowRateUnits) + " flow units connected to Fluigent device)");
		try
		{
			return Double.parseDouble(fluigentDevice.getProperty("flowUnit" + Integer.toString(flowUnit+1) + ".currentPressure").getValue());
		}
		catch(NumberFormatException e)
		{
			throw new ResourceException("Could not parse pressure.", e);
		}
		catch(MicroscopeException e)
		{
			throw new ResourceException("Could not obtain pressure.", e);
		}
		catch(DeviceException e)
		{
			throw new ResourceException("Could not obtain pressure.", e);
		}
	}
	
	@Override
	public String getStateAsString(String state, String defaultValue) throws RemoteException
	{
		String value = states.get(state);
		if(value == null)
			return defaultValue;
		return value;
	}

	@Override
	public double getStateAsDouble(String state, double defaultValue) throws RemoteException, NumberFormatException
	{
		return Double.parseDouble(getStateAsString(state, Double.toString(defaultValue)));
	}

	@Override
	public int getStateAsInteger(String state, int defaultValue) throws RemoteException, NumberFormatException
	{
		double val = getStateAsDouble(state, defaultValue);
		if(Math.abs(val - ((int)val)) < 0.000000001)
			return (int)val;

		throw new NumberFormatException("State " + state + " has value " + Double.toString(val) + ", which is not an integer.");
	}

	@Override
	public void setState(String state, String value) throws RemoteException
	{
		states.put(state, value);
	}

	@Override
	public void setState(String state, int value) throws RemoteException
	{
		states.put(state, Integer.toString(value));
	}

	@Override
	public void setState(String state, double value) throws RemoteException
	{
		states.put(state, Double.toString(value));
	}
	@Override
	public String getFluigentDeviceName()
	{
		return fluigentDeviceName;
	}
	@Override
	public void setFluigentDeviceName(String deviceName) throws RemoteException, ComponentRunningException
	{
		assertRunning();
		fluigentDeviceName = deviceName;
		
	}
	
	@Override
	public void removeTableListener(TableListener listener)
	{
		synchronized(tableDataListeners)
		{
			tableDataListeners.remove(listener);
		}
	}

	@Override
	public void addTableListener(TableListener listener)
	{
		synchronized(tableDataListeners)
		{
			tableDataListeners.add(listener);
		}
	}

	@Override
	public TableDefinition getProducedTableDefinition()
	{
		return FluigentStateTable.getTableDefinition();
	}

	@Override
	public int getNumberOfFlowUnits() throws RemoteException,
			ResourceException, InterruptedException {
		return numFlowRateUnits;
	}
}
