/**
 * 
 */
package ch.ethz.csb.youscope.addon.matlabfocusscores;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import ch.ethz.csb.youscope.shared.ImageEvent;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.ResourceConfiguration;
import ch.ethz.csb.youscope.shared.measurement.MeasurementContext;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.resource.ResourceAdapter;
import ch.ethz.csb.youscope.shared.measurement.resource.ResourceException;
import ch.ethz.csb.youscope.shared.resource.focusscore.FocusScoreResource;

/**
 * @author Moritz Lang
 *
 */
class MatlabScoresAddon  extends ResourceAdapter<MatlabScoresConfiguration> implements FocusScoreResource
{
	private ScriptEngine scriptEngine = null;
	private static final StringWriter ENGINE_WRITER = new StringWriter();
	MatlabScoresAddon(PositionInformation positionInformation, ResourceConfiguration configuration) throws ConfigurationException
	{
		super(positionInformation, configuration, MatlabScoresConfiguration.CONFIGURATION_ID, MatlabScoresConfiguration.class, "Matlab focus score");
	}
	
	@Override
	public synchronized void initialize(MeasurementContext measurementContext) throws ResourceException, RemoteException
	{
		if(isInitialized())
			return;
		super.initialize(measurementContext);
		 
		List<ScriptEngineFactory> factories = new ScriptEngineManager(MatlabScoresAddon.class.getClassLoader()).getEngineFactories();
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
			throw new ResourceException(message);
		}			
		
		scriptEngine = theFactory.getScriptEngine();
		if(scriptEngine == null)
			throw new ResourceException("Could not create local script engine with name \"Matlab Scripting\".");
		// Set output writer of engine
		scriptEngine.getContext().setWriter(ENGINE_WRITER);
		try
		{
			scriptEngine.eval("disp('Matlab score is initialized.')");
		}
		catch(ScriptException e)
		{
			throw new ResourceException("Matlab is not interpreting messages as expected.", e);
		}
		receiveEngineMessages();
	}
	private void receiveEngineMessages()
	{
		synchronized(ENGINE_WRITER)
		{
			ENGINE_WRITER.flush();
			String message = ENGINE_WRITER.toString();
			if(message != null && message.length() > 0)
				sendMessage(message);
			ENGINE_WRITER.getBuffer().setLength(0);
		}
	}
	@Override
	public synchronized void uninitialize(MeasurementContext measurementContext) throws ResourceException, RemoteException
	{
		scriptEngine = null;
		super.uninitialize(measurementContext);
	}
	
	@Override
	public double calculateScore(ImageEvent e) throws ResourceException, RemoteException
	{
		assertInitialized();
		MatlabScoresConfiguration configuration = getConfiguration();
		if(e == null)
			throw new ResourceException("Image for which focus score should be calculated is null.");

		// Create image sink if highlighted image should be generated.
		class FocusSinkImpl extends UnicastRemoteObject implements FocusSink
		{
			/**
			 * Generated Serial Version UID.
			 */
			private static final long	serialVersionUID	= 7509421301791026841L;
			public FocusSinkImpl() throws RemoteException
			{
				// do nothing.
			}
			private volatile double lastScore = -1.0;
			@Override
			public void setScore(double score)
			{
				lastScore = score;
			}

			@Override
			public double getLastScore()
			{
				return lastScore;
			}
			
		}
		FocusSinkImpl focusSink;
		try
		{
			focusSink = new FocusSinkImpl();
		}
		catch(RemoteException e2)
		{
			throw new ResourceException("Could not create remote focus score sink.", e2);
		}
		
		// Pass parameters to script
		scriptEngine.put("focusSink", focusSink);
		scriptEngine.put("imageEvent", e);
		receiveEngineMessages();
		
		// Open & eval matlab file
		String matlabFileURL = null;
		switch(configuration.getScoreAlgorithm())
		{
			case HISTOGRAM_RANGE:
				matlabFileURL = "ch/ethz/csb/youscope/addon/matlabfocusscores/HistogramRangeInvoker.m";
				break;
			case SOBEL3:
				matlabFileURL = "ch/ethz/csb/youscope/addon/matlabfocusscores/Sobel3Invoker.m";
				break;
			case SOBEL5:
				matlabFileURL = "ch/ethz/csb/youscope/addon/matlabfocusscores/Sobel5Invoker.m";
				break;
			case SOBEL7:
				matlabFileURL = "ch/ethz/csb/youscope/addon/matlabfocusscores/Sobel7Invoker.m";
				break;
			case NORMALIZED_VARIANCES:
				matlabFileURL = "ch/ethz/csb/youscope/addon/matlabfocusscores/NormalizedVarianceInvoker.m";
				break;
		}
		
		URL matlabFile = getClass().getClassLoader().getResource(matlabFileURL);
		if(matlabFile == null)
			throw new ResourceException("Could not find Matlab script to calculate focus score. Expected location: " + matlabFileURL);
		InputStreamReader fileReader = null;
		BufferedReader bufferedReader;
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
			throw new ResourceException(errorMessage);
		}
		catch(IOException e1)
		{
			throw new ResourceException("Script file " + matlabFile.toString() + " could not be opened.", e1);
		}
		finally
		{
			receiveEngineMessages();
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
		
		return focusSink.getLastScore();
	}
}
