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
