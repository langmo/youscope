/**
 * 
 */
package org.youscope.plugin.custommicroplates;

import org.youscope.addon.tool.ToolAddonFactoryAdapter;

/**
 * @author Moritz Lang
 *
 */
public class CustomMicroplatesFactory extends ToolAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public CustomMicroplatesFactory()
	{
		super(CustomMicroplatesTool.class, CustomMicroplatesTool.getMetadata());
	}
}
