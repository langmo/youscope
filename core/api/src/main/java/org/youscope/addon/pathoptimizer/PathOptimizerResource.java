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
package org.youscope.addon.pathoptimizer;

import java.awt.geom.Point2D;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import org.youscope.common.PositionInformation;
import org.youscope.common.resource.Resource;
import org.youscope.common.resource.ResourceException;

/**
 * A path optimizer is a method to optimize the path of a microplate measurement, such that the distances between the measured
 * wells/ positions is minimal.
 * @author Moritz Lang
 *
 */
public interface PathOptimizerResource extends Resource
{
	/**
	 * Returns the optimized path for a given set of positions.
	 * @param positions Positions to calculate optimal path of.
	 * @return optimized path.
	 * @throws ResourceException 
	 * @throws RemoteException 
	 */
	public List<PositionInformation> getPath(Map<PositionInformation, ? extends Point2D.Double> positions) throws ResourceException, RemoteException;
}
