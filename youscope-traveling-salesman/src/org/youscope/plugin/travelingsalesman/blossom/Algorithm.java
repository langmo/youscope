package org.youscope.plugin.travelingsalesman.blossom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Realizes the blossom minimal perfect matching algorithm.
 * @author Moritz Lang
 *
 */
public class Algorithm
{
	private final ArrayList<Edge> allEdges = new ArrayList<Edge>();
	private final ArrayList<Node> exteriorNodes  = new ArrayList<>();
	private final ArrayList<Node> allNodes  = new ArrayList<>();
	private Node currentRoot = null;
	
	/**
	 * Constructor.
	 * @param vertices Vertices which should be matched.
	 * @param edges Edges which should be matched.
	 */
	public Algorithm(List<? extends Vertex<?>> vertices, List<? extends Edge> edges)
	{
		allEdges.addAll(edges);
		exteriorNodes.addAll(vertices);
		allNodes.addAll(vertices);
	}
	
	boolean isTight(Edge edge)
	{
		return Math.abs(getSlack(edge)) < 100*Double.MIN_VALUE;
	}
	double getSlack(Edge edge)
	{
		double slack = edge.getWeight();
		for(Node node:allNodes)
		{
			if(node.isBoundary(edge))
				slack-=node.dual;
		}
		return slack;
	}
	/**
	 * Makes a dual update, that is, changes the dual variables of the nodes in the current tree. Returns false if dual variables could not be changed (i.e. when all inequality conditions were already tight).
	 * @return True if an action could be done, false otherwise.
	 */
	public double dualUpdate()
	{
		selectNewRoot();
		if(currentRoot == null)
			return 0;
		// dual change
		double delta = Double.MAX_VALUE;
		// blossoms can only exist in current Tree. Thus, delta must be smaller than all blossom ys with label "-"
		for(Node node : exteriorNodes)
		{
			if(!(node instanceof Blossom) || node.label != Node.Label.MINUS)
				continue;
			if(delta > node.dual)
				delta = node.dual;
		}
		
		for(Edge edge:allEdges)
		{
			Node v1 = edge.getV1Node();
			Node v2 = edge.getV2Node();
			
			// Can happen for blossums
			if(v1.equals(v2))
				continue;
			
			boolean v1Contained = currentRoot.isInSubtree(v1);
			boolean v2Contained = currentRoot.isInSubtree(v2);
			if(!v1Contained && !v2Contained)
				continue;
			double slack = getSlack(edge);
			// Make v1 always contained
			if(!v1Contained)
			{
				boolean tempC = v1Contained;
				v1Contained = v2Contained;
				v2Contained = tempC;
				Node tempV = v1;
				v1 = v2;
				v2 = tempV;
			}
			if(v1.label == Node.Label.PLUS && v2.label == Node.Label.EMPTY)
			{
				if(delta > slack)
					delta = slack;
				continue;
			}
			else if(v1.label == Node.Label.PLUS && v2.label == Node.Label.PLUS && !v2Contained)
			{
				if(delta > slack)
					delta = slack;
				continue;
			}
			else if(v1.label == Node.Label.PLUS && v2.label == Node.Label.PLUS && v2Contained)
			{
				if(delta > slack/2)
				{
					delta = slack/2;
					System.out.println("Slack "+Double.toString(slack)+", V1=" +v1.toString()+", V2="+v2.toString()+", C="+Double.toString(edge.getWeight()));
				}
				continue;
			}
		}
		if(delta < 100*Double.MIN_VALUE)
			return 0;
		updateDualSubTree(currentRoot, delta);
		return delta;
		
	}
	
