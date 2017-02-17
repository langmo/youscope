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
package org.youscope.plugin.screeningpath;

import java.awt.geom.Point2D.Double;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.youscope.addon.pathoptimizer.PathOptimizerResource;
import org.youscope.common.PositionInformation;
import org.youscope.common.Well;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.resource.ResourceAdapter;
import org.youscope.common.resource.ResourceException;

/**
 * Path optimized for screening of rectangular microplates with all wells/tiles selected.
 * @author Moritz Lang
 *
 */
public class ScreeningPathResource  extends ResourceAdapter<ScreeningPathConfiguration> implements PathOptimizerResource
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 4690319825201388272L;

	/**
	 * Constructor.
	 * @param positionInformation logical position in measurement.
	 * @param configuration Configuration of resource.
	 * @throws RemoteException 
	 * @throws ConfigurationException
	 */
	public ScreeningPathResource(PositionInformation positionInformation, ScreeningPathConfiguration configuration) throws RemoteException, ConfigurationException {
		super(positionInformation, configuration, ScreeningPathConfiguration.TYPE_IDENTIFIER, ScreeningPathConfiguration.class, "Screening path");
		
	}

	@Override
	public List<PositionInformation> getPath(Map<PositionInformation, ? extends Double> positions)
			throws ResourceException, RemoteException {
		
		ArrayList<PositionInformation> path = new ArrayList<>(positions.keySet());
		// Get all used rows and columns
		final HashSet<Integer> wellRowsSet = new HashSet<>();
		final HashSet<Integer> wellColumnsSet = new HashSet<>();
		final HashSet<Integer> tileRowsSet = new HashSet<>();
		final HashSet<Integer> tileColumnsSet = new HashSet<>();
		for(PositionInformation positionInformation : path)
		{
			Well well = positionInformation.getWell();
			if(well != null)
			{
				wellRowsSet.add(well.getWellY());
				wellColumnsSet.add(well.getWellX());
			}
			if(positionInformation.getNumPositions() > 0)
			{
				tileRowsSet.add(positionInformation.getPosition(0));
				if(positionInformation.getNumPositions() > 1)
					tileColumnsSet.add(positionInformation.getPosition(1));
			}
		}
		
		// Get indices of even rows
		ArrayList<Integer> wellRows = new ArrayList<>(wellRowsSet);
		Collections.sort(wellRows);
		ArrayList<Integer> wellColumns = new ArrayList<>(wellColumnsSet);
		Collections.sort(wellColumns);
		ArrayList<Integer> tileRows = new ArrayList<>(tileRowsSet);
		Collections.sort(tileRows);
		ArrayList<Integer> tileColumns = new ArrayList<>(tileColumnsSet);
		Collections.sort(tileColumns);
		
		final int numWellX = wellColumns.size();
		final int numWellY = wellRows.size();
		final int numTileX = tileColumns.size();
		final int numTileY = tileRows.size();
		
		final int wellFirstRow = wellRows.isEmpty() ? Integer.MAX_VALUE : wellRows.get(0);
		// we only have to care about second row if number of rows is odd...
		final int wellSecondRow = wellRows.size()<2 ? Integer.MAX_VALUE : (numWellY%2!=0?wellRows.get(1):Integer.MAX_VALUE);
		final int wellFirstColumn = wellColumns.isEmpty() ? Integer.MAX_VALUE : wellColumns.get(0);
		final int wellSecondColumn = wellColumns.size() > 1 ? wellColumns.get(1) : Integer.MAX_VALUE;
		final int wellLastColumn = wellColumns.isEmpty() ? Integer.MAX_VALUE : wellColumns.get(wellColumns.size()-1);
		
		final int tileFirstRow = tileRows.isEmpty() ? Integer.MAX_VALUE : tileRows.get(0);
		final int tileLastRow = tileRows.isEmpty() ? Integer.MAX_VALUE : tileRows.get(tileRows.size()-1);
		final int tileFirstColumn = tileColumns.isEmpty() ? Integer.MAX_VALUE : tileColumns.get(0);
		final int tileLastColumn = tileColumns.isEmpty() ? Integer.MAX_VALUE : tileColumns.get(tileColumns.size()-1);
		
		// get rows/columns which are even
		boolean even = false;
		ListIterator<Integer> iterator;
		if(!wellRows.isEmpty())
		{
			boolean skipFirst = numWellY % 2 != 0;
			even = false;
			iterator = wellRows.listIterator();
			while(iterator.hasNext())
			{
				iterator.next();
				if(!even)
					iterator.remove();
				if(!skipFirst)
					even = !even;
				else
					skipFirst = false;
					
			}
			// put back in hash set for quicker sorting
			wellRowsSet.clear();
			wellRowsSet.addAll(wellRows);
		}
		if(!wellColumns.isEmpty())
		{
			even = false;
			iterator = wellColumns.listIterator();
			while(iterator.hasNext())
			{
				iterator.next();
				if(!even)
					iterator.remove();
				even = !even;	
			}
			// put back in hash set for quicker sorting
			wellColumnsSet.clear();
			wellColumnsSet.addAll(wellColumns);
		}
		if(!tileRows.isEmpty())
		{
			even = false;
			iterator = tileRows.listIterator(tileRows.size());
			while(iterator.hasPrevious())
			{
				iterator.previous();
				if(!even)
					iterator.remove();
				even = !even;
			}
			
			// put back in hash set for quicker sorting
			tileRowsSet.clear();
			tileRowsSet.addAll(tileRows);
		}
		if(!tileColumns.isEmpty())
		{
			even = false;
			iterator = tileColumns.listIterator(tileColumns.size());
			while(iterator.hasPrevious())
			{
				iterator.previous();
				if(!even)
					iterator.remove();
				even = !even;
			}
			
			// put back in hash set for quicker sorting
			tileColumnsSet.clear();
			tileColumnsSet.addAll(tileColumns);
		}
		
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
							// First well in first column at the very beginning.
							if(well1.getWellX() == wellFirstColumn && well1.getWellY() == wellFirstRow)
								return -1;
							else if(well2.getWellX() == wellFirstColumn && well2.getWellY() == wellFirstRow)
								return 1;
							
							// all other wells in first column at the very end, and down to up
							if(well1.getWellX() == wellFirstColumn && well2.getWellX() != wellFirstColumn)
								return 1;
							else if(well1.getWellX() != wellFirstColumn && well2.getWellX() == wellFirstColumn)
								return -1;
							else if(well1.getWellX() == wellFirstColumn && well2.getWellX() == wellFirstColumn)
								return well1.getWellY() < well2.getWellY() ? 1 : -1;
							
							// special rule: if odd number of rows, first two in vertical zig-zag
							if(wellSecondRow != Integer.MAX_VALUE && well1.getWellY() <= wellSecondRow && well2.getWellY() <= wellSecondRow)
							{
								if(well1.getWellX() != well2.getWellX())
									return well1.getWellX() < well2.getWellX() ? -1 : 1;
								else if(well1.getWellY() == wellFirstRow && well2.getWellY() == wellSecondRow)
									return wellColumnsSet.contains(well1.getWellX()) ? -1 : 1;
								else //if(well2.getWellY() == wellFirstRow && well1.getWellY() == wellSecondRow)
									return wellColumnsSet.contains(well1.getWellX()) ? 1 : -1;
							}
							// all other wells in zig-zag
							else if(well1.getWellY() < well2.getWellY())
								return -1;
							else if(well1.getWellY() > well2.getWellY())
								return 1;
							if(wellRowsSet.contains(well1.getWellY()))
							{
								return well1.getWellX() > well2.getWellX() ? -1 : 1;
							}
							return well1.getWellX() < well2.getWellX() ? -1 : 1;
						}
						
						int[] pos1 = positionInformation1.getPositions();
						int[] pos2 = positionInformation2.getPositions();
						
						// Either one or both have zero length
						if(pos1.length == 0 && pos2.length == 0)
							return 0;
						else if(pos1.length == 0 && pos2.length != 0)
							return -1;
						else if (pos1.length != 0 && pos2.length == 0)
							return 1;
						// Either one or both have one length
						else if (pos1.length == 1 && pos2.length == 1)
							return pos1[0] < pos2[0] ? -1 : (pos1[0] > pos2[0] ? 1 : 0);
						else if (pos1.length > 1 && pos2.length == 1)
						{
							if(pos1[0] != pos2[0])
								return pos1[0] < pos2[0] ? -1 : 1;
							return 1;
						}
						else if (pos1.length == 1 && pos2.length > 1)
						{
							if(pos1[0] != pos2[0])
								return pos1[0] < pos2[0] ? -1 : 1;
							return -1;
						}
						// Both have length two or higher
						else if(pos1[0] == pos2[0] && pos1[1] == pos2[1])
						{
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
						else if((well1 == null && well2 == null) || (numWellX <= 1 && numWellY <= 1)) 
						{
							// both wells are null, or we have only one well, and we have two different tiles. Make snake with tiles
							// First tile in first column at the very beginning.
							if(pos1[1] == tileFirstColumn && pos1[0] == tileFirstRow)
								return -1;
							else if(pos2[1] == tileFirstColumn && pos2[0] == tileFirstRow)
								return 1;
							
							// all other tiles in first column at the very end, and down to up
							if(pos1[1] == tileFirstColumn && pos2[1] != tileFirstColumn)
								return 1;
							else if(pos1[1] != tileFirstColumn && pos2[1] == tileFirstColumn)
								return -1;
							else if(pos1[1] == tileFirstColumn && pos2[1] == tileFirstColumn)
								return pos1[0] < pos2[0] ? 1 : -1;
							
							// all other wells in zig-zag
							if(pos1[0] < pos2[0])
								return -1;
							else if(pos1[0] > pos2[0])
								return 1;
							if(!tileRowsSet.contains(pos1[0]))
							{
								return pos1[1] > pos2[1] ? -1 : 1;
							}
							return pos1[1] < pos2[1] ? -1 : 1;
						}
						// special rule for first column of wells
						else if(well1!= null && well1.getWellX() == wellFirstColumn && well1.getWellY() != wellFirstRow)
						{
							// yet another special rule for second row, if number of rows odd
							if(well1.getWellY() == wellSecondRow)
							{
								if(pos1[0] == tileLastRow && pos2[0] == tileLastRow)
									return pos1[1] < pos2[1] ? -1 : 1;
								else if(pos1[0] == tileLastRow)
									return -1;
								else if(pos2[0] == tileLastRow)
									return 1;
								else if(pos1[1]!=pos2[1])
									return pos1[1]>pos2[1] ? -1 : 1;
								else if(!tileColumnsSet.contains(pos1[1]))
									return pos1[0] > pos2[0] ? -1 : 1;
								return pos1[0] < pos2[0] ? -1 : 1;
							}
							
							// other rows
							if(wellRowsSet.contains(well1.getWellY()) || numTileY % 2 == 0)
							{
								if(pos1[0] != pos2[0])
									return pos1[0] > pos2[0] ? -1 : 1;
								else if(!tileRowsSet.contains(pos1[0]))
									return pos1[1] > pos2[1] ? -1 : 1;
								else
									return pos1[1] < pos2[1] ? -1 : 1;
							}
							if(pos1[0] != pos2[0])
								return pos1[0] > pos2[0] ? -1 : 1;
							else if(!tileRowsSet.contains(pos1[0]))
								return pos1[1] < pos2[1] ? -1 : 1;
							else
								return pos1[1] > pos2[1] ? -1 : 1;
						}
						// special rule for second row of wells, if number of rows is odd
						else if(well1!= null && well1.getWellX() != wellFirstColumn && well1.getWellY() == wellSecondRow)
						{
							if(wellColumnsSet.contains(well1.getWellX()))
							{
								if(pos1[0] == tileFirstRow && pos2[0] == tileFirstRow)
									return pos1[1] > pos2[1] ? -1 : 1;
								else if(pos1[0] == tileFirstRow)
									return -1;
								else if(pos2[0] == tileFirstRow)
									return 1;
								else if(pos1[1] < pos2[1])
									return -1;
								else if(pos1[1] > pos2[1])
									return 1;
								else if(tileColumnsSet.contains(pos1[1]) == (numTileX % 2 == 1))
									return pos1[0] < pos2[0] ? 1 : -1;
								return pos1[0] > pos2[0] ? 1 : -1;
							}
							if(pos1[0] == tileFirstRow && pos2[0] == tileFirstRow)
								return pos1[1] > pos2[1] ? -1 : 1;
							else if(pos1[0] == tileFirstRow)
								return 1;
							else if(pos2[0] == tileFirstRow)
								return -1;
							else if(pos1[1] < pos2[1])
								return -1;
							else if(pos1[1] > pos2[1])
								return 1;
							else if(!tileColumnsSet.contains(pos1[1]))
								return pos1[0] < pos2[0] ? 1 : -1;
							return pos1[0] > pos2[0] ? 1 : -1;
						}
						// all other columns
						else if(well1!= null && wellRowsSet.contains(well1.getWellY()))
						{
							// Check if one or both are in first column
							if(pos1[1] == tileFirstColumn && pos2[1] == tileFirstColumn)
							{
								return pos1[0] < pos2[0] ? -1 : 1;
							}
							else if(pos1[1] == tileFirstColumn)
								return 1;
							else if(pos2[1] == tileFirstColumn)
								return -1;
							// special rule for most right column of wells, most right tiles
							else if(well1.getWellX() == wellLastColumn && pos1[1] == tileLastColumn && pos2[1] == tileLastColumn)
								return pos1[0] < pos2[0] ? -1 : 1;
							else  if(well1.getWellX() == wellLastColumn && pos1[1] == tileLastColumn)
								return -1;
							else  if(well1.getWellX() == wellLastColumn && pos2[1] == tileLastColumn)
								return 1;
							// None in first column. Go inverse snake up
							else if(pos1[0] != pos2[0])
								return pos1[0] > pos2[0] ? -1 : 1;
							else if(!tileRowsSet.contains(pos1[0]))
								return pos1[1] > pos2[1] ? -1 : 1;
							else
								return pos1[1] < pos2[1] ? -1 : 1;
						}
						else // if(well1!= null && !wellRowsSet.contains(well1.getWellY()))
						{
							// Check if one or both are in last column
							if(pos1[1] == tileLastColumn && pos2[1] == tileLastColumn)
								return pos1[0] < pos2[0] ? -1 : 1;
							else if(pos1[1] == tileLastColumn)
								return 1;
							else if(pos2[1] == tileLastColumn)
								return -1;
							// special rule for second column of wells, most left tiles, but only if not first row of wells
							else if(well1.getWellX() == wellSecondColumn && well1.getWellY() != wellFirstRow && pos1[1] == tileFirstColumn && pos2[1] == tileFirstColumn)
								return pos1[0] < pos2[0] ? -1 : 1;
							else  if(well1.getWellX() == wellSecondColumn && well1.getWellY() != wellFirstRow && pos1[1] == tileFirstColumn)
								return -1;
							else  if(well1.getWellX() == wellSecondColumn && well1.getWellY() != wellFirstRow && pos2[1] == tileFirstColumn)
								return 1;
							// None in last column. Go snake up
							else if(pos1[0] != pos2[0])
								return pos1[0] > pos2[0] ? -1 : 1;
							else if(!tileRowsSet.contains(pos1[0]))
								return pos1[1] > pos2[1] ? 1 : -1;
							else
								return pos1[1] < pos2[1] ? 1 : -1;
						}
						
					}
			
				});
		return path;
	}
}
