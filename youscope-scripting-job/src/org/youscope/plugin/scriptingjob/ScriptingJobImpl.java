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
package org.youscope.plugin.scriptingjob;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.callback.CallbackException;
import org.youscope.common.image.ImageProducer;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobAdapter;
import org.youscope.common.job.JobException;
import org.youscope.common.job.basicjobs.ScriptingJob;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.scripting.MicroscopeScriptException;
import org.youscope.common.scripting.RemoteScriptEngine;
import org.youscope.common.scripting.ScriptImageStorage;
import org.youscope.common.scripting.ScriptInterface;

/**
 * @author langmo
 */
class ScriptingJobImpl extends JobAdapter implements ScriptingJob
{
	/**
	 * Serial Version UID.
	 */
	private static final long			serialVersionUID			= -6144168331912515357L;

	private final ArrayList<Job>	jobs						= new ArrayList<Job>();

	private String						scriptEngineName			= null;

	private URL							scriptFile					= null;

	private RemoteScriptEngine			remoteEngine				= null;
	private ScriptEngine				localEngine					= null;

	private final Vector<String>		userKeys					= new Vector<String>();

	private final Vector<Object>		userValues					= new Vector<Object>();

	private final Vector<Job>	jobQueue					= new Vector<Job>();
	
	private final StringWriter outputListener = new StringWriter();

	public ScriptingJobImpl(PositionInformation positionInformation) throws RemoteException
	{
		super(positionInformation);
	}

	@Override
	protected String getDefaultName()
	{
		return "script.execute(" + scriptFile.toString() + ")";
	}

	@Override
	public void initializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		super.initializeJob(microscope, measurementContext);

		// Initialize sub-jobs
		for(Job job : jobs)
		{
			job.initializeJob(microscope, measurementContext);
		}
		
		remoteEngine = null;
		localEngine = null;

		// Check configuration
		if(scriptEngineName == null)
			throw new JobException("Engine name is null.");
		if(scriptFile == null)
			throw new JobException("No file to evaluate.");

		// Get and initialize script engine
		if(remoteEngine != null)
		{
			// Initialize engine
			try
			{
				remoteEngine.initializeCallback();
			}
			catch(CallbackException e)
			{
				throw new JobException("Could not initialize remote script engine.", e);
			}

			// Set objects which can be accessed in script.
			// Give script standard variables.
			remoteEngine.put("jobs", jobs.toArray(new Job[jobs.size()]));
			remoteEngine.put("microscope", microscope);

			// User defined variables
			for(int i = 0; i < userKeys.size() && i < userValues.size(); i++)
			{
				if(userKeys.elementAt(i) == null || userKeys.elementAt(i).length() < 1)
					continue;
				remoteEngine.put(userKeys.elementAt(i), userValues.elementAt(i));
			}

			// Set current evaluation number.
			remoteEngine.put("evaluationNumber", 0);
		}
		else // == local script engine
		{
			// Load script engine.
			List<ScriptEngineFactory> factories = new ScriptEngineManager(ScriptingJobImpl.class.getClassLoader()).getEngineFactories();
			ScriptEngineFactory theFactory = null;
			for(ScriptEngineFactory factory : factories)
			{
				if(factory.getEngineName().compareToIgnoreCase(scriptEngineName)==0)
				{
					theFactory = factory;
					break;
				}
			}
			if(theFactory == null)
			{
				String message = "No local script engine with name " + scriptEngineName + " is registered. Registered engines:\n";
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
				throw new JobException("Could not create local script engine with name " + scriptEngineName + ".");

			// Set output writer of engine
			localEngine.getContext().setWriter(outputListener);
			receiveEngineMessages();			
			
			// Set objects which can be accessed in script.
			// Give script standard variables.
			localEngine.put("jobs", jobs.toArray(new Job[jobs.size()]));
			localEngine.put("microscope", microscope);
			
			// User defined variables
			for(int i = 0; i < userKeys.size() && i < userValues.size(); i++)
			{
				if(userKeys.elementAt(i) == null || userKeys.elementAt(i).length() < 1)
					continue;
				localEngine.put(userKeys.elementAt(i), userValues.elementAt(i));
			}

			// Set current evaluation number.
			localEngine.put("evaluationNumber", 0);
			receiveEngineMessages();
		}
	}
	
	private void receiveEngineMessages()
	{
		outputListener.flush();
		sendMessage(outputListener.toString());
		outputListener.getBuffer().setLength(0);
	}

	@Override
	public void uninitializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		super.uninitializeJob(microscope, measurementContext);
		if(remoteEngine != null)
		{
			try
			{
				remoteEngine.uninitializeCallback();
			}
			catch(@SuppressWarnings("unused") CallbackException e)
			{
				// do nothing.
			}
		}
		localEngine = null;
		
