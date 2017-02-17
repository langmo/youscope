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
package org.youscope.addon.focussearch;

import java.rmi.RemoteException;

import org.youscope.common.resource.Resource;
import org.youscope.common.resource.ResourceException;

/**
 * A focus search algorithm addon is a class defining an algorithm to autofocus based on a given focus score.
 * @author Moritz Lang
 *
 */
public interface FocusSearchResource extends Resource
{
	/**
	 * Runs the focus score algorithm using the given oracle to determine the focus scores.
	 * @param oracle An oracle to determine the focus score at a given (relative) focal plane.
	 * @return The focus position of the best focal plane.
	 * @throws ResourceException 
	 * @throws RemoteException 
	 */
	public double runAutofocus(FocusSearchOracle oracle) throws ResourceException, RemoteException;
}
