/**
 * 
 */

package org.youscope.plugin.multicolorstream;

import org.youscope.addon.tool.ToolAddonFactoryAdapter;

/**
 * @author Moritz Lang
 *
 */
public class MultiColorStreamFactory extends ToolAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public MultiColorStreamFactory()
	{
		super(MultiColorStream.class, MultiColorStream.getMetadata());
	}
}