	/**
	 * Prints the current state of the algorithm to standard output.
	 */
	public void printState()
	{
		System.out.println("Trees:");
		for(Node node : exteriorNodes)
		{
			if(node.getEdgeToParent()==null)
			{
				printSubTree(node, 0);
				System.out.println();
			}
		}
		System.out.println("Free Nodes:");
		for(Edge edge: allEdges)
		{
			Node v1 = edge.getV1Node();
			Node v2 = edge.getV2Node();
			// self loops can happen for edges inside bossoms...
			if(v1==v2) 
				continue;
			
			if(edge.isMatch() && v1.getLabel() == Node.Label.EMPTY)
			{
				System.out.println(edge.getV1Node().toString()+" <=> "+edge.getV2Node().toString());
			}
		}
	}
	private void printSubTree(Node root, int level)
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
		builder.append(root.toString());
		System.out.println(builder.toString());
		for(Edge edge:root.getEdgesToChildren())
		{
			printSubTree(edge.to(root), level+1);
		}
		
	}
	
	private void updateDualSubTree(Node root, double delta)
	{
		if(root.label == Node.Label.PLUS)
			root.dual += delta;
		else if(root.label == Node.Label.MINUS)
			root.dual -= delta;
		else
			throw new RuntimeException("Node in tree with invalid label");
		for(Edge edge:root.getEdgesToChildren())
		{ 
			updateDualSubTree(edge.to(root), delta);
		}
	}
	
	/**
	 * Type of update performed by {@link Algorithm#primalUpdate()}.
	 * @author Moritz Lang
	 *
	 */
	public enum PrimalUpdateType
	{
		/**
		 * Current tree was grown by two free nodes.
		 */
		GROW,
		/**
		 * Two trees were joined to a graph. Subsequently, graph was splitted into pairs of free nodes.
		 */
		AUGMENT,
		/**
		 * A cycle was shrinked into a blossom.
		 */
		SHRINK,
		/**
		 * A blossom was expanded.
		 */
		EXPAND,
		/**
		 * The algorithm has finished.
		 */
		FINISHED,
		/**
		 * No update could be performed.
		 */
		NONE;
		
		@Override
		public String toString()
		{
			switch(this)
			{
			case GROW:
				return "grow";
			case AUGMENT:
				return "augment";
			case SHRINK:
				return "shrink";
			case EXPAND:
				return "expand";
			case FINISHED:
				return "finished";
			case NONE:
				return "none";
			default:
				return "unknown: " + super.toString();
			}
		}
	}
	private void selectNewRoot()
	{
		if(currentRoot == null)
		{
			// we only have trees consisting of exactly one node. Pick one.
			for(Node node : exteriorNodes)
			{
				if(node.label == Node.Label.PLUS)
				{
					if(node.containedIn != null)
						throw new RuntimeException("Blossoms can only be in current tree, or have label empty.");
					currentRoot = node;
					break;
				}
			}
		}
	}
	/**
	 * Makes a primal update, that is, grows or augments the current tree. Returns false if primal update was not possible.
	 * @return The type of update which was made, {@link PrimalUpdateType#FINISHED} if no trees exist anymore, or {@link PrimalUpdateType#NONE} if no update could be performed, but the algorithm is yet not finished. 
	 */
	public PrimalUpdateType primalUpdate()
	{
		selectNewRoot();
		if(currentRoot == null)
		{
			return PrimalUpdateType.FINISHED;
		}
		for(Edge edge : allEdges)
		{
			// Matches connect free nodes...
			if(edge.isMatch() || !isTight(edge))
				continue;
			
			Node v1 = edge.getV1Node();
			Node v2 = edge.getV2Node();
			
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
			
			Node.Label label1 = v1.getLabel();
			Node.Label label2 = v2.getLabel();
			
			if(v1InCurrentTree != v2InCurrentTree && label1 == Node.Label.PLUS && label2 == Node.Label.PLUS )
			{
				augment(edge);
				return PrimalUpdateType.AUGMENT;
			}
			else if((v1InCurrentTree && !v2InCurrentTree && label1 == Node.Label.PLUS && label2 == Node.Label.EMPTY)
				||  (v2InCurrentTree && !v1InCurrentTree && label2 == Node.Label.PLUS && label1 == Node.Label.EMPTY))
			{
				grow(edge);
				return PrimalUpdateType.GROW;
			}
			else if(v1InCurrentTree && v2InCurrentTree && label1 == Node.Label.PLUS && label2 == Node.Label.PLUS)
			{
				shrink(edge); 
				return PrimalUpdateType.SHRINK;
			}
		}
		
		for(Node node : exteriorNodes)
		{
			if(!(node instanceof Blossom))
				continue;
			Blossom blossom = (Blossom) node;
			if(blossom.label != Node.Label.MINUS)
				continue;
			if(Math.abs(blossom.dual) > 100*Double.MIN_VALUE)
				continue;
			expand(blossom);
			return PrimalUpdateType.EXPAND;
		}
		
		return PrimalUpdateType.NONE;
	}
	private void expand(Blossom blossom)
	{
		// when we call expand, we know that the blossom has label "-", implying that it has exactly one child.
		Collection<Edge> childEdges = blossom.getEdgesToChildren();
		if(childEdges.size() != 1)
			throw new RuntimeException("Blossom which should be extended must have label - and, thus, exactly one child.");
		Edge childEdge = childEdges.iterator().next();
		Edge parentEdge = blossom.getEdgeToParent();
		ArrayList<Edge> blossomEdges = new ArrayList<>(blossom.getBlossomEdges());
		ArrayList<Node> blossomNodes = new ArrayList<>(blossom.getBlossomNodes());
		// destroy the blossom
		allNodes.remove(blossom);
		exteriorNodes.remove(blossom);
		for(Node node:blossomNodes)
		{
			node.containedIn = null;
		}
		
		// There are two ways from the parent to the child. we need the even one.
		Node start = parentEdge.getV1Node();
		if(!blossom.contains(start))
			start = parentEdge.getV2Node();
		Node goal = childEdge.getV1Node();
		if(!blossom.contains(goal))
			goal = parentEdge.getV2Node();
		
		ArrayList<Edge> walk1 = new ArrayList<Edge>();
		ArrayList<Edge> walk2 = new ArrayList<Edge>();
		Node last1 = start;
		Node last2 = start;
		while(!blossomEdges.isEmpty())
		{
			Iterator<Edge> iterator = blossomEdges.iterator();
			while(iterator.hasNext())
			{
				Edge edge = iterator.next();
				Node to;
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
			throw new RuntimeException("One walk must be even, one odd.");
		if(walk1.size() % 2 != 0)
		{
			ArrayList<Edge> temp = walk1;
			walk1 = walk2;
			walk2 = temp;
		}
		
		// reconstruct tree with walk1
		last1 = start;
		last1.label = Node.Label.MINUS;
		for(int i=0; i<walk1.size(); i++)
		{
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
			last1.label = (i%2==0) ? Node.Label.PLUS : Node.Label.MINUS;
		}
		
		// make free nodes with walk2
		for(int i=0; i<walk2.size(); i++)
		{
			Edge edge = walk2.get(i);
			if(i%2==0)
				edge.setMatch(false);
			else
			{
				edge.setMatch(true);
				Node v1 = edge.getV1Node();
				Node v2 = edge.getV2Node();
				v1.setEdgeToParent(edge);
				v2.setEdgeToParent(edge);
				v1.label = Node.Label.EMPTY;
				v2.label = Node.Label.EMPTY;
			}
		}
		
			
	}
	private void shrink(Edge edge)
	{
		Node v1 = edge.getV1Node();
		Node v2 = edge.getV2Node();
		
		// get all nodes and edges in blossom
		ArrayList<Node> nodes = new ArrayList<>();
		ArrayList<Edge> edges = new ArrayList<>();
		edges.add(edge);
		while(true)
		{
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
		for(Node node:nodes)
		{
			node.containedIn=blossom;
		}
		
		if(currentRoot == v1 || currentRoot == v2)
			currentRoot = blossom;
	}
	
	private void grow(Edge edge)
	{
		Node v1 = edge.getV1Node();
		Node v2;
		if(currentRoot.isInSubtree(v1))
			v2=edge.getV2Node();
		else
		{
			v2=v1;
			v1 = edge.getV2Node();
		}
		Edge partnerEdge = v2.getEdgeToParent();
		Node partner = partnerEdge.to(v2);
		
		v2.setEdgeToParent(edge);
		v1.addEdgeToChild(edge);
		v2.addEdgeToChild(partnerEdge);
		v2.label = Node.Label.MINUS;
		partner.label = Node.Label.PLUS;
	}
	
	/**
	 * Augment the current tree with the edge...
	 * @param edge
	 */
	private void augment(Edge edge)
	{
		Node v1Node = edge.getV1Node();
		Node v2Node = edge.getV2Node();
		
		reverseEdgesToRoot(v1Node);
		reverseEdgesToRoot(v2Node);
		destroySubTree(currentRoot);
		currentRoot = null; 
		edge.setMatch(true);
		v1Node.setEdgeToParent(edge);
		v2Node.setEdgeToParent(edge);
		v1Node.label = Node.Label.EMPTY;
		v2Node.label = Node.Label.EMPTY;
	}
	
	private void destroySubTree(Node root)
	{
		for(Edge edge : root.getEdgesToChildren())
		{
			Node child = edge.to(root).getOuterNode();
			if(edge.isMatch())
			{
				root.setEdgeToParent(edge);
			}
			destroySubTree(child);
		}
		root.label = Node.Label.EMPTY;
		root.clearEdgesToChilds();
	}
	
	private void reverseEdgesToRoot(Node node)
	{
		do
		{
			Edge edge = node.getEdgeToParent();
			if(edge == null)
				break;
			edge.setMatch(!edge.isMatch()); 
			node = edge.to(node).getOuterNode();
		} while(node != null);
	}
}
