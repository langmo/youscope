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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.youscope.common.callback.CallbackException;
import org.youscope.common.scripting.RemoteScriptEngine;

/**
 * @author langmo
 *
 */
class RemoteScriptEngineImpl extends UnicastRemoteObject implements RemoteScriptEngine
{

	/**
	 * Serial Version UID:
	 */
	private static final long	serialVersionUID	= 6240593513089350044L;
	private final ScriptEngine engine;
	private final String engineName;
	/**
	 * @throws RemoteException
	 */
	protected RemoteScriptEngineImpl(ScriptEngine engine, String engineName) throws RemoteException
	{
		super(); 
		this.engineName = engineName;
		this.engine = engine;
	}

	@Override
	public void eval(String script) throws RemoteException, ScriptException
	{
		engine.eval(script);
	}

	@Override
	public void put(String key, Object value) throws RemoteException, NullPointerException, IllegalArgumentException
	{
		engine.put(key, value);
	}

	@Override
	public void eval(URL scriptFile) throws RemoteException, ScriptException, IOException
	{
		if(scriptFile == null)
		{
		 	throw new IOException("Script file is null.");		
		}
		InputStreamReader fileReader = null;
        BufferedReader bufferedReader = null;	
		
        String content = "";
        try
        {
           fileReader = new InputStreamReader(scriptFile.openStream());
           bufferedReader = new BufferedReader(fileReader);
           while (true)
           {
               String line = bufferedReader.readLine();
               if (line == null)
                   break;
               content += line + "\n";
           }
       
    	   
       }
	   finally
       {
           if (fileReader != null)
           {
               fileReader.close();
           }
           if (bufferedReader != null)
           {
               bufferedReader.close();
           }
       }
       eval(content);		
	}

	@Override
	public void pingCallback() throws RemoteException {
		// do nothing.
	}

	@Override
	public void initializeCallback(Serializable... arguments) throws RemoteException, CallbackException {
		// do nothing.
	}

	@Override
	public void uninitializeCallback() throws RemoteException, CallbackException {
		// do nothing.
	}

	@Override
	public String getTypeIdentifier() throws RemoteException {
		return engineName;
	}
}
