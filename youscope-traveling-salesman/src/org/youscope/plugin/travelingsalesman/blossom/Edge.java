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

/**
 * Realizes a weighted, undirected edge between two vertices.
 * @author Moritz Lang
 *
 */
public class Edge implements Comparable<Edge>
{
	private final Vertex v1;
	private final Vertex v2;
	private final double weight;
	// True if edge belongs to matching.
	private boolean match = false;
	/**
	 * Constructor.
	 * @param v1 First vertex connected by this edge.
	 * @param v2 Second vertex.
	 * @param weight Weight/cost of this edge.
	 */
	public Edge(Vertex v1, Vertex v2, double weight)
	{
		this.v1 = v1;
		this.v2 =v2;
		this.weight = weight;
	}
	/**
	 * Returns true if this edge belongs to the perfect matching.
	 * @return True if edge belongs to the matching.
	 */
	boolean isMatch()
	{
		return match;
	}
	void setMatch(boolean match)
	{
		this.match = match;
	}

	/**
	 * Returns the weight of this vertex.
	 * @return Non-negative weight of this edge.
	 */
	public double getWeight()
	{
		return weight;
	}
	/**
	 * Returns the first node this edge is connecting. If this node belongs to a blossom, returns the blossom.
	 * @return First node this edge is connecting, or containing blossom.
	 * @throws InterruptedException 
	 */
	PseudoNode getV1VirtualNode() throws InterruptedException
	{
		return v1.getOuterNode();
	}
	/**
	 * Gets the first vertex this edge is connecting.
	 * @return first vertex of edge.
	 */
	public Vertex getV1()
	{
		return v1;
	}
	/**
	 * Gets the second vertex this edge is connecting.
	 * @return second vertex of edge.
	 */
	public Vertex getV2()
	{
		return v2;
	}
	/**
	 * Returns the second node this edge is connecting. If this node belongs to a blossom, returns the blossom.
	 * @return Second node this edge is connecting, or containing blossom.
	 * @throws InterruptedException 
	 */
	PseudoNode getV2VirtualNode() throws InterruptedException
	{
		return v2.getOuterNode();
	}
	
	
	/**
	 * Returns the other (external) node this edge is connecting, that is, v1 if from is (or contains) v2, and otherwise v1. 
	 * @param from The node connected by the edge which should not be returned.
	 * @return
	 * @throws BlossomException 
	 * @throws InterruptedException 
	 */
	PseudoNode to(PseudoNode from) throws BlossomException, InterruptedException
	{
		if(from.contains(v1))
			return v2.getOuterNode();
		else if(from.contains(v2))
			return v1.getOuterNode();
		else
			return null;
	}
	/**
	 * Returns true if and only if the edge neither v1 nor v2 belong to a blossom.
	 * @return True if edge connects external vertices.
	 */
	boolean isConnectNonBlossoms()
	{
		return v1.containedIn == null && v2.containedIn == null;
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(getV1().toString());
		if(match)
			builder.append("<=");
		else
			builder.append("<-");
		builder.append(Double.toString(weight));
		if(match)
			builder.append("=>");
		else
			builder.append("->");
		builder.append(getV2().toString());
		return builder.toString();
	}
	@Override
	public int compareTo(Edge other) {
		if(getWeight() < other.getWeight())
			return -1;
		else if(getWeight() > other.getWeight())
			return 1;
		return 0;
	}
}
