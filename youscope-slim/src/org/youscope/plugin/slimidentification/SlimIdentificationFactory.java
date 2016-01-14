/**
 * 
 */
package org.youscope.plugin.slimidentification;

import org.youscope.addon.tool.ToolAddonFactoryAdapter;

/**
 * @author Moritz Lang
 *
 */
public class SlimIdentificationFactory extends ToolAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public SlimIdentificationFactory()
	{
		addAddon(SlimIdentification.class, SlimIdentification.getMetadata());
	}
	
}
