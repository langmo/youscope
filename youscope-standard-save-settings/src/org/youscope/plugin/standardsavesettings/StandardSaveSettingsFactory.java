/**
 * 
 */
package org.youscope.plugin.standardsavesettings;

import org.youscope.addon.component.ComponentAddonFactoryAdapter;

/**
 * Factory for standard save settings.
 * @author Moritz Lang
 */
public class StandardSaveSettingsFactory extends ComponentAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public StandardSaveSettingsFactory()
	{
		super(StandardSaveSettingsUI.class, StandardSaveSettings.class, StandardSaveSettingsUI.getMetadata());
	}
}
