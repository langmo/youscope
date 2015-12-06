/**
 * 
 */
package org.youscope.plugin.devicesettingmanager;

import org.youscope.addon.tool.ToolAddonFactoryAdapter;

/**
 * @author Moritz Lang
 *
 */
public class DeviceSettingManagerFactory extends ToolAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public DeviceSettingManagerFactory()
	{
		super(DeviceSettingManager.class, DeviceSettingManager.getMetadata());
	}
}
