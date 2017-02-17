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
package org.youscope.addon.measurement;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUI;
import org.youscope.addon.component.ComponentMetadata;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.serverinterfaces.ConstructionContext;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * Adapter simplifying addon creation. 
 * @author Moritz Lang
 *
 */
public class MeasurementAddonFactoryAdapter implements MeasurementAddonFactory
{
	private final ArrayList<SupportedAddon<?>> supportedAddons = new ArrayList<SupportedAddon<?>>(1);
	private class SupportedAddon<C extends MeasurementConfiguration>
	{
		public final Class<? extends ComponentAddonUI<C>> configurationAddonClass;
		public final ComponentMetadata<C> metadata; 
		public final Class<C> configurationClass;
		public final String typeIdentifier;
		public final MeasurementInitializer<C> customInitializer;
		SupportedAddon(Class<? extends ComponentAddonUI<C>> addonClass, MeasurementInitializer<C> customInitializer, ComponentMetadata<C> metadata)
		{
			if(addonClass == null || metadata == null||customInitializer==null)
				throw new NullPointerException();
			this.configurationAddonClass = addonClass;
			this.configurationClass = metadata.getConfigurationClass();
			this.typeIdentifier = metadata.getTypeIdentifier();
			this.metadata = metadata;
			this.customInitializer = customInitializer;
		}
		ComponentAddonUI<C> createConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
		{
			Constructor<? extends ComponentAddonUI<C>> constructor;
			try {
				constructor = configurationAddonClass.getDeclaredConstructor(YouScopeClient.class, YouScopeServer.class);
			} catch (NoSuchMethodException e) {
				throw new AddonException("Could not create resource configuration for "+typeIdentifier+" since configuration class "+configurationAddonClass.getName()+" does not support appropriate constructors. Argument list of constructor must be (YouScopeClient, YouScopeServer).", e);
			} 
			catch (SecurityException e) {
				throw new AddonException("Could not create configuration due to security exception.", e);
			}
			try 
			{
				constructor.setAccessible(true);
				return constructor.newInstance(client, server);
			} 
			catch (Exception e) 
			{
				throw new AddonException("Could not create resource.", e);
			}
		}
		
		ComponentMetadata<C> getConfigurationMetadata()
		{
			return metadata;
		}
		
		String getTypeIdentifer()
		{
			return metadata.getTypeIdentifier();
		}
		boolean isTypeIdentifier(String typeIdentifier)
		{
			return getTypeIdentifer().equals(typeIdentifier);
		}
		
		void initializeMeasurement(Measurement measurement, Configuration rawConfiguration, ConstructionContext constructionContext) throws ConfigurationException, AddonException
		{
			if(!configurationClass.isInstance(rawConfiguration))
				throw new AddonException("Configuration with type identifier " + rawConfiguration.getTypeIdentifier() + " has class " + rawConfiguration.getClass().getName()+", which is not of class " + configurationClass.getName() + " which is expected.");
			C configuration = configurationClass.cast(rawConfiguration);
			customInitializer.initializeMeasurement(measurement, configuration, constructionContext);
		}		
	}
	
	/**
	 * Constructor. When invoked, the factory exposes the given addon.
	 * @param configurationAddonClass The UI exposed to the user.
	 * @param customInitializer Object for measurement initialization.
	 * @param metadata Metadata about this addon.
	 */
	public <C extends MeasurementConfiguration> MeasurementAddonFactoryAdapter(Class<? extends ComponentAddonUI<C>> configurationAddonClass, MeasurementInitializer<C> customInitializer, ComponentMetadata<C> metadata)
	{
		addAddon(configurationAddonClass, customInitializer, metadata);
	}
	
	/**
	 * Constructor. Does no invoke any addon. Use <code>addAddon()</code> to expose addons later on.
	 */
	public MeasurementAddonFactoryAdapter() 
	{
		// do nothing.
	}
	/**
	 * Adds an addon.
	 * @param configurationAddonClass Class providing the UI elements.
	 * @param customInitializer A custom initializer for the measurement.
	 * @param metadata Metadata of the measurement.
	 */
	public <C extends MeasurementConfiguration> void addAddon(Class<? extends ComponentAddonUI<C>> configurationAddonClass, MeasurementInitializer<C> customInitializer, ComponentMetadata<C> metadata)
	{
		supportedAddons.add(new SupportedAddon<C>(configurationAddonClass, customInitializer, metadata));
	}

	@Override
	public String[] getSupportedTypeIdentifiers() 
	{
		String[] typeIdentifiers = new String[supportedAddons.size()];
		int i=0;
		for(SupportedAddon<?> addon : supportedAddons)
		{
			typeIdentifiers[i] = addon.typeIdentifier;
			i++;
		}
		return typeIdentifiers;
	}

	@Override
	public boolean isSupportingTypeIdentifier(String typeIdentifier) 
	{
		for(SupportedAddon<?> addon : supportedAddons)
		{
			if(addon.typeIdentifier.equals(typeIdentifier))
				return true;
		}
		return false;
	}

	@Override
	public ComponentAddonUI<? extends MeasurementConfiguration> createMeasurementUI(
			String typeIdentifier, YouScopeClient client, YouScopeServer server)
			throws AddonException {
		if(typeIdentifier == null)
			throw new AddonException("No type identifier provided.");
		for(SupportedAddon<?> addon : supportedAddons)
		{
			if(!addon.typeIdentifier.equals(typeIdentifier))
				continue;
			return addon.createConfigurationAddon(client, server);
		}
		throw new AddonException("Configuration type identifier " + typeIdentifier + " not supported by this factory.");
	}
	@Override
	public ComponentMetadata<? extends MeasurementConfiguration> getComponentMetadata(String typeIdentifier) throws AddonException 
	{
		for(SupportedAddon<?> addon : supportedAddons)
		{
			if(addon.typeIdentifier.equals(typeIdentifier))
			{
				return addon.getConfigurationMetadata();
			}
		}
		throw new AddonException("Configuration type identifier " + typeIdentifier + " not supported by this factory.");
	}

	@Override
	public void initializeMeasurement(Measurement measurement, MeasurementConfiguration configuration,
			ConstructionContext constructionContext) throws ConfigurationException, AddonException {
		if(configuration == null)
			throw new AddonException("Provided configuration is null.");
		for(SupportedAddon<?> addon : supportedAddons)
		{
			if(!addon.isTypeIdentifier(configuration.getTypeIdentifier()))
				continue;
			addon.initializeMeasurement(measurement, configuration, constructionContext);
			return;
		}
		throw new AddonException("Creation of components with type identifier " + configuration.getTypeIdentifier() + " not supported by this construction measurement factory.");
	}
}
