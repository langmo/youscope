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
package org.youscope.plugin.cellx;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.youscope.addon.celldetection.CellDetectionAddon;
import org.youscope.addon.celldetection.CellDetectionException;
import org.youscope.addon.celldetection.CellDetectionResult;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.image.ImageAdapter;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.resource.ResourceAdapter;
import org.youscope.common.resource.ResourceException;
import org.youscope.common.table.Table;
import org.youscope.common.table.TableDataAdapter;
import org.youscope.common.table.TableDefinition;
import org.youscope.common.table.TableListener;

/**
 * Uses the CellX cell detection environment to detect cells in microscope images (e.g. out-of-focus) and quantify fluorescence images.
 * @author Moritz Lang
 *
 */
class CellXAddon extends ResourceAdapter<CellXConfiguration> implements CellDetectionAddon
{
	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -7479289653519664098L;
	private ScriptEngine scriptEngine;
	private final StringWriter outputListener = new StringWriter();
	private volatile CellXLastResult lastResult = null;
	private final ArrayList<TableListener> tableListeners = new ArrayList<TableListener>();
	CellXAddon(PositionInformation positionInformation, CellXConfiguration configuration) throws ConfigurationException, RemoteException
	{
		super(positionInformation, configuration, CellXConfiguration.TYPE_IDENTIFIER,CellXConfiguration.class, "CellX cell detection.");
	}
	
	@Override
	public void initialize(MeasurementContext measurementContext) throws ResourceException, RemoteException
	{
		if(isInitialized())
			return;
		super.initialize(measurementContext);
		
		CellXConfiguration configuration = getConfiguration();
		
		if(configuration.getConfigurationFile() == null || configuration.getConfigurationFile().length() < 1)
			throw new CellDetectionException("No XML file for CellX detection algorithm set.");
		
		List<ScriptEngineFactory> factories = new ScriptEngineManager(CellXAddon.class.getClassLoader()).getEngineFactories();
		ScriptEngineFactory theFactory = null;
		for(ScriptEngineFactory factory : factories)
		{
			if(factory.getEngineName().compareTo("Matlab Scripting")==0)
			{
				theFactory = factory;
				break;
			}
		}
		if(theFactory == null)
		{
			String message = "No local script engine with name Matlab Scripting is registered. Registered engines:\n";
			boolean first = true;
			for(ScriptEngineFactory factory : factories)
			{
				if(first)
					first = false;
				else
					message += ", ";
				message += factory.getEngineName();
			}
			throw new CellDetectionException(message);
		}			
		
		scriptEngine = theFactory.getScriptEngine();
		if(scriptEngine == null)
			throw new CellDetectionException("Could not create local script engine with name Matlab Scripting.");
		// Set output writer of engine
		scriptEngine.getContext().setWriter(outputListener);
		Object returnVal;
		try
		{
			returnVal = scriptEngine.eval("disp('CellX Cell Detection is initialized.')");
		}
		catch(ScriptException e)
		{
			throw new CellDetectionException("Matlab is not interpreting messages as expected.", e);
		}
		receiveEngineMessages();	
		if(returnVal != null)
			sendMessage(returnVal.toString());

	}

	@Override
	public void uninitialize(MeasurementContext measurementContext) throws ResourceException, RemoteException
	{
		scriptEngine = null;
		lastResult = null;
		super.uninitialize(measurementContext);
	}

