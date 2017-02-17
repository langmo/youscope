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
package org.youscope.serverinterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.youscope.addon.component.ComponentCreationException;
import org.youscope.common.callback.CallbackProvider;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.image.ImageListener;
import org.youscope.common.job.basicjobs.ContinuousImagingJob;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementConfiguration;

/**
 * A class being able to construct new measurements.
 * 
 * @author Moritz Lang
 * @see Measurement
 */
public interface MeasurementProvider extends Remote
{
	/**
	 * Creates a new empty measurement. Same as
	 * {@link #createMeasurement(int)}(-1).
	 * 
	 * @return The created measurement.
	 * @throws RemoteException
	 */
	Measurement createMeasurement() throws RemoteException;

	/**
	 * Creates a new empty measurement.
	 * 
	 * @param measurementRuntime Runtime of measurement in milliseconds. After this
	 *            time the measurement will be automatically stopped. Set to -1 for unlimited runtime.
	 * @return The measurement object.
	 * @throws RemoteException
	 */
	Measurement createMeasurement(int measurementRuntime) throws RemoteException;

	/**
	 * Creates a new measurement from the configuration. Same as
	 * {@link #createMeasurement(MeasurementConfiguration, CallbackProvider)}(configuration, null).
	 * 
	 * @param configuration The configuration of the measurement.
	 * @return The newly created measurement.
	 * @throws RemoteException
	 * @throws ConfigurationException Thrown if configuration is invalid.
	 * @throws ComponentCreationException Thrown if an error occurred in the creation of the components (e.g. jobs) belonging to the measurement.
	 */
	Measurement createMeasurement(MeasurementConfiguration configuration) throws RemoteException, ConfigurationException, ComponentCreationException;

	/**
	 * Creates a new measurement from the configuration.
	 * 
	 * @param configuration The configuration of the measurement.
	 * @param callbackProvider Provider of callbacks, with which the measurement can send messages to the client, e.g. to prompt the client to graphically display the state of the measurement, or even to manipulate the measurement at runtime.
	 * @return The newly created measurement.
	 * @throws RemoteException
	 * @throws ConfigurationException Thrown if configuration is invalid.
	 * @throws ComponentCreationException Thrown if an error occurred in the creation of the components (e.g. jobs) belonging to the measurement.
	 */
	Measurement createMeasurement(MeasurementConfiguration configuration, CallbackProvider callbackProvider) throws RemoteException, ConfigurationException, ComponentCreationException;

	/**
	 * Convenience function to create a measurement, in which every imagingPeriod ms an image is made in the given channel with the given period, using the default camera.
	 * The images made are not saved automatically, but intended to be displayed to the user.
	 * Internally, this is done by creating an empty measurement and adding a {@link ContinuousImagingJob} with the respective settings to it.
	 * This function mainly servers the propose of displaying the current microscope image in a client, a functionality which is so often needed such that a default implementation seemed necessary.
	 * 
	 * @param cameraID The device name of the camera with which it should be imaged. Set to null to use standard camera.
	 * @param channelGroup The channel group where the channel is defined.
	 * @param channel The channel in which the images should be made.
	 * @param imagingPeriod The time between two successive images.
	 * @param exposure The exposure time for imaging.
	 * @param imageListener Listener to which the images made by the microscope are send.
	 * @return The newly created measurement.
	 * @throws RemoteException
	 * @throws ComponentCreationException Thrown if an error occurred in the creation of the components (e.g. jobs) belonging to the measurement.
	 */
	Measurement createContinuousMeasurement(String cameraID, String channelGroup, String channel, int imagingPeriod, double exposure, ImageListener imageListener) throws RemoteException, ComponentCreationException;
}
