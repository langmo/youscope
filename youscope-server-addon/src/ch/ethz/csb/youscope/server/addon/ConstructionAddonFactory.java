package ch.ethz.csb.youscope.server.addon;

import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.configuration.Configuration;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationMetadata;
import ch.ethz.csb.youscope.shared.measurement.Component;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;

/**
 * Base class of all addon factories. Subclasses must always be public. Should be exported by .jar file of addon by creating the file
 * <code>META_INF/services/ch.ethz.csb.youscope.server.addon.ConstructionAddonFactory</code>, and adding for every addon factory
 * a line defining the full qualifying name of the factory. Several factories have to be separated by a line break. For example, if
 * the factory <code>public class FooConstructionAddonFactory implements ConstructionAddonFactory {}</code> is located in the package
 * <code>ch.ethz.csb.youscope.addon.FooAddon</code>, the addon .jar archieve <code>FooAddon.jar</code>must contain the file <code>META_INF/services/ch.ethz.csb.youscope.server.addon.ConstructionAddonFactory</code>
 * with the content <code>ch.ethz.csb.youscope.addon.FooAddon.FooConstructionAddonFactory</code>.
 * @author Moritz Lang
 *
 */
public interface ConstructionAddonFactory 
{
	/**
	 * Creates an addon for the given configuration.
     * Throws a construction addon exception if the configuration type provided is not supported.
     * 
	 * @param positionInformation The position in the measurement hierarchy where the addon should be constructed.
	 * @param configuration The configuration of the addon.
	 * @param constructionContext The context of the construction, providing information and functionality in the construction of the addon.
	 * @return The constructed addon.
	 * @throws ConfigurationException Thrown if the configuration is invalid.
	 * @throws AddonException Thrown if an error occured during the construction.
	 */
	Component createComponent(PositionInformation positionInformation, Configuration configuration, ConstructionContext constructionContext) throws ConfigurationException, AddonException;

    /**
	 * Returns a list of all type identifiers supported by this factory.
	 * @return List of supported configuration types.
	 */
	String[] getSupportedTypeIdentifiers();

	/**
	 * Returns true if this factory supports configurations with the given ID, false otherwise.
	 * @param typeIdentifier The type identifier for which it should be queried if this factory supports its construction.
	 * @return True if this factory supports the given type identifier, false otherwise.
	 */
	boolean isSupportingTypeIdentifier(String typeIdentifier);
	
	/**
	 * Returns metadata about the component type constructed by this factory, and the configuration type consumed. 
	 * @param typeIdentifier Type identifier corresponding to the component.
	 * @return Metadata about the component.
	 * @throws AddonException Thrown if type identifier is not supported by this factory.
	 */
	ConfigurationMetadata<?> getConfigurationMetadata(String typeIdentifier) throws AddonException;
}
