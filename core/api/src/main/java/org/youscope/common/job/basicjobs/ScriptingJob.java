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
package org.youscope.common.job.basicjobs;

import java.net.URL;
import java.rmi.RemoteException;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.job.CompositeJob;
import org.youscope.common.job.Job;
import org.youscope.common.scripting.RemoteScriptEngine;

/**
 * A scripting job executes a script every time it runs, and-depending on the script-its child jobs.
 * @author Moritz Lang
 */
public interface ScriptingJob extends Job, CompositeJob
{
	/**
	 * The type identifier of the default implementation of this job. 
	 * Basic jobs are considered such essential to YouScope
	 * such that their interfaces are made part of the shared library. However, their implementation are not, and there
	 * might be several addons providing (different) implementations of this job. Most of these implementations, however, are specific
	 * for a given application and not general. The addon exposing this identifier should be general, that is, every other
	 * part of YouScope accessing this job over the default identifier is expecting the job to behave in the general way.
	 * Only one implementation (addon) should expose the default identifier. Typically, this implementation is already part of YouScope,
	 * such that implementing this addon is not necessary. However, there might be cases when the default implementation should be overwritten,
	 * which is why the interface, but not the implementation is part of YouScope's core elements. In this case, the default implementation
	 * already part of YouScope should be removed (i.e. the corresponding default plug-in deleted).
	 * 
	 */
	public static final String	DEFAULT_TYPE_IDENTIFIER	= "YouScope.ScriptingJob";
	
	/**
	 * Adds a key value pair to the script engine, so that a variable with the name <key> can be
	 * used in the script.
	 * 
	 * @param key Name of variable to be used in script.
	 * @param value Value of the variable.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void putVariable(String key, Object value) throws RemoteException, ComponentRunningException;

	/**
	 * Sets the remote script engine which should be null.
	 * 
	 * @param scriptEngine Sets the script engine to use.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void setRemoteScriptEngine(RemoteScriptEngine scriptEngine) throws RemoteException, ComponentRunningException;

	/**
	 * Sets the script engine with which the scripts should be evaluated. If a remote script engine is set, this setting is ignored.
	 * 
	 * @param engine The script engine to use.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void setScriptEngine(String engine) throws RemoteException, ComponentRunningException;

	/**
	 * Returns the name of the script engine, or null, if script engine is not set.
	 * @return Script engine factory name.
	 * @throws RemoteException
	 */
	String getScriptEngine() throws RemoteException;

	/**
	 * Sets the file of the script which should be evaluated by the corresponding script engine.
	 * @param scriptFile The file containing the script which should be evaluated.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void setScriptFile(URL scriptFile) throws RemoteException, ComponentRunningException;

	/**
	 * Returns the file of the script which should be evaluated by the corresponding script engine.
	 * @return The file containing the script which should be evaluated, or null, if not set.
	 * @throws RemoteException
	 */
	URL getScriptFile() throws RemoteException;
}
