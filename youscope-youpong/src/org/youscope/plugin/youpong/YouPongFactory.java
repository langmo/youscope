/**
 * 
 */
package org.youscope.plugin.youpong;

import org.youscope.addon.tool.ToolAddonFactoryAdapter;

/**
 * @author Moritz Lang
 *
 */
public class YouPongFactory extends ToolAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public YouPongFactory()
	{
		super(YouPong.class, YouPong.getMetadata());
	}
}
