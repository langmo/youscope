package org.youscope.plugin.travelingsalesman.blossom;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Abstract superclass of vertices (=elementary nodes) and blossoms (=nodes containing other nodes)
 * @author mlang
 *
 */
abstract class Node 
{
	public enum Label
	{
		PLUS,
		MINUS,
		EMPTY;
		@Override
		public String toString()
		{
			if(this==PLUS)
				return "+";
			else if(this==MINUS)
				return "-";
			else
				return "o";
		}
	}
	Label label = Label.PLUS;
	
	Blossom containedIn = null;
	
	double dual = 0;
	abstract boolean isBoundary(Edge edge);
	abstract Edge getEdgeToParent();
	abstract void setEdgeToParent(Edge edge);
	abstract Collection<Edge> getEdgesToChildren();
	abstract void addEdgeToChild(Edge edge);
	abstract void clearEdgesToChilds();
	abstract void removeEdgesToChilds(Collection<Edge> edges);
	abstract void removeEdgeToChild(Edge edge);
	Label getLabel()
	{
		return label;
	}
	Node getOuterNode()
	{
		if(containedIn == null)
			return this;
		return containedIn.getOuterNode();
	}
	
	abstract boolean contains(Node node);
	abstract String toShortString();
	
	boolean isInSubtree(Node node) 
	{
		if(containedIn != null)
			throw new RuntimeException("Call to isInSubtree on node in blossom.");
		if(contains(node))
			return true;
		for(Edge edge : getEdgesToChildren())
		{
			Node other = edge.to(this);
			if(other.isInSubtree(node))
				return true;
		}
		return false;
	}
}
