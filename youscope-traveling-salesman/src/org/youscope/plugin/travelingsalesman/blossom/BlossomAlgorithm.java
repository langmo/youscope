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
package org.youscope.plugin.travelingsalesman.blossom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Realizes the blossom minimal perfect matching algorithm.
 * The algorithm is a direct implementation of Edmonds's blossom algorithm, as described in chapter 2 of
 * Kolmogorov, Vladimir. "Blossom V: a new implementation of a minimum cost perfect matching algorithm." Mathematical Programming Computation 1.1 (2009): 43-67.
 * Note: Even though the reference is concerning an advanced implementation, we use the implementation as described in chapter 2. Specifically, we use the single tree
 * approach, as descrobed in Chapter 2.3.
 * @author Moritz Lang
 *
 */
public class BlossomAlgorithm
{
	private final ArrayList<Edge> allEdges = new ArrayList<Edge>();
	private final ArrayList<PseudoNode> exteriorNodes  = new ArrayList<>();
	private final ArrayList<PseudoNode> allNodes  = new ArrayList<>();
	private PseudoNode currentRoot = null;
	private final ArrayList<BlossomListener> listeners = new ArrayList<>();
	private static final double DELTA = 1e-10;
	/**
	 * Constructor.
	 * @param vertices Vertices which should be matched.
	 * @param edges Edges which should be matched.
	 */
	public BlossomAlgorithm(List<? extends Vertex> vertices, List<? extends Edge> edges)
	{
		allEdges.addAll(edges);
		// Sort: we are always picking early edges, first, thus, make early edges more likely to be edges with zero slack.
		Collections.sort(allEdges);
		exteriorNodes.addAll(vertices);
		allNodes.addAll(vertices);
	}
	
	/**
	 * Adds a listener which gets informed about the individual steps of the Blossom algorithm.
	 * @param listener Listener to add.
	 */
	public void addBlossomListener(BlossomListener listener)
	{
		synchronized (listeners) 
		{
			listeners.add(listener);
		}
	}
	
	/**
	 * Removes a previously added listener.
	 * @param listener Listener to remove.
	 */
	public void removeBlossomListener(BlossomListener listener)
	{
		synchronized (listeners) 
		{
			listeners.remove(listener);
		}
	}
	
	private void informPrimal(PrimalUpdateType type)
	{
		synchronized(listeners)
		{
			for(BlossomListener listener : listeners)
			{
				listener.primalUpdate(type);
			}
		}
	}
	
	private void informDual(double dualChange)
	{
		synchronized(listeners)
		{
			for(BlossomListener listener : listeners)
			{
				listener.dualUpdate(dualChange);
			}
		}
	}
	
