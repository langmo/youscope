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
package org.youscope.uielements;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JToolTip;

import org.youscope.addon.AddonMetadata;
import org.youscope.common.util.TextTools;

/**
 * Menu Item representing an addon, e.g. a menu item to show the user interface of an addon. 
 * The text, tooltip and icon is automatically initialized to the information provided in the addon metadata.
 * @author Moritz Lang
 * @param <T> The type of metadata this menu item accepts.
 *
 */
public class AddonMenuItem<T extends AddonMetadata> extends JMenuItem
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -9156398862335295519L;
	private final T addonMetadata;
	/**
	 * Constructor.
	 * @param addonMetadata The metadata of the addon this button represents.
	 * @param defaultIcon A default icon, if the addon does not provide an own one.
	 */
	public AddonMenuItem(T addonMetadata, Icon defaultIcon) {
		super(TextTools.capitalize(addonMetadata.getName()));
		this.addonMetadata = addonMetadata;
		String toolTipText = TextTools.capitalize(addonMetadata.getName());
		if(addonMetadata.getDescription() != null)
			toolTipText+="\n"+addonMetadata.getDescription();
		else
			toolTipText+="\n+No description available.";
		setToolTipText(toolTipText);
		
		Icon icon = addonMetadata.getIcon();
		if(icon == null)
			icon = defaultIcon;
		if(icon != null)
		{
			setIcon(icon);
			
		}
	}
	@Override
	public JToolTip createToolTip() 
	{
		AddonToolTip toolTip = new AddonToolTip(addonMetadata);
		toolTip.setComponent(this);
		return toolTip;
	}

	@Override
	public Point getToolTipLocation(MouseEvent arg0) {
		return new Point(getWidth(), 0);
	}
	/**
	 * Returns the type identifier of the addon this menu item represents.
	 * @return type identifier of menu item.
	 */
	public String getTypeIdentifier()
	{
		return addonMetadata.getTypeIdentifier();
	}
	
	/**
	 * Returns the metadata of the addon this menu item represents.
	 * @return metadata of addon.
	 */
	public T getAddonMetadata()
	{
		return addonMetadata;
	}
}
