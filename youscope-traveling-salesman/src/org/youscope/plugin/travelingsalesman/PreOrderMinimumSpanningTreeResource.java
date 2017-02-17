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
package org.youscope.plugin.travelingsalesman;

import java.awt.geom.Point2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.youscope.addon.pathoptimizer.PathOptimizerResource;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.resource.ResourceAdapter;
import org.youscope.common.resource.ResourceException;


/**
 * 2-approximation of the traveling salesman problem, by using a pre-order walk of the minimum spanning tree.
 * The resulting path is at most two times as long as the optimal one.
 * @author Moritz Lang
 *
 */
public class PreOrderMinimumSpanningTreeResource extends ResourceAdapter<PreOrderMinimumSpanningTreeConfiguration> implements PathOptimizerResource
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -821986122194394006L;
	/**
	 * Constructor.
	 * @param positionInformation logical position in measurement.
	 * @param configuration Configuration of resource.
	 * @throws RemoteException 
	 * @throws ConfigurationException
	 */
	public PreOrderMinimumSpanningTreeResource(PositionInformation positionInformation, PreOrderMinimumSpanningTreeConfiguration configuration) throws ConfigurationException, RemoteException {
		super(positionInformation, configuration, PreOrderMinimumSpanningTreeConfiguration.TYPE_IDENTIFIER, PreOrderMinimumSpanningTreeConfiguration.class, "Pre-order search of minimum spanning tree (2-approximation)");
	}
	@Override
	public List<PositionInformation> getPath(Map<PositionInformation, ? extends Point2D.Double> positions)
			throws ResourceException {
				// Represent all positions in the microplate as a Vertex.
				ArrayList<Vertex> vertices;
				try {
					vertices = new ArrayList<>(OptimizerHelper.toVertices(positions));
				} catch (InterruptedException e) {
					throw new ResourceException("Computation interrupted by user.", e);
				}
				if(vertices.size() <= 0)
					return new ArrayList<PositionInformation>(0);
				// Calculate shortest path approximation
				Vertex[] hamiltonianCycle;
				try {
					hamiltonianCycle = OptimizerHelper.salesmanPreorderMSP(vertices, OptimizerHelper.getMetric(getConfiguration().getMetric()));
				} catch (InterruptedException e) {
					throw new ResourceException("Computation interrupted by user.", e);
				}
				
				// Convert to output format
				return OptimizerHelper.toOutput(hamiltonianCycle);
	}

	
}
