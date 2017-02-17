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
package org.youscope.plugin.composedimaging;

import java.awt.Dimension;
import java.rmi.RemoteException;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.job.CompositeJob;
import org.youscope.common.job.Job;


/**
 * @author langmo
 */
public interface PlateScanningJob extends Job, CompositeJob
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
	 * @throws ComponentRunningException
	 */
	void setDeltaX(double deltaX) throws RemoteException, ComponentRunningException;

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
	 * @throws ComponentRunningException
	 */
	void setDeltaY(double deltaY) throws RemoteException, ComponentRunningException;

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
	 * @throws ComponentRunningException
	 */
	void setNumTiles(Dimension numTiles) throws RemoteException, ComponentRunningException;
}
