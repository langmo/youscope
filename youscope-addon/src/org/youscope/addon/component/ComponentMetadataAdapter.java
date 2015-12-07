package org.youscope.addon.component;

import org.youscope.addon.AddonMetadataAdapter;
import org.youscope.common.configuration.Configuration;
import org.youscope.common.measurement.Component;

/**
 * Adapter class to simplify the construction of custom metadata for configurations.
 * @author Moritz Lang
 *
 * @param <C>
 */
public class ComponentMetadataAdapter<C extends Configuration> extends AddonMetadataAdapter implements ComponentMetadata<C> 
{
	
	private final Class<C> configurationClass;
	private final Class<? extends Component> componentInterface;
	
	/**
	 * Constructor.
	 * @param typeIdentifier Type identifier of the configuration.
	 * @param configurationClass Class of the configuration.
	 * @param componentInterface The interface of the measurement component which is created when the configuration is compiled.
	 * @param typeName Human readable name for this configuration.
	 * @param configurationClassification Classification of configuration. This classification can e.g. be used to order configurations into a certain folder structure.
	 * @param iconPath Path to an icon representing this configuration. This path should be a valid path to a resource, i.e. an icon saved inside a jar file loaded by YouScope.
	 */
	public ComponentMetadataAdapter(final String typeIdentifier,
			final Class<C> configurationClass,
			final Class<? extends Component> componentInterface,
			final String typeName,
			final String[] configurationClassification,
			final String iconPath) 
	{
		super(typeIdentifier, typeName, configurationClassification, iconPath);
		this.configurationClass = configurationClass;
		this.componentInterface = componentInterface;
	}
	
	/**
	 * Constructor.
	 * @param typeIdentifier Type identifier of the configuration.
	 * @param configurationClass Class of the configuration.
	 * @param componentInterface The interface of the measurement component which is created when the configuration is compiled.
	 * @param typeName Human readable name for this configuration.
	 * @param configurationClassification Classification of configuration. This classification can e.g. be used to order configurations into a certain folder structure.
	 */
	public ComponentMetadataAdapter(final String typeIdentifier,
			final Class<C> configurationClass,
			final Class<? extends Component> componentInterface,
			final String typeName,
			final String[] configurationClassification)
	{
		this(typeIdentifier,
				configurationClass, componentInterface,
				typeName,
				configurationClassification,
				null);
	}
	
	/**
	 * Constructor.
	 * @param typeIdentifier Type identifier of the configuration.
	 * @param configurationClass Class of the configuration.
	 * @param componentInterface The interface of the measurement component which is created when the configuration is compiled.
	 * @param typeName Human readable name for this configuration.
	 * @param iconPath Path to an icon representing this configuration. This path should be a valid path to a resource, i.e. an icon saved inside a jar file loaded by YouScope.
	 */
	public ComponentMetadataAdapter(final String typeIdentifier,
			final Class<C> configurationClass,
			final Class<? extends Component> componentInterface,
			final String typeName,
			final String iconPath)
	{
		this(typeIdentifier,
				configurationClass, componentInterface,
				typeName,
				null,
				iconPath);
	}
	
	/**
	 * Constructor.
	 * @param typeIdentifier Type identifier of the configuration.
	 * @param configurationClass Class of the configuration.
	 * @param componentInterface The interface of the measurement component which is created when the configuration is compiled.
	 * @param typeName Human readable name for this configuration.
	 */
	public ComponentMetadataAdapter(final String typeIdentifier,
			final Class<C> configurationClass,
			final Class<? extends Component> componentInterface,
			final String typeName)
	{
		this(typeIdentifier,
				configurationClass, componentInterface,
				typeName,
				null,
				null);
	}

	@Override
	public Class<C> getConfigurationClass() 
	{
		return configurationClass;
	}

	@Override
	public Class<? extends Component> getComponentInterface() 
	{
		return componentInterface;
	}
}
