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
package org.youscope.plugin.quickdetect;

import java.io.StringWriter;
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
import org.youscope.addon.celldetection.utils.MatlabFunctionCreator;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.image.ImageAdapter;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.resource.ResourceAdapter;
import org.youscope.common.resource.ResourceException;
import org.youscope.common.table.Table;
import org.youscope.common.table.TableDefinition;
import org.youscope.common.table.TableListener;

/**
 * An algorithm which uses the image analysis toolbox of Matlab to quickly detect cells in an image.
 * This algorithm is optimized for speed, and thus might return results of poorer quality as compared to other algorithms.
 * It furthermore supports to bin pixels in an image before quantification (i.e. resize the image) to further increase speed.
 * This plugin requires the Matlab scripting plugin to be installed.
 * @author Moritz Lang
 *
 */
class QuickDetectAddon extends ResourceAdapter<QuickDetectConfiguration> implements CellDetectionAddon
{
	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -527721856301731020L;
	private ScriptEngine scriptEngine;
	private final StringWriter outputListener = new StringWriter();
	private final ArrayList<TableListener> tableListeners = new ArrayList<TableListener>();
	private MatlabFunctionCreator functionCreator = null;
	private final static String[] MATLAB_INVOKER_ARGUMENTS = {"imageEvent", "quantImages", "threshold", "internalBinning", "maxCellDiameter", "minCellDiameter", "imageSink", "tableSink"};
	QuickDetectAddon(PositionInformation positionInformation, QuickDetectConfiguration configuration) throws ConfigurationException, RemoteException
	{
		super(positionInformation, configuration, QuickDetectConfiguration.TYPE_IDENTIFIER,QuickDetectConfiguration.class, "Quick-Detect cell detection.");
	}
	
	@Override
	public void initialize(MeasurementContext measurementContext) throws ResourceException,RemoteException
	{
		if(isInitialized())
			return;
		super.initialize(measurementContext);
		
		List<ScriptEngineFactory> factories = new ScriptEngineManager(QuickDetectAddon.class.getClassLoader()).getEngineFactories();
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
		try
		{
			scriptEngine.eval("disp('QuickDetect Cell Detection is initialized.')");
		}
		catch(ScriptException e)
		{
			throw new CellDetectionException("Matlab is not interpreting messages as expected.", e);
		}
		receiveEngineMessages();	

		functionCreator = new MatlabFunctionCreator("org/youscope/plugin/quickdetect/QuickDetectInvoker.m", MATLAB_INVOKER_ARGUMENTS);
		functionCreator.initialize();
	}

	@Override
	public void uninitialize(MeasurementContext measurementContext) throws ResourceException, RemoteException
	{
		scriptEngine = null;
		functionCreator.uninitialize();
		functionCreator = null;
		super.uninitialize(measurementContext);
	}

	@Override
	public CellDetectionResult detectCells(ImageEvent<?> detectionImage) throws CellDetectionException, RemoteException
	{
		return detectCells(detectionImage, new ImageEvent[0]);
	}
	@Override
	public CellDetectionResult detectCells(ImageEvent<?> detectionImage, ImageEvent<?>[] quantificationImages) throws CellDetectionException, RemoteException
	{
		if(!isInitialized())
			throw new CellDetectionException("Addon not yet initialized.");
		else if(detectionImage == null)
			throw new CellDetectionException("Image in which cells should be detected is null.");
		else if(detectionImage.getExecutionInformation() == null || detectionImage.getPositionInformation() == null)
			throw new CellDetectionException("Metadata (position or execution information) of image in which cells should be detected is not initialized.");
		
		QuickDetectConfiguration configuration = getConfiguration();
		
		// Create sinks for matlab results.
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
			
		}
		TableSinkImpl tableSink = new TableSinkImpl(detectionImage.getCreationTime(), detectionImage.getPositionInformation(), detectionImage.getExecutionInformation());
		
		// Send variables to matlab.
		scriptEngine.put("tableSink", tableSink);
		scriptEngine.put("imageEvent", detectionImage);
		scriptEngine.put("quantImages", quantificationImages);
		scriptEngine.put("threshold", configuration.getDetectionThreshold()); 
		scriptEngine.put("internalBinning", configuration.getInternalBinning());
		scriptEngine.put("maxCellDiameter", configuration.getMaxCellDiameter());
		scriptEngine.put("minCellDiameter", configuration.getMinCellDiameter());
		scriptEngine.put("imageSink", imageSink);
		
		// generate function call logic.
		String fullInvokeString = functionCreator.getFullInvokeString();
		
		// Eval matlab script
		try
		{
			scriptEngine.eval(fullInvokeString);
		}
		catch(ScriptException ex)
		{
			String errorMessage = "Error in script on line " + ex.getLineNumber() + ", column " + ex.getColumnNumber() + ". Calling line was \""+fullInvokeString+"\".";
			Throwable cause = ex;
			while(true)
			{
				errorMessage += "\n" + cause.getMessage();
				cause = cause.getCause();
				if(cause == null)
					break;
			}
			throw new CellDetectionException(errorMessage, ex);
		}
		
		// process results.
		receiveEngineMessages();
		
		Table table =tableSink.table;
		sendTableToListeners(table);
		CellDetectionResult result = new CellDetectionResult(table,
				imageSink == null ? null : imageSink.clearImage());
		
		return result;
	}
	
	private void receiveEngineMessages()
	{
		outputListener.flush();
		String message = outputListener.toString();
		outputListener.getBuffer().setLength(0);
		this.sendMessage(message);
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
		return QuickDetectTable.getTableDefinition();
	}	
}
