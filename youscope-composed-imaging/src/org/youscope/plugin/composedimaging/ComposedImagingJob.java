/**
 * 
 */
package org.youscope.plugin.composedimaging;

import java.awt.Dimension;
import java.rmi.RemoteException;

import org.youscope.common.image.ImageProducer;
import org.youscope.common.job.Job;
import org.youscope.common.measurement.MeasurementRunningException;


/**
 * @author langmo
 */
public interface ComposedImagingJob extends Job, ImageProducer
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
	void setExposure(double exposure) throws RemoteException, MeasurementRunningException;

	/**
	 * Gets the exposure. If more than one camera is initialized, returns the exposure of the first camera.
	 * 
	 * @return The exposure.
	 * @throws RemoteException
	 */
	double getExposure() throws RemoteException;

	/**
	 * Sets the exposures of the cameras.
	 * Should have the same size as cameras, otherwise an exception is thrown.
	 * Each element can be set to -1 to not set the exposure for the given camera.
	 * 
	 * @param exposures The exposures.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 * @throws IllegalArgumentException Thrown if number of elements in exposure array is unequal to the number of cameras.
	 */
	void setExposures(double[] exposures) throws RemoteException, MeasurementRunningException, IllegalArgumentException;

	/**
	 * Gets the exposures of the initialized cameras.
	 * 
	 * @return The exposures.
	 * @throws RemoteException
	 */
	double[] getExposures() throws RemoteException;

	/**
	 * Sets the cameras with which it should be imaged. If set to null, the default (currently initialized) camera is used.
	 * Each camera should only appear ones in the array. One element of the array can be NULL, such that the default camera is used.
	 * 
	 * @param cameras Device names of the cameras
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void setCameras(String[] cameras) throws RemoteException, MeasurementRunningException;

	/**
	 * Gets the cameras.
	 * 
	 * @return The cameras.
	 * @throws RemoteException
	 */
	String[] getCameras() throws RemoteException;
	
	/**
	 * Returns the distance between two horizontally attached sub-images.
	 * 
	 * @return distance in micro meter.
	 * @throws RemoteException
	 */
	double getDeltaX() throws RemoteException;

	/**
	 * Sets the distance between two horizontally attached sub-images.
	 * 
	 * @param deltaX
	 *            Distance in micro meter.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void setDeltaX(double deltaX) throws RemoteException,
	MeasurementRunningException;

	/**
	 * Returns the distance between two vertically attached sub-images.
	 * 
	 * @return distance in micro meter.
	 * @throws RemoteException
	 */
	double getDeltaY() throws RemoteException;

	/**
	 * Sets the distance between two vertically attached sub-images.
	 * 
	 * @param deltaY
	 *            Distance in micro meter.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void setDeltaY(double deltaY) throws RemoteException,
	MeasurementRunningException;

	/**
	 * Returns the number of sub-images.
	 * 
	 * @return Number of sub-images
	 * @throws RemoteException
	 */
	Dimension getSubImageNumber() throws RemoteException;

	/**
	 * Sets the number of sub-images
	 * 
	 * @param imageNumbers
	 *            Number of sub-images.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void setSubImageNumber(Dimension imageNumbers) throws RemoteException,
	MeasurementRunningException;
}
