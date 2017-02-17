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
package org.youscope.plugin.sawpath;

import java.awt.geom.Point2D.Double;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.youscope.addon.pathoptimizer.PathOptimizerResource;
import org.youscope.common.PositionInformation;
import org.youscope.common.Well;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.resource.ResourceAdapter;
import org.youscope.common.resource.ResourceException;

/**
 * This optimizer actually does not really optimize the path in any way, but just iterates through all wells from left to right and from top to bottom.
 * The same for the positions in a well.
 * @author Moritz Lang
 *
 */
public class SawPathResource extends ResourceAdapter<SawPathConfiguration> implements PathOptimizerResource
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -7269306932049313657L;

	/**
	 * Constructor.
	 * @param positionInformation logical position in measurement.
	 * @param configuration Configuration of resource.
	 * @throws RemoteException 
	 * @throws ConfigurationException
	 */
	public SawPathResource(PositionInformation positionInformation, SawPathConfiguration configuration) throws RemoteException, ConfigurationException {
		super(positionInformation, configuration, SawPathConfiguration.TYPE_IDENTIFIER, SawPathConfiguration.class, "Saw path");
		
	}

	@Override
	public List<PositionInformation> getPath(Map<PositionInformation, ? extends Double> positions)
			throws ResourceException, RemoteException {
		ArrayList<PositionInformation> path = new ArrayList<>(positions.keySet());
		if(getConfiguration().getDirection() == SawPathConfiguration.Direction.HORIZONTALLY)
			Collections.sort(path);
		else
			Collections.sort(path, new Comparator<PositionInformation>()
					{

						@Override
						public int compare(PositionInformation o1, PositionInformation o2) 
						{
							Well well1 = o1.getWell();
							Well well2 = o2.getWell();
							if(well1 == null && well2 != null)
								return -1;
							else if (well1 != null && well2 == null)
								return 1;
							else if(well1 != null && well2 != null)
							{
								if(well1.getWellX() != well2.getWellX())
									return well1.getWellX() < well2.getWellX() ? -1 : 1;
								if(well1.getWellY() != well2.getWellY())
									return well1.getWellY() < well2.getWellY() ? -1 : 1;
							}
							int[] positions1 = o1.getPositions();
							int[] positions2 = o2.getPositions();
							if(positions1.length >= 2 && positions2.length >= 2)
							{
								if(positions1[1] != positions2[1])
									return positions1[1] < positions2[1] ? -1 : 1;
							}
							for(int i=0; i<positions1.length && i < positions2.length; i++)
							{
								if(positions1[i]!=positions2[i])
									return positions1[i] < positions2[i] ? -1 : 1;
							}
							if(positions1.length < positions2.length)
								return -1;
							else if(positions1.length > positions2.length)
								return 1;
							return 0;
						}
				
					});
		return path;
	}
}
