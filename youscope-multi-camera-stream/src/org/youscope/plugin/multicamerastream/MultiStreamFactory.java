/**
 * 
 */
package org.youscope.plugin.multicamerastream;

import org.youscope.addon.tool.ToolAddonFactoryAdapter;

/**
 * @author Moritz Lang
 *
 */
public class MultiStreamFactory extends ToolAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public MultiStreamFactory()
	{
		super(MultiStream.class, MultiStream.getMetadata());
	}
}
