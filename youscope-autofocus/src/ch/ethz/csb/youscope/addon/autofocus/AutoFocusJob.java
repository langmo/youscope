/**
 * 
 */
package ch.ethz.csb.youscope.addon.autofocus;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.shared.measurement.ImageProducer;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.job.EditableJobContainer;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.resource.focusscore.FocusScoreResource;
import ch.ethz.csb.youscope.shared.resource.focussearch.FocusSearchResource;
import ch.ethz.csb.youscope.shared.table.TableProducer;

/**
 * Interface of a measurement job which automatically adjusts the focus.
 * @author Moritz Lang
 * 
 */
public interface AutoFocusJob extends Job, EditableJobContainer, ImageProducer, TableProducer
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
	 * Defines if the focus should be reset to its original value after the job (and all sub-jobs) finished.
	 * @param resetFocusAfterSearch Set to true to return to original value.
	 * @throws RemoteException 
	 * @throws MeasurementRunningException 
	 */
	public void setResetFocusAfterSearch(boolean resetFocusAfterSearch) throws RemoteException, MeasurementRunningException;

	/**
	 * Set to true if next focus search should be centered around the last iterations maximal focal plane. If false, it always starts from focus before the job.
	 * @param rememberFocus True if next focus search should be centered around last maximum.
	 * @throws RemoteException 
	 * @throws MeasurementRunningException 
	 */
	public void setRememberFocus(boolean rememberFocus) throws RemoteException, MeasurementRunningException;

	/**
	 * Returns true if next focus search should be centered around the last iterations maximal focal plane. If false, it always starts from focus before the job.
	 * @return True if next focus search is centered around last maximum.
	 * @throws RemoteException 
	 */
	public boolean isRememberFocus() throws RemoteException;
	
	/**
	 * Defines if the focus should be reset to its original value after the job (and all sub-jobs) finished.
	 * @return true if returning to original value.
	 * @throws RemoteException 
	 */
	public boolean isResetFocusAfterSearch() throws RemoteException;
	
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
	 * Sets the algorithm with which the score of an image is calculated (i.e. with which it is determined if an image made at a give focus position is better than an image made in another).
	 * @param focusScoreAlgorithm
	 * @throws RemoteException 
	 * @throws MeasurementRunningException 
	 */
	public void setFocusScoreAlgorithm(FocusScoreResource focusScoreAlgorithm) throws RemoteException, MeasurementRunningException;

	/**
	 * Sets the algorithm with which it is determined at which focus positions images are made to determine the best focal plane. This decision in general
	 * depends on the scores of the images already made in different focal positions. 
	 * @param focusSearchAlgorithm
	 * @throws RemoteException 
	 * @throws MeasurementRunningException 
	 */
	public void setFocusSearchAlgorithm(FocusSearchResource focusSearchAlgorithm) throws RemoteException, MeasurementRunningException;

	/**
	 * Returns the focus algorithm with which it is detected if the focus of an image made at a give focus position is better than the other.
	 * @return focus score algorithm configuration, or null.
	 * @throws RemoteException 
	 */
	public FocusScoreResource getFocusScoreAlgorithm() throws RemoteException;
	
	/**
	 * Returns the algorithm with which it is determined at which focus positions images are made to determine the best focal plane. This decision in general
	 * depends on the scores of the images already made in different focal positions.
	 * @return focus search algorithm configuration, or null.
	 * @throws RemoteException 
	 */
	public FocusSearchResource getFocusSearchAlgorithm() throws RemoteException;

	/**
	 * Sets a short string describing the images which are made by this job.
	 * @param description The description which should be returned for the images produced by this job, or null, to switch to the default description.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void setImageDescription(String description) throws RemoteException, MeasurementRunningException;
}