	private boolean isTight(Edge edge) throws BlossomException, InterruptedException
	{
		return Math.abs(getSlack(edge)) < DELTA;
	}
	private double getSlack(Edge edge) throws BlossomException, InterruptedException
	{
		double slack = edge.getWeight();
		for(PseudoNode node:allNodes)
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			if(node.isBoundary(edge))
				slack-=node.dual;
		}
		return slack;
	}
	/**
	 * Makes a dual update, that is, changes the dual variables of the nodes in the current tree. Returns false if dual variables could not be changed (i.e. when all inequality conditions were already tight).
	 * @return True if an action could be done, false otherwise.
	 * @throws BlossomException 
	 * @throws InterruptedException 
	 */
	private double dualUpdate() throws BlossomException, InterruptedException
	{
		selectNewRoot();
		if(currentRoot == null)
			return 0;
		// dual change
		double delta = Double.MAX_VALUE;
		// blossoms can only exist in current Tree. Thus, delta must be smaller than all blossom ys with label "-"
		for(PseudoNode node : exteriorNodes)
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			if(!(node instanceof Blossom) || node.getLabel() != NodeLabel.MINUS)
				continue;
			if(delta > node.dual)
			{
				delta = node.dual;
			}
		}
		
		for(Edge edge:allEdges)
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			PseudoNode v1 = edge.getV1VirtualNode();
			PseudoNode v2 = edge.getV2VirtualNode();
			
			// Can happen for blossums
			if(v1.equals(v2))
				continue;
			
			boolean v1Contained = currentRoot.isInSubtree(v1);
			boolean v2Contained = currentRoot.isInSubtree(v2);
			if(!v1Contained && !v2Contained)
				continue;
			double slack = getSlack(edge);
			if(slack<-DELTA)
			{
				throw new BlossomException("Edge "+edge.toString()+" has slack "+Double.toString(slack));
			}
			// Make v1 always contained
			if(!v1Contained)
			{
				boolean tempC = v1Contained;
				v1Contained = v2Contained;
				v2Contained = tempC;
				PseudoNode tempV = v1;
				v1 = v2;
				v2 = tempV;
			}
			if(v1.getLabel() == NodeLabel.PLUS && v2.getLabel() == NodeLabel.EMPTY)
			{
				if(delta > slack)
				{
					delta = slack;
				}
				continue;
			}
			else if(v1.getLabel() == NodeLabel.PLUS && v2.getLabel() == NodeLabel.PLUS && !v2Contained)
			{
				if(delta > slack)
				{
					delta = slack;
				}
				continue;
			}
			else if(v1.getLabel() == NodeLabel.PLUS && v2.getLabel() == NodeLabel.PLUS && v2Contained)
			{
				if(delta > slack/2)
				{
					delta = slack/2;
				}
				continue;
			}
		}
		if(delta < DELTA) 
			return 0;
		updateDualSubTree(currentRoot, delta);
		return delta;
		
	}
	
	/**
	 * Returns a description of the current state of the algorithm.
	 * @return Description of state.
	 * @throws BlossomException 
	 * @throws InterruptedException 
	 */
	public String printState() throws BlossomException, InterruptedException
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Trees:\n");
		for(PseudoNode node : exteriorNodes)
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			if(node.getEdgeToParent()==null)
			{
				builder.append(printSubTree(node, 0));
				builder.append("\n");
			}
		}
		builder.append("Free Nodes:\n");
		for(Edge edge: getFreeEdges())
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			builder.append(edge.getV1VirtualNode().toString()+" <=> "+edge.getV2VirtualNode().toString()+"\n");
		}
		return builder.toString(); 
	}
	
	private Collection<Edge> getFreeEdges() throws InterruptedException
	{
		ArrayList<Edge> freeEdges = new ArrayList<>();
		for(Edge edge: allEdges)
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			PseudoNode v1 = edge.getV1VirtualNode();
			PseudoNode v2 = edge.getV2VirtualNode();
			// self loops can happen for edges inside bossoms...
			if(v1.equals(v2)) 
				continue;
			
			if(edge.isMatch() && v1.getLabel() == NodeLabel.EMPTY&& v2.getLabel() == NodeLabel.EMPTY)
			{
				freeEdges.add(edge);
			}
		}
		return freeEdges;
	}
	
	private String printSubTree(PseudoNode root, int level) throws BlossomException, InterruptedException
	{
		StringBuilder builder = new StringBuilder();
		if(level > 0)
		{
			for(int i=1;i<level; i++)
			{
				builder.append("|   ");
			}
			builder.append("|- ");
		}
		builder.append(root.toString()+"\n");
		for(Edge edge:root.getEdgesToChildren())
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			builder.append(printSubTree(edge.to(root), level+1));
		}
		return builder.toString();
	}
	
	private void updateDualSubTree(PseudoNode root, double delta) throws BlossomException, InterruptedException
	{
		if(root.getLabel() == NodeLabel.PLUS)
			root.dual += delta;
		else if(root.getLabel() == NodeLabel.MINUS)
			root.dual -= delta;
		else
			throw new BlossomException("Node in tree with invalid label");
		for(Edge edge:root.getEdgesToChildren())
		{ 
			if(Thread.interrupted())
				throw new InterruptedException();
			PseudoNode child = edge.to(root);
			if(root.equals(child))
				continue;
			updateDualSubTree(edge.to(root), delta);
		}
	}
	
	
	private void selectNewRoot() throws BlossomException, InterruptedException
	{
		if(currentRoot == null)
		{
			// we only have trees consisting of exactly one node. Pick one.
			for(PseudoNode node : exteriorNodes)
			{
				if(Thread.interrupted())
					throw new InterruptedException();
				if(node.getLabel() == NodeLabel.PLUS)
				{
					if(node.containedIn != null)
						throw new BlossomException("Blossoms can only be in current tree, or have label empty.");
					currentRoot = node;
					break;
				}
			}
		}
	}
	/**
	 * Makes a primal update, that is, grows or augments the current tree. Returns false if primal update was not possible.
	 * @return The type of update which was made, {@link PrimalUpdateType#FINISHED} if no trees exist anymore, or {@link PrimalUpdateType#NONE} if no update could be performed, but the algorithm is yet not finished. 
	 * @throws BlossomException 
	 * @throws InterruptedException 
	 */
	private PrimalUpdateType primalUpdate() throws BlossomException, InterruptedException
	{
		selectNewRoot();
		if(currentRoot == null)
		{
			solveBlossoms();
			return PrimalUpdateType.FINISHED; 
		}
		for(Edge edge : allEdges)
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			
			// Matches connect free nodes...
			if(edge.isMatch() || !isTight(edge))
				continue;
			
			PseudoNode v1 = edge.getV1VirtualNode();
			PseudoNode v2 = edge.getV2VirtualNode();
			
			// Self loops can happen if there is an edge inside a blossom...
			if(v1==v2)
				continue;
			
			// Since we only have one non-trivial tree at the same time, to check if two nodes belong to the same tree, we 
			// only have to check if they are both below currentRoot.
			boolean v1InCurrentTree = currentRoot.isInSubtree(v1);
			boolean v2InCurrentTree = currentRoot.isInSubtree(v2);
			
			// Only operate on current tree!
			if(!v1InCurrentTree && !v2InCurrentTree)
				continue;
			
			NodeLabel label1 = v1.getLabel();
			NodeLabel label2 = v2.getLabel();
			
			if(v1InCurrentTree != v2InCurrentTree && label1 == NodeLabel.PLUS && label2 == NodeLabel.PLUS )
			{
				augment(edge);
				return PrimalUpdateType.AUGMENT;
			}
			else if((v1InCurrentTree && !v2InCurrentTree && label1 == NodeLabel.PLUS && label2 == NodeLabel.EMPTY)
				||  (v2InCurrentTree && !v1InCurrentTree && label2 == NodeLabel.PLUS && label1 == NodeLabel.EMPTY))
			{
				grow(edge);
				return PrimalUpdateType.GROW;
			}
			else if(v1InCurrentTree && v2InCurrentTree && label1 == NodeLabel.PLUS && label2 == NodeLabel.PLUS)
			{
				shrink(edge); 
				return PrimalUpdateType.SHRINK;
			}
		}
		
		for(PseudoNode node : exteriorNodes)
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			if(!(node instanceof Blossom))
				continue;
			Blossom blossom = (Blossom) node;
			if(blossom.getLabel() != NodeLabel.MINUS)
				continue;
			if(Math.abs(blossom.dual) > 100*Double.MIN_VALUE)
				continue;
			expand(blossom);
			return PrimalUpdateType.EXPAND;
		}
		
		return PrimalUpdateType.NONE;
	}
	private void expand(Blossom blossom) throws BlossomException, InterruptedException
	{
		// when we call expand, we know that the blossom has label "-", implying that it has exactly one child.
		Collection<Edge> childEdges = blossom.getEdgesToChildren();
		if(childEdges.size() != 1)
			throw new BlossomException("Blossom which should be extended must have label - and, thus, exactly one child.");
		Edge childEdge = childEdges.iterator().next();
		Edge parentEdge = blossom.getEdgeToParent();
		System.out.println("Parent Edge: "+parentEdge);
		System.out.println("Child Edge: "+childEdge);
		ArrayList<Edge> blossomEdges = new ArrayList<>(blossom.getBlossomEdges());
		ArrayList<PseudoNode> blossomNodes = new ArrayList<>(blossom.getBlossomNodes());
		// destroy the blossom
		allNodes.remove(blossom);
		exteriorNodes.remove(blossom);
		for(PseudoNode node:blossomNodes)
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			node.containedIn = null;
		}
		// add all blossom nodes to exterior nodes
		exteriorNodes.addAll(blossomNodes); 
		
		// There are two ways from the parent to the child. we need the even one.
		PseudoNode start = parentEdge.getV1VirtualNode();
		if(!blossom.contains(start))
			start = parentEdge.getV2VirtualNode();
		PseudoNode goal = childEdge.getV1VirtualNode();
		if(!blossom.contains(goal))
			goal = childEdge.getV2VirtualNode();
		
		ArrayList<Edge> walk1 = new ArrayList<Edge>();
		ArrayList<Edge> walk2 = new ArrayList<Edge>();
		PseudoNode last1 = start;
		PseudoNode last2 = start;
		while(!blossomEdges.isEmpty())
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			Iterator<Edge> iterator = blossomEdges.iterator();
			while(iterator.hasNext())
			{
				if(Thread.interrupted())
					throw new InterruptedException();
				Edge edge = iterator.next();
				PseudoNode to;
				if(!(last1.equals(goal)))
				{
					to = edge.to(last1);
					if(to != null)
					{
						walk1.add(edge);
						last1 = to;
						iterator.remove();
						continue;
					}
				}
				if(!(last2.equals(goal))||start.equals(goal))
				{
					to = edge.to(last2);
					if(to != null)
					{
						walk2.add(edge);
						last2 = to;
						iterator.remove();
						continue;
					}
				}
			}
		}
		// make walk1 the even walk.
		if((walk1.size() % 2) + (walk2.size() % 2)!=1)
			throw new BlossomException("One walk must be even, one odd.");
		if(walk1.size() % 2 != 0)
		{
			ArrayList<Edge> temp = walk1;
			walk1 = walk2;
			walk2 = temp;
		}
		
		// Print out walks:
		last1 = start;
		System.out.print("Walk 1 [l="+Integer.toString(walk1.size())+"]: "+last1.toString());
		for(int i=0; i<walk1.size(); i++)
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			Edge edge = walk1.get(i);
			last1 = edge.to(last1);
			System.out.print("->"+last1.toString());
		}
		System.out.println();
		last2 = start;
		System.out.print("Walk 2 [l="+Integer.toString(walk2.size())+"]: "+last2.toString());
		for(int i=0; i<walk2.size(); i++)
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			Edge edge = walk2.get(i);
			last2 = edge.to(last2);
			System.out.print("->"+last2.toString());
		}
		System.out.println();
		
		// reconstruct tree with walk1
		last1 = start;
		last1.setLabel(NodeLabel.MINUS);
		for(int i=0; i<walk1.size(); i++)
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			Edge edge = walk1.get(i);
			if(i%2==0)
			{
				edge.setMatch(true);
			}
			else
				edge.setMatch(false);
			last1.addEdgeToChild(edge);
			last1 = edge.to(last1);
			last1.setEdgeToParent(edge);
			last1.setLabel((i%2==0) ? NodeLabel.PLUS : NodeLabel.MINUS);
		}
		
		// make free nodes with walk2
		for(int i=0; i<walk2.size(); i++)
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			Edge edge = walk2.get(i);
			if(i%2==0)
				edge.setMatch(false);
			else
			{
				edge.setMatch(true);
				PseudoNode v1 = edge.getV1VirtualNode();
				PseudoNode v2 = edge.getV2VirtualNode();
				v1.setEdgeToParent(edge);
				v2.setEdgeToParent(edge);
				v1.setLabel(NodeLabel.EMPTY);
				v2.setLabel(NodeLabel.EMPTY);
			}
		}
			
	}
	
	/**
	 * Very last method of the algorithm: if all remaining nodes are free nodes, we can solve the blossoms, that is, determine the match state of 
	 * the blossom forming edges.
	 * @throws BlossomException 
	 * @throws InterruptedException 
	 */
	private void solveBlossoms() throws BlossomException, InterruptedException
	{
		// get all blossoms
		ArrayList<Blossom> blossoms = new ArrayList<>();
		for(PseudoNode node : allNodes)
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			if(node instanceof Blossom)
				blossoms.add((Blossom)node);
		}
		while(blossoms.size() != 0)
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			// find exterior node which is blossom
			Blossom blossom = null;
			for(Blossom blossomE : blossoms)
			{
				if(Thread.interrupted())
					throw new InterruptedException();
				if(blossomE.containedIn == null)
				{
					blossom = blossomE;
					break;
				}
			}
			blossoms.remove(blossom);
			
			ArrayList<Edge> blossomEdges = new ArrayList<>(blossom.getBlossomEdges());
			ArrayList<PseudoNode> blossomNodes = new ArrayList<>(blossom.getBlossomNodes());
			
			Edge parentEdge = blossom.getEdgeToParent();
			if(parentEdge == null)
			{
				throw new BlossomException("Blossom "+blossom.toString()+" is not free.");
			}
			
			// destroy the blossom
			allNodes.remove(blossom);
			exteriorNodes.remove(blossom);
			for(PseudoNode node:blossomNodes)
			{
				if(Thread.interrupted())
					throw new InterruptedException();
				node.containedIn = null;
			}
			// add all blossom nodes to exterior nodes
			exteriorNodes.addAll(blossomNodes);
			
			PseudoNode start = parentEdge.getV1VirtualNode();
			if(!blossom.contains(start))
				start = parentEdge.getV2VirtualNode();
			
			// arrange edges in circle
			ArrayList<Edge> circle = new ArrayList<>(blossomEdges.size());
			PseudoNode last = start;
			while(!blossomEdges.isEmpty())
			{
				if(Thread.interrupted())
					throw new InterruptedException();
				Iterator<Edge> iterator = blossomEdges.iterator();
				while(iterator.hasNext())
				{
					if(Thread.interrupted())
						throw new InterruptedException();
					Edge edge = iterator.next();
					PseudoNode to = edge.to(last);
					if(to != null)
					{
						circle.add(edge);
						last = to;
						iterator.remove();
						continue;
					}
				}
			}
			
			// create free nodes out of circle
			for(int i=0; i<circle.size(); i++)
			{
				if(Thread.interrupted())
					throw new InterruptedException();
				if(i%2==0)
				{
					circle.get(i).setMatch(false);
				}
				else
				{
					Edge edge = circle.get(i);
					edge.setMatch(true);
					edge.getV1VirtualNode().setEdgeToParent(edge);
					edge.getV2VirtualNode().setEdgeToParent(edge);
					edge.getV1VirtualNode().setLabel(NodeLabel.EMPTY);
					edge.getV2VirtualNode().setLabel(NodeLabel.EMPTY);
				}
			}
			start.setLabel(NodeLabel.EMPTY);
		}
	}
	
	/**
	 * Runs the Blossom algorithm, and returns the minimum perfect matching.
	 * @return Minimum perfect matching.
	 * @throws BlossomException thrown if algorithm fails.
	 * @throws InterruptedException 
	 */
	public Collection<Edge> getMatches() throws BlossomException, InterruptedException
	{
		boolean changed;
		do
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			changed = false;
			PrimalUpdateType primalType = primalUpdate();
			informPrimal(primalType);
			if(primalType == PrimalUpdateType.FINISHED)
				return getFreeEdges();
			changed |= (primalType != PrimalUpdateType.NONE && primalType != PrimalUpdateType.FINISHED);
			double delta = dualUpdate();
			informDual(delta);
			changed |= delta > 0;
		}
		while(changed);
		throw new BlossomException("Algorithm did not converge.");
	}
	
	
	private void shrink(Edge edge) throws BlossomException, InterruptedException
	{
		PseudoNode v1 = edge.getV1VirtualNode();
		PseudoNode v2 = edge.getV2VirtualNode();
		
		// get all nodes and edges in blossom
		ArrayList<PseudoNode> nodes = new ArrayList<>();
		ArrayList<Edge> edges = new ArrayList<>();
		edges.add(edge);
		while(true)
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			nodes.add(v1);
			if(v1.isInSubtree(v2))
			{
				break;
			}
			Edge parentEdge =v1.getEdgeToParent(); 
			edges.add(parentEdge);
			v1.setEdgeToParent(null);
			v1 = parentEdge.to(v1);
			v1.removeEdgeToChild(parentEdge);
			
		}
		// v1 is now the root of the blossom
		while(!v2.equals(v1))
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			nodes.add(v2);
			Edge parentEdge =v2.getEdgeToParent(); 
			edges.add(parentEdge);
			v2.setEdgeToParent(null);
			v2 = parentEdge.to(v2);
			v2.removeEdgeToChild(parentEdge);
		}
		
		Blossom blossom = new Blossom(nodes, edges);
		exteriorNodes.add(blossom);
		allNodes.add(blossom);
		exteriorNodes.removeAll(nodes);
		for(PseudoNode node:nodes)
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			node.containedIn=blossom;
		}
		
		if(currentRoot == v1 || currentRoot == v2)
			currentRoot = blossom;
	}
	
	private void grow(Edge edge) throws BlossomException, InterruptedException
	{
		PseudoNode v1 = edge.getV1VirtualNode();
		PseudoNode v2;
		if(currentRoot.isInSubtree(v1))
			v2=edge.getV2VirtualNode();
		else
		{
			v2=v1;
			v1 = edge.getV2VirtualNode();
		}
		Edge partnerEdge = v2.getEdgeToParent();
		PseudoNode partner = partnerEdge.to(v2);
		
		v2.setEdgeToParent(edge);
		v1.addEdgeToChild(edge);
		v2.addEdgeToChild(partnerEdge);
		v2.setLabel(NodeLabel.MINUS);
		partner.setLabel(NodeLabel.PLUS);
	}
	
	/**
	 * Augment the current tree with the edge...
	 * @param edge
	 * @throws BlossomException 
	 * @throws InterruptedException 
	 */
	private void augment(Edge edge) throws BlossomException, InterruptedException
	{
		PseudoNode v1Node = edge.getV1VirtualNode();
		PseudoNode v2Node = edge.getV2VirtualNode();
		
		reverseEdgesToRoot(v1Node);
		reverseEdgesToRoot(v2Node);
		destroySubTree(currentRoot);
		currentRoot = null; 
		edge.setMatch(true);
		v1Node.setEdgeToParent(edge);
		v2Node.setEdgeToParent(edge);
		v1Node.setLabel(NodeLabel.EMPTY);
		v2Node.setLabel(NodeLabel.EMPTY);
	}
	
	private void destroySubTree(PseudoNode root) throws BlossomException, InterruptedException
	{
		for(Edge edge : root.getEdgesToChildren())
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			PseudoNode child = edge.to(root).getOuterNode();
			if(edge.isMatch())
			{
				root.setEdgeToParent(edge);
			}
			destroySubTree(child);
		}
		root.setLabel(NodeLabel.EMPTY);
		root.clearEdgesToChilds();
	}
	
	private void reverseEdgesToRoot(PseudoNode node) throws BlossomException, InterruptedException
	{
		do
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			Edge edge = node.getEdgeToParent();
			if(edge == null)
				break;
			edge.setMatch(!edge.isMatch()); 
			node = edge.to(node).getOuterNode();
		} while(node != null);
	}
}
