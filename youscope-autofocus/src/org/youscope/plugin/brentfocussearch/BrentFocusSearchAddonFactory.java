package org.youscope.plugin.brentfocussearch;

import org.youscope.addon.component.ComponentAddonFactoryAdapter;
 
/**
 * Implementation of the Brent focus search algorithm
 * @author Moritz Lang
 *
 */
public class BrentFocusSearchAddonFactory extends ComponentAddonFactoryAdapter
{

	/**
	 * Constructor.
	 */
	public BrentFocusSearchAddonFactory()
	{
		super(BrentFocusSearchConfiguration.CONFIGURATION_ID, BrentFocusSearchConfiguration.class, BrentFocusSearchAddon.class);
	}
}
