package ch.ethz.csb.youscope.addon.dropletmicrofluidics.defaultobserver;

import ch.ethz.csb.youscope.addon.adapters.AddonFactoryAdapter;
 
/**
 * Implementation of the an observer based on the discrete Fourier transformation. The observer separately learns the individual and the mean
 * droplet heights. 
 * @author Moritz Lang
 *
 */
public class DefaultObserverFactory extends AddonFactoryAdapter
{

	/**
	 * Constructor.
	 */
	public DefaultObserverFactory()
	{
		super(DefaultObserverConfiguration.TYPE_IDENTIFIER, DefaultObserverConfiguration.class, DefaultObserver.class);
	}
}
