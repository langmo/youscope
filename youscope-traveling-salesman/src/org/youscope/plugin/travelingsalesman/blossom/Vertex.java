package org.youscope.plugin.travelingsalesman.blossom;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A vertex (elementary node) in the graph.
 * Each vertex can contain some content which might be used to identify or label the vertex, or to store anything one wants.
 * @author Moritz Lang
 *
 * @param <T> Type of content stored in this node.
 */
public class Vertex<T extends Object> extends Node
{
	private final T content;
	private final ArrayList<Edge> edgesToChilds = new ArrayList<Edge>();
	private Edge edgeToParent = null;
	
	/**
	 * Contstructor.
	 * @param content Content stored in this vertex.
	 */
	public Vertex(T content)
	{
		this.content = content;
	}
	@Override
	boolean isBoundary(Edge edge) 
	{
		if(edge.getV1Node().contains(this) || edge.getV2Node().contains(this))
			return true;
		return false;
	}
	
	@Override
	boolean contains(Node node) 
	{
		return equals(node);
	}
	
	/**
	 * Returns the content stored in this vertex.
	 * @return Content of vertex.
	 */
	public T getContent()
	{
		return content;
	}
	
	@Override
	public String toString()
	{
		return content.toString()+"["+label.toString()+","+Double.toString(dual)+"]";
	}
	@Override
	public String toShortString()
	{
		return content.toString();
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
	void setEdgeToParent(Edge edge) {
		if(edge!=null &&edge.getOriginalV1() != this && edge.getOriginalV2() != this)
			throw new RuntimeException("Edge does not link to this vertex.");
		edgeToParent = edge;
	}
	@Override
	void addEdgeToChild(Edge edge) 
	{
		if(edge.getOriginalV1() != this && edge.getOriginalV2() != this)
			throw new RuntimeException("Edge does not link to this vertex.");
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
