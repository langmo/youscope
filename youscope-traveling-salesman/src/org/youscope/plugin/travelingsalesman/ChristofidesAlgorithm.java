/**
 * 
 */
package org.youscope.plugin.travelingsalesman;

import java.util.ArrayList;

import org.youscope.addon.pathoptimizer.PathOptimizer;
import org.youscope.addon.pathoptimizer.PathOptimizerPosition;
import org.youscope.common.measurement.microplate.Well;
import org.youscope.common.resource.ResourceException;
import org.youscope.plugin.microplatemeasurement.MicroplatePositionConfiguration;

/**
 * 2-approximation of the traveling salesman problem, by using a pre-order walk of the minimum spanning tree.
 * The resulting path is at most two times as long as the optimal one.
 * @author Moritz Lang
 *
 */
public class ChristofidesAlgorithm implements PathOptimizer 
{
	@Override
	public Iterable<PathOptimizerPosition> getPath(MicroplatePositionConfiguration posConf) throws ResourceException
	{
		// Represent all positions in the microplate as a Vertex.
		ArrayList<Vertex> vertices;
		try {
			vertices = new ArrayList<>(OptimizerHelper.toVertices(posConf));
		} catch (Exception e1) {
			throw new ResourceException("Could not calculate shortest path.", e1);
		}
		if(vertices.size() <= 0)
			return new ArrayList<PathOptimizerPosition>(0);
		// Calculate shortest path approximation
		Vertex[] hamiltonianCycle;
		try {
			hamiltonianCycle = OptimizerHelper.salesmanChristofides(vertices, OptimizerHelper.getManhattenMetric());
		} catch (Exception e) {
			throw new ResourceException("Could not calculate shortest path.", e);
		}
		
		// Convert to output format
		ArrayList<PathOptimizerPosition> result = new ArrayList<>(hamiltonianCycle.length);
		for(Vertex vertex:hamiltonianCycle)
		{
			result.add(new PathOptimizerPosition(posConf.getPosition(new Well(vertex.wellY, vertex.wellX), vertex.posY, vertex.posX), vertex.wellY, vertex.wellX, vertex.posY, vertex.posX));
		}
		return result;
	}

	@Override
	public boolean isApplicable(MicroplatePositionConfiguration posConf)
	{
		return true;
	}

	@Override
	public double getSpecificity(MicroplatePositionConfiguration posConf)
	{
		// a very good approximation
		return 0.95;
	}

	@Override
	public String getName()
	{
		return "Christofides's Approximation (1.5-approximation)";
	}

	@Override
	public String getOptimizerID()
	{
		return "YouScope.Christofides";
	}

}
