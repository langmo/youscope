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
package org.youscope.uielements;

import java.awt.Image;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Set of tools to simplify image loading.
 * @author langmo
 *
 */
public class ImageLoadingTools
{
	/**
	 * Icon to display a folder.
	 */
	public static final Icon FOLDER_ICON = ImageLoadingTools.getResourceIcon("icons/folder-horizontal-open.png", "Folder");
	/**
	 * Default icon for a measurement which does not provide its own icon.
	 */
	public static final Icon DEFAULT_MEASUREMENT_ICON = ImageLoadingTools.getResourceIcon("icons/receipt--plus.png", "Create Measurement");
	/**
	 * Default icon for a tool which does not provide its own icon.
	 */
	public static final Icon DEFAULT_TOOL_ICON = ImageLoadingTools.getResourceIcon("icons/application-form.png", "Open Tool");
	/**
	 * Default icon for a script which does not provide its own icon.
	 */
	public static final Icon DEFAULT_SCRIPT_ICON = ImageLoadingTools.getResourceIcon("icons/script--arrow.png", "Run Script");
	/**
	 * Default icon for a measurement component which does not provide its own icon, and for which we do not know/care if it is a job, measurement, or whatever else.
	 */
	public static final Icon DEFAULT_COMPONENT_ICON = ImageLoadingTools.getResourceIcon("icons/block--plus.png", "Open Component");
	
	/**
	 * Returns the icon from the given path, initialized with the given description.
	 * @param iconPath The path of the icon.
	 * @param description The description of the icon.
	 * @return The icon or NULL, if it could not be found or loaded.
	 */
	public static Icon getResourceIcon(String iconPath, String description)
	{
		URL iconURL = ImageLoadingTools.class.getClassLoader().getResource(iconPath);
		if (iconURL != null)
			return new ImageIcon(iconURL, description);
		return null;
	}
	
	/**
	 * Returns the image from the given path, initialized with the given description.
	 * @param imagePath The path of the image.
	 * @param description The description of the image.
	 * @return The image or NULL, if it could not be found or loaded.
	 */
	public static Image getResourceImage(String imagePath, String description)
	{
		Icon icon = getResourceIcon(imagePath, description);
		return icon == null ? null : ((ImageIcon)icon).getImage();
	}
}
