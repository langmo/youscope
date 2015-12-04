/**
 * 
 */
package org.youscope.plugin.composedimaging;

import java.awt.Dimension;
import java.rmi.RemoteException;

import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.job.EditableJobContainer;
import org.youscope.common.measurement.job.Job;


/**
 * @author langmo
 */
public interface PlateScanningJob extends Job, EditableJobContainer
{
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
	void setDeltaX(double deltaX) throws RemoteException, MeasurementRunningException;

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
	void setDeltaY(double deltaY) throws RemoteException, MeasurementRunningException;

	/**
	 * Returns the number of tiles/sub-images.
	 * 
	 * @return Number of tiles/sub-images
	 * @throws RemoteException
	 */
	Dimension getNumTiles() throws RemoteException;

	/**
	 * Sets the number of tiles/sub-images
	 * 
	 * @param numTiles Number of tiles/sub-images.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void setNumTiles(Dimension numTiles) throws RemoteException, MeasurementRunningException;
}
