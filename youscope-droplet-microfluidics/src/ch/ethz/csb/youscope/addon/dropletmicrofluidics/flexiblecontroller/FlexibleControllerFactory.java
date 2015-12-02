/**
 * 
 */
package ch.ethz.csb.youscope.addon.dropletmicrofluidics.flexiblecontroller;

import ch.ethz.csb.youscope.addon.adapters.AddonFactoryAdapter;

/**
 * Controller for droplet-based microfluidics based on a syringe table.
 * @author Moritz Lang
 */
public class FlexibleControllerFactory extends AddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public FlexibleControllerFactory()
	{
		super(FlexibleControllerUI.class, FlexibleController.class, FlexibleControllerUI.getMetadata());
	}
}
