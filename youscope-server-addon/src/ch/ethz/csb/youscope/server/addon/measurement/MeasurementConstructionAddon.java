/**
 * 
 */
package ch.ethz.csb.youscope.server.addon.measurement;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.MeasurementConfiguration;
import ch.ethz.csb.youscope.shared.measurement.Measurement;
import ch.ethz.csb.youscope.shared.measurement.job.JobCreationException;

/**
 * @author langmo
 * 
 */
public interface MeasurementConstructionAddon
{
	/**
	 * Called when this addon should initialize the tasks (and only the tasks) of the measurement according to the configuration.
	 * @param measurement The measurement whose tasks should be initialized.
	 * @param measurementConfiguration The configuration according to which the tasks should be initialized.
	 * @param jobInitializer An interface to an object allowing to initialize the various job types.
	 * @throws ConfigurationException
	 * @throws RemoteException
	 * @throws JobCreationException 
	 */
	void initializeTasksOfMeasurement(Measurement measurement, MeasurementConfiguration measurementConfiguration, ConstructionContext jobInitializer) throws ConfigurationException, RemoteException, JobCreationException;
}
