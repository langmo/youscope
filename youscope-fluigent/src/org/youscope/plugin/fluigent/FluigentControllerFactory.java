/**
 * 
 */
package org.youscope.plugin.fluigent;

import org.youscope.addon.tool.ToolAddonFactoryAdapter;

/**
 * Tool to control the Fluigent pump device.
 * @author Moritz Lang
 *
 */
public class FluigentControllerFactory extends ToolAddonFactoryAdapter
{
	/**
	 * Constructor,
	 */
	public FluigentControllerFactory()
	{
		super(FluigentController.class, FluigentController.getMetadata());
	}
}
