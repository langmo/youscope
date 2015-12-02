package ch.ethz.csb.youscope.addon.exhaustivefocussearch;

import ch.ethz.csb.youscope.addon.adapters.AddonFactoryAdapter;

/**
 * Factory for the creation of exhaustive search focus search.
 * @author Moritz Lang
 *
 */
public class ExhaustiveFocusSearchAddonFactory extends AddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public ExhaustiveFocusSearchAddonFactory()
	{
		super(ExhaustiveFocusSearchConfiguration.CONFIGURATION_ID, ExhaustiveFocusSearchConfiguration.class, ExhaustiveFocusSearchAddon.class);
	}
}
