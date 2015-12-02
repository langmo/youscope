package ch.ethz.csb.youscope.addon.adapters;

import ch.ethz.csb.youscope.client.addon.ConfigurationAddonFactory;
import ch.ethz.csb.youscope.server.addon.ConstructionAddonFactory;

/**
 * General interface for all addons which provide a UI for the configuration of a measurement component, and a factory for the
 * construction of a measurement component given a configuration.
 * @author Moritz Lang
 *
 */
public interface AddonFactory extends ConfigurationAddonFactory, ConstructionAddonFactory 
{
	// Marker interface to simplify addon construction.
}
