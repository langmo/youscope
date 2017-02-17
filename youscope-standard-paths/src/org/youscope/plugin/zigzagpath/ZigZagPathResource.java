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
package org.youscope.plugin.zigzagpath;

import java.awt.geom.Point2D.Double;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.youscope.addon.pathoptimizer.PathOptimizerResource;
import org.youscope.common.PositionInformation;
import org.youscope.common.Well;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.resource.ResourceAdapter;
import org.youscope.common.resource.ResourceException;

/**
 * An optimizer which assumes no specific selected wells and positions. Just iterates over all wells in a zick-zack manner
 * (first row from lowest well to highest, second row from highest to lowest, and so forth). For every well, it also iterates in a
 * zick-zack manner over all positions. However, it alternates iterating from top to bottom and from bottom to top, as well as starts
 * iterating from the leftmost well if the well iteration is currently positive, and vice versa.
 * Probably the best general optimizer if no specific information about selected wells and positions can be assumed, and if no specific optimization algorithm should be run.
 * @author Moritz Lang
 *
 */
public class ZigZagPathResource  extends ResourceAdapter<ZigZagPathConfiguration> implements PathOptimizerResource
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 4390319825201388272L;

	/**
	 * Constructor.
	 * @param positionInformation logical position in measurement.
	 * @param configuration Configuration of resource.
	 * @throws RemoteException 
	 * @throws ConfigurationException
	 */
	public ZigZagPathResource(PositionInformation positionInformation, ZigZagPathConfiguration configuration) throws RemoteException, ConfigurationException {
		super(positionInformation, configuration, ZigZagPathConfiguration.TYPE_IDENTIFIER, ZigZagPathConfiguration.class, "Zig-zag path");
		
	}

	@Override
	public List<PositionInformation> getPath(Map<PositionInformation, ? extends Double> positions)
			throws ResourceException, RemoteException {
		
		ArrayList<PositionInformation> path = new ArrayList<>(positions.keySet());
		
		if(getConfiguration().getDirection() == ZigZagPathConfiguration.Direction.HORIZONTALLY)
		{
			// Get all used rows
			final HashSet<Integer> wellRows = new HashSet<>();
			final HashSet<Integer> tileRows = new HashSet<>();
			for(PositionInformation positionInformation : path)
			{
				if(positionInformation.getWell() != null)
					wellRows.add(positionInformation.getWell().getWellY());
				if(positionInformation.getNumPositions() > 0)
					tileRows.add(positionInformation.getPosition(0));
			}
			
			// Get indices of even rows
			ArrayList<Integer> evenWellRows = new ArrayList<>(wellRows);
			Collections.sort(evenWellRows);
			ArrayList<Integer> evenTileRows = new ArrayList<>(tileRows);
			Collections.sort(evenTileRows);
			boolean even = false;
			Iterator<Integer> iterator = evenWellRows.iterator();
			while(iterator.hasNext())
			{
				iterator.next();
				if(!even)
					iterator.remove();
				even = !even;
					
			}
			even = false;
			iterator = evenTileRows.iterator();
			while(iterator.hasNext())
			{
				iterator.next();
				if(!even)
					iterator.remove();
				even = !even;
			}
			
			// put back in hash set for quicker sorting
			wellRows.clear();
			wellRows.addAll(evenWellRows);
			tileRows.clear();
			tileRows.addAll(evenTileRows);
			
			Collections.sort(path, new Comparator<PositionInformation>()
					{
						@Override
						public int compare(PositionInformation positionInformation1, PositionInformation positionInformation2) 
						{
							Well well1 = positionInformation1.getWell();
							Well well2 = positionInformation2.getWell();
							if(well1 == null && well2 != null)
								return -1;
							else if (well1 != null && well2 == null)
								return 1;
							else if(well1 != null && well2 != null && !well1.equals(well2))
							{
								if(well1.getWellY() < well2.getWellY())
									return -1;
								else if(well1.getWellY() > well2.getWellY())
									return 1;
								if(wellRows.contains(well1.getWellY()))
								{
									return well1.getWellX() > well2.getWellX() ? -1 : 1;
								}
								return well1.getWellX() < well2.getWellX() ? -1 : 1;
									
							}
							int[] pos1 = positionInformation1.getPositions();
							int[] pos2 = positionInformation2.getPositions();
							
							if(pos1.length == 0 && pos2.length == 0)
								return 0;
							else if(pos1.length == 0 && pos2.length != 0)
								return -1;
							else if (pos1.length != 0 && pos2.length == 0)
								return 1;
							else if(pos1[0] != pos2[0])
								return pos1[0] < pos2[0] ? -1 : 1;
							else if(pos1.length == 1 && pos2.length == 1)
								return 0;
							else if(pos1.length == 1 && pos2.length != 1)
								return -1;
							else if (pos1.length != 1 && pos2.length == 1)
								return 1;	
							else if(pos1[1] != pos2[1])
							{
								if((well1 != null && wellRows.contains(well1.getWellY())) != tileRows.contains(pos1[0]))
								{
									return pos1[1] > pos2[1] ? -1 : 1;
								}
								return pos1[1] < pos2[1] ? -1 : 1;
							}
							// Now we know that the wells, and the first two positions are equal. Check all other positions normally.
							for(int i=0; i<pos1.length && i < pos2.length; i++)
							{
								if(pos1[i]!=pos2[i])
									return pos1[i] < pos2[i] ? -1 : 1;
							}
							if(pos1.length < pos2.length)
								return -1;
							else if(pos1.length > pos2.length)
								return 1;
							return 0;
						}
				
					});
			
		}
		else
		{
			// Get all used columns
			final HashSet<Integer> wellColumns = new HashSet<>();
			final HashSet<Integer> tileColumns = new HashSet<>();
			for(PositionInformation positionInformation : path)
			{
				if(positionInformation.getWell() != null)
					wellColumns.add(positionInformation.getWell().getWellX());
				if(positionInformation.getNumPositions() > 1)
					tileColumns.add(positionInformation.getPosition(1));
			}
			
			// Get indices of even columns
			ArrayList<Integer> evenWellColumns = new ArrayList<>(wellColumns);
			Collections.sort(evenWellColumns);
			ArrayList<Integer> evenTileColumns = new ArrayList<>(tileColumns);
			Collections.sort(evenTileColumns);
			boolean even = false;
			Iterator<Integer> iterator = evenWellColumns.iterator();
			while(iterator.hasNext())
			{
				iterator.next();
				if(!even)
					iterator.remove();
				even = !even;
					
			}
			even = false;
			iterator = evenTileColumns.iterator();
			while(iterator.hasNext())
			{
				iterator.next();
				if(!even)
					iterator.remove();
				even = !even;
			}
			
			// put back in hash set for quicker sorting
			wellColumns.clear();
			wellColumns.addAll(evenWellColumns);
			tileColumns.clear();
			tileColumns.addAll(evenTileColumns);
			
			Collections.sort(path, new Comparator<PositionInformation>()
					{
						@Override
						public int compare(PositionInformation positionInformation1, PositionInformation positionInformation2) 
						{
							Well well1 = positionInformation1.getWell();
							Well well2 = positionInformation2.getWell();
							if(well1 == null && well2 != null)
								return -1;
							else if (well1 != null && well2 == null)
								return 1;
							else if(well1 != null && well2 != null && !well1.equals(well2))
							{
								if(well1.getWellX() < well2.getWellX())
									return -1;
								else if(well1.getWellX() > well2.getWellX())
									return 1;
								if(wellColumns.contains(well1.getWellX()))
								{
									return well1.getWellY() > well2.getWellY() ? -1 : 1;
								}
								return well1.getWellY() < well2.getWellY() ? -1 : 1;
									
							}
							int[] pos1 = positionInformation1.getPositions();
							int[] pos2 = positionInformation2.getPositions();
							
							if(pos1.length < 2 || pos2.length < 2)
							{
								for(int i=0; i<pos1.length && i<pos2.length; i++)
								{
									if(pos1[i] != pos2[i])
										return pos1[i] < pos2[i] ? -1 : 1;
								}
								if(pos1.length != pos2.length)
									return pos1.length < pos2.length ? -1 : 1;
								return 0;
							}
							else if(pos1[1] != pos2[1])
								return pos1[1] < pos2[1] ? -1 : 1;
							else if(pos1[0] != pos2[0])
							{
								if((well1 != null && wellColumns.contains(well1.getWellX())) != tileColumns.contains(pos1[1]))
								{
									return pos1[0] > pos2[0] ? -1 : 1;
								}
								return pos1[0] < pos2[0] ? -1 : 1;
							}
							// Now we know that the wells, and the first two positions are equal. Check all other positions normally.
							for(int i=2; i<pos1.length && i < pos2.length; i++)
							{
								if(pos1[i]!=pos2[i])
									return pos1[i] < pos2[i] ? -1 : 1;
							}
							if(pos1.length < pos2.length)
								return -1;
							else if(pos1.length > pos2.length)
								return 1;
							return 0;
						}
				
					});
		}
		return path;
	}
}
