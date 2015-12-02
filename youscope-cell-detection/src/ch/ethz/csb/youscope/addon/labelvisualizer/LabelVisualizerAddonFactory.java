/**
 * 
 */
package ch.ethz.csb.youscope.addon.labelvisualizer;

import ch.ethz.csb.youscope.addon.adapters.AddonFactoryAdapter;

/**
 * @author Moritz Lang
 *
 */
public class LabelVisualizerAddonFactory  extends AddonFactoryAdapter
{

	/**
	 * Constructor.
	 */
	public LabelVisualizerAddonFactory()
	{
		super(LabelVisualizerConfiguration.CONFIGURATION_ID, LabelVisualizerConfiguration.class, LabelVisualizerAddon.class);
	}

}
