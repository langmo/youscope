/**
 * 
 */
package org.youscope.plugin.customjob;

import org.youscope.addon.tool.ToolAddonFactoryAdapter;

/**
 * @author Moritz Lang
 *
 */
public class CustomJobToolFactory extends ToolAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public CustomJobToolFactory()
	{
		super(CustomJobTool.class, CustomJobTool.getMetadata());
	}
}
