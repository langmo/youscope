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

import java.util.ArrayList;
import java.util.List;

/**
 * Stand-alone console application to run a few test cases on the optimizers.
 * @author Moritz Lang
 *
 */
public class TestCases 
{
	private static void printUsage()
	{
		System.out.println("Usage:");
		System.out.println("youscope-traveling-salesman algo testcase");
		System.out.println();
		System.out.println("Parameters:");
		System.out.println("algo:     1 for pre-order walk on minimum spanning tree,");
		System.out.println("          2 for Christofides's approximation");
		System.out.println("          3 for Blossom algorithm");
		System.out.println("testcase: 0..3 for different test cases.");
	}
	/**
	 * Stand-alone test case main function. 
	 * Prints out test path.
	 * @param args Not considered.
	 */
	public static void main(String[] args)
	{
		List<List<Vertex>> testCases = getTestCases();
		int algo;
		int testCaseID;
		try
		{
			algo = Integer.parseInt(args[0]);
			testCaseID = Integer.parseInt(args[1]);
		}
		catch(@SuppressWarnings("unused") Exception e)
		{
			printUsage();
			return;
		}
		if(algo <1 || algo>3 || testCaseID < 0 || testCaseID >= testCases.size())
		{
			printUsage();
			return;
		}
		
		
		final Metric manhatten = OptimizerHelper.getManhattenMetric();
		if(algo == 1)
		{
			System.out.println("Algo = PreorderWalk");
		}
		else if(algo == 2)
		{
			System.out.println("Algo = Christofides");
		}
		else
		{
			System.out.println("Algo = Blossom");
		}
		
		
		List<Vertex> testCase = testCases.get(testCaseID);
		System.out.println("Example problem: ");
		System.out.println("=================");
		for(Vertex vertex:testCase)
		{
			System.out.println(vertex.positionInformation.getWell().toString()+": x/y="+Double.toString(vertex.x)+"/"+Double.toString(vertex.y));
		}
		Vertex[] path;
		if(algo == 1)
		{
			try {
				path = OptimizerHelper.salesmanPreorderMSP(testCase, manhatten);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
		}
		else if(algo == 2)
		{
			try {
				path = OptimizerHelper.salesmanChristofides(testCase, manhatten);
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
				System.exit(1);
				return;
			}
		}
		else
		{
			try {
				OptimizerHelper.blossom(testCase, manhatten);
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
				System.exit(1);
				return;
			}
			return;
		}
		
		System.out.println();
		try {
			System.out.println("Shortest path ( Cost = "+ Double.toString(OptimizerHelper.getTourCost(path, manhatten))+"):");
		} catch (InterruptedException e) {
			e.printStackTrace();
			return;
		}
		System.out.println("=================");
		for(int i=0; i<path.length; i++)
		{
			if(i>0)
				System.out.print(" -> ");
			System.out.print(path[i].positionInformation.getWell().toString());
		}
		System.out.println();
	}
	
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
		
		// Test case 4
		testCase = new ArrayList<>();
		
		int B=1;
		int C=2;
		int D=3;
		int E=4;
		int F=5;
		int G=6;
		int H=7;
		
		testCase.add(new Vertex(-3600.0, -3600.0, B, 4, 0, 0));
		testCase.add(new Vertex(0.0, -3600.0, B, 4, 0, 2));
		testCase.add(new Vertex(-1800.0, -3600.0, B, 4, 0, 1)); 
		testCase.add(new Vertex(9000.0, -3600.0, B, 5, 0, 2)); 
		testCase.add(new Vertex(7200.0, -3600.0, B, 5, 0, 1)); 
		testCase.add(new Vertex(18000.0, -3600.0, B, 6, 0, 2)); 
		testCase.add(new Vertex(16200.0, -3600.0, B, 6, 0, 1)); 
		testCase.add(new Vertex(27000.0, 0.0, B, 7, 2, 2)); 
		testCase.add(new Vertex(25200.0, -3600.0, B, 7, 0, 1)); 
		testCase.add(new Vertex(27000.0, 9000.0, C, 7, 2, 2));
		
		testCase.add(new Vertex(23400.0, 5400.0, C, 7, 0, 0)); 
		testCase.add(new Vertex(25200.0, 5400.0, C, 7, 0, 1));
		testCase.add(new Vertex(14400.0, 5400.0, C, 6, 0, 0));
		testCase.add(new Vertex(16200.0, 5400.0, C, 6, 0, 1));
		testCase.add(new Vertex(5400.0, 5400.0, C, 5, 0, 0));
		testCase.add(new Vertex(7200.0, 5400.0, C, 5, 0, 1));
		testCase.add(new Vertex(-1800.0, 5400.0, C, 4, 0, 1));
		testCase.add(new Vertex(54000.0, 9000.0, C, 10, 2, 2));
		testCase.add(new Vertex(50400.0, 5400.0, C, 10, 0, 0));
		testCase.add(new Vertex(50400.0, 7200.0, C, 10, 1, 0));
		
		testCase.add(new Vertex(50400.0, -1800.0, B, 10, 1, 0));
		testCase.add(new Vertex(50400.0, 18000.0, D, 10, 2, 0));
		testCase.add(new Vertex(52200.0, 14400.0, D, 10, 0, 1));
		testCase.add(new Vertex(54000.0, 27000.0, E, 10, 2, 2));
		testCase.add(new Vertex(52200.0, 23400.0, E, 10, 0, 1));
		testCase.add(new Vertex(50400.0, 36000.0, F, 10, 2, 0));
		testCase.add(new Vertex(52200.0, 32400.0, F, 10, 0, 1));
		testCase.add(new Vertex(50400.0, 45000.0, G, 10, 2, 0));
		testCase.add(new Vertex(54000.0, 41400.0, G, 10, 0, 2));
		testCase.add(new Vertex(52200.0, 41400.0, G, 10, 0, 1));
		
		testCase.add(new Vertex(68400.0, 32400.0, F, 12, 0, 0));
		testCase.add(new Vertex(68400.0, 36000.0, F, 12, 2, 0));
		testCase.add(new Vertex(68400.0, 23400.0, E, 12, 0, 0));
		testCase.add(new Vertex(68400.0, 25200.0, E, 12, 1, 0));
		testCase.add(new Vertex(68400.0, 14400.0, D, 12, 0, 0));
		testCase.add(new Vertex(68400.0, 16200.0, D, 12, 1, 0));
		testCase.add(new Vertex(68400.0, 5400.0, C, 12, 0, 0));
		testCase.add(new Vertex(68400.0, 7200.0, C, 12, 1, 0));
		testCase.add(new Vertex(68400.0, -1800.0, B, 12, 1, 0));
		testCase.add(new Vertex(23400.0, 50400.0, H, 7, 0, 0));
		
		testCase.add(new Vertex(25200.0, 50400.0, H, 7, 0, 1));
		testCase.add(new Vertex(-1800.0, 50400.0, H, 4, 0, 1));
		testCases.add(testCase);
		
		return testCases;
	}

}
