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

import java.net.URL;

import javax.swing.ImageIcon;

import org.youscope.common.Component;
import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.YSConfigAlias;
import org.youscope.common.configuration.YSConfigClassification;
import org.youscope.common.configuration.YSConfigDescription;
import org.youscope.common.configuration.YSConfigIcon;

/**
 * Helper class to construct configuration metadata. Most of the metadata is directly extracted from the configuration class using java reflection.
 * During this extraction, the annotations defined in org.youscope.common.configuration.annotations are used, if given, or generic names for the
 * respective properties is extracted given e.g. the name of the class. 
 * @author Moritz Lang
 *
 * @param <C> Specific subclass of configuration for which an UI should be created.
 */
public class GenericComponentMetadata<C extends Configuration> implements ComponentMetadata<C> 
{

	private final String typeIdentifier;
	private final Class<C> configurationClass;
	private final Class<? extends Component> componentInterface;
	/**
	 * Constructor.
	 * @param typeIdentifier Type identifier of the configuration.
	 * @param configurationClass Class of the configuration.
	 * @param componentInterface The interface of the measurement component which is created when the configuration is compiled.
	 */
	public GenericComponentMetadata(final String typeIdentifier, 
			final Class<C> configurationClass, 
			final Class<? extends Component> componentInterface) 
	{
		this.typeIdentifier = typeIdentifier;
		this.configurationClass = configurationClass;
		this.componentInterface = componentInterface;
	} 
	
	

	@Override
	public String getName() 
	{
		YSConfigAlias alias = configurationClass.getAnnotation(YSConfigAlias.class);
		if(alias != null)
			return alias.value();
		return configurationClass.getSimpleName();
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
	public ImageIcon getIcon() 
	{
		YSConfigIcon path = configurationClass.getAnnotation(YSConfigIcon.class);
		if(path == null)
			return null;
		URL iconURL = GenericComponentMetadata.class.getClassLoader().getResource(path.value());
		if (iconURL != null)
			return new ImageIcon(iconURL, getName());
		return null;
	}

	@Override
	public String[] getClassification() {
		YSConfigClassification classification = configurationClass.getAnnotation(YSConfigClassification.class);
		if(classification != null)
			return classification.value();
		return new String[0];
	}

	@Override
	public Class<? extends Component> getComponentInterface() {
		return componentInterface;
	}



	@Override
	public String getDescription() {
		YSConfigDescription alias = configurationClass.getAnnotation(YSConfigDescription.class);
		if(alias != null)
			return alias.value();
		return null;
	}
}
