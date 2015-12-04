/**
 * 
 */
package org.youscope.plugin.glowvisualizer;

import org.youscope.addon.component.ComponentAddonFactoryAdapter;

/**
 * @author Moritz Lang
 *
 */
public class GlowVisualizerAddonFactory extends ComponentAddonFactoryAdapter
{

	/**
	 * Constructor.
	 */
	public GlowVisualizerAddonFactory()
	{
		super(GlowVisualizerConfiguration.CONFIGURATION_ID, GlowVisualizerConfiguration.class, GlowVisualizerAddon.class);
	}
}
