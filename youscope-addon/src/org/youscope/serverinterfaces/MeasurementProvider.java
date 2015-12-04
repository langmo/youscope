/**
 * 
 */
package org.youscope.serverinterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.youscope.common.ImageListener;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.MeasurementConfiguration;
import org.youscope.common.measurement.ComponentCreationException;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.callback.CallbackProvider;

/**
 * A class being able to construct new measurements.
 * 
 * @author Moritz Lang
 * @see Measurement
 */
public interface MeasurementProvider extends Remote
{
	/**
	 * Creates a new empty measurement which is configurable by the user. Same as
	 * createConfigurableMeasurement(-1).
	 * 
	 * @return The measurement object.
	 * @throws RemoteException
	 */
	Measurement createMeasurement() throws RemoteException;

	/**
	 * Creates a new empty measurement which is configurable by the user.
	 * 
	 * @param measurementRuntime Total maximal runtime of measurement in milliseconds. After this
	 *            time the measurement will be automatically canceled. Set to -1 for unlimited
	 *            maximal runtime.
	 * @return The measurement object.
	 * @throws RemoteException
	 */
	Measurement createMeasurement(int measurementRuntime) throws RemoteException;

	/**
	 * Creates a new measurement and configures it according to the provided configuration. Same as
	 * createMeasurement(configuration, null).
	 * 
	 * @param configuration The configuration of the measurement.
	 * @return The measurement object.
	 * @throws RemoteException
	 * @throws ConfigurationException
	 * @throws ComponentCreationException
	 */
	Measurement createMeasurement(MeasurementConfiguration configuration) throws RemoteException, ConfigurationException, ComponentCreationException;

	/**
	 * Creates a new measurement and configures it according to the provided configuration.
	 * 
	 * @param configuration The configuration of the measurement.
	 * @param scriptEngineManager The script engine manager to obtain the script engines which should run on the client side.
	 * @param callbackProvider Object providing access to the callbacks of the client which can be called by the measurement.
	 * @return The measurement object.
	 * @throws RemoteException
	 * @throws ConfigurationException
	 * @throws ComponentCreationException
	 */
	Measurement createMeasurement(MeasurementConfiguration configuration, CallbackProvider callbackProvider) throws RemoteException, ConfigurationException, ComponentCreationException;

	/**
	 * Short way to create a measurement, where every [imagingPeriod] ms an image is made in the given channel with the given period.
	 * The images made are not saved.
	 * Internally, this is done by creating an empty measurement and adding a continuous imaging job with the respective settings to it.
	 * This function mainly servers the propose of displaying the current microscope image in a client, a functionality which is so often needed such that a default implementation seemed necessary.
	 * 
	 * @param cameraID The device name of the camera with which it should be imaged. Set to null to use standard camera.
	 * @param channelGroup The channel group where the channel is defined.
	 * @param channel The channel in which the images should be made.
	 * @param imagingPeriod The time between two successive images.
	 * @param exposure The exposure time for imaging.
	 * @param imageListener A listener which should be informed when new images are made.
	 * @return The measurement.
	 * @throws RemoteException
	 * @throws ComponentCreationException 
	 */
	Measurement createContinuousMeasurement(String cameraID, String channelGroup, String channel, int imagingPeriod, double exposure, ImageListener imageListener) throws RemoteException, ComponentCreationException;
}
