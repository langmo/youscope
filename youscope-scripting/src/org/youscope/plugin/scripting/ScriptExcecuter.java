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
package org.youscope.plugin.scripting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import org.youscope.addon.AddonException;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author langmo
 *
 */
class ScriptExcecuter implements EvaluationListener
{
	private ScriptEngine engine = null;
	private String engineName = null;
	private final YouScopeClient client;
	private final YouScopeServer server;
	private volatile Vector<ScriptMessageListener> messageListeners = new Vector<ScriptMessageListener>();
	private volatile Vector<ScriptVariablesListener> variablesListeners = new Vector<ScriptVariablesListener>();
	
	/**
	 * The listener which gets messages from the script engine.
	 */
	private StringWriter outputListener = new StringWriter();
	
	public ScriptExcecuter(YouScopeClient client, YouScopeServer server)
	{
		this.client = client;
		this.server = server;
	}
	
	public String getCurrentEngineName()
	{
		return engineName;
	}
	
	public void addMessageListener(ScriptMessageListener listener)
	{
		synchronized(messageListeners)
		{
			messageListeners.add(listener);
		}
	}
	public void removeMessageListener(ScriptMessageListener listener)
	{
		synchronized(messageListeners)
		{
			messageListeners.remove(listener);
		}
	}
	
	public void addVariablesListener(ScriptVariablesListener listener)
	{
		synchronized(variablesListeners)
		{
			variablesListeners.add(listener);
		}
	}
	public void removeVariablesListener(ScriptVariablesListener listener)
	{
		synchronized(variablesListeners)
		{
			variablesListeners.remove(listener);
		}
	}
	private void sendInputMessage(String message)
	{
		synchronized(messageListeners)
		{
			for(ScriptMessageListener listener : messageListeners)
			{
				listener.inputMessage(message);
			}
		}
	}
	private void sendOutputMessage(String message)
	{
		synchronized(messageListeners)
		{
			for(ScriptMessageListener listener : messageListeners)
			{
				listener.outputMessage(message);
			}
		}
	}
	
	private void variablesChanged()
	{
		synchronized(variablesListeners)
		{
			if(variablesListeners.size() > 0)
			{
				Set<Entry<String, Object>> variables = getDefinedVariables();
				for(ScriptVariablesListener listener : variablesListeners)
				{
					listener.variablesChanged(variables);
				}
			}
		}
	}
	
	// TODO: Make work again.
	/*
	public void initializeDebugMode(ScriptingJob scriptingJob) throws Exception
	{	
		scriptingJob.setScriptEngine(new RemoteScriptEngineAndFactoryImpl(engine, engineName));
		Microscope microscope = server.getMicroscope();
		scriptingJob.initializeJob(microscope);
		variablesChanged();
	}*/
	
	public void initializeStandardMode()
	{
		initializeEngineVariables();
    	variablesChanged();
	}
	
	public void loadEngine(final String engineName) throws Exception
	{
		if(engineName == null || engineName.length() <= 0)
		{
			throw new Exception("Script engine name not defined.");
		}
		ScriptEngineFactory factory;
		try
		{
			factory = client.getAddonProvider().getScriptEngineFactory(engineName);
		}
		catch(AddonException e)
		{
        	throw new Exception("Could not get script engine factory for script engine name " + engineName + ".", e);
		}
		this.engineName = engineName;
		engine = factory.getScriptEngine();
				
        if(engine != null)
        {
        	outputListener.flush();
        	outputListener.getBuffer().setLength(0);
        	engine.getContext().setWriter(outputListener);
        	
        	outputListener.flush();
    		sendOutputMessage(outputListener.toString());
    		outputListener.getBuffer().setLength(0);
    		
        	return;
        }
		throw new Exception("Could not create script engine for engine name " + engineName + ".");
	}
	private Set<Entry<String, Object>> getDefinedVariables()
	{
		if(engine == null)
			return null;
		return engine.getBindings(ScriptContext.ENGINE_SCOPE).entrySet();
	}
	private void initializeEngineVariables()
	{
		try
		{
			engine.put("youscopeServer", server);
			engine.put("youscopeClient", client);
		}
		catch(@SuppressWarnings("unused") Exception e)
		{
			// Do nothing, some script engines just do not support java objects...
		}
	}
	
	@Override
	public void evalString(String script) throws ScriptException
	{
		if(engine == null)
			return;
		sendInputMessage(script);
		
		ScriptException thrownException = null;
		Object returnVal = null;
		try
		{
			engine.put(ScriptEngine.FILENAME, "User Input");
			returnVal = engine.eval(script);
		}
		catch (ScriptException e1)
		{
			thrownException = e1;
		}
		catch(Exception e)
		{
			thrownException = new ScriptException(e);
		}
		outputListener.flush();
		sendOutputMessage(outputListener.toString());
		outputListener.getBuffer().setLength(0);
		if(returnVal != null)
			sendOutputMessage(returnVal.toString());
		variablesChanged();
		
		// If exception occurred, print it and re-throw it.
		if(thrownException != null)
		{
			Throwable cause = thrownException;
			for(int i = 0; cause != null && i < 5; i++)
			{
				sendOutputMessage(cause.getMessage());
				cause = cause.getCause();
			}
			throw thrownException;
		}
	}
	
	@Override
	public void evalFile(File file) throws ScriptException
	{
		if(file == null || !file.exists() || file.isDirectory())
			return;
		ScriptException thrownException = null;
		Object returnVal = null;
		FileReader fileReader = null;
		try
		{
			engine.put(ScriptEngine.FILENAME, file.getAbsolutePath());
			fileReader = new FileReader(file);
			returnVal = engine.eval(fileReader);
		}
		catch (ScriptException e1)
		{
			thrownException = e1;
		}
		catch(@SuppressWarnings("unused") FileNotFoundException e)
		{
			sendOutputMessage("Error: Could not find or open file \"" + file.getAbsolutePath() +"\".");
		}
		catch(Exception e)
		{
			thrownException = new ScriptException(e);
		}
		finally
		{
			try {
				fileReader.close();
			} catch (@SuppressWarnings("unused") IOException e) {
				// do nothing.
			}
		}
		outputListener.flush();
		sendOutputMessage(outputListener.toString());
		outputListener.getBuffer().setLength(0);
		
		if(returnVal != null)
			sendOutputMessage(returnVal.toString());
		variablesChanged();
		
		// If exception occurred, print it and re-throw it.
		if(thrownException != null)
		{
			Throwable cause = thrownException;
			for(int i = 0; cause != null && i < 5; i++)
			{
				sendOutputMessage(cause.getMessage());
				cause = cause.getCause();
			}
			throw thrownException;
		}
	}
	
	public String[] getScriptEngines()
    {
		Vector<String> engineNames = new Vector<String>();
		for (ScriptEngineFactory factory : client.getAddonProvider().getScriptEngineFactories())
        {
			engineNames.add(factory.getEngineName());
        }
		return engineNames.toArray(new String[engineNames.size()]);
    }
}
