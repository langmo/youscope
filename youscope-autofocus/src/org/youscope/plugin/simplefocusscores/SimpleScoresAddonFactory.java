/**
 * 
 */
package org.youscope.plugin.simplefocusscores;

import org.youscope.addon.component.ComponentAddonFactoryAdapter;

/**
 * @author Moritz Lang
 */ 
public class SimpleScoresAddonFactory extends ComponentAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public SimpleScoresAddonFactory()
	{
		addAddon(AutocorrelationFocusScoreConfiguration.CONFIGURATION_ID, AutocorrelationFocusScoreConfiguration.class, AutocorrelationFocusScoreAddon.class);
		addAddon(VarianceFocusScoreConfiguration.CONFIGURATION_ID, VarianceFocusScoreConfiguration.class, VarianceFocusScoreAddon.class);
		
	}
}
