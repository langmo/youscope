package org.youscope.plugin.darkskin;

import java.awt.Color;
import java.util.Properties;

import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.youscope.addon.AddonException;
import org.youscope.addon.AddonMetadata;
import org.youscope.addon.AddonMetadataAdapter;
import org.youscope.addon.skin.Skin;
import org.youscope.client.uielements.QuickLogger;
import org.youscope.client.uielements.plaf.BasicQuickLoggerUI;
import org.youscope.client.uielements.plaf.ImageDesktopPaneUI;
import org.youscope.uielements.ImageLoadingTools;

class DarkSkin implements Skin {

	public static final String TYPE_IDENTIFIER = "YouScope.Skins.Dark";
	
	@Override
	public AddonMetadata getMetadata() {
		return createMetadata();
	}
	
	static AddonMetadata createMetadata()
	{
		return new AddonMetadataAdapter(TYPE_IDENTIFIER, 
				"Dark skin", 
				new String[]{"Skins"}, "icons/system-monitor.png");
	}

	@Override
	public void applySkin() throws AddonException 
	{
		UIManager.getDefaults().clear();
		final Properties props = new Properties();
        props.put("logoString", "");
        props.put("textShadow", "off");
        props.put("textShadow", "off");

        final String fontSpecs = "SansSerif 12";
        props.setProperty("controlTextFont", fontSpecs);
        props.setProperty("systemTextFont", fontSpecs);
        props.setProperty("userTextFont", fontSpecs);
        props.setProperty("menuTextFont", fontSpecs);
        props.setProperty("windowTitleFont", fontSpecs);
        props.setProperty("subTextFont", fontSpecs);

        props.put("backgroundPattern", "off");
        props.put("buttonColorLight", "0 0 0");
        props.put("buttonColorDark", "0 0 0");

        props.put("windowTitleColorLight", "40 40 40");
        props.put("windowTitleColorDark", "40 40 40");
        props.put("windowInactiveTitleColorLight", "40 40 40");
        props.put("windowInactiveTitleColorDark", "40 40 40");
        props.put("controlColorLight", "0 0 0");
        props.put("controlColorDark", "0 0 0");
        com.jtattoo.plaf.hifi.HiFiLookAndFeel.setCurrentTheme(props);
        
        UIManager.getDefaults().put("DesktopPaneUI", ImageDesktopPaneUI.class.getName());
        UIManager.getDefaults().put(ImageDesktopPaneUI.PROPERTY_BACKGROUND_COLOR, new ColorUIResource(Color.BLACK));
        UIManager.getDefaults().put(ImageDesktopPaneUI.PROPERTY_BACKGROUND_ICON, ImageLoadingTools.getResourceIcon("org/youscope/plugin/darkskin/images/background-logo.png", "Logo"));
        
        UIManager.getDefaults().put(QuickLogger.UI_CLASS_ID, BasicQuickLoggerUI.class.getName());
        UIManager.getDefaults().put(BasicQuickLoggerUI.PROPERTY_BACKGROUND, new ColorUIResource(Color.BLACK));
        UIManager.getDefaults().put(BasicQuickLoggerUI.PROPERTY_DATE_FOREGROUND, new ColorUIResource(new Color(0.7F, 0.7F, 0.7F)));
        UIManager.getDefaults().put(BasicQuickLoggerUI.PROPERTY_MESSAGE_FOREGROUND, new ColorUIResource(Color.WHITE));
        
        // Set default HTML style sheet
        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("p {color:#ffffff;font-family:sans-serif;font-size:12pt;margin-top:4px;margin-bottom:0px}");
        
        UIManager.getDefaults().put("EditorPane.foreground", new ColorUIResource(Color.WHITE));
        UIManager.getDefaults().put("EditorPane.inactiveForeground", new ColorUIResource(Color.WHITE));
        
        try {
           //Thread.currentThread().setContextClassLoader(DarkSkin.class.getClassLoader());
            UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
		} catch (Exception e) {
			throw new AddonException("Could not set look and feel to system look and feel.", e);
		}
	}

}
