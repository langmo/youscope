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
import java.util.ArrayList;

import org.youscope.common.PositionInformation;
import org.youscope.common.Well;

class Vertex extends Point2D.Double implements Comparable<Vertex>
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 8412311532339449645L;
		final PositionInformation positionInformation;
		final ArrayList<Vertex> edgesTo = new ArrayList<>(2);
		public Vertex(Point2D.Double position, PositionInformation positionInformation)
		{
			super(position.getX(), position.getY());
			this.positionInformation = positionInformation;
		}
		
		Vertex(double x, double y, int wellY, int wellX, int tileY, int tileX)
		{
			super(x, y);
			PositionInformation wellInfo = new PositionInformation(new Well(wellY, wellX));
			PositionInformation tileInfo = new PositionInformation(wellInfo, PositionInformation.POSITION_TYPE_YTILE, tileY);
			this.positionInformation = new PositionInformation(tileInfo, PositionInformation.POSITION_TYPE_XTILE, tileX);
		}
		
		@Override
		public String toString()
		{
			return positionInformation.toString();
		}

		@Override
		public int compareTo(Vertex o) {
			return o==null ? -1 : positionInformation.compareTo(o.positionInformation);
		}
}
