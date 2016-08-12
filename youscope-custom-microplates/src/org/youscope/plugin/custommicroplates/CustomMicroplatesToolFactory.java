/**
 * 
 */
package org.youscope.plugin.custommicroplates;

import org.youscope.addon.tool.ToolAddonFactoryAdapter;

/**
 * @author Moritz Lang
 *
 */
public class CustomMicroplatesToolFactory extends ToolAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public CustomMicroplatesToolFactory()
	{
		super(CustomMicroplatesTool.class, CustomMicroplatesTool.getMetadata());
	}
}
