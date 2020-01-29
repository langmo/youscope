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
/**
 * 
 */
package org.youscope.plugin.standardsavesettings;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Enumeration of the different possibilities for standard folder structure types.
 * 
 * @author Moritz Lang
 * 
 */
@XStreamAlias("folder-structure-type")
public enum FolderStructureType
{
	/**
	 * A folder is created for every well, then every position and then every
	 * channel.
	 */
	SEPARATE_WELL_POSITION_AND_CHANNEL("Separate folder for each well, position, and channel."),
	/**
	 * A folder is created for every well and then every channel.
	 */
	SEPARATE_WELL_AND_CHANNEL("Separate folder for each well, and channel."),
	/**
	 * A folder is created for every well and then every position.
	 */
	SEPARATE_WELL_AND_POSITION("Separate folder for each well, and position."),
	/**
	 * All images are saved in one folder.
	 */
	ALL_IN_ONE_FOLDER("Store all images in the same folder.");

	private final String	description;

	FolderStructureType(String description)
	{
		this.description = description;
	}

	@Override
	public String toString()
	{
		return description;
	}
}
