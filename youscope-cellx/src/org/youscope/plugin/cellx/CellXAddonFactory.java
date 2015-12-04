/**
 * 
 */
package org.youscope.plugin.cellx;

import org.youscope.addon.component.ComponentAddonFactoryAdapter;

/**
 * @author Moritz Lang
 *
 */
public class CellXAddonFactory extends ComponentAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public CellXAddonFactory()
	{
		super(CellXConfigurationAddon.class, CellXAddon.class, CellXConfigurationAddon.getMetadata());
	}
}
