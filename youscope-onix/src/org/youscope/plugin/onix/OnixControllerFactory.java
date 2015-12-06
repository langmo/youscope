/**
 * 
 */
package org.youscope.plugin.onix;

import org.youscope.addon.tool.ToolAddonFactoryAdapter;

/**
 * @author Moritz Lang
 *
 */
public class OnixControllerFactory extends ToolAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public OnixControllerFactory()
	{
		super(OnixController.class, OnixController.getMetadata());
	}
}
