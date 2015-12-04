/**
 * 
 */
package org.youscope.common.measurement.job.basicjobs;

import java.rmi.RemoteException;

import org.youscope.common.measurement.ImageProducer;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.job.Job;

/**
 * A job which makes a microscope image in the defined channel.
 * 
 * @author langmo
 */
public interface ImagingJob extends Job, ImageProducer
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
	public static final String	DEFAULT_TYPE_IDENTIFIER	= "CSB::ImagingJob";
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
	 * Sets the camera with which it should be imaged. If set to null, the default (currently initialized) camera is used.
	 * 
	 * @param camera Device names of the camera.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void setCamera(String camera) throws RemoteException, MeasurementRunningException;

	/**
	 * Returns the names/IDs of the cameras. Elements being null correspond to the default camera.
	 * 
	 * @return The cameras.
	 * @throws RemoteException
	 */
	String[] getCameras() throws RemoteException;

	/**
	 * Returns the camera with which it is imaged, or null if the default camera is used. If with more than one camera it is imaged,
	 * returns the first camera.
	 * 
	 * @return The name of the (first) camera, or null for default camera.
	 * @throws RemoteException
	 */
	String getCamera() throws RemoteException;

	/**
	 * Sets a short string describing the images which are made by this job.
	 * @param description The description which should be returned for the images produced by this job, or null, to switch to the default description.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void setImageDescription(String description) throws RemoteException, MeasurementRunningException;
}
