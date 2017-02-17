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
package org.youscope.addon.focussearch;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.youscope.common.resource.ResourceException;

/**
 * Oracle for a focus score algorithm to get the focus score for a given focal plane.
 * @author langmo
 *
 */
public interface FocusSearchOracle extends Remote {
	/**
	 * Returns the focal score for a given focal plane. Higher scores indicate better focal planes.
	 * @param relativeFocusPosition The focal position (z-position) for which the focus score should be querried.
	 * @return The focus score of the given focal position.
	 * @throws ResourceException
	 * @throws RemoteException
	 */
	public double getFocusScore(double relativeFocusPosition) throws ResourceException, RemoteException;
}
