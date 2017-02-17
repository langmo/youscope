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
package org.youscope.common.scripting;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.script.ScriptException;

import org.youscope.common.callback.Callback;

/**
 * @author langmo
 */
public interface RemoteScriptEngine extends Callback
{
	/**
	 * Executes the specified script. The default ScriptContext for the ScriptEngine is used.
	 * 
	 * @param script The source for the script.
	 * @throws RemoteException
	 * @throws ScriptException Thrown if an error during the execution of the script occurred.
	 */
	public void eval(String script) throws RemoteException, ScriptException;

	/**
	 * Executes the script in the specified file. The default ScriptContext for the ScriptEngine is used.
	 * An IO exception is thrown if the script could not be opened or was not found.
	 * @param scriptFile The file containing the script. The path of the file has to be set for the computer where this script engine resides on.
	 * 
	 * @throws RemoteException
	 * @throws ScriptException Thrown if an error during the execution of the script occurred.
	 * @throws IOException Thrown if the given file could not be found or opened.
	 */
	public void eval(URL scriptFile) throws RemoteException, ScriptException, IOException;

	/**
	 * Sets a key/value pair in the state of the RemoteScriptEngine that may either create a Java
	 * Language Binding to be used in the execution of scripts or be used in some other way,
	 * depending on whether the key is reserved.
	 * Might throw a remote exception if value is neither serializable nor implementing interface Remote.
	 * 
	 * @param key The name of named value to add.
	 * @param value The value of named value to add.
	 * @throws RemoteException
	 * @throws NullPointerException if key is null.
	 * @throws IllegalArgumentException if key is empty.
	 */
	public void put(String key, Object value) throws RemoteException, NullPointerException, IllegalArgumentException;
}
