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
package org.youscope.plugin.fluigent;

import java.rmi.RemoteException;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.job.Job;
import org.youscope.common.table.TableConsumer;
import org.youscope.common.table.TableProducer;

/**
 * Job to control the Fluigent syringe. 
 * @author Moritz Lang
 *
 */
public interface FluigentJob extends Job, TableConsumer, TableProducer
{
	/**
	 * Sets the script engine with which the scripts should be evaluated. 
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
	 * Sets the script which gets evaluated by the script engine every time the job runs.
	 * @param script script to evaluate, following the rules of the chosen script engine.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void setScript(String script) throws RemoteException, ComponentRunningException;

	/**
	 * Returns the script which gets evaluated by the script engine every time the script runs.
	 * @return script to evaluate.
	 * @throws RemoteException
	 */
	String getScript() throws RemoteException;
	
	/**
	 * Returns the name of the Fluigent device.
	 * @return Name of Fluigent device, or null if not set.
	 * @throws RemoteException
	 */
	String getFluigentDeviceName() throws RemoteException;
	
	/**
	 * Sets the name of the Fluigent device. If the device is not a Fluigent device, an error is thrown during job initialization.
	 * @param deviceName The name of the Fluigent device.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void setFluigentDeviceName(String deviceName) throws RemoteException, ComponentRunningException;	
}
