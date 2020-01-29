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
package org.youscope.plugin.travelingsalesman;

import java.awt.geom.Point2D;

/**
 * An implementation of a metric, that is, a definition of the distance between two points on the cartesian plane.
 * @author Moritz Lang
 *
 */
interface Metric 
{
	/**
	 * Returns the distance between the two points. For example, the Euclidean distance would be sqrt((x1-x2)^2+(y1-y2)^2).
	 * @param point1 First point.
	 * @param point2 Second point.
	 * @return distance between points.
	 */
	public double distance(Point2D.Double point1, Point2D.Double point2); 

}
