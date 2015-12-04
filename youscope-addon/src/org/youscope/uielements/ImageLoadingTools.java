/**
 * 
 */
package org.youscope.uielements;

import java.net.URL;

import javax.swing.ImageIcon;

/**
 * Set of tools to simplify image loading.
 * @author langmo
 *
 */
public class ImageLoadingTools
{
	/**
	 * Returns the icon from the given path, initialized with the given description.
	 * @param iconPath The path of the icon.
	 * @param description The description of the icon.
	 * @return The icon or NULL, if it could not be found or loaded.
	 */
	public static ImageIcon getResourceIcon(String iconPath, String description)
	{
		URL iconURL = ImageLoadingTools.class.getClassLoader().getResource(iconPath);
		if (iconURL != null)
			return new ImageIcon(iconURL, description);
		return null;
	}
}
