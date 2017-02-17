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
package org.youscope.plugin.glowvisualizer;

import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.youscope.addon.celldetection.CellDetectionResult;
import org.youscope.addon.celldetection.CellVisualizationAddon;
import org.youscope.addon.celldetection.CellVisualizationException;
import org.youscope.addon.celldetection.utils.MatlabFunctionCreator;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.image.ImageAdapter;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.resource.ResourceAdapter;
import org.youscope.common.resource.ResourceException; 

/**
 * @author Moritz Lang
 *
 */
class GlowVisualizerAddon extends ResourceAdapter<GlowVisualizerConfiguration> implements CellVisualizationAddon
{
	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 783142044778589073L;
	private ScriptEngine scriptEngine;
	private final StringWriter outputListener = new StringWriter();
	private MatlabFunctionCreator functionCreator = null;
	private final static String[] MATLAB_INVOKER_ARGUMENTS = {"imageEvent", "detectionResult", "glowStrength", "imageSink"};
	
	 
	GlowVisualizerAddon(PositionInformation positionInformation, GlowVisualizerConfiguration configuration) throws ConfigurationException, RemoteException
	{
		super(positionInformation, configuration, GlowVisualizerConfiguration.CONFIGURATION_ID,GlowVisualizerConfiguration.class, "Glow Visualizer");
	}
	
	@Override
	public void initialize(MeasurementContext measurementContext) throws ResourceException,RemoteException
	{
		super.initialize(measurementContext);
		if(isInitialized())
			return;
		
		List<ScriptEngineFactory> factories = new ScriptEngineManager(GlowVisualizerAddon.class.getClassLoader()).getEngineFactories();
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
			throw new CellVisualizationException(message);
		}			
		
		scriptEngine = theFactory.getScriptEngine();
		if(scriptEngine == null)
			throw new CellVisualizationException("Could not create local script engine with name Matlab Scripting.");
		// Set output writer of engine
		scriptEngine.getContext().setWriter(outputListener);
		try
		{
			scriptEngine.eval("disp('GlowVisualizer cell visualizer is initialized.')");
		}
		catch(ScriptException e)
		{
			throw new CellVisualizationException("Matlab is not interpreting messages as expected.", e);
		}
		receiveEngineMessages();	

		functionCreator = new MatlabFunctionCreator("org/youscope/plugin/glowvisualizer/GlowVisualizerInvoker.m", MATLAB_INVOKER_ARGUMENTS);
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
	public boolean isInitialized()
	{
		return scriptEngine != null;
	}
	
	private void receiveEngineMessages()
	{
		outputListener.flush();
		String message = outputListener.toString();
		outputListener.getBuffer().setLength(0);
		
		sendMessage(message);
	}

	@Override
	public ImageEvent<?> visualizeCells(ImageEvent<?> e, CellDetectionResult detectionResult) throws CellVisualizationException
	{
		if(!isInitialized())
			throw new CellVisualizationException("Addon not yet initialized.");
		if(e == null)
			throw new CellVisualizationException("Image in which cells should be visualized is null.");
		if(detectionResult == null)
			throw new CellVisualizationException("Cell detection result is null.");
		// Create image sink if highlighted image should be generated.
		ImageAdapter imageSink = null;
		try
		{
			imageSink = new ImageAdapter();
		}
		catch(RemoteException e1)
		{
			throw new CellVisualizationException("Could not create image adapter for matlab cell visualization script.", e1);
		}
		
		// Pass parameters to script
		scriptEngine.put("imageSink", imageSink);
		scriptEngine.put("imageEvent", e);
		scriptEngine.put("detectionResult", detectionResult); 
		scriptEngine.put("glowStrength", getConfiguration().getGlowStrength());
		
		
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
			throw new CellVisualizationException(errorMessage, ex);
		}
		receiveEngineMessages();
		
		return imageSink.clearImage();
	}

}
