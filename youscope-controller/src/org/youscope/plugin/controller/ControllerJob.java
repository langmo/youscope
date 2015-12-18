/**
 * 
 */
package org.youscope.plugin.controller;

import java.rmi.RemoteException;

import org.youscope.common.job.Job;
import org.youscope.common.job.JobContainer;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.table.TableConsumer;
import org.youscope.common.table.TableProducer;

/**
 * Job which implements a controller, consisting of an input, a control algorithm and an output. 
 * @author Moritz Lang
 *
 */
public interface ControllerJob extends Job, JobContainer, TableProducer
{
	/**
	 * Sets the script engine with which the scripts should be evaluated. 
	 * 
	 * @param engine The script engine to use.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void setControllerScriptEngine(String engine) throws RemoteException, MeasurementRunningException;

	/**
	 * Returns the name of the script engine, or null, if script engine is not set.
	 * @return Script engine factory name.
	 * @throws RemoteException
	 */
	String getControllerScriptEngine() throws RemoteException;

	/**
	 * Sets the script for the controller which gets evaluated by the script engine.
	 * @param controllerScript Controller script to evaluate.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void setControllerScript(String controllerScript) throws RemoteException, MeasurementRunningException;

	/**
	 * Returns the script of the controller which gets evaluated by the script engine.
	 * @return Script of the controller.
	 * @throws RemoteException
	 */
	String getControllerScripScript() throws RemoteException;
	
	/**
	 * Returns the job which is used to produce the input for the controller.
	 * It is guaranteed that this job implements the TableDataProducer interface.
	 * @return Table data producing used as input, or null.
	 * @throws RemoteException 
	 */
	public Job getInputJob() throws RemoteException;

	/**
	 * Sets the job which should be used to create the table data used as input for the controller.
	 * The job must implement the {@link TableProducer}. If this is not given, an IllegalArgumentException will be thrown.
	 * @param inputJob Table producing job for control algorithm.
	 * @throws RemoteException 
	 * @throws MeasurementRunningException 
	 * @throws IllegalArgumentException If inputJob does not implement {@link TableProducer}.
	 * @throws NullPointerException Thrown if inputJob is null.
	 */
	public void setInputJob(Job inputJob) throws RemoteException, MeasurementRunningException, IllegalArgumentException, NullPointerException;
	
	/**
	 * Returns the job which is used to consume the output of the controller.
	 * It is guaranteed that this job implements the OutputConsumer interface.
	 * @return Output consumer job, or null if yet not set.
	 * @throws RemoteException 
	 */
	public Job getOutputJob() throws RemoteException;

	/**
	 * Sets the job which should be used to consume the output from the controller.
	 * The job must implement the {@link TableProducer}. If this is not given, an IllegalArgumentException will be thrown.
	 * @param outputJob output consumer job.
	 * @throws RemoteException 
	 * @throws MeasurementRunningException 
	 * @throws IllegalArgumentException If outputJob does not implement {@link TableConsumer}.
	 * @throws NullPointerException Thrown if outputJob is null,
	 */
	public void setOutputJob(Job outputJob) throws RemoteException, MeasurementRunningException, IllegalArgumentException, NullPointerException;
}
