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
package org.youscope.plugin.systemskin;

import java.awt.Color;

import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.youscope.addon.AddonException;
import org.youscope.addon.AddonMetadata;
import org.youscope.addon.AddonMetadataAdapter;
import org.youscope.addon.skin.Skin;
import org.youscope.uielements.ImageLoadingTools;
import org.youscope.uielements.QuickLogger;
import org.youscope.uielements.plaf.BasicQuickLoggerUI;
import org.youscope.uielements.plaf.ImageDesktopPaneUI;

class SystemSkin implements Skin {

	public static final String TYPE_IDENTIFIER = "YouScope.Skin.System";
	
	@Override
	public AddonMetadata getMetadata() {
		return createMetadata();
	}
	
	static AddonMetadata createMetadata()
	{
		return new AddonMetadataAdapter(TYPE_IDENTIFIER, 
				"System skin", 
				new String[]{"Skins"}, 
				"Skin resembling the appearance of the operating system.",
				"icons/application-blog.png");
	}

	@Override
	public void applySkin() throws AddonException 
	{
		Thread.currentThread().setContextClassLoader(SystemSkin.class.getClassLoader());
		UIManager.getDefaults().clear();
		
		UIManager.getDefaults().put(QuickLogger.UI_CLASS_ID, BasicQuickLoggerUI.class.getName());
        UIManager.getDefaults().put(BasicQuickLoggerUI.PROPERTY_BACKGROUND, new ColorUIResource(Color.WHITE));
        UIManager.getDefaults().put(BasicQuickLoggerUI.PROPERTY_DATE_FOREGROUND, new ColorUIResource(new Color(0.4F, 0.4F, 0.4F)));
        UIManager.getDefaults().put(BasicQuickLoggerUI.PROPERTY_MESSAGE_FOREGROUND, new ColorUIResource(Color.BLACK));
        
        UIManager.getDefaults().put("ToolBar.background", new ColorUIResource(Color.WHITE)); 
        UIManager.getDefaults().put("DesktopPaneUI", ImageDesktopPaneUI.class.getName());
        UIManager.getDefaults().put(ImageDesktopPaneUI.PROPERTY_BACKGROUND_COLOR, new ColorUIResource(new Color(210, 210, 210)));
        UIManager.getDefaults().put(ImageDesktopPaneUI.PROPERTY_BACKGROUND_ICON, ImageLoadingTools.getResourceIcon("org/youscope/plugin/systemskin/images/background-logo.png", "Logo"));
		
     // Set default HTML style sheet
        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("p {color:#000000;font-family:sans-serif;font-size:12pt;margin-top:0px;margin-bottom:4px}");
        styleSheet.addRule("a {color:#000000;font-family:sans-serif;font-size:12pt;margin-top:0px;margin-bottom:4px}");
        styleSheet.addRule("li {color:#000000;font-family:sans-serif;font-size:12pt;margin-top:0px;margin-bottom:4px}");
        styleSheet.addRule("h1 {color:#000000;font-weight: bold;font-family:sans-serif;font-size:14pt;margin-top:0px;margin-bottom:4px}");
        styleSheet.addRule("h2 {color:#000000;font-weight: bold;font-family:sans-serif;font-size:12pt;margin-top:0px;margin-bottom:4px}");
        
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			throw new AddonException("Could not set look and feel to system look and feel.", e);
		}
		
	}

}
