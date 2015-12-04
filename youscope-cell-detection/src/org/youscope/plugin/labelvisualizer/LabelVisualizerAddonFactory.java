/**
 * 
 */
package org.youscope.plugin.labelvisualizer;

import org.youscope.addon.component.ComponentAddonFactoryAdapter;

/**
 * @author Moritz Lang
 *
 */
public class LabelVisualizerAddonFactory  extends ComponentAddonFactoryAdapter
{

	/**
	 * Constructor.
	 */
	public LabelVisualizerAddonFactory()
	{
		super(LabelVisualizerConfiguration.CONFIGURATION_ID, LabelVisualizerConfiguration.class, LabelVisualizerAddon.class);
	}

}
