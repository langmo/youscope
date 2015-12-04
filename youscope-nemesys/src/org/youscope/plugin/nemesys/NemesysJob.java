/**
 * 
 */
package org.youscope.plugin.nemesys;

import java.rmi.RemoteException;

import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.job.Job;
import org.youscope.common.table.TableConsumer;
import org.youscope.common.table.TableProducer;

/**
 * Job to control the Nemesys syringe. 
 * @author Moritz Lang
 *
 */
public interface NemesysJob extends Job, TableConsumer, TableProducer
{
	/**
	 * Sets the script engine with which the scripts should be evaluated. 
	 * 
	 * @param engine The script engine to use.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void setScriptEngine(String engine) throws RemoteException, MeasurementRunningException;

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
	 * @throws MeasurementRunningException
	 */
	void setScript(String script) throws RemoteException, MeasurementRunningException;

	/**
	 * Returns the script which gets evaluated by the script engine every time the script runs.
	 * @return script to evaluate.
	 * @throws RemoteException
	 */
	String getScript() throws RemoteException;
	
	/**
	 * Returns the name of the Nemesys device.
	 * @return Name of Nemesys device, or null if not set.
	 * @throws RemoteException
	 */
	String getNemesysDeviceName() throws RemoteException;
	
	/**
	 * Sets the name of the Nemesys device. If the device is not a Nemesys device, an error is thrown during job initialization.
	 * @param deviceName The name of the Nemesys device.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void setNemesysDeviceName(String deviceName) throws RemoteException, MeasurementRunningException;
}
