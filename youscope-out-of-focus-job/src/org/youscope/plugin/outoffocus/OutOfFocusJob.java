/**
 * 
 */
package org.youscope.plugin.outoffocus;

import java.rmi.RemoteException;

import org.youscope.common.measurement.ImageProducer;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.job.Job;

/**
 * A job which makes a microscope image in the defined channel with a defined focus offset.
 * 
 * @author langmo
 */
public interface OutOfFocusJob extends Job, ImageProducer
{
	/**
	 * Sets the channel.
	 * 
	 * @param deviceGroup The device group where the channel is defined.
	 * @param channel The channel.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void setChannel(String deviceGroup, String channel) throws RemoteException, MeasurementRunningException;

	/**
	 * Gets the channel.
	 * 
	 * @return The channel.
	 * @throws RemoteException
	 */
	String getChannel() throws RemoteException;

	/**
	 * Gets the channel group.
	 * 
	 * @return The channel group.
	 * @throws RemoteException
	 */
	String getChannelGroup() throws RemoteException;

	/**
	 * Sets the exposure. If more than one camera is initialized, it sets the exposure of all cameras.
	 * Set to -1 if exposure should not be set.
	 * 
	 * @param exposure The exposure.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	public void setExposure(double exposure) throws RemoteException, MeasurementRunningException;

	/**
	 * Gets the exposure. If more than one camera is initialized, returns the exposure of the first camera.
	 * 
	 * @return The exposure.
	 * @throws RemoteException
	 */
	double getExposure() throws RemoteException;
	
	/**
	 * Returns the focus adjustment time (the time the microscope is paused after the new focus position was set) in ms.
	 * @return Focus adjustment time.
	 * @throws RemoteException
	 */
	int getFocusAdjustmentTime() throws RemoteException;

	/**
	 * Sets the focus adjustment time (the time the microscope is paused after the new focus position was set) in ms.
	 * @param adjustmentTime Focus adjustment time.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void setFocusAdjustmentTime(int adjustmentTime) throws RemoteException, MeasurementRunningException;

	/**
	 * Gets the offset of the focus device.
	 * 
	 * @return The offset, in muM.
	 * @throws RemoteException
	 */
	double getOffset() throws RemoteException;

	/**
	 * Sets the focus offset in which the image should be taken.
	 * 
	 * @param offset The focus offset, in muM.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void setOffset(double offset) throws RemoteException, MeasurementRunningException;

	/**
	 * Returns the focus device name for which the position should be changed.
	 * @return Focus device name, or null, if the currently active focus device should be changed.
	 * @throws RemoteException
	 */
	String getFocusDevice() throws RemoteException;

	/**
	 * Sets the focus device name for which the position should be changed.
	 * Initialized to be null.
	 * @param focusDevice Focus device name, or null, if the currently active focus device should be changed.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void setFocusDevice(String focusDevice) throws RemoteException, MeasurementRunningException;
	
	/**
	 * Sets a short string describing the images which are made by this job.
	 * @param description The description which should be returned for the images produced by this job, or null, to switch to the default description.
	 * @throws RemoteException
	 * @throws MeasurementRunningException 
	 */
	void setImageDescription(String description) throws RemoteException, MeasurementRunningException;
}