		// Uninitialize sub-jobs
		for(Job job : jobs)
		{
			job.uninitializeJob(microscope, measurementContext);
		}

	}

	@Override
	public void runJob(ExecutionInformation executionInformation, Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		try
		{
			if(localEngine != null)
			{
				// Set current evaluation number and well/position
				localEngine.put("evaluationNumber", executionInformation.getEvaluationNumber());
				if(getPositionInformation().getWell() != null)
					localEngine.put("well", getPositionInformation().getWell().getWellName());
				int[] pos = getPositionInformation().getPositions(); 
				if(pos != null && pos.length > 0)
					localEngine.put("position", pos);
				
				// Open file
				InputStreamReader fileReader = null;
				BufferedReader bufferedReader = null;
				try
				{
					fileReader = new InputStreamReader(scriptFile.openStream());
					bufferedReader = new BufferedReader(fileReader);
					localEngine.eval(bufferedReader);
				}
				finally
				{
					if(fileReader != null)
						fileReader.close();
					if(bufferedReader != null)
						bufferedReader.close();
				}
				receiveEngineMessages();
			}
			else
			{
				// Set current evaluation number, well and position
				remoteEngine.put("evaluationNumber", executionInformation.getEvaluationNumber());
				if(getPositionInformation().getWell() != null)
					remoteEngine.put("well", getPositionInformation().getWell().getWellName());
				int[] pos = getPositionInformation().getPositions(); 
				if(pos != null && pos.length > 0)
					remoteEngine.put("position", pos);
				
				// evaluate script
				remoteEngine.eval(scriptFile);
			}
		}
		catch(ScriptException ex)
		{
			String errorMessage = "Error in script on line " + ex.getLineNumber() + ", column " + ex.getColumnNumber() + ".";
			throw new JobException(errorMessage, ex);
		}
		catch(IOException e)
		{
			throw new JobException("Script file was not found or could not be opened.", e);
		}

		// Evaluate all jobs started by script
		for(Job job : jobQueue)
		{
			job.executeJob(executionInformation, microscope, measurementContext);
		}
		jobQueue.clear();
	}

	@Override
	public void putVariable(String key, Object value)
	{
		userKeys.add(key);
		userValues.add(value);
	}

	class ScriptInterfaceImpl extends UnicastRemoteObject implements ScriptInterface
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -3597461042806721456L;

		ScriptInterfaceImpl() throws RemoteException
		{
			super();
		}

		@Override
		public void startJob(Job job) throws MicroscopeScriptException
		{
			if(job == null)
				throw new MicroscopeScriptException("Parameter \"job\" must not be a NULL-pointer.");

			// Add job to queue.
			jobQueue.add(job);
		}

		@Override
		public void println(String text) throws RemoteException
		{
			sendMessage(text);	
		}

		@Override
		public ScriptImageStorage createImageStorage(ImageProducer job) throws RemoteException
		{
			try
			{
				return new ScriptImageStorageImpl(job);
			}
			catch(ComponentRunningException e)
			{
				println("Could not produce image storage for script: " + e.getMessage());
				return null;
			}
		}

		@Override
		public ScriptingJob getScriptingJob() throws RemoteException
		{
			return ScriptingJobImpl.this;
		}

	}

	@Override
	public synchronized void addJob(Job job) throws RemoteException, ComponentRunningException
	{
		assertRunning();
		jobs.add(job);
	}

	@Override
	public synchronized void removeJob(int jobIndex) throws RemoteException, ComponentRunningException, IndexOutOfBoundsException
	{
		assertRunning();
		jobs.remove(jobIndex);
	}

	@Override
	public synchronized void clearJobs() throws RemoteException, ComponentRunningException
	{
		assertRunning();
		jobs.clear();
	}

	@Override
	public Job[] getJobs() throws RemoteException
	{
		return jobs.toArray(new Job[jobs.size()]);
	}

	@Override
	public void setScriptEngine(String engine) throws ComponentRunningException
	{
		assertRunning();
		this.scriptEngineName = engine;
	}

	@Override
	public void setScriptFile(URL scriptFile) throws ComponentRunningException
	{
		assertRunning();
		this.scriptFile = scriptFile;

	}

	@Override
	public URL getScriptFile()
	{
		return scriptFile;
	}

	@Override
	public String getScriptEngine() throws RemoteException
	{
		return scriptEngineName;
	}

	@Override
	public void insertJob(Job job, int jobIndex)
			throws RemoteException, ComponentRunningException, IndexOutOfBoundsException {
		assertRunning();
		jobs.add(jobIndex, job);
	}

	@Override
	public int getNumJobs() throws RemoteException {
		return jobs.size();
	}

	@Override
	public Job getJob(int jobIndex) throws RemoteException, IndexOutOfBoundsException {
		return jobs.get(jobIndex);
	}

	@Override
	public void setRemoteScriptEngine(RemoteScriptEngine scriptEngine)
			throws RemoteException, ComponentRunningException {
		assertRunning();
		this.remoteEngine = scriptEngine;
	}
}
