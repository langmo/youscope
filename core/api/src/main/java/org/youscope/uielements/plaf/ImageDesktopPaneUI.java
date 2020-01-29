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
package org.youscope.uielements.plaf;

import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicDesktopPaneUI;

/**
 * UI Delegate for {@link JDesktopPane} displaying a centered background image.
 * @author mlang
 *
 */
public class ImageDesktopPaneUI extends BasicDesktopPaneUI
{
	/**
	 * Image or icon to display in the background. Use {@link ImageIcon} to display an image.
	 */
	public static final String PROPERTY_BACKGROUND_ICON = "ImageDesktopPane.backgroundIcon";
	/**
	 * Background color.
	 */
	public static final String PROPERTY_BACKGROUND_COLOR = "ImageDesktopPane.backgroundColor";
	private final Icon icon;
	ImageDesktopPaneUI()
	{
		icon = UIManager.getIcon(PROPERTY_BACKGROUND_ICON);
	}
	@Override
    public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        if(icon != null)
        	icon.paintIcon(c, g,(c.getWidth() - icon.getIconWidth())/2, (c.getHeight() - icon.getIconHeight())/2);
    }
	
	public static ComponentUI createUI(JComponent c) {
		ImageDesktopPaneUI ui = new ImageDesktopPaneUI();   	
    	return ui;
    }
	
	@Override
	protected void installDefaults()
	{
	      desktop.setBackground(UIManager.getColor(PROPERTY_BACKGROUND_COLOR));
	}
}
