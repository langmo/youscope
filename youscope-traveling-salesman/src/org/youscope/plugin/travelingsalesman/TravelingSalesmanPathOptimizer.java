/**
 * 
 */
package org.youscope.plugin.travelingsalesman;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.youscope.addon.pathoptimizer.PathOptimizer;
import org.youscope.addon.pathoptimizer.PathOptimizerPosition;
import org.youscope.common.measurement.microplate.Well;
import org.youscope.plugin.microplatemeasurement.MicroplatePositionConfiguration;
import org.youscope.plugin.microplatemeasurement.XYAndFocusPosition;
import org.youscope.plugin.travelingsalesman.blossom.Algorithm;
import org.youscope.plugin.travelingsalesman.blossom.Algorithm.PrimalUpdateType;

import sun.swing.plaf.synth.Paint9Painter.PaintType;

/**
 * An optimizer which assumes no specific selected wells and positions. Just iterates over all wells in a zick-zack manner
 * (first row from lowest well to highest, second row from highest to lowest, and so forth). For every well, it also iterates in a
 * zick-zack manner over all positions. However, it alternates iterating from top to bottom and from bottom to top, as well as starts
 * iterating from the leftmost well if the well iteration is currently positive, and vice versa.
 * Probably the best general optimizer if no specific information about selected wells and positions can be assumed, and if no specific optimization algorithm should be run.
 * @author Moritz Lang 
 *
 */
