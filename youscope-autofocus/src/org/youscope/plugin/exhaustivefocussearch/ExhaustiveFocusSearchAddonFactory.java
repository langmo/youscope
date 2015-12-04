package org.youscope.plugin.exhaustivefocussearch;

import org.youscope.addon.component.ComponentAddonFactoryAdapter;

/**
 * Factory for the creation of exhaustive search focus search.
 * @author Moritz Lang
 *
 */
public class ExhaustiveFocusSearchAddonFactory extends ComponentAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public ExhaustiveFocusSearchAddonFactory()
	{
		super(ExhaustiveFocusSearchConfiguration.CONFIGURATION_ID, ExhaustiveFocusSearchConfiguration.class, ExhaustiveFocusSearchAddon.class);
	}
}
