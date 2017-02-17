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
/**
 * 
 */
package org.youscope.plugin.travelingsalesman;

import org.youscope.addon.component.ComponentAddonFactoryAdapter;
/**
 * Factory for Traveling Salesman path optimization algorithms.
 * @author Moritz Lang
 */
public class TravelingSalesmanFactory extends ComponentAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public TravelingSalesmanFactory()
	{
		addAddon(PreOrderMinimumSpanningTreeConfiguration.TYPE_IDENTIFIER, PreOrderMinimumSpanningTreeConfiguration.class, PreOrderMinimumSpanningTreeResource.class);
		addAddon(ChristofidesAlgorithmConfiguration.TYPE_IDENTIFIER, ChristofidesAlgorithmConfiguration.class, ChristofidesAlgorithmResource.class);
		
	}
}
