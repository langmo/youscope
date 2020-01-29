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
package org.youscope.addon.skin;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import org.youscope.addon.AddonException;
import org.youscope.addon.AddonMetadata;

/**
 * Adapter simplifying skin addon creation. 
 * @author Moritz Lang
 *
 */
public class SkinFactoryAdapter implements SkinFactory
{
	private final ArrayList<SupportedAddon> supportedAddons = new ArrayList<SupportedAddon>(1);
	private class SupportedAddon
	{
		public final Class<? extends Skin> addonClass;
		public final AddonMetadata metadata; 
		public final String typeIdentifier;
		SupportedAddon(Class<? extends Skin> toolUIClass, AddonMetadata metadata)
		{
			if(toolUIClass == null || metadata == null)
				throw new NullPointerException();
			this.addonClass = toolUIClass;
			this.typeIdentifier = metadata.getTypeIdentifier();
			this.metadata = metadata;
		}
		Skin createAddon() throws AddonException
		{
			Constructor<? extends Skin> constructor;
			try {
				constructor = addonClass.getDeclaredConstructor();
			} catch (NoSuchMethodException e) {
				throw new AddonException("Could not create look-and-feel addon for "+typeIdentifier+" since class "+addonClass.getName()+" does not support appropriate constructors. Argument list of constructor must be empty.", e);
			} 
			catch (SecurityException e) {
				throw new AddonException("Could not create look-and-feel addon due to security exception.", e);
			}
			try 
			{
				constructor.setAccessible(true);
				return constructor.newInstance();
			} 
			catch (Exception e) 
			{
				throw new AddonException("Could not create look-and-feel addon.", e);
			}
		}
		
		AddonMetadata getMetadata()
		{
			return metadata;
		}
	}
	
	/**
	 * Constructor. When invoked, the factory exposes the given look-and-feel addon.
	 * @param addonClass The class of the look and feel exposed to the user.
	 * @param metadata Metadata about this look-and-feel.
	 */
	public SkinFactoryAdapter(Class<? extends Skin> addonClass, AddonMetadata metadata)
	{
		addAddon(addonClass, metadata);
	}
	
	/**
	 * Constructor. Factory does no (yet) expose any addon. Use {@link #addAddon} to expose addons.
	 */
	public SkinFactoryAdapter() 
	{
		// do nothing.
	}
	/**
	 * Adds an addon.  When invoked, the factory exposes the given look-and-feel addon.
	 * @param addonClass The class of the look-and-feel exposed to the user.
	 * @param metadata Metadata about this look-and-feel.
	 */
	public void addAddon(Class<? extends Skin> addonClass, AddonMetadata metadata)
	{
		supportedAddons.add(new SupportedAddon(addonClass, metadata));
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
	public Skin createSkin(String typeIdentifier) throws AddonException {
		if(typeIdentifier == null)
			throw new AddonException("No type identifier provided.");
		for(SupportedAddon addon : supportedAddons)
		{
			if(!addon.typeIdentifier.equals(typeIdentifier))
				continue;
			return addon.createAddon();
		}
		throw new AddonException("Look-and-feel with type identifier " + typeIdentifier + " not supported by this factory.");
	}
	@Override
	public AddonMetadata getMetadata(String typeIdentifier) throws AddonException 
	{
		for(SupportedAddon addon : supportedAddons)
		{
			if(addon.typeIdentifier.equals(typeIdentifier))
			{
				return addon.getMetadata();
			}
		}
		throw new AddonException("Look-and-feel with type identifier " + typeIdentifier + " not supported by this factory.");
	}
}
