/**
 * 
 */
package ch.ethz.csb.youscope.addon.fluigent;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.table.TableConsumer;
import ch.ethz.csb.youscope.shared.table.TableProducer;

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
	 * Returns the name of the Fluigent device.
	 * @return Name of Fluigent device, or null if not set.
	 * @throws RemoteException
	 */
	String getFluigentDeviceName() throws RemoteException;
	
	/**
	 * Sets the name of the Fluigent device. If the device is not a Fluigent device, an error is thrown during job initialization.
	 * @param deviceName The name of the Fluigent device.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void setFluigentDeviceName(String deviceName) throws RemoteException, MeasurementRunningException;	
}
