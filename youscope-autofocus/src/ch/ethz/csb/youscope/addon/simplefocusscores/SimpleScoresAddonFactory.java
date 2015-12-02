/**
 * 
 */
package ch.ethz.csb.youscope.addon.simplefocusscores;

import ch.ethz.csb.youscope.addon.adapters.AddonFactoryAdapter;

/**
 * @author Moritz Lang
 */ 
public class SimpleScoresAddonFactory extends AddonFactoryAdapter
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
