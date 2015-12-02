/**
 * 
 */
package ch.ethz.csb.youscope.addon.glowvisualizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import ch.ethz.csb.youscope.shared.ImageAdapter;
import ch.ethz.csb.youscope.shared.ImageEvent;
import ch.ethz.csb.youscope.shared.addon.celldetection.CellDetectionResult;
import ch.ethz.csb.youscope.shared.addon.celldetection.CellVisualizationAddon;
import ch.ethz.csb.youscope.shared.addon.celldetection.CellVisualizationException;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.ResourceConfiguration;
import ch.ethz.csb.youscope.shared.measurement.MeasurementContext;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.resource.ResourceAdapter;
import ch.ethz.csb.youscope.shared.measurement.resource.ResourceException;

/**
 * @author Moritz Lang
 *
 */
class GlowVisualizerAddon extends ResourceAdapter<GlowVisualizerConfiguration> implements CellVisualizationAddon
{
	private ScriptEngine scriptEngine;
	private final StringWriter outputListener = new StringWriter();
	
	GlowVisualizerAddon(PositionInformation positionInformation, ResourceConfiguration configuration) throws ConfigurationException
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
			throw new CellVisualizationException(message);
		}			
		
		scriptEngine = theFactory.getScriptEngine();
		if(scriptEngine == null)
			throw new CellVisualizationException("Could not create local script engine with name \"Matlab Scripting\".");
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

	}

	@Override
	public void uninitialize(MeasurementContext measurementContext) throws ResourceException, RemoteException
	{
		scriptEngine = null;
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
	public ImageEvent visualizeCells(ImageEvent e, CellDetectionResult detectionResult) throws CellVisualizationException
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
		File scriptsFolder = new File("scripts/Matlab/tools/");
		if(!scriptsFolder.exists() || !scriptsFolder.isDirectory())
			throw new CellVisualizationException("Scripts folder \"scripts/Matlab/tools/\" does not exist. Check your installation.");			
		scriptEngine.put("scriptsFolder", scriptsFolder.getAbsolutePath());
		
		// Open & eval matlab file
		URL matlabFile = getClass().getClassLoader().getResource("ch/ethz/csb/youscope/addon/glowvisualizer/GlowVisualizerInvoker.m");
		InputStreamReader fileReader = null;
		BufferedReader bufferedReader = null;
		try
		{
			bufferedReader = new BufferedReader(fileReader);
			fileReader = new InputStreamReader(matlabFile.openStream());
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
			throw new CellVisualizationException(errorMessage);
		}
		catch(IOException e1)
		{
			throw new CellVisualizationException("Script file " + matlabFile.toString() + " could not be opened.", e1);
		}
		finally
		{
			if(bufferedReader != null)
			{
				try {
					bufferedReader.close();
				} catch (@SuppressWarnings("unused") IOException e1) {
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
		
		return imageSink.clearImage();
	}

}
