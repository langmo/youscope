package org.youscope.plugin.travelingsalesman.blossom;

/**
 * Label of a pseudonode. If node is in tree, labels alternate from root (PLUS) to leaves (PLUS) between PLUS and MINUS.
 * If pseudonode is free, label must be EMPTY}. 
 * @author mlang
 *
 */
enum NodeLabel 
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
