/**
 * 
 */

package org.youscope.plugin.multicameraandcolorstream;

import org.youscope.addon.tool.ToolAddonFactoryAdapter;

/**
 * @author Moritz Lang
 *
 */
public class MultiCameraAndColorStreamFactory extends ToolAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public MultiCameraAndColorStreamFactory()
	{
		super(MultiCameraAndColorStream.class, MultiCameraAndColorStream.getMetadata());
	}
}
