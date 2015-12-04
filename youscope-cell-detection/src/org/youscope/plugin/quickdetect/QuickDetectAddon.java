/**
 * 
 */
package org.youscope.plugin.quickdetect;

import java.io.BufferedReader;
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
import org.youscope.common.ImageAdapter;
import org.youscope.common.ImageEvent;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.ResourceConfiguration;
import org.youscope.common.measurement.MeasurementContext;
import org.youscope.common.measurement.PositionInformation;
import org.youscope.common.measurement.resource.ResourceAdapter;
import org.youscope.common.measurement.resource.ResourceException;
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
	private ScriptEngine scriptEngine;
	private final StringWriter outputListener = new StringWriter();
	private final ArrayList<TableListener> tableListeners = new ArrayList<TableListener>();
	QuickDetectAddon(PositionInformation positionInformation, ResourceConfiguration configuration) throws ConfigurationException
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
			String message = "No local script engine with name \"Matlab Scripting\" is registered. Registered engines:\n";
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
			throw new CellDetectionException("Could not create local script engine with name \"Matlab Scripting\".");
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

	}

	@Override
	public void uninitialize(MeasurementContext measurementContext) throws ResourceException, RemoteException
	{
		scriptEngine = null;
		super.uninitialize(measurementContext);
	}

	@Override
	public CellDetectionResult detectCells(ImageEvent detectionImage) throws CellDetectionException, RemoteException
	{
		return detectCells(detectionImage, new ImageEvent[0]);
	}
	@Override
	public CellDetectionResult detectCells(ImageEvent detectionImage, ImageEvent[] quantificationImages) throws CellDetectionException, RemoteException
	{
		if(!isInitialized())
			throw new CellDetectionException("Addon not yet initialized.");
		if(detectionImage == null)
			throw new CellDetectionException("Image in which cells should be detected is null.");
		
		QuickDetectConfiguration configuration = getConfiguration();
		
		// Pass parameters to script
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
		
		TableSinkImpl tableSink = new TableSinkImpl();
		
		scriptEngine.put("tableSink", tableSink);
		scriptEngine.put("imageEvent", detectionImage);
		scriptEngine.put("quantImages", quantificationImages);
		scriptEngine.put("threshold", configuration.getDetectionThreshold()); 
		scriptEngine.put("internalBinning", configuration.getInternalBinning());
		scriptEngine.put("maxCellDiameter", configuration.getMaxCellDiameter());
		scriptEngine.put("minCellDiameter", configuration.getMinCellDiameter());
		
		// Open & eval matlab file
		URL matlabFile = getClass().getClassLoader().getResource("org/youscope/plugin/quickdetect/QuickDetectInvoker.m");
		InputStreamReader fileReader = null;
		BufferedReader bufferedReader = null;
		try
		{
			fileReader = new InputStreamReader(matlabFile.openStream());
			bufferedReader = new BufferedReader(fileReader);
			scriptEngine.eval(bufferedReader);
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
				} catch (@SuppressWarnings("unused") IOException e) {
					// do nothing.
				}
			}
			if(fileReader != null)
			{
				try
				{
					fileReader.close();
				}
				catch(@SuppressWarnings("unused") IOException e1)
				{
					// Do nothing.
				}
			}
		}
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
