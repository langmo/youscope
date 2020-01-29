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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import org.youscope.addon.AddonException;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.Component;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.serverinterfaces.ConstructionContext;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * Adapter simplifying addon creation. 
 * @author Moritz Lang
 *
 */
public class ComponentAddonFactoryAdapter implements ComponentAddonFactory
{
	private final ArrayList<SupportedAddon<?>> supportedAddons = new ArrayList<SupportedAddon<?>>(1);
	private class SupportedAddon<C extends Configuration>
	{
		public final Class<? extends ComponentAddonUI<C>> configurationAddonClass;
		public final ComponentMetadata<C> metadata; 
		public final Class<C> configurationClass;
		public final String typeIdentifier;
		public final Class<? extends Component> componentClass;
		public final CustomAddonCreator<C,?> customCreator;
		SupportedAddon(Class<? extends ComponentAddonUI<C>> addonClass, Class<? extends Component> componentClass, ComponentMetadata<C> metadata)
		{
			if(addonClass == null || metadata == null || componentClass == null)
				throw new NullPointerException();
			this.configurationAddonClass = addonClass;
			this.configurationClass = metadata.getConfigurationClass();
			this.typeIdentifier = metadata.getTypeIdentifier();
			this.metadata = metadata;
			this.componentClass = componentClass;
			this.customCreator = null;
		}
		SupportedAddon(Class<? extends ComponentAddonUI<C>> addonClass, CustomAddonCreator<C,?> customCreator, ComponentMetadata<C> metadata)
		{
			if(addonClass == null || metadata == null||customCreator==null)
				throw new NullPointerException();
			this.configurationAddonClass = addonClass;
			this.configurationClass = metadata.getConfigurationClass();
			this.typeIdentifier = metadata.getTypeIdentifier();
			this.metadata = metadata;
			this.componentClass = null;
			this.customCreator = customCreator;
		}
		SupportedAddon(String typeIdentifier, Class<C> configurationClass, Class<? extends Component> componentClass)
		{
			if(configurationClass == null || typeIdentifier == null || componentClass == null)
				throw new NullPointerException();
			this.configurationAddonClass = null;
			this.configurationClass = configurationClass;
			this.typeIdentifier = typeIdentifier;
			this.metadata = new GenericComponentMetadata<C>(typeIdentifier, configurationClass, componentClass);
			this.componentClass = componentClass;
			this.customCreator = null;
		}
		SupportedAddon(String typeIdentifier, Class<C> configurationClass, CustomAddonCreator<C,?> customCreator)
		{
			if(typeIdentifier == null || configurationClass == null || customCreator == null)
				throw new NullPointerException();
			this.configurationAddonClass = null;
			this.configurationClass = configurationClass;
			this.typeIdentifier = typeIdentifier;
			this.metadata = new GenericComponentMetadata<C>(typeIdentifier, configurationClass, customCreator.getComponentInterface());
			this.componentClass = null;
			this.customCreator = customCreator;
		}
		ComponentAddonUI<C> createConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
		{
			if(configurationAddonClass != null)
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
			else if(configurationClass!=null)
			{
				return new GenericComponentAddonUI<C>(typeIdentifier, configurationClass, componentClass, client, server);
			}
			else
				throw new AddonException("Configuration addon added invalidly to factory.");
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
		
		Component createComponent(PositionInformation positionInformation, Configuration rawConfiguration, ConstructionContext constructionContext) throws ConfigurationException, AddonException
		{
			if(!configurationClass.isInstance(rawConfiguration))
				throw new AddonException("Configuration with type identifier " + rawConfiguration.getTypeIdentifier() + " has class " + rawConfiguration.getClass().getName()+", which is not of class " + configurationClass.getName() + " which is expected.");
			C configuration = configurationClass.cast(rawConfiguration);
			if(customCreator != null)
			{
				return createComponentCustom(positionInformation, configuration, constructionContext);
			}
			return createComponentAutomatic(positionInformation, configuration, constructionContext);
		}
		
		Component createComponentCustom(PositionInformation positionInformation, C configuration, ConstructionContext constructionContext) throws ConfigurationException, AddonException 
		{
			return customCreator.createCustom(positionInformation, configuration, constructionContext);
		}
		
		Component createComponentAutomatic(PositionInformation positionInformation, C configuration, ConstructionContext constructionContext) throws ConfigurationException, AddonException 
		{
			Constructor<?>[] declaredConstructors = componentClass.getDeclaredConstructors();
			try
			{
	outer:		for(Constructor<?> constructor : declaredConstructors)
				{
					
					Class<?>[] expectedTypes = constructor.getParameterTypes();
					Object[] parameters = new Object[expectedTypes.length];
					for(int i=0; i<expectedTypes.length; i++)
					{
						if(expectedTypes[i].isAssignableFrom(PositionInformation.class))
						{
							parameters[i] = positionInformation;
						}
						else if(expectedTypes[i].isAssignableFrom(configurationClass))
						{
							parameters[i] = configuration;
						}
						else if(expectedTypes[i].isAssignableFrom(ConstructionContext.class))
						{
							parameters[i] = constructionContext;
						}
						else
							continue outer;
					}
					// If we are here, the constructor fits
					constructor.setAccessible(true);
					Component component = componentClass.cast(constructor.newInstance(parameters));
					try {
						component.addMessageListener(constructionContext.getLogger());
					} catch (@SuppressWarnings("unused") RemoteException e) {
						// do nothing, should not happen since local.
					}
					return component;
				}
			}
			catch (SecurityException e) {
				throw new AddonException("Could not create component due to security exception.", e);
			}
			catch (InvocationTargetException e) {
				Throwable cause = e.getCause();
				if(cause != null && cause instanceof ConfigurationException)
					throw (ConfigurationException)cause;
				else if(cause != null && cause instanceof AddonException)
					throw (AddonException) cause;
				else
					throw new AddonException("Could not create addon", e);
			} catch (InstantiationException e) {
				throw new AddonException("Could not create addon", e);
			} catch (IllegalAccessException e) {
				throw new AddonException("Could not create addon", e);
			} catch (IllegalArgumentException e) {
				throw new AddonException("Could not create addon", e);
			}
			
			// If we are here, there is no appropriate constructor.
			String declaredConstructorsString = "";
			for(Constructor<?> constructor : declaredConstructors)
			{
				declaredConstructorsString += componentClass.getSimpleName()+"(";
				Class<?>[] expectedTypes = constructor.getParameterTypes();
				for(int i=0; i<expectedTypes.length; i++)
				{
					if(i>0)
						declaredConstructorsString+=", ";
					declaredConstructorsString+=expectedTypes[i].getSimpleName();
				}
				declaredConstructorsString+=")\n";
			}
			
			throw new AddonException("Could not create component "+getTypeIdentifer()+" since Component class "+componentClass.getName()+" does not support appropriate constructors.\nComponent has the following constructors:\n"+declaredConstructorsString+"\nSee documentation of ConstructionAddonFactoryAdapter for more information.");
		}
		
	}
	@Override
	public Component createComponent(PositionInformation positionInformation, Configuration configuration, ConstructionContext constructionContext) throws ConfigurationException, AddonException 
	{
		if(configuration == null)
			throw new AddonException("Provided configuration is null.");
		for(SupportedAddon<?> addon : supportedAddons)
		{
			if(!addon.isTypeIdentifier(configuration.getTypeIdentifier()))
				continue;
			return addon.createComponent(positionInformation, configuration, constructionContext);	
		}
		throw new AddonException("Creation of components with type identifier " + configuration.getTypeIdentifier() + " not supported by this construction addon factory.");
	}
	/**
	 * Constructor. When invoked, the factory exposes the given addon.
	 * The class componentClass must have one of the following constructors:
	 * <ul>
	 * <li><code>Foo(PositionInformation positionInformation)<code></li>
	 * <li><code>Foo(PositionInformation positionInformation, C configuration)<code></li>
	 * <li><code>Foo(PositionInformation positionInformation, C configuration, ConstructionContext initializer)<code></li>
	 * </ul>
	 * In these constructors, C is identical with the class returned by <code>metadata.getConfigurationClass()</code>, respectively the generic constructor type.
	 * @param configurationAddonClass The UI exposed to the user.
	 * @param componentClass The component which can be created by the addon.
	 * @param metadata  Metadata about this addon.
	 */
	public <C extends Configuration> ComponentAddonFactoryAdapter(Class<? extends ComponentAddonUI<C>> configurationAddonClass, Class<? extends Component> componentClass, ComponentMetadata<C> metadata) 
	{
		addAddon(configurationAddonClass, componentClass, metadata);
	}
	/**
	 * Constructor. When invoked, the factory exposes the given addon.
	 * The class componentClass must have one of the following constructors:
	 * <ul>
	 * <li><code>Foo(PositionInformation positionInformation)<code></li>
	 * <li><code>Foo(PositionInformation positionInformation, C configuration)<code></li>
	 * <li><code>Foo(PositionInformation positionInformation, C configuration, ConstructionContext initializer)<code></li>
	 * </ul>
	 * In these constructors, C is identical with configurationClass, respectively the generic constructor type.
	 * @param typeIdentifier Type identifier of addon.
	 * @param configurationClass Configuration class of addon. Used in automatic UI construction.
	 * @param componentClass The component which can be created by the addon.
	 */
	public <C extends Configuration> ComponentAddonFactoryAdapter(String typeIdentifier, Class<C> configurationClass, Class<? extends Component> componentClass)
	{
		addAddon(typeIdentifier, configurationClass, componentClass);
	}
	/**
	 * Constructor. When invoked, the factory exposes the given addon.
	 * @param configurationAddonClass The UI exposed to the user.
	 * @param customCreator A custom creator to initialize the component given the configuration.
	 * @param metadata Metadata about this addon.
	 */
	public <C extends Configuration> ComponentAddonFactoryAdapter(Class<? extends ComponentAddonUI<C>> configurationAddonClass, CustomAddonCreator<C,?> customCreator, ComponentMetadata<C> metadata)
	{
		addAddon(configurationAddonClass, customCreator, metadata);
	}
	/**
	 * Constructor. When invoked, the factory exposes the given addon.
	 * @param typeIdentifier Type identifier of addon.
	 * @param configurationClass Configuration class of addon. Used in automatic UI construction.
	 * @param customCreator A custom creator to initialize the component given the configuration.
	 */
	public <C extends Configuration> ComponentAddonFactoryAdapter(String typeIdentifier, Class<C> configurationClass, CustomAddonCreator<C,?> customCreator)
	{
		addAddon(typeIdentifier,configurationClass,customCreator);
	}
	/**
	 * Constructor. Does no invoke any addon. Use <code>addAddon()</code> to expose addons later on.
	 */
	public ComponentAddonFactoryAdapter() 
	{
		// do nothing.
	}
	/**
	 * Adds an addon which does component creation on its own, as well as has an own UI.
	 * @param configurationAddonClass Class providing the UI elements.
	 * @param customCreator A custom creator to initialize the component given the configuration.
	 * @param metadata Metadata of the component.
	 */
	public <C extends Configuration> void addAddon(Class<? extends ComponentAddonUI<C>> configurationAddonClass, CustomAddonCreator<C,?> customCreator, ComponentMetadata<C> metadata)
	{
		supportedAddons.add(new SupportedAddon<C>(configurationAddonClass, customCreator, metadata));
	}
	/**
	 * Adds an addon with an own UI, but automatic component creation by appropriate constructors of the component.
	 * The class componentClass must have one of the following constructors:
	 * <ul>
	 * <li><code>Foo(PositionInformation positionInformation)<code></li>
	 * <li><code>Foo(PositionInformation positionInformation, C configuration)<code></li>
	 * <li><code>Foo(PositionInformation positionInformation, C configuration, ConstructionContext initializer)<code></li>
	 * </ul>
	 * In these constructors, C is identical with the class returned by <code>metadata.getConfigurationClass()</code>, respectively the generic constructor type.
	 * @param configurationAddonClass
	 * @param componentClass
	 * @param metadata
	 */
	public <C extends Configuration> void addAddon(Class<? extends ComponentAddonUI<C>> configurationAddonClass, Class<? extends Component> componentClass, ComponentMetadata<C> metadata)
	{
		supportedAddons.add(new SupportedAddon<C>(configurationAddonClass, componentClass, metadata));
	}
	/**
	 * Adds an addon with an automatically generated UI, as well as automatic component creation by appropriate constructors of the component.
	 * The class componentClass must have one of the following constructors:
	 * <ul>
	 * <li><code>Foo(PositionInformation positionInformation)<code></li>
	 * <li><code>Foo(PositionInformation positionInformation, C configuration)<code></li>
	 * <li><code>Foo(PositionInformation positionInformation, C configuration, ConstructionContext initializer)<code></li>
	 * </ul>
	 * In these constructors, C is identical with configurationClass, respectively the generic constructor type.
	 
	 * @param typeIdentifier
	 * @param configurationClass
	 * @param componentClass
	 */
	public <C extends Configuration> void addAddon(String typeIdentifier, Class<C> configurationClass, Class<? extends Component> componentClass)
	{
		supportedAddons.add(new SupportedAddon<C>(typeIdentifier, configurationClass, componentClass));
	}
	
	/**
	 * Adds an addon which does component creation on its own, but has generic UI.
	 * @param typeIdentifier
	 * @param configurationClass
	 * @param customCreator 
	 */
	public <C extends Configuration> void addAddon(String typeIdentifier, Class<C> configurationClass, CustomAddonCreator<C,?> customCreator)
	{
		supportedAddons.add(new SupportedAddon<C>(typeIdentifier, configurationClass, customCreator));
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
	public ComponentAddonUI<?> createComponentUI(
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
	public ComponentMetadata<?> getComponentMetadata(String typeIdentifier) throws AddonException 
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

}
