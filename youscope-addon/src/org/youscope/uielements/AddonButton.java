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
import javax.swing.JButton;
import javax.swing.JToolTip;
import javax.swing.SwingConstants;

import org.youscope.addon.AddonMetadata;
import org.youscope.common.util.TextTools;

/**
 * Button representing an addon, e.g. a button to show the user interface of an addon. 
 * The text, tooltip and icon is automatically initialized to the information provided in the addon metadata.
 * @author Moritz Lang
 * @param <T> The type of metadata this button accepts.
 *
 */
public class AddonButton<T extends AddonMetadata> extends JButton
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 7754882737215008622L;
	private final T addonMetadata;
	/**
	 * Constructor.
	 * @param addonMetadata The metadata of the addon this button represents.
	 * @param defaultIcon A default icon, if the addon does not provide an own one.
	 */
	public AddonButton(T addonMetadata, Icon defaultIcon)
	{
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
		setVerticalTextPosition(SwingConstants.BOTTOM);
		setHorizontalTextPosition(SwingConstants.CENTER);
		
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
		return new Point(0, getHeight());
	}
	/**
	 * Returns the type identifier of the addon this button represents.
	 * @return type identifier of button.
	 */
	public String getAddonTypeIdentifier()
	{
		return addonMetadata.getTypeIdentifier();
	}
	
	/**
	 * Returns the metadata of the addon this button represents.
	 * @return metadata of addon.
	 */
	public T getAddonMetadata()
	{
		return addonMetadata;
	}
	
}
