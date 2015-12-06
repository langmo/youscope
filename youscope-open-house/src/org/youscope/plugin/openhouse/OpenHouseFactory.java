/**
 * 
 */
package org.youscope.plugin.openhouse;

import org.youscope.addon.tool.ToolAddonFactoryAdapter;

/**
 * Tool to control the Nemesys Syringe system.
 * @author Moritz Lang
 *
 */
public class OpenHouseFactory extends ToolAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public OpenHouseFactory()
	{
		super(OpenHouse.class, OpenHouse.getMetadata());
	}
}
