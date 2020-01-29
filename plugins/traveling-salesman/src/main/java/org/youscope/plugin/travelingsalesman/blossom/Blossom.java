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

class Blossom extends PseudoNode
{
	
	private final ArrayList<PseudoNode> blossomNodes = new ArrayList<PseudoNode>();
	private final ArrayList<Edge> blossomEdges  = new ArrayList<Edge>();
	Blossom(Collection<PseudoNode> blossomNodes, Collection<Edge> blossomEdges) throws BlossomException
	{
		if(blossomNodes.size() < 3)
			throw new BlossomException("Blossom must consist of at least three nodes.");
		if(blossomEdges.size() != blossomNodes.size())
			throw new BlossomException("Blossom must be a circle, i.e. have as many nodes as edges.");
		this.blossomNodes.addAll(blossomNodes);
		this.blossomEdges.addAll(blossomEdges);
	}
	
	Collection<PseudoNode> getBlossomNodes()
	{
		return blossomNodes;
	}
	Collection<Edge> getBlossomEdges()
	{
		return blossomEdges;
	}
	
	@Override
	boolean contains(PseudoNode node) throws BlossomException, InterruptedException
	{
		if(equals(node))
			return true;
		for(PseudoNode n : blossomNodes)
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			if(n.contains(node))
				return true;
		}
		return false;
	}

	@Override
	boolean isBoundary(Edge edge) throws BlossomException, InterruptedException 
	{
		boolean isIn1 = contains(edge.getV1());
		boolean isIn2 = contains(edge.getV2());
		return (isIn1 && !isIn2) || (!isIn1 && isIn2);
	}
	
	@Override
	public String toString() 
	{
		StringBuilder builder;
		if(containedIn == null)
			builder = new StringBuilder("X["+getLabel().toString()+","+Double.toString(dual)+"](");
		else
			builder = new StringBuilder("X["+Double.toString(dual)+"](");
		boolean first = true;
		if(blossomNodes.isEmpty())
			builder.append("empty");
		else
		{
			for(PseudoNode node:blossomNodes)
			{
				if(first)
					first = false;
				else
					builder.append(", ");
				builder.append(node.toString());
			}
		}
		builder.append(')');
		return builder.toString();
	}

	@Override
	Edge getEdgeToParent() throws BlossomException, InterruptedException 
	{
		// There can be at max 1 edge to the exterior world...
		Edge parentEdge = null;
		for(PseudoNode node : blossomNodes)
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			Edge edge = node.getEdgeToParent();
			if(edge!= null)
			{
				if(parentEdge != null)
					throw new BlossomException("Blossom has at least two parent edges: "+parentEdge+", "+edge);
				parentEdge = edge;
			}
		}
		return parentEdge;
	}

	@Override
	Collection<Edge> getEdgesToChildren() throws BlossomException, InterruptedException
	{
		ArrayList<Edge> edges = new ArrayList<>();
		for(PseudoNode node : blossomNodes)
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			edges.addAll(node.getEdgesToChildren());
		}
		return edges;
	}

	@Override
	void setEdgeToParent(Edge edge) throws BlossomException, InterruptedException 
	{
		if(edge == null)
		{
			for(PseudoNode node : blossomNodes)
			{
				if(Thread.interrupted())
					throw new InterruptedException();
				node.setEdgeToParent(null);
			}
		}
		else
		{
			setEdgeToParent(null);
			Vertex v1 = edge.getV1();
			Vertex v2 = edge.getV2();
			for(PseudoNode node : blossomNodes)
			{
				if(Thread.interrupted())
					throw new InterruptedException();
				if(node.contains(v1) || node.contains(v2))
				{
					node.setEdgeToParent(edge);
					return;
				}
			}
			throw new BlossomException("Edge "+edge.toString()+" does not link to node in blossom "+this.toString()+".");
		}
	}

	@Override
	void addEdgeToChild(Edge edge) throws BlossomException, InterruptedException {
		Vertex v1 = edge.getV1();
		Vertex v2 = edge.getV2();
		for(PseudoNode node : blossomNodes)
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			if(node.contains(v1) || node.contains(v2))
			{
				node.addEdgeToChild(edge);
				return;
			}
		}
		throw new BlossomException("Edge does not link to node in blossom.");
		
	}

	@Override
	void clearEdgesToChilds() throws BlossomException, InterruptedException
	{
		for(PseudoNode node : blossomNodes)
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			node.clearEdgesToChilds();
		}
	}

	@Override
	void removeEdgesToChilds(Collection<Edge> edges) throws BlossomException, InterruptedException
	{
		for(PseudoNode node : blossomNodes)
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			node.removeEdgesToChilds(edges);
		}
	}

	@Override
	void removeEdgeToChild(Edge edge) throws BlossomException, InterruptedException{
		for(PseudoNode node : blossomNodes)
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			node.removeEdgeToChild(edge);
		}
	}
}
