/**
 * 
 */
package org.youscope.plugin.customsavesettings;

import org.youscope.addon.tool.ToolAddonFactoryAdapter;

/**
 * @author Moritz Lang
 *
 */
public class CustomSaveSettingToolFactory extends ToolAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public CustomSaveSettingToolFactory()
	{
		super(CustomSaveSettingTool.class, CustomSaveSettingTool.getMetadata());
	}
}
