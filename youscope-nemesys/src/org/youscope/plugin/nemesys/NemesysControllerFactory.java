/**
 * 
 */
package org.youscope.plugin.nemesys;

import org.youscope.addon.tool.ToolAddonFactoryAdapter;

/**
 * Tool to control the Nemesys Syringe system.
 * @author Moritz Lang
 *
 */
public class NemesysControllerFactory extends ToolAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public NemesysControllerFactory()
	{
		super(NemesysController.class, NemesysController.getMetadata());
	}
}
