/**
 * 
 */
package org.youscope.plugin.livestream;

import org.youscope.addon.tool.ToolAddonFactoryAdapter;

/**
 * @author Moritz Lang
 *
 */
public class LiveStreamFactory extends ToolAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public LiveStreamFactory()
	{
		addAddon(LiveStream.class, LiveStream.getMetadata());
		addAddon(LiveStreamOld.class, LiveStreamOld.getMetadata());
	}
	
}
