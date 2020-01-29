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
package org.youscope.plugin.matlabfocusscores;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface to temporary save the focus score.
 * @author Moritz Lang
 *
 */
public interface FocusSink extends Remote
{
	/**
	 * Sets the focus score.
	 * @param score
	 * @throws RemoteException
	 */
	public void setScore(double score) throws RemoteException;
	
	/**
	 * Returns the last focus score.
	 * @return Last focus score, or -1, if yet not set.
	 * @throws RemoteException
	 */
	public double getLastScore() throws RemoteException;
}
