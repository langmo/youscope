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

import java.awt.Component;
import java.lang.reflect.Constructor;
import java.util.ArrayList;


import org.youscope.addon.AddonException;
import org.youscope.addon.AddonUIAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * An adapter class to simplify UI addon development.
 * @author mlang
 * @param <C> 
 *
 */
public abstract class ComponentAddonUIAdapter<C extends Configuration> extends AddonUIAdapter<ComponentMetadata<C>> implements ComponentAddonUI<C> 
{
	private static final String DEFAULT_COMMIT_BUTTON_LABEL = "Commit";
	private ArrayList<ComponentAddonUIListener<? super C>> configurationListeners = new ArrayList<ComponentAddonUIListener<? super C>>();
	private C configuration = null;

	/**
	 * Constructor.
	 * @param metadata The metadata of the addon.
	 * @param client The YouScope client.
	 * @param server The YouScope server.
	 * @throws AddonException
	 */
	public ComponentAddonUIAdapter(final ComponentMetadata<C> metadata,  final YouScopeClient client, final YouScopeServer server) throws AddonException 
	{
		super(metadata,  client, server);
		setCloseButtonLabel(DEFAULT_COMMIT_BUTTON_LABEL);
		setShowCloseButton(true);
	}
	
	/**
	 * Returns the configuration class.
	 * @return Configuration class.
	 */
	protected Class<C> getConfigurationClass()
	{
		return getAddonMetadata().getConfigurationClass();
	}
	
	@Override
	public void addUIListener(ComponentAddonUIListener<? super C> listener) 
	{
		synchronized(configurationListeners)
		{
			configurationListeners.add(listener);
		}
	}

	@Override
	public void removeUIListener(ComponentAddonUIListener<? super C> listener) 
	{
		synchronized(configurationListeners)
		{
			configurationListeners.remove(listener);
		}
	}
	
	@Override
	protected final Component createUI() throws AddonException
	{
		if(configuration == null)
		{
			// Create default configuration.
			Class<C> configurationClass = getAddonMetadata().getConfigurationClass();
			Constructor<C> constructor;
			try {
				constructor = configurationClass.getDeclaredConstructor();
			} catch (NoSuchMethodException e) {
				throw new AddonException("Configuration class " + configurationClass.getName() + " does not have a no-arguments constructor.", e);
			} catch (SecurityException e) {
				throw new AddonException("Could not get constructor of configuration class " + configurationClass.getName() + ".", e);
			}
			constructor.setAccessible(true);
			try {
				configuration = constructor.newInstance();
			} 
			catch (Exception e) 
			{
				throw new AddonException("Could not construct default configuration of configuration class " + configurationClass.getName() + ".", e);
			}
			initializeDefaultConfiguration(configuration);
		}
		return createUI(configuration);
	}
	
	/**
	 * This function is called, and only called, if {@link #toFrame()} or {@link #toPanel(YouScopeFrame)} is called without
	 * calling before {@link #setConfiguration(Configuration)}. The configuration should then be initialized to its default settings,
	 * which may depend on the state of the microscope, etc.
	 * @param configuration Configuration to set to initial values.
	 */
	protected abstract void initializeDefaultConfiguration(C configuration) throws AddonException;
	
	/**
	 * Return a Component (typically a JPanel) containing the UI elements of the addon. These elements will
	 * be automatically layouted to fit into a frame or a panel, depending on what is requested. Do not add any
	 * confirm button, but rather change the label of the button which is automatically added ({@link #setShowCloseButton(boolean)}).
	 * @param configuration The current configuration which should be loaded.
	 * @return Component containing the UI elements of this addon.
	 * 
	 */
	protected abstract Component createUI(C configuration) throws AddonException;
	
	/**
	 * Call this function to signal that configuration in the UI has finished.
	 * Function is automatically called when pressing commit button.
	 * If initialized in a frame, this will close the frame.
	 * Only call after UI became visible.
	 */
	@Override
	protected void closeAddon()
	{
		C configuration = getConfiguration();
		try 
		{
			configuration.checkConfiguration();
		} 
		catch(ConfigurationException e)
		{
			YouScopeFrame errorFrame = ComponentAddonTools.displayConfigurationInvalid(e, getClient());
			getContainingFrame().addModalChildFrame(errorFrame);
			errorFrame.setVisible(true);
			return;
		}
		
		synchronized(configurationListeners)
		{
			for(ComponentAddonUIListener<? super C> configurationListener : configurationListeners)
			{
				configurationListener.configurationFinished(configuration);
			}
		}
		super.closeAddon();
	}
	
	@Override
	public void setConfiguration(Configuration configuration)
			throws AddonException, ConfigurationException 
	{
		String typeIdentifier = getAddonMetadata().getTypeIdentifier();
		Class<C> configurationClass = getAddonMetadata().getConfigurationClass();
		if(configuration == null)
			throw new AddonException("Configuration which should be loaded is null.");
		if(isInitialized())
			throw new AddonException("Configuration can only be set before toXXXFrame() or toPanel() is called.");
		if(!typeIdentifier.equals(configuration.getTypeIdentifier()))
			throw new AddonException("Provided configuration has type identifier " + configuration.getTypeIdentifier()+", however, type identifier "+typeIdentifier+" is required.");
		if(!configurationClass.isInstance(configuration))
			throw new AddonException("Configuration type identifier " + configuration.getTypeIdentifier()+" is valid for this configuration addon, however, the class of the configuration " + configuration.getClass().getName() + " is not a subclass of " + configurationClass.getName()+".");
		this.configuration = configurationClass.cast(configuration);
	}

	/**
	 * Is called when the current state of all forms etc. should be saved into the configuration.
	 * Guaranteed to be called only after createUI() has been called.
	 * @param configuration The configuration in which changes should be saved. 
	 */
	protected abstract void commitChanges(C configuration);
	
	@Override
	public C getConfiguration()
	{
		if(isInitialized())
			commitChanges(configuration);
		return configuration;
	}
}
