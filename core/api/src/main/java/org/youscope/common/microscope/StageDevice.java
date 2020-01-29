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
package org.youscope.common.microscope;

import java.awt.geom.Point2D;
import java.rmi.RemoteException;

/**
 * @author langmo
 * 
 */
public interface StageDevice extends Device
{
	/**
	 * Returns the current position of the plate.
	 * 
	 * @return Position of the plate.
	 * @throws RemoteException
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 */
	Point2D.Double getPosition() throws RemoteException, MicroscopeException, InterruptedException;

	/**
	 * Sets the absolute position of the stage. Same as setPosition(x, y,
	 * true).
	 * 
	 * @param x X-position of the stage in mu.
	 * @param y Y-Position of the stage in mu.
	 * @throws RemoteException
	 * @throws MicroscopeLockedException
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 */
	void setPosition(double x, double y) throws RemoteException, MicroscopeLockedException, MicroscopeException, InterruptedException;

	/**
	 * Sets the position of the stage relative to the current position.
	 * 
	 * @param dx relative X-position of the stage.
	 * @param dy relative Y-Position of the stage.
	 * @throws RemoteException
	 * @throws MicroscopeLockedException
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 */
	void setRelativePosition(double dx, double dy) throws RemoteException, MicroscopeLockedException, MicroscopeException, InterruptedException;

	/**
	 * Sets if the x-direction should be transposed. If true, all x-coordinates are replaced by their negative value (x -> -x).
	 * This is done automatically for all position related functions.
	 * @param transpose True, if the x-direction should be transposed.
	 * @throws RemoteException
	 * @throws MicroscopeLockedException
	 */
	void setTransposeX(boolean transpose) throws RemoteException, MicroscopeLockedException;

	/**
	 * Sets if the y-direction should be transposed. If true, all y-coordinates are replaced by their negative value (y -> -y).
	 * This is done automatically for all position related functions.
	 * @param transpose True, if the y-direction should be transposed.
	 * @throws RemoteException
	 * @throws MicroscopeLockedException
	 */
	void setTransposeY(boolean transpose) throws RemoteException, MicroscopeLockedException;

	/**
	 * Returns if the x-direction is transposed (x -> -x).
	 * This function is for information only, the change of the direction is done automatically.
	 * @return True, if the x-direction is transposed.
	 * @throws RemoteException
	 */
	boolean isTransposeX() throws RemoteException;

	/**
	 * Returns if the y-direction is transposed (y -> -y).
	 * This function is for information only, the change of the direction is done automatically.
	 * @return True, if the y-direction is transposed.
	 * @throws RemoteException
	 */
	boolean isTransposeY() throws RemoteException;

	/**
	 * Returns the magnification factor from native stage device units to micro meters:
	 * microMeters = units * unitMagnifier
	 * @return Magnification factor for native stage unit to micro meter conversion.
	 * @throws RemoteException
	 */
	double getUnitMagnifier() throws RemoteException;

	/**
	 * Sets the magnification factor from native stage device units to micro meters:
	 * microMeters = units * unitMagnifier
	 * @param unitMagnifier Magnification factor for native stage unit to micro meter conversion.
	 * @throws RemoteException
	 * @throws MicroscopeLockedException
	 */
	void setUnitMagnifier(double unitMagnifier) throws RemoteException, MicroscopeLockedException;
}
