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

import javax.swing.Icon;

import org.youscope.addon.AddonMetadataAdapter;

/**
 * Adapter to simplify {@link ToolMetadata} construction.
 * @author Moritz Lang
 *
 */
public class ToolMetadataAdapter extends AddonMetadataAdapter implements ToolMetadata
{	
	
	/**
	 * Constructor. Sets the icon to the default icon.
	 * @param typeIdentifier Unique type identifier of the tool type. Must not be null.
	 * @param name Human readable name of the tool. Must not be null.
	 * @param classification Classification of the tool. Set to null for unclassified.
	 * @param description Description of the addon. Set to null to not provide any description.
	 */
	public ToolMetadataAdapter(String typeIdentifier, String name, String[] classification, String description)
	{
		super(typeIdentifier, name, classification, description);
	}
	/**
	 * Constructor. 
	 * @param typeIdentifier Unique type identifier of the tool type. Must not be null.
	 * @param name Human readable name of the tool. Must not be null.
	 * @param classification Classification of the tool. Set to null for unclassified.
	 * @param description Description of the addon. Set to null to not provide any description.
	 * @param icon Icon of the tool. Set to null for default icon.
	 */
	public ToolMetadataAdapter(String typeIdentifier, String name, String[] classification, String description, Icon icon)
	{
		super(typeIdentifier, name, classification, description, icon);
	}
	
	/**
	 * Constructor.
	 * @param typeIdentifier Unique type identifier of the tool type. Must not be null.
	 * @param name Human readable name of the tool. Must not be null.
	 * @param classification Classification of the tool. Set to null for unclassified.
	 * @param description Description of the addon. Set to null to not provide any description.
	 * @param iconPath Path of icon of the tool. Set to null for default icon.
	 */
	public ToolMetadataAdapter(String typeIdentifier, String name, String[] classification, String description, String iconPath) 
	{
		super(typeIdentifier, name, classification, description, iconPath);
	}
}
