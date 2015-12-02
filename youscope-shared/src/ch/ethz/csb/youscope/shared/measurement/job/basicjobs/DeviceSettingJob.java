/**
 * 
 */
package ch.ethz.csb.youscope.shared.measurement.job.basicjobs;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.microscope.DeviceSettingDTO;
import ch.ethz.csb.youscope.shared.table.TableConsumer;

/**
 * @author Moritz Lang
 */
public interface DeviceSettingJob extends Job, TableConsumer
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
	public static final String	DEFAULT_TYPE_IDENTIFIER	= "CSB::SimpleDeviceJob";
	/**
	 * Returns the device settings made when job runs.
	 * 
	 * @return List of device settings.
	 * @throws RemoteException
	 */
	DeviceSettingDTO[] getDeviceSettings() throws RemoteException;

	/**
	 * Sets the device settings made when job runs.
	 * 
	 * @param settings List of device settings.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void setDeviceSettings(DeviceSettingDTO[] settings) throws RemoteException, MeasurementRunningException;

	/**
	 * Removes all settings.
	 * 
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void clearDeviceSettings() throws RemoteException, MeasurementRunningException;

	/**
	 * Adds a new device setting.
	 * 
	 * @param device Name of the device.
	 * @param property Name of the device's property.
	 * @param value Value to set device's property to.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void addDeviceSetting(String device, String property, String value) throws RemoteException, MeasurementRunningException;
}
