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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.youscope.common.PositionInformation;
import org.youscope.common.resource.ResourceException;
import org.youscope.plugin.travelingsalesman.blossom.BlossomAlgorithm;
import org.youscope.plugin.travelingsalesman.blossom.BlossomException;
import org.youscope.plugin.travelingsalesman.blossom.BlossomListener;
import org.youscope.plugin.travelingsalesman.blossom.PrimalUpdateType;


/**
 * Helper functions for path optimizers
 * @author Moritz Lang
 *
 */
class OptimizerHelper  
{
	
	public static Metric getMetric(MetricType metricType)
	{
		switch(metricType)
		{
		case MANHATTEN:
			return getManhattenMetric();
		case EUCLIDEAN:
			return getEuclideanMetric();
		case MAXIMUM:
			return getMaximumMetric();
		default:
			return getManhattenMetric();	
		}
	}
	/**
	 * Returns the Manhattan metric.
	 * @return Manhattan metric.
	 */
	public static Metric getManhattenMetric()
	{
		return new Metric()
		{
			@Override
			public double distance(java.awt.geom.Point2D.Double point1, java.awt.geom.Point2D.Double point2) {
				return Math.abs(point1.x-point2.x) + Math.abs(point1.y-point2.y);
			}
		};
	}
	
	/**
	 * Returns the Euclidean metric.
	 * @return Euclidean metric.
	 */
	public static Metric getEuclideanMetric()
	{
		return new Metric()
		{
			@Override
			public double distance(java.awt.geom.Point2D.Double point1, java.awt.geom.Point2D.Double point2) {
				return Math.sqrt(Math.pow(point1.x-point2.x, 2.) + Math.pow(point1.y-point2.y, 2.));
			}
		};
	}
	
