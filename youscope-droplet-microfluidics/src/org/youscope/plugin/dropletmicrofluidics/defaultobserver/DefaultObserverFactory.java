package org.youscope.plugin.dropletmicrofluidics.defaultobserver;

import org.youscope.addon.component.ComponentAddonFactoryAdapter;
 
/**
 * Implementation of the an observer based on the discrete Fourier transformation. The observer separately learns the individual and the mean
 * droplet heights. 
 * @author Moritz Lang
 *
 */
public class DefaultObserverFactory extends ComponentAddonFactoryAdapter
{

	/**
	 * Constructor.
	 */
	public DefaultObserverFactory()
	{
		super(DefaultObserverConfiguration.TYPE_IDENTIFIER, DefaultObserverConfiguration.class, DefaultObserver.class);
	}
}
