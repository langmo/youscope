/**
 * 
 */
package ch.ethz.csb.youscope.addon.cellx;

import ch.ethz.csb.youscope.addon.adapters.AddonFactoryAdapter;

/**
 * @author Moritz Lang
 *
 */
public class CellXAddonFactory extends AddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public CellXAddonFactory()
	{
		super(CellXConfigurationAddon.class, CellXAddon.class, CellXConfigurationAddon.getMetadata());
	}
}
