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

/**
 * A vertex (elementary node) in the graph.
 * Each vertex can contain some content which might be used to identify or label the vertex, or to store anything one wants.
 * @author Moritz Lang
 *
 */
public class Vertex extends PseudoNode
{
	private final Object content;
	private final ArrayList<Edge> edgesToChilds = new ArrayList<Edge>();
	private Edge edgeToParent = null;
	
	/**
	 * Contstructor.
	 * @param content Content stored in this vertex.
	 */
	public Vertex(Object content)
	{
		this.content = content;
	}
	@Override
	boolean isBoundary(Edge edge) 
	{
		if(edge.getV1().equals(this) || edge.getV2().equals(this))
			return true;
		return false;
	}
	
	@Override
	boolean contains(PseudoNode node) 
	{
		return equals(node);
	}
	
	/**
	 * Returns the content stored in this vertex.
	 * @return Content of vertex.
	 */
	public Object getContent()
	{
		return content;
	}
	
	@Override
	public String toString()
	{
		String contentString = content == null ? "UNDEFINED" : content.toString();
		if(containedIn == null)
			return contentString+"["+getLabel().toString()+","+Double.toString(dual)+"]";
		return contentString+"["+Double.toString(dual)+"]";
	}
	@Override
	Edge getEdgeToParent() {
		return edgeToParent;
	}
	@Override
	Collection<Edge> getEdgesToChildren() {
		return edgesToChilds;
	}
	@Override
	void setEdgeToParent(Edge edge) throws BlossomException {
		if(edge!=null &&edge.getV1() != this && edge.getV2() != this)
			throw new BlossomException("Edge does not link to this vertex.");
		edgeToParent = edge;
	}
	@Override
	void addEdgeToChild(Edge edge) throws BlossomException 
	{
		if(edge.getV1() != this && edge.getV2() != this)
			throw new BlossomException("Edge does not link to this vertex.");
		edgesToChilds.add(edge);
	}
	@Override
	void clearEdgesToChilds() {
		edgesToChilds.clear();
	}
	@Override
	void removeEdgesToChilds(Collection<Edge> edges) {
		edgesToChilds.removeAll(edges);
	}
	@Override
	void removeEdgeToChild(Edge edge) {
		edgesToChilds.remove(edge);
	}
}
