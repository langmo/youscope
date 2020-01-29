/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
package org.youscope.addon.component;

import javax.swing.Icon;

import org.youscope.addon.AddonMetadataAdapter;
import org.youscope.common.Component;
import org.youscope.common.configuration.Configuration;

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
	 * @param name Human readable name for this configuration.
	 * @param classification Classification of configuration. This classification can e.g. be used to order configurations into a certain folder structure.
	 * @param description Description of the addon. Set to null to not provide any description.
	 * @param iconPath Path to an icon representing this configuration. This path should be a valid path to a resource, i.e. an icon saved inside a jar file loaded by YouScope.
	 */
	public ComponentMetadataAdapter(final String typeIdentifier,
			final Class<C> configurationClass,
			final Class<? extends Component> componentInterface,
			final String name,
			final String[] classification,
			final String description,
			final String iconPath) 
	{
		super(typeIdentifier, name, classification, description, iconPath);
		this.configurationClass = configurationClass;
		this.componentInterface = componentInterface;
	}
	
	/**
	 * Constructor.
	 * @param typeIdentifier Type identifier of the configuration.
	 * @param configurationClass Class of the configuration.
	 * @param componentInterface The interface of the measurement component which is created when the configuration is compiled.
	 * @param name Human readable name for this configuration.
	 * @param classification Classification of configuration. This classification can e.g. be used to order configurations into a certain folder structure.
	 * @param description Description of the addon. Set to null to not provide any description.
	 * @param icon Icon of the addon. Set to null for default icon.
	 */
	public ComponentMetadataAdapter(final String typeIdentifier,
			final Class<C> configurationClass,
			final Class<? extends Component> componentInterface,
			final String name,
			final String[] classification,
			final String description,
			final Icon icon) 
	{
		super(typeIdentifier, name, classification, description, icon);
		this.configurationClass = configurationClass;
		this.componentInterface = componentInterface;
	}
	
	/**
	 * Constructor. Sets the icon to the default icon, and the description to null/not available.
	 * @param typeIdentifier Type identifier of the configuration.
	 * @param configurationClass Class of the configuration.
	 * @param componentInterface The interface of the measurement component which is created when the configuration is compiled.
	 * @param name Human readable name for this configuration.
	 * @param classification Classification of configuration. This classification can e.g. be used to order configurations into a certain folder structure.
	 */
	public ComponentMetadataAdapter(final String typeIdentifier,
			final Class<C> configurationClass,
			final Class<? extends Component> componentInterface,
			final String name,
			final String[] classification)
	{
		this(typeIdentifier,
				configurationClass, componentInterface,
				name,
				classification,
				null, (Icon)null);
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
