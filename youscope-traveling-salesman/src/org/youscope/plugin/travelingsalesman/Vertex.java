package org.youscope.plugin.travelingsalesman;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.youscope.common.measurement.microplate.Well;

class Vertex  extends Point2D.Double
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 8412311532339449645L;
		final int wellX;
		final int wellY;
		final int posX;
		final int posY;
		final ArrayList<Vertex> edgesTo = new ArrayList<>(2);
		public Vertex(double x, double y, int wellY, int wellX, int posY, int posX)
		{
			super(x,y);
			this.wellX = wellX;
			this.wellY = wellY;
			this.posX = posX;
			this.posY = posY;
		}
		
		@Override
		public String toString()
		{
			return new Well(wellY, wellX).toString()+"-"+Integer.toString(posY)+"-"+Integer.toString(posX);
		}
}
