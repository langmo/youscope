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
