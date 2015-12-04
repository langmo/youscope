package org.youscope.addon.component;

import java.net.URL;

import javax.swing.ImageIcon;

import org.youscope.common.configuration.Configuration;
import org.youscope.common.measurement.Component;

/**
 * Adapter class to simplify the construction of custom metadata for configurations.
 * @author Moritz Lang
 *
 * @param <C>
 */
public class ComponentMetadataAdapter<C extends Configuration>  implements ComponentMetadata<C> 
{
	
	private final String typeName;
	private final Class<C> configurationClass;
	private final String typeIdentifier;
	private final String iconPath;
	private final Class<? extends Component> componentInterface;
	private final String[] configurationClassification;

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
		this.typeIdentifier = typeIdentifier;
		this.configurationClass = configurationClass;
		this.typeName = typeName;
		this.configurationClassification = configurationClassification;
		this.iconPath = iconPath;
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
	public String getTypeName() {
		return typeName;
	}

	@Override
	public Class<C> getConfigurationClass() 
	{
		return configurationClass;
	}

	@Override
	public String getTypeIdentifier() 
	{
		return typeIdentifier;
	}

	@Override
	public ImageIcon getIcon() {
		if(iconPath == null)
			return null;
		URL iconURL = ComponentMetadataAdapter.class.getClassLoader().getResource(iconPath);
		if (iconURL != null)
			return new ImageIcon(iconURL, getTypeName());
		return null;
	}

	@Override
	public String[] getConfigurationClassification() {
		if(configurationClassification == null || configurationClassification.length <=0)
			return new String[0];
		// Make copy of classification for user.
		String[] copy = new String[configurationClassification.length];
		System.arraycopy(configurationClassification, 0, copy, 0, configurationClassification.length);
		return copy;
	}

	@Override
	public Class<? extends Component> getComponentInterface() 
	{
		return componentInterface;
	}

}
