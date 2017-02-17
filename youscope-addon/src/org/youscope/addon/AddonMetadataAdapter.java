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
package org.youscope.addon;

import javax.swing.Icon;

import org.youscope.uielements.ImageLoadingTools;

/**
 * Adapter to simplify {@link AddonMetadata} construction.
 * @author Moritz Lang
 *
 */
public class AddonMetadataAdapter implements AddonMetadata
{
	private final String typeIdentifier;
	private final String name;
	private final String description;
	private final String[] classification;
	private final Icon icon;
	
	/**
	 * Constructor. Sets icon to default icon.
	 * @param typeIdentifier Unique type identifier of the addon. Must not be null.
	 * @param name Human readable name of the addon. Must not be null.
	 * @param description Description of the addon. Set to null to not provide any description.
	 * @param classification Classification of the addon. Set to null for unclassified.
	 */
	public AddonMetadataAdapter(String typeIdentifier, String name, String[] classification, String description)
	{
		this(typeIdentifier, name, classification, description, (Icon)null);
	}
	
	/**
	 * Constructor. 
	 * @param typeIdentifier Unique type identifier of the addon. Must not be null.
	 * @param name Human readable name of the addon. Must not be null.
	 * @param classification Classification of the addon. Set to null for unclassified.
	 * @param description Description of the addon. Set to null to not provide any description.
	 * @param icon Icon of the addon. Set to null for default icon.
	 */
	public AddonMetadataAdapter(String typeIdentifier, String name, String[] classification, String description, Icon icon)
	{
		if(typeIdentifier == null || name == null)
			throw new NullPointerException();
		
		this.typeIdentifier = typeIdentifier;
		this.name = name;
		if(classification != null)
			this.classification = classification;
		else
			this.classification = new String[0];
		this.icon = icon;
		
		this.description = description;
	}
	
	/**
	 * Constructor.
	 * @param typeIdentifier Unique type identifier of the addon. Must not be null.
	 * @param name Human readable name of the addon. Must not be null.
	 * @param classification Classification of the addon. Set to null for unclassified.
	 * @param description Description of the addon. Set to null to not provide any description.
	 * @param iconPath Path of icon of the addon. Set to null for default icon.
	 */
	public AddonMetadataAdapter(String typeIdentifier, String name, String[] classification, String description, String iconPath)
	{
		this(typeIdentifier, name, classification, description, iconPath == null ? null : ImageLoadingTools.getResourceIcon(iconPath, name));
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getTypeIdentifier()
	{
		return typeIdentifier;
	}

	@Override
	public Icon getIcon()
	{
		return icon;
	}

	@Override
	public String[] getClassification()
	{
		return classification;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
