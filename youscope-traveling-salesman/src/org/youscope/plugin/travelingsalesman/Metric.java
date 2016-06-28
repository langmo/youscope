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
