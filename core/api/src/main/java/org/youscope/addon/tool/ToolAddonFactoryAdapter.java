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
package org.youscope.addon.tool;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import org.youscope.addon.AddonException;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * Adapter simplifying tool addon creation. 
 * @author Moritz Lang
 *
 */
public class ToolAddonFactoryAdapter implements ToolAddonFactory
{
	private final ArrayList<SupportedAddon> supportedAddons = new ArrayList<SupportedAddon>(1);
	private class SupportedAddon
	{
		public final Class<? extends ToolAddonUI> toolUIClass;
		public final ToolMetadata metadata; 
		public final String typeIdentifier;
		SupportedAddon(Class<? extends ToolAddonUI> toolUIClass, ToolMetadata metadata)
		{
			if(toolUIClass == null || metadata == null)
				throw new NullPointerException();
			this.toolUIClass = toolUIClass;
			this.typeIdentifier = metadata.getTypeIdentifier();
			this.metadata = metadata;
		}
		ToolAddonUI createAddonUI(YouScopeClient client, YouScopeServer server) throws AddonException
		{
			Constructor<? extends ToolAddonUI> constructor;
			try {
				constructor = toolUIClass.getDeclaredConstructor(YouScopeClient.class, YouScopeServer.class);
			} catch (NoSuchMethodException e) {
				throw new AddonException("Could not create tool addon UI for "+typeIdentifier+" since class "+toolUIClass.getName()+" does not support appropriate constructors. Argument list of constructor must be (YouScopeClient, YouScopeServer).", e);
			} 
			catch (SecurityException e) {
				throw new AddonException("Could not create tool addon UI due to security exception.", e);
			}
			try 
			{
				constructor.setAccessible(true);
				return constructor.newInstance(client, server);
			} 
			catch (Exception e) 
			{
				throw new AddonException("Could not create tool addon UI.", e);
			}
		}
		
		ToolMetadata getMetadata()
		{
			return metadata;
		}
	}
	
	/**
	 * Constructor. When invoked, the factory exposes the given tool addon.
	 * @param toolAddonUIClass The class of the UI exposed to the user.
	 * @param metadata Metadata about this tool.
	 */
	public ToolAddonFactoryAdapter(Class<? extends ToolAddonUI> toolAddonUIClass, ToolMetadata metadata)
	{
		addAddon(toolAddonUIClass, metadata);
	}
	
	/**
	 * Constructor. Factory does no (yet) expose any addon. Use {@link #addAddon} to expose addons.
	 */
	public ToolAddonFactoryAdapter() 
	{
		// do nothing.
	}
	/**
	 * Adds an addon.  When invoked, the factory exposes the given tool addon.
	 * @param toolAddonUIClass The class of the UI exposed to the user.
	 * @param metadata Metadata about this tool.
	 */
	public void addAddon(Class<? extends ToolAddonUI> toolAddonUIClass, ToolMetadata metadata)
	{
		supportedAddons.add(new SupportedAddon(toolAddonUIClass, metadata));
	}

	@Override
	public String[] getSupportedTypeIdentifiers() 
	{
		String[] typeIdentifiers = new String[supportedAddons.size()];
		int i=0;
		for(SupportedAddon addon : supportedAddons)
		{
			typeIdentifiers[i] = addon.typeIdentifier;
			i++;
		}
		return typeIdentifiers;
	}

	@Override
	public boolean isSupportingTypeIdentifier(String typeIdentifier) 
	{
		for(SupportedAddon addon : supportedAddons)
		{
			if(addon.typeIdentifier.equals(typeIdentifier))
				return true;
		}
		return false;
	}

	@Override
	public ToolAddonUI createToolUI(
			String typeIdentifier, YouScopeClient client, YouScopeServer server)
			throws AddonException 
	{
		if(typeIdentifier == null)
			throw new AddonException("No type identifier provided.");
		for(SupportedAddon addon : supportedAddons)
		{
			if(!addon.typeIdentifier.equals(typeIdentifier))
				continue;
			return addon.createAddonUI(client, server);
		}
		throw new AddonException("Tool with type identifier " + typeIdentifier + " not supported by this factory.");
	}
	@Override
	public ToolMetadata getToolMetadata(String typeIdentifier) throws AddonException 
	{
		for(SupportedAddon addon : supportedAddons)
		{
			if(addon.typeIdentifier.equals(typeIdentifier))
			{
				return addon.getMetadata();
			}
		}
		throw new AddonException("Tool with type identifier " + typeIdentifier + " not supported by this factory.");
	}
}