public class TravelingSalesmanPathOptimizer implements PathOptimizer
{
	private static class Vertex extends Point2D.Double
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
				return new Well(wellY, wellX).toString();
			}
	}
	/**
	 * An implementation of a metric, that is, a definition of the distance between two points on the cartesian plane.
	 * @author mlang
	 *
	 */
	public static interface Metric
	{
		/**
		 * Returns the distance between the two points. For example, the Euclidean distance would be sqrt((x1-x2)^2+(y1-y2)^2).
		 * @param point1 First point.
		 * @param point2 Second point.
		 * @return distance between points.
		 */
		public double distance(Point2D.Double point1, Point2D.Double point2);
	}
	
	/**
	 * Implementation of the Manhatten distance.
	 */
	public static final Metric MANHATTEN = new Metric()
	{
		@Override
		public double distance(java.awt.geom.Point2D.Double point1, java.awt.geom.Point2D.Double point2) {
			return Math.abs(point1.x-point2.x) + Math.abs(point1.y-point2.y);
		}
	};
	
	private static List<List<Vertex>> getTestCases()
	{
		ArrayList<List<Vertex>> testCases = new ArrayList<>();
		
		// Test case 1
		ArrayList<Vertex> testCase = new ArrayList<>();
		double distanceWell = 9000;
		for(int wellY = 0; wellY < 3; wellY++)
		{
			for(int wellX = 0; wellX < 3; wellX++)
			{
				if(wellY==0 && wellX == 1)
					continue;
				testCase.add(new Vertex(wellX*distanceWell, wellY*distanceWell,wellY,wellX,0,0));
			}
		}
		testCases.add(testCase);
		
		// Test case 2
		testCase = new ArrayList<>();
		for(int wellY = 0; wellY < 4; wellY++)
		{
			for(int wellX = 0; wellX < 3; wellX++)
			{
				if(wellY==0 && wellX == 1)
					continue;
				testCase.add(new Vertex(wellX*distanceWell, wellY*distanceWell,wellY,wellX,0,0));
			}
		}
		testCases.add(testCase);

		// Test case 3
		testCase = new ArrayList<>();
		for(int wellY = 0; wellY < 5; wellY++)
		{
			for(int wellX = 0; wellX < 5; wellX++)
			{
				if(wellY == 0 && wellX != 0 && wellX != 4)
					continue;
				if(wellY == 1 && wellX != 1 && wellX != 4 )
					continue;
				testCase.add(new Vertex(wellX*distanceWell, wellY*distanceWell,wellY,wellX,0,0));
			}
		}
		testCases.add(testCase);
				
		
		return testCases;
	}
	
	/**
	 * Stand-alone test case main function. 
	 * Prints out test path.
	 * @param args Not considered.
	 */
	public static void main(String[] args)
	{
		if(args.length == 0)
		{
			List<List<Vertex>> testCases = getTestCases();
			for(List<Vertex> testCase : testCases)
			{
				System.out.println("Example problem: ");
				System.out.println("=================");
				for(Vertex vertex:testCase)
				{
					String wellY = ""+(char) ('A'+vertex.wellY);
					String wellX = Integer.toString(vertex.wellX+1);
					System.out.println(wellY+wellX+": x/y="+Double.toString(vertex.x)+"/"+Double.toString(vertex.y));
				}
				Vertex[] path = salesmanPreorderMSP(testCase, MANHATTEN);
				
				System.out.println();
				System.out.println("Shortest path: ");
				System.out.println("=================");
				for(int i=0; i<path.length; i++)
				{
					if(i>0)
						System.out.print(" -> ");
					String wellY = ""+(char) ('A'+path[i].wellY);
					String wellX = Integer.toString(path[i].wellX+1);
					System.out.print(wellY+wellX);
				}
				System.out.println();
				System.out.println();
			}
		}
		else
		{
			List<List<Vertex>> testCases = getTestCases();
			List<Vertex> testCase = testCases.get(2);
			
			System.out.println("Example problem: ");
			System.out.println("=================");
			for(Vertex vertex:testCase)
			{
				String wellY = ""+(char) ('A'+vertex.wellY);
				String wellX = Integer.toString(vertex.wellX+1);
				System.out.println(wellY+wellX+": x/y="+Double.toString(vertex.x)+"/"+Double.toString(vertex.y));
			}
			salesmanChristofides(testCase, MANHATTEN);
		}
	}
	
	@Override
	public Iterable<PathOptimizerPosition> getPath(MicroplatePositionConfiguration posConf)
	{
		// Represent all positions in the microplate as a Vertex.
		ArrayList<Vertex> vertices = new ArrayList<>(posConf.getNumMeasuredWells()*posConf.getNumMeasuredPos());
		for(int wellY = 0; wellY < posConf.getNumWellsY(); wellY++)
		{
			for(int wellX = 0; wellX < posConf.getNumWellsX(); wellX++)
			{
				if(!posConf.isMeasureWell(new Well(wellY, wellX)))
					continue;
				for(int posY = 0; posY < posConf.getWellNumPositionsY(); posY++)
				{
					for(int posX =0; posX < posConf.getWellNumPositionsX(); posX++)
					{
						if(!posConf.isMeasurePosition(posY, posX))
							continue;
						XYAndFocusPosition pos = posConf.getPosition(new Well(wellY, wellX), posY, posX);
						vertices.add(new Vertex(pos.getX(), pos.getY(), wellY, wellX, posY, posX));
					}
				}
			}
		}
		if(vertices.size() <= 0)
			return new ArrayList<PathOptimizerPosition>(0);
		// Calculate shortest path approximation
		Vertex[] hamiltonianCycle = salesmanPreorderMSP(vertices, MANHATTEN);
		
		// Convert to output format
		ArrayList<PathOptimizerPosition> result = new ArrayList<>(hamiltonianCycle.length);
		for(Vertex vertex:hamiltonianCycle)
		{
			result.add(new PathOptimizerPosition(posConf.getPosition(new Well(vertex.wellY, vertex.wellX), vertex.posY, vertex.posX), vertex.wellY, vertex.wellX, vertex.posY, vertex.posX));
		}
		return result;
	}
	
	/**
	 * Approximating the travelling salesman problem by a pre-order walk of the minimal spanning tree.
	 * Known to to be a 2-approximation algorithm, i.e. maximally twice as bad as the optimal solution.
	 * @param vertices Vertices of the graph.
	 * @param metric Metric to use to calculate distances between vertices.
	 * @return Hamiltonian path (but without start point at end).
	 */
	public static Vertex[] salesmanPreorderMSP(List<Vertex> vertices, Metric metric)
	{
		Vertex[] spanningTree = toMinimumSpanningTree(vertices, metric);
		List<Vertex> hamiltonianCycle = preOrderWalk(spanningTree[0]);
		return hamiltonianCycle.toArray(new Vertex[hamiltonianCycle.size()]);
	}
	
	public static Vertex[] salesmanChristofides(List<Vertex> vertices, Metric metric)
	{
		Vertex[] spanningTree = toMinimumSpanningTree(vertices, metric);
		// make graph out of spanning tree
		for(Vertex vertex:spanningTree)
		{
			for(Vertex child : vertex.edgesTo)
			{
				child.edgesTo.add(vertex);
			}
		}
		
		// get odd degree vertices.
		ArrayList<org.youscope.plugin.travelingsalesman.blossom.Vertex<Vertex>> blossomVertices = new ArrayList<>();
		for(Vertex vertex:spanningTree)
		{
			if(vertex.edgesTo.size()%2 == 1)
			{
				blossomVertices.add(new org.youscope.plugin.travelingsalesman.blossom.Vertex<Vertex>(vertex));
				 
			}
		}
		System.out.println("======================="); 
		System.out.println("= Odd degree vertices =");
		System.out.println("=======================");
		for(org.youscope.plugin.travelingsalesman.blossom.Vertex<Vertex> vertex : blossomVertices)
		{
			System.out.println("V:"+ vertex.toString());
		}
		
		// Create edges.
		ArrayList<org.youscope.plugin.travelingsalesman.blossom.Edge> blossomEdges = new ArrayList<>();
		for(int i=0; i<blossomVertices.size()-1; i++)
		{
			org.youscope.plugin.travelingsalesman.blossom.Vertex<Vertex> v1 = blossomVertices.get(i);
			for(int j=i+1; j<blossomVertices.size(); j++)
			{
				org.youscope.plugin.travelingsalesman.blossom.Vertex<Vertex> v2 = blossomVertices.get(j);
				blossomEdges.add(new org.youscope.plugin.travelingsalesman.blossom.Edge(v1,v2, metric.distance(v1.getContent(), v2.getContent())));
			}
		}
		
		Algorithm christofides = new Algorithm(blossomVertices, blossomEdges);
		System.out.println("=======================");
		System.out.println("=   Initial State     =");
		System.out.println("=======================");
		christofides.printState();
		
		boolean changed = false;
		int iteration = 1;
		do
		{
			System.out.println();
			System.console().readLine("Press OK to continue...");
			
			changed = false;
			Algorithm.PrimalUpdateType primalType;
			primalType = christofides.primalUpdate();
			changed |= (primalType != PrimalUpdateType.NONE && primalType != PrimalUpdateType.FINISHED);
			double delta; 
			RuntimeException lastException = null;
			try
			{
				delta =christofides.dualUpdate(); 
			}
			catch(RuntimeException e)
			{
				lastException = e;
				delta = -1;
			}
			changed |= delta > 0;
			System.out.println("=======================");
			System.out.println("=   Iteration " + Integer.toString(iteration++)+"      =");
			System.out.println("=   delta=" + Double.toString(delta)+"     =");
			System.out.println("=   primal=" + primalType.toString()+"     ="); 
			System.out.println("=======================");
			christofides.printState();
			if(lastException != null)
				throw lastException;
		}while(changed);
		System.out.println();
		System.console().readLine("Nothing left to do.");
		return null;
	}
	
	private static List<Vertex> preOrderWalk(Vertex root)
	{
		ArrayList<Vertex> walk = new ArrayList<>();
		walk.add(root);
		for(Vertex child : root.edgesTo)
		{
			walk.addAll(preOrderWalk(child));
		}
		return walk;
	}
	private static Vertex[] toMinimumSpanningTree(List<Vertex> vertices, Metric metric)
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
			minDist = Double.MAX_VALUE;
			// Prefer leaf vertices to be connected to new vertices.
			for(int i=numTreeVertices-1; i>=0; i--)
			{
				for(int j=vertices.size()-1; j>=0; j--)
				{
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

	@Override
	public boolean isApplicable(MicroplatePositionConfiguration posConf)
	{
		if(!posConf.isAliasMicroplate())
			return true;
		return false;
	}

	@Override
	public double getSpecificity(MicroplatePositionConfiguration posConf)
	{
		// not very specific, but better than no optimization at all...
		if(isApplicable(posConf))
			return 0.1;
		return -1;
	}

	@Override
	public String getName()
	{
		return "Zig-Zag Path";
	}

	@Override
	public String getOptimizerID()
	{
		return "YouScope.ZigZagPathOptimizer";
	}

}
