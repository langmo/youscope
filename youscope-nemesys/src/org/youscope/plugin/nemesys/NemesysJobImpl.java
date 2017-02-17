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
package org.youscope.plugin.nemesys;

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
class NemesysJobImpl extends JobAdapter implements NemesysJob, NemesysScriptCallback
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -343965116066788997L;
	
	private final HashMap<String, String> states = new HashMap<String,String>();
	private String scriptEngine = "Mozilla Rhino";
	
	private String script = "";
	
	private ScriptEngine				localEngine					= null;
	
	private final StringWriter scriptOutputListener = new StringWriter();
	
	private String nemesysDeviceName = null;
	
	private Device nemesysDevice = null;
	
	private int numDosingUnits = -1;
	
	private TimingScriptEngine timingTable = null;
	
	private volatile Table tableToEvaluate = null;
	
	private final ArrayList<TableListener> tableListeners = new ArrayList<TableListener>();
	
	public NemesysJobImpl(PositionInformation positionInformation) throws RemoteException
	{
		super(positionInformation);
	}
	@Override
	public void setScriptEngine(String engine) throws RemoteException, ComponentRunningException
	{
		if(engine == null)
			this.scriptEngine = "Mozilla Rhino";
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
			else if(timing.flowRates.length != numDosingUnits)
			{
				throw new JobException("Nemesys timing protocol has different number of dosing units than are currently connected.");
			}
			else
			{
				for(int i=0; i<numDosingUnits; i++)
				{
					try
					{
						this.setFlowRate(i, timing.flowRates[i]);
					}
					catch(NemesysException e)
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
			localEngine.put("nemesys", this);
			
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
				throw new JobException("Nemesys syringe job failed due to error in control script.", e);
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
						sendErrorMessage("Could not close buffer for control script.", e);
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
		TableListener[] listeners = tableListeners.toArray(new TableListener[tableListeners.size()]);
		if(listeners.length > 0)
		{
			Table monitorTable = new Table(getProducedTableDefinition(), measurementContext.getMeasurementRuntime(), getPositionInformation(), executionInformation);
			for(int i=0; i<numDosingUnits; i++)
			{
				try
				{
					monitorTable.addRow(new Integer(i), new Double(getFlowRate(i)), new Double(getVolume(i)));
				}
				catch(NemesysException e)
				{
					throw new JobException("Could not obtain current flow rate or volume of syringe " + Integer.toString(i) + ".", e);
				}
				catch(TableException e)
				{
					throw new JobException("Could not add current flow rate to monitor table", e);
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
		return "Nemesys Job";
	}

	@Override
	public void initializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		super.initializeJob(microscope, measurementContext);
		
		localEngine = null;
		states.clear();
		nemesysDevice = null;
		numDosingUnits = 0;
		tableToEvaluate = null;
		if(nemesysDeviceName == null)
			throw new JobException("Name of Nemesys device not specified", new NullPointerException());
		
		
		try
		{
			nemesysDevice = microscope.getDevice(nemesysDeviceName);
		}
		catch(DeviceException e)
		{
			throw new JobException("Nemesys device could not be found.", e);
		}
		if(!nemesysDevice.getDriverID().equals("Nemesys") || !nemesysDevice.getLibraryID().equals("NemesysPump"))
		{
			throw new JobException("Device with ID "+nemesysDeviceName+" is not a Nemesys device driver.");
		}
		
		try
		{
			numDosingUnits = Integer.parseInt(nemesysDevice.getProperty("numDosingUnits").getValue());
		}
		catch(NumberFormatException e)
		{
			throw new JobException("Could not parse number of dosing units.", e);
		}
		catch(MicroscopeException e)
		{
			throw new JobException("Could not obtain number of dosing units.", e);
		}
		catch(DeviceException e)
		{
			throw new JobException("Could not obtain number of dosing units.", e);
		}

		// Load script engine.
		if(NemesysJobConfiguration.SCRIPT_ENGINE_TIMETABLE.equals(scriptEngine))
		{
			try
			{
				timingTable = new TimingScriptEngine(script);
			}
			catch(NemesysException e)
			{
				throw new JobException("Error while initializing Nemesys protocol.", e);
			}
		}
		else
		{
			List<ScriptEngineFactory> factories = new ScriptEngineManager(NemesysJobImpl.class.getClassLoader()).getEngineFactories();
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
		nemesysDevice = null;
		numDosingUnits = -1;
		tableToEvaluate = null;
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
		return NemesysControlTable.getTableDefinition();
	}
	
	@Override
	public int getNumberOfDosingUnits() throws RemoteException, NemesysException, InterruptedException
	{
		return numDosingUnits;
	}
	@Override
	public double getFlowRateMax(int dosingUnit) throws RemoteException, NemesysException, InterruptedException
	{
		if(dosingUnit < 0 || dosingUnit >= numDosingUnits)
			throw new NemesysException("The parameter dosingUnit must be bigger or equal to 0 and smaller than " + Integer.toString(numDosingUnits) + " (" + Integer.toString(numDosingUnits) + " dosing units connected to Nemesys device)");
		try
		{
			return Double.parseDouble(nemesysDevice.getProperty("syringe" + Integer.toString(dosingUnit+1) + ".flowRateMax").getValue());
		}
		catch(NumberFormatException e)
		{
			throw new NemesysException("Could not parse maximal flow rate.", e);
		}
		catch(MicroscopeException e)
		{
			throw new NemesysException("Could not obtain maximal flow rate.", e);
		}
		catch(DeviceException e)
		{
			throw new NemesysException("Could not obtain maximal flow rate.", e);
		}
	}
	@Override
	public void setFlowRate(int dosingUnit, double flowRate) throws RemoteException, NemesysException, InterruptedException
	{
		if(dosingUnit < 0 || dosingUnit >= numDosingUnits)
			throw new NemesysException("The parameter dosingUnit must be bigger or equal to 0 and smaller than " + Integer.toString(numDosingUnits) + " (" + Integer.toString(numDosingUnits) + " dosing units connected to Nemesys device)");
		try
		{
			nemesysDevice.getProperty("syringe" + Integer.toString(dosingUnit+1) + ".flowRate").setValue(Double.toString(flowRate));
		}
		catch(MicroscopeException e)
		{
			throw new NemesysException("Could not set flow rate.", e);
		}
		catch(DeviceException e)
		{
			throw new NemesysException("Could not set flow rate.", e);
		}
		catch(MicroscopeLockedException e)
		{
			throw new NemesysException("Could not set flow rate.", e);
		}
	}
	
	@Override
	public double getFlowRate(int dosingUnit) throws RemoteException, NemesysException, InterruptedException
	{
		if(dosingUnit < 0 || dosingUnit >= numDosingUnits)
			throw new NemesysException("The parameter dosingUnit must be bigger or equal to 0 and smaller than " + Integer.toString(numDosingUnits) + " (" + Integer.toString(numDosingUnits) + " dosing units connected to Nemesys device)");
		try
		{
			return Double.parseDouble(nemesysDevice.getProperty("syringe" + Integer.toString(dosingUnit+1) + ".flowRate").getValue());
		}
		catch(NumberFormatException e)
		{
			throw new NemesysException("Could not parse flow rate.", e);
		}
		catch(MicroscopeException e)
		{
			throw new NemesysException("Could not obtain flow rate.", e);
		}
		catch(DeviceException e)
		{
			throw new NemesysException("Could not obtain flow rate.", e);
		}
	}
	public double getVolume(int dosingUnit) throws RemoteException, NemesysException, InterruptedException
	{
		if(dosingUnit < 0 || dosingUnit >= numDosingUnits)
			throw new NemesysException("The parameter dosingUnit must be bigger or equal to 0 and smaller than " + Integer.toString(numDosingUnits) + " (" + Integer.toString(numDosingUnits) + " dosing units connected to Nemesys device)");
		try
		{
			return Double.parseDouble(nemesysDevice.getProperty("syringe" + Integer.toString(dosingUnit+1) + ".volume").getValue());
		}
		catch(NumberFormatException e)
		{
			throw new NemesysException("Could not parse volume.", e);
		}
		catch(MicroscopeException e)
		{
			throw new NemesysException("Could not obtain flow rate.", e);
		}
		catch(DeviceException e)
		{
			throw new NemesysException("Could not obtain flow rate.", e);
		}
	}
	@Override
	public String getFlowUnit(int dosingUnit) throws RemoteException, NemesysException, InterruptedException
	{
		if(dosingUnit < 0 || dosingUnit >= numDosingUnits)
			throw new NemesysException("The parameter dosingUnit must be bigger or equal to 0 and smaller than " + Integer.toString(numDosingUnits) + " (" + Integer.toString(numDosingUnits) + " dosing units connected to Nemesys device)");
		try
		{
			return nemesysDevice.getProperty("syringe" + Integer.toString(dosingUnit+1) + ".flowUnit").getValue();
		}
		catch(NumberFormatException e)
		{
			throw new NemesysException("Could not parse flow rate.", e);
		}
		catch(MicroscopeException e)
		{
			throw new NemesysException("Could not obtain flow rate.", e);
		}
		catch(DeviceException e)
		{
			throw new NemesysException("Could not obtain flow rate.", e);
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
	public String getNemesysDeviceName()
	{
		return nemesysDeviceName;
	}
	@Override
	public void setNemesysDeviceName(String deviceName) throws RemoteException, ComponentRunningException
	{
		assertRunning();
		nemesysDeviceName = deviceName;
		
	}
	
	@Override
	public void removeTableListener(TableListener listener)
	{
		synchronized(tableListeners)
		{
			tableListeners.remove(listener);
		}
	}

	@Override
	public void addTableListener(TableListener listener)
	{
		synchronized(tableListeners)
		{
			tableListeners.add(listener);
		}
	}

	@Override
	public TableDefinition getProducedTableDefinition()
	{
		return NemesysStateTable.getTableDefinition();
	}

}
