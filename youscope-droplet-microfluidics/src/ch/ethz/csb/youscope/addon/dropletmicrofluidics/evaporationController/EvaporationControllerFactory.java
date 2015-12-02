/**
 * 
 */
package ch.ethz.csb.youscope.addon.dropletmicrofluidics.evaporationController;

import ch.ethz.csb.youscope.addon.adapters.AddonFactoryAdapter;

/**
 * Controller for droplet-based microfluidics based on a syringe table.
 * @author Moritz Lang
 */
public class EvaporationControllerFactory extends AddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public EvaporationControllerFactory()
	{
		super(EvaporationControllerUI.class, EvaporationController.class, EvaporationControllerUI.getMetadata());
	}
}
