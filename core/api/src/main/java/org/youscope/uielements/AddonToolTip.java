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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.swing.JToolTip;
import javax.swing.UIManager;

import org.youscope.addon.AddonMetadata;
import org.youscope.uielements.plaf.BasicAddonToolTipUI;

import org.youscope.uielements.plaf.AddonToolTipUI;

/**
 * A tooltip showing information on an addon, and providing an (optional) link to the component's wiki page.
 * @author Moritz Lang
 *
 */
public class AddonToolTip extends JToolTip
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 4554723202700892230L;
	/**
	 * UI Delegate name.
	 */
	public static final String UI_CLASS_ID = "SmartToolTipUI";
	private String wikiPage = null;
	private String name = null;
	private boolean temporary = true;
	
	/**
	 * Constructor. All fields are kept blank/unset.
	 */
	public AddonToolTip()
	{
		updateUI();
	}
	
	/**
	 * Copy constructor.
	 * @param addonToolTip Tool tip to copy.
	 */
	public AddonToolTip(AddonToolTip addonToolTip)
	{
		wikiPage = addonToolTip.wikiPage;
		name = addonToolTip.name;
		temporary = addonToolTip.temporary;
		setAddonDescription(addonToolTip.getAddonDescription());
		setComponent(addonToolTip.getComponent());
		updateUI();
	}
	
	/**
	 * Sets if this tool tip is temporary (default = true). A temporary tool tip is displayed simplified and intended to be used as the initial tool tip style. If the user moves
	 * the mouse into the tooltip, the extended information on the addon is shown.
	 * @param temporary True if in a temporary popup.
	 */
	public void setTemporary(boolean temporary)
	{
		boolean oldValue = this.temporary;
		this.temporary = temporary;
		this.firePropertyChange("temporary", oldValue, temporary);
	}
	
	/**
	 * Returns if this tool tip is temporary (default = true). A temporary tool tip is displayed simplified and intended to be used as the initial tool tip style. If the user moves
	 * the mouse into the tooltip, the extended information on the addon is shown.
	 * @return True if in a temporary popup.
	 */
	public boolean isTemporary()
	{
		return temporary;
	}
	
	/**
	 * Constructor. Initializes the tooltip with the information in the provided metadata. 
	 * @param addonMetadata The metadata of the addon this tool tip describes.
	 */
	public AddonToolTip(AddonMetadata addonMetadata)
	{
		initialize(addonMetadata);
		updateUI();		
	}
	/**
	 * Initializes the tooltip with the information in the provided metadata. Sets the wiki page name to the name of the addon, with special characters encoded by {@link URLEncoder#encode(String, String)}.
	 * @param addonMetadata The metadata of the addon this tool tip describes.
	 */
	public void initialize(AddonMetadata addonMetadata)
	{
		try {
			setAddonWikiPage(URLEncoder.encode(addonMetadata.getName(), "UTF-8"));
		} catch (@SuppressWarnings("unused") UnsupportedEncodingException e) {
			setAddonWikiPage(null);
		}
		setAddonName(addonMetadata.getName());
		String description = addonMetadata.getDescription();
		if(description == null)
			description = "No description available.";
		setAddonDescription(description);
	}
	
	/**
	 * Sets the description of the addon. Same as {@link #setTipText(String)}.
	 * @param description Description of the addon.
	 */
	public void setAddonDescription(String description)
	{
		setToolTipText(description);
	}
	
	/**
	 * Returns the description of the addon. Same as {@link #getToolTipText()}.
	 * @return Description of the addon.
	 */
	public String getAddonDescription()
	{
		return getToolTipText();
	}
	
	/**
	 * Sets the name of the wiki page. If set to null, link to the wiki page is not shown.
	 * @param wikiPage Name of the wiki page.
	 */
	public void setAddonWikiPage(String wikiPage)
	{
		String oldWikiPage = this.wikiPage;
		this.wikiPage = wikiPage;
		this.firePropertyChange("addonWikiPage", oldWikiPage, wikiPage);
	}
	/**
	 * Returns the name of the wiki page. If null, link to the wiki page is not shown.
	 * @return Name of the wiki page.
	 */
	public String getAddonWikiPage()
	{
		return wikiPage;
	}
	/**
	 * Sets the name of the addon, which is displayed as a header above the addon's description.
	 * @param name Name of the addon, or null to not display the header.
	 */
	public void setAddonName(String name)
	{
		String oldName = this.name;
		this.name = name;
		this.firePropertyChange("addonName", oldName, name);
	}
	/**
	 * Returns the name of the addon, which is displayed as a header above the addon's description.
	 * @return Name of the addon, or null if no header is displayed.
	 */
	public String getAddonName()
	{
		return name;
	}
	@Override
	public void updateUI() {
		if (UIManager.get(getUIClassID()) != null) {
			AddonToolTipUI ui = (AddonToolTipUI) UIManager.getUI(this);
            setUI(ui);
        } else {
            setUI(new BasicAddonToolTipUI());
        }

	}

	 @Override
    public String getUIClassID() {
        return UI_CLASS_ID;
    }
}
