/**
 * 
 */
package ch.ethz.csb.youscope.addon.glowvisualizer;

import ch.ethz.csb.youscope.addon.adapters.AddonFactoryAdapter;

/**
 * @author Moritz Lang
 *
 */
public class GlowVisualizerAddonFactory extends AddonFactoryAdapter
{

	/**
	 * Constructor.
	 */
	public GlowVisualizerAddonFactory()
	{
		super(GlowVisualizerConfiguration.CONFIGURATION_ID, GlowVisualizerConfiguration.class, GlowVisualizerAddon.class);
	}
}
