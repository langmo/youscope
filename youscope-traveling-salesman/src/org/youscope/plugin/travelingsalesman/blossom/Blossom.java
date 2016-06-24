package org.youscope.plugin.travelingsalesman.blossom;

import java.util.ArrayList;
import java.util.Collection;

class Blossom extends Node
{
	
	private final ArrayList<Node> blossomNodes = new ArrayList<Node>();
	private final ArrayList<Edge> blossomEdges  = new ArrayList<Edge>();
	
	Blossom(Collection<Node> blossomNodes, Collection<Edge> blossomEdges)
	{
		if(blossomNodes.size() < 3)
			throw new RuntimeException("Blossom must consist of at least three nodes.");
		if(blossomEdges.size() != blossomNodes.size())
		{
			StringBuilder builder = new StringBuilder("Blossom must be a circle, i.e. have as many nodes as edges. Edges: ");
			boolean first = true;
			for(Edge edge : blossomEdges)
			{
				if(first)
					first = false;
				else
					builder.append(", ");
				builder.append(edge.toString());
			}
			builder.append(". Nodes: ");
			first = true;
			for(Node node : blossomNodes)
			{
				if(first)
					first = false;
				else
					builder.append(", ");
				builder.append(node.toString());
			}
			builder.append('.');
			throw new RuntimeException(builder.toString());
		}
		
		this.blossomNodes.addAll(blossomNodes);
		this.blossomEdges.addAll(blossomEdges);
	}
	
	Collection<Node> getBlossomNodes()
	{
		return blossomNodes;
	}
	Collection<Edge> getBlossomEdges()
	{
		return blossomEdges;
	}
	
	@Override
	boolean contains(Node node)
	{
		if(equals(node))
			return true;
		for(Node n : blossomNodes)
		{
			if(n.contains(node))
				return true;
		}
		return false;
	}

	@Override
	boolean isBoundary(Edge edge) 
	{
		boolean isIn1 = contains(edge.getV1Node());
		boolean isIn2 = contains(edge.getV2Node());
		return (isIn1 && !isIn2) || (!isIn1 && isIn2);
	}
	
	@Override
	public String toString() 
	{
		StringBuilder builder = new StringBuilder("X"+"["+label.toString()+","+Double.toString(dual)+"](");
		boolean first = true;
		if(blossomNodes.isEmpty())
			builder.append("empty");
		else
		{
			for(Node node:blossomNodes)
			{
				if(first)
					first = false;
				else
					builder.append(", ");
				builder.append(node.toShortString());
			}
		}
		builder.append(')');
		return builder.toString();
	}
	@Override
	public String toShortString() 
	{
		StringBuilder builder = new StringBuilder("X(");
		boolean first = true;
		if(blossomNodes.isEmpty())
			builder.append("empty");
		else
		{
			for(Node node:blossomNodes)
			{
				if(first)
					first = false;
				else
					builder.append(", ");
				builder.append(node.toShortString());
			}
		}
		builder.append(')');
		return builder.toString();
	}

	@Override
	Edge getEdgeToParent() 
	{
		// There can be at max 1 edge to the exterior world...
		for(Node node : blossomNodes)
		{
			Edge edge = node.getEdgeToParent();
			if(edge!= null)
				return edge;
		}
		return null;
	}

	@Override
	Collection<Edge> getEdgesToChildren() 
	{
		ArrayList<Edge> edges = new ArrayList<>();
		for(Node node : blossomNodes)
		{
			edges.addAll(node.getEdgesToChildren());
		}
		return edges;
	}

	@Override
	void setEdgeToParent(Edge edge) 
	{
		if(edge == null)
		{
			for(Node node : blossomNodes)
			{
				node.setEdgeToParent(null);
			}
		}
		else
		{
			Vertex<?> v1 = edge.getOriginalV1();
			Vertex<?> v2 = edge.getOriginalV2();
			for(Node node : blossomNodes)
			{
				if(node.contains(v1) || node.contains(v2))
				{
					node.setEdgeToParent(edge);
					return;
				}
			}
			throw new RuntimeException("Edge "+edge.toString()+" does not link to node in blossom "+this.toString()+".");
		}
	}

	@Override
	void addEdgeToChild(Edge edge) {
		Vertex<?> v1 = edge.getOriginalV1();
		Vertex<?> v2 = edge.getOriginalV2();
		for(Node node : blossomNodes)
		{
			if(node.contains(v1) || node.contains(v2))
			{
				node.addEdgeToChild(edge);
				return;
			}
		}
		throw new RuntimeException("Edge does not link to node in blossom.");
		
	}

	@Override
	void clearEdgesToChilds() 
	{
		for(Node node : blossomNodes)
		{
			node.clearEdgesToChilds();
		}
	}

	@Override
	void removeEdgesToChilds(Collection<Edge> edges) 
	{
		for(Node node : blossomNodes)
		{
			node.removeEdgesToChilds(edges);
		}
	}

	@Override
	void removeEdgeToChild(Edge edge) {
		for(Node node : blossomNodes)
		{
			node.removeEdgeToChild(edge);
		}
	}
}
