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

import java.util.Collection;

/**
 * Abstract superclass of vertices (=elementary nodes) and blossoms (=nodes containing other nodes)
 * @author mlang
 *
 */
abstract class PseudoNode 
{
	private NodeLabel label = NodeLabel.PLUS;
	
	Blossom containedIn = null;
	
	double dual = 0;
	abstract boolean isBoundary(Edge edge) throws BlossomException, InterruptedException;
	abstract Edge getEdgeToParent() throws BlossomException, InterruptedException;
	abstract void setEdgeToParent(Edge edge) throws BlossomException, InterruptedException;
	abstract Collection<Edge> getEdgesToChildren() throws BlossomException, InterruptedException;
	abstract void addEdgeToChild(Edge edge) throws BlossomException, InterruptedException;
	abstract void clearEdgesToChilds() throws BlossomException, InterruptedException;
	abstract void removeEdgesToChilds(Collection<Edge> edges) throws BlossomException, InterruptedException;
	abstract void removeEdgeToChild(Edge edge) throws BlossomException, InterruptedException;
	NodeLabel getLabel()
	{
		return label;
	}
	void setLabel(NodeLabel label)
	{
		this.label = label;
	}
	PseudoNode getOuterNode() throws InterruptedException
	{
		if(Thread.interrupted())
			throw new InterruptedException();
		if(containedIn == null)
			return this;
		return containedIn.getOuterNode();
	}
	
	abstract boolean contains(PseudoNode node) throws BlossomException, InterruptedException;
	
	boolean isInSubtree(PseudoNode node) throws BlossomException, InterruptedException
	{
		if(containedIn != null)
			throw new BlossomException("Call to isInSubtree on node in blossom.");
		if(contains(node))
			return true;
		for(Edge edge : getEdgesToChildren())
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			PseudoNode other = edge.to(this);
			if(other.isInSubtree(node))
				return true;
		}
		return false;
	}
}
