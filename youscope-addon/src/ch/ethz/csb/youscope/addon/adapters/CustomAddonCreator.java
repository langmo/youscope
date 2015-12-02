package ch.ethz.csb.youscope.addon.adapters;

import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.configuration.Configuration;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.measurement.Component;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;

/**
 * Interface which has to be implemented if the creation of a component, given its configuration, should be done
 * differently from the default mechanism.
 * @author Moritz Lang
 * @param <C> The configuration class consumed for the creation.
 * @param <T> Public interface of created component.
 *
 */
public interface CustomAddonCreator<C extends Configuration, T extends Component>
{
	/**
	 * Creates an component for the given configuration. The created component must be in agreement with the component interface type returned in the metadata.
     * 
	 * @param positionInformation The position in the measurement hierarchy where the addon should be constructed.
	 * @param configuration The configuration of the addon.
	 * @param constructionContext The context of the construction, providing information and functionality in the construction of the addon.
	 * @return The constructed component.
	 * @throws ConfigurationException Thrown if the configuration is invalid.
	 * @throws AddonException Thrown if an error occurred during the construction.
	 */
	public T createCustom(PositionInformation positionInformation, C configuration, ConstructionContext constructionContext) throws ConfigurationException, AddonException;
	
	/**
	 * Returns the public interface of the component which gets created.
	 * @return Interface of created component.
	 */
	public Class<T> getComponentInterface();
}