	/**
	 * Returns the Maximum metric.
	 * @return Maximum metric.
	 */
	public static Metric getMaximumMetric()
	{
		return new Metric()
		{
			@Override
			public double distance(java.awt.geom.Point2D.Double point1, java.awt.geom.Point2D.Double point2) {
				return Math.max(Math.abs(point1.x-point2.x), Math.abs(point1.y-point2.y));
			}
		};
	}
	/**
	 * Returns the cost of a tour.
	 * @param tour Tour (without back).
	 * @param metric Metric to calculate the cost.
	 * @return Cost of tour (with back to first vertex).
	 * @throws InterruptedException 
	 */
	public static double getTourCost(Vertex[] tour, Metric metric) throws InterruptedException
	{
		double cost = 0;
		for(int i=1; i<tour.length; i++)
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			cost += metric.distance(tour[i-1], tour[i]);
		}
		cost += metric.distance(tour[tour.length-1], tour[0]);
		return cost;
	}
	/**
	 * Makes a pre-order walk through the tree with the given root.
	 * @param root Root of the tree.
	 * @return Result of the pre-order walk, without dublicates.
	 * @throws InterruptedException 
	 */
	public  static List<Vertex> preOrderWalk(Vertex root) throws InterruptedException
	{
		ArrayList<Vertex> walk = new ArrayList<>();
		walk.add(root);
		for(Vertex child : root.edgesTo)
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			walk.addAll(preOrderWalk(child));
		}
		return walk;
	}
	/**
	 * Returns the minimum spanning tree of a set of vertices.
	 * @param vertices Vertices of tree.
	 * @param metric Metric to calculate distances.
	 * @return Minimum spanning tree.
	 * @throws InterruptedException 
	 */
	public  static Vertex[] toMinimumSpanningTree(List<Vertex> vertices, Metric metric) throws InterruptedException
	{
		final Vertex[] treeVertices = new Vertex[vertices.size()]; 
		
		// Construct minimum spanning tree.
		treeVertices[0]=vertices.remove(0);
		int numTreeVertices = 1;
		
		int minFrom;
		int minTo;
		double minDist;

		while(numTreeVertices<treeVertices.length)
		{
			minFrom = -1;
			minTo = -1;
			minDist = java.lang.Double.MAX_VALUE;
			// Prefer leaf vertices to be connected to new vertices.
			for(int i=numTreeVertices-1; i>=0; i--)
			{
				for(int j=vertices.size()-1; j>=0; j--)
				{
					if(Thread.interrupted())
						throw new InterruptedException();
					double dist = metric.distance(treeVertices[i], vertices.get(j));
					if(dist<minDist)
					{
						minFrom = i;
						minTo = j;
						minDist = dist;
					}
				}
			}
			treeVertices[numTreeVertices] = vertices.remove(minTo);
			minTo = numTreeVertices++;
			treeVertices[minFrom].edgesTo.add(treeVertices[minTo]);
		}
		
		return treeVertices;
	}
	
	/**
	 * Approximating the travelling salesman problem by a pre-order walk of the minimal spanning tree.
	 * Known to to be a 2-approximation algorithm, i.e. maximally twice as bad as the optimal solution.
	 * @param vertices Vertices of the graph.
	 * @param metric Metric to use to calculate distances between vertices.
	 * @return Hamiltonian path (but without start point at end).
	 * @throws InterruptedException 
	 */
	public static Vertex[] salesmanPreorderMSP(List<Vertex> vertices, Metric metric) throws InterruptedException
	{
		Vertex[] spanningTree = OptimizerHelper.toMinimumSpanningTree(vertices, metric);
		List<Vertex> hamiltonianCycle = OptimizerHelper.preOrderWalk(spanningTree[0]);
		return hamiltonianCycle.toArray(new Vertex[hamiltonianCycle.size()]);
	}
	
	public static Collection<Vertex> toVertices(Map<PositionInformation, ? extends Point2D.Double> positions) throws InterruptedException
	{
		// Represent all positions in the microplate as a Vertex.
		ArrayList<Vertex> vertices = new ArrayList<>(positions.size());
		for(Entry<PositionInformation, ? extends Point2D.Double> position : positions.entrySet())
		{
			vertices.add(new Vertex(position.getValue(), position.getKey()));
		}
		Collections.sort(vertices);
		return vertices;
	}
	public static List<PositionInformation> toOutput(Vertex[] path)
	{
		// Convert to output format
		ArrayList<PositionInformation> result = new ArrayList<>(path.length);
		for(final Vertex vertex:path)
		{
			result.add(vertex.positionInformation);
		}
		return result;
	}
	
	/**
	 * Calls directly the blossom algorithm on the vertices. Prints state after each iteration of the blossom algorithm
	 * to the console. Ment for debugging.
	 * @param vertices Vertices to connect. Must be an even number.
	 * @param metric Metric to use.
	 * @throws ResourceException
	 * @throws InterruptedException
	 */
	static void blossom(List<Vertex> vertices, Metric metric) throws ResourceException, InterruptedException
	{
		// get odd degree vertices.
		ArrayList<org.youscope.plugin.travelingsalesman.blossom.Vertex> blossomVertices = new ArrayList<>();
		for(Vertex vertex:vertices)
		{
				blossomVertices.add(new org.youscope.plugin.travelingsalesman.blossom.Vertex(vertex));
		}
		
		// Create edges.
		ArrayList<org.youscope.plugin.travelingsalesman.blossom.Edge> blossomEdges = new ArrayList<>();
		for(int i=0; i<blossomVertices.size()-1; i++)
		{
			org.youscope.plugin.travelingsalesman.blossom.Vertex v1 = blossomVertices.get(i);
			for(int j=i+1; j<blossomVertices.size(); j++)
			{
				if(Thread.interrupted())
					throw new InterruptedException();
				org.youscope.plugin.travelingsalesman.blossom.Vertex v2 = blossomVertices.get(j);
				blossomEdges.add(new org.youscope.plugin.travelingsalesman.blossom.Edge(v1,v2, metric.distance((Vertex)v1.getContent(), (Vertex)v2.getContent())));
			}
		}
		
		// Run Blossom algorithm to find minimum perfect matching.
		final BlossomAlgorithm blossom = new BlossomAlgorithm(blossomVertices, blossomEdges);
		blossom.addBlossomListener(new BlossomListener() 
		{
			private int iteration = 1;
			@Override
			public void primalUpdate(PrimalUpdateType type) 
			{
				System.out.println("/**************************");
				System.out.println("/* Primal update " + Integer.toString(iteration));
				System.out.println("/* Type "+ type.toString());
				System.out.println("/**************************");
				try {
					System.out.println(blossom.printState());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void dualUpdate(double dualChange) {
				System.out.println("/* Dual update:");
				System.out.println("delta="+ java.lang.Double.toString(dualChange));
				System.console().readLine("Press button to continue!");
			}
		});
		try {
			blossom.getMatches();
		} catch (BlossomException e) {
			String state;
			try {
				state = blossom.printState();
			} catch (BlossomException e1) {
				state="Error "+e1.getMessage()+" determining current state.\n";
			}
			StringBuilder initialState = new StringBuilder();
			for(org.youscope.plugin.travelingsalesman.blossom.Vertex blossomVertex : blossomVertices)
			{
				Vertex vertex = (Vertex) blossomVertex.getContent();
				initialState.append(vertex.toString()+"(x="+java.lang.Double.toString(vertex.x)+", y="+java.lang.Double.toString(vertex.y)+")\n");
			}
			
			throw new ResourceException("Could not calculate perfect matching with Blossom algorithm.\nInitial state:\n"+initialState.toString()+"\n\nCurrent state:\n"+state, e);
		}
	}
	
	/**
	 * Approximates traveling salesman problem using the Christofides approximation.
	 * Known to to be a 1.5-approximation algorithm, i.e. maximally 1.5 as bad as the optimal solution.
	 * @param vertices Vertices of the graph.
	 * @param metric Metric to use to calculate distances between vertices.
	 * @return Hamiltonian path (but without start point at end).
	 * @throws ResourceException Thrown if path could not be computed.
	 * @throws InterruptedException Thrown if interrupted by user.
	 */
	public static Vertex[] salesmanChristofides(List<Vertex> vertices, Metric metric) throws ResourceException, InterruptedException
	{
		Vertex[] spanningTree = OptimizerHelper.toMinimumSpanningTree(vertices, metric);
		// make graph out of spanning tree
		for(Vertex vertex:spanningTree)
		{
			for(Vertex child : vertex.edgesTo)
			{
				if(Thread.interrupted())
					throw new InterruptedException();
				if(!child.edgesTo.contains(vertex))
					child.edgesTo.add(vertex);
			}
		}
		
		// get odd degree vertices.
		ArrayList<org.youscope.plugin.travelingsalesman.blossom.Vertex> blossomVertices = new ArrayList<>();
		for(Vertex vertex:spanningTree)
		{
			if(vertex.edgesTo.size()%2 == 1)
			{
				if(Thread.interrupted())
					throw new InterruptedException();
				blossomVertices.add(new org.youscope.plugin.travelingsalesman.blossom.Vertex(vertex));
			}
		}
		
		// Create edges.
		ArrayList<org.youscope.plugin.travelingsalesman.blossom.Edge> blossomEdges = new ArrayList<>();
		for(int i=0; i<blossomVertices.size()-1; i++)
		{
			org.youscope.plugin.travelingsalesman.blossom.Vertex v1 = blossomVertices.get(i);
			for(int j=i+1; j<blossomVertices.size(); j++)
			{
				if(Thread.interrupted())
					throw new InterruptedException();
				org.youscope.plugin.travelingsalesman.blossom.Vertex v2 = blossomVertices.get(j);
				blossomEdges.add(new org.youscope.plugin.travelingsalesman.blossom.Edge(v1,v2, metric.distance((Vertex)v1.getContent(), (Vertex)v2.getContent())));
			}
		}
		
		// Run Blossom algorithm to find minimum perfect matching.
		BlossomAlgorithm blossom = new BlossomAlgorithm(blossomVertices, blossomEdges);
		Collection<org.youscope.plugin.travelingsalesman.blossom.Edge> matches;
		try {
			matches = blossom.getMatches();
		} catch (BlossomException e) {
			String state;
			try {
				state = blossom.printState();
			} catch (BlossomException e1) {
				state="Error "+e1.getMessage()+" determining current state.\n";
			}
			StringBuilder initialState = new StringBuilder();
			for(org.youscope.plugin.travelingsalesman.blossom.Vertex blossomVertex : blossomVertices)
			{
				Vertex vertex = (Vertex) blossomVertex.getContent();
				initialState.append(vertex.toString()+"(x="+java.lang.Double.toString(vertex.x)+", y="+java.lang.Double.toString(vertex.y)+")\n");
			}
			
			throw new ResourceException("Could not calculate perfect matching with Blossom algorithm.\nInitial state:\n"+initialState.toString()+"\n\nCurrent state:\n"+state, e);
		}
		
		// Add matches to graph
		for(org.youscope.plugin.travelingsalesman.blossom.Edge edge : matches)
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			Vertex v1 = (Vertex)edge.getV1().getContent();
			Vertex v2 = (Vertex)edge.getV2().getContent();
			v1.edgesTo.add(v2);
			v2.edgesTo.add(v1);
		}
		
		// Make Eulerian tours
		ArrayList<Vertex> tour = null;
		while(tour==null || tour.size() < spanningTree.length)
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			int lastTourIdx = -1;
			ArrayList<Vertex> subTour = new ArrayList<Vertex>();
			Vertex start = null;
			if(tour == null)
				start = spanningTree[0];
			else
			{
				for(Vertex tVertex : tour)
				{
					if(Thread.interrupted())
						throw new InterruptedException();
					lastTourIdx++;
					if(!tVertex.edgesTo.isEmpty())
					{
						start = tVertex;
						break;
					}
				}
				if(start == null)
					throw new ResourceException("Cannot piece together Eulerian tours, since tours are not connected.");
			}
			Vertex last = start;
			while(true)
			{
				if(Thread.interrupted())
					throw new InterruptedException();
				if(last.edgesTo.isEmpty())
				{
					throw new ResourceException("Found vertex with odd degree: "+last.toString());
				}
				Vertex next = last.edgesTo.remove(0);
				if(!next.edgesTo.remove(last))
				{
					throw new ResourceException("Found invalid edge.");
				}
				if(next == start)
					break;
				subTour.add(next);
				last = next;
			}
			if(tour == null)
			{
				subTour.add(0, start);
				tour = subTour;
			}
			else
			{
				// join the two tours...
				joinTours(tour, subTour, lastTourIdx+1, metric);
			}
			//remove dublicates
			ArrayList<Vertex> seen = new ArrayList<Vertex>(tour.size());
			Iterator<Vertex> iterator = tour.iterator();
			while(iterator.hasNext())
			{
				if(Thread.interrupted())
					throw new InterruptedException();
				Vertex vertex = iterator.next();
				if(seen.contains(vertex))
					iterator.remove();
				else
					seen.add(vertex);
			}
		}
		
		return tour.toArray(new Vertex[tour.size()]);
	}
	
	private static void joinTours(List<Vertex> mainTour, List<Vertex> subTour, int lastTourIdx, Metric metric)
	{
		// There are four different ways how to join the walks.
		Vertex v1;
		if(lastTourIdx-1>=0)
			v1 = mainTour.get(lastTourIdx-1);
		else
			v1 = null;
		Vertex v2 = mainTour.get(lastTourIdx);
		Vertex v3;
		if(lastTourIdx+1<mainTour.size())
			v3 = mainTour.get(lastTourIdx+1);
		else
			v3 = null;
		
		Vertex w1 = subTour.get(0);
		Vertex w2 = subTour.get(subTour.size()-1);
		
		double[] tours = new double[4];
		if(v1 != null)
		{
			tours[0] = metric.distance(v1, w1)+metric.distance(v2, w2);
			tours[1] = metric.distance(v1, w2)+metric.distance(v2, w1);
		}
		else
		{
			tours[0] = java.lang.Double.MAX_VALUE;
			tours[1] = java.lang.Double.MAX_VALUE;
		}
		
		if(v3 != null)
		{
			tours[2] = metric.distance(v2, w1)+metric.distance(v3, w2);
			tours[3] = metric.distance(v2, w2)+metric.distance(v3, w1);
		}
		else
		{
			tours[2] = java.lang.Double.MAX_VALUE;
			tours[3] = java.lang.Double.MAX_VALUE;
		}
		int bestTour = -1;
		double minCost = java.lang.Double.MAX_VALUE;
		for(int i=0; i<4; i++)
		{
			if(tours[i]<minCost)
			{
				bestTour = i;
				minCost = tours[i];
			}
		}
		if(bestTour == 1 || bestTour == 3)
			Collections.reverse(subTour);
		int idx;
		if(bestTour == 0 || bestTour == 1)
			idx = lastTourIdx;
		else
			idx = lastTourIdx+1;
		mainTour.addAll(idx, subTour);
	}
}
