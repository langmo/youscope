package ch.ethz.csb.youscope.addon.brentfocussearch;

import ch.ethz.csb.youscope.addon.adapters.AddonFactoryAdapter;
 
/**
 * Implementation of the Brent focus search algorithm
 * @author Moritz Lang
 *
 */
public class BrentFocusSearchAddonFactory extends AddonFactoryAdapter
{

	/**
	 * Constructor.
	 */
	public BrentFocusSearchAddonFactory()
	{
		super(BrentFocusSearchConfiguration.CONFIGURATION_ID, BrentFocusSearchConfiguration.class, BrentFocusSearchAddon.class);
	}
}