	@Override
	public CellDetectionResult detectCells(ImageEvent<?> detectionImage) throws CellDetectionException,RemoteException
	{
		return detectCells(detectionImage, new ImageEvent[0]);
	}
	@Override
	public CellDetectionResult detectCells(ImageEvent<?> detectionImage, ImageEvent<?>[] quantificationImages) throws CellDetectionException, RemoteException
	{
		if(!isInitialized())
			throw new CellDetectionException("Addon not yet initialized.");
		if(detectionImage == null)
			throw new CellDetectionException("Image in which cells should be detected is null.");
		
		TableDataAdapter tableDataSink;
		try
		{
			tableDataSink = new TableDataAdapter();
		}
		catch(RemoteException e1)
		{
			throw new CellDetectionException("Could not create table data adapter for matlab cell detection script.", e1);
		}
		CellXLastResult currentResult;
		try
		{
			currentResult = new CellXLastResultImpl();
		}
		catch(RemoteException e)
		{
			throw new CellDetectionException("Could not create object to store CellX's current result.", e);
		}
		
		// Pass parameters to script
		CellXConfiguration configuration = getConfiguration();
		ImageAdapter imageSink = null;
		if(configuration.isGenerateLabelImage())
		{
			try
			{
				imageSink = new ImageAdapter();
			}
			catch(RemoteException e1)
			{
				throw new CellDetectionException("Could not create image adapter for cell detection script.", e1);
			}
			scriptEngine.put("imageSink", imageSink);
		}
		scriptEngine.put("tableDataSink", tableDataSink);
		scriptEngine.put("detectionImage", detectionImage);
		scriptEngine.put("fluorescenceImages", quantificationImages);
		scriptEngine.put("configFileName", configuration.getConfigurationFile());
		//scriptEngine.put("debug_mode", 1);
		scriptEngine.put("lastResult", lastResult);
		scriptEngine.put("currentResult", currentResult);
		scriptEngine.put("trackCells", configuration.isTrackCells() ? 1 : 0);
		
		File scriptsFolder = new File("cellx/");
		if(!scriptsFolder.exists() || !scriptsFolder.isDirectory())
			throw new CellDetectionException("CellX folder ("+scriptsFolder.getAbsolutePath()+") does not exist. Check your installation.");			
		scriptEngine.put("scriptsFolder", scriptsFolder.getAbsolutePath());
		
		// Open & eval matlab file
		URL matlabFile = getClass().getClassLoader().getResource("org/youscope/plugin/cellx/CellXInvoker.m");
		if(matlabFile == null)
			throw new CellDetectionException("Could not detect matlab script in CellX JAR file. Check file consistency.");
		InputStreamReader fileReader = null;
		
		Object returnVal;
		scriptEngine.getContext().setWriter(outputListener);
		BufferedReader bufferedReader = null;
		try
		{
			fileReader = new InputStreamReader(matlabFile.openStream());
			bufferedReader = new BufferedReader(fileReader);
			returnVal = scriptEngine.eval(bufferedReader);
		}
		catch(ScriptException ex)
		{
			String errorMessage = "Error in script on line " + ex.getLineNumber() + ", column " + ex.getColumnNumber() + ".";
			Throwable cause = ex;
			while(true)
			{
				errorMessage += "\n" + cause.getMessage();
				cause = cause.getCause();
				if(cause == null)
					break;
			}
			throw new CellDetectionException(errorMessage);
		}
		catch(IOException e1)
		{
			throw new CellDetectionException("Script file " + matlabFile.toString() + " could not be opened.", e1);
		}
		finally
		{
			if(bufferedReader != null)
			{
				try {
					bufferedReader.close();
				} catch (IOException e) {
					sendErrorMessage("Could not close IO Buffer.", e);
				}
			}
			if(fileReader != null)
			{
				try
				{
					fileReader.close();
				}
				catch(IOException e1)
				{
					sendErrorMessage("Could not close file.", e1);
				}
			}
		}
		receiveEngineMessages();
		if(returnVal != null)
			sendMessage(returnVal.toString());
		
		// TODO: update script for new table layout.
		Table table = tableDataSink.clear();
		sendTableToListeners(table);		
		CellDetectionResult result = new CellDetectionResult(table, imageSink == null ? null : imageSink.clearImage());
		
		lastResult = currentResult;
		return result;
	}
	
	private void receiveEngineMessages()
	{
		outputListener.flush();
		String message = outputListener.toString();
		outputListener.getBuffer().setLength(0);
		if(message != null && message.length() > 0)
			sendMessage(message);
	}

	private void sendTableToListeners(Table table)
	{
		synchronized(tableListeners)
		{
			for(Iterator<TableListener> iterator = tableListeners.iterator(); iterator.hasNext();)
			{
				TableListener listener = iterator.next();
				try {
					listener.newTableProduced(table.clone());
				} catch (@SuppressWarnings("unused") RemoteException e) {
					iterator.remove();
				}
			}
		}
	}
	
	@Override
	public void removeTableListener(TableListener listener) throws RemoteException {
		synchronized(tableListeners)
		{
			tableListeners.add(listener);
		}
	}

	@Override
	public void addTableListener(TableListener listener) throws RemoteException {
		synchronized(tableListeners)
		{
			tableListeners.remove(listener);
		}
	}

	@Override
	public TableDefinition getProducedTableDefinition() throws RemoteException {
		return CellXTable.getTableDefinition();
	}	
}
