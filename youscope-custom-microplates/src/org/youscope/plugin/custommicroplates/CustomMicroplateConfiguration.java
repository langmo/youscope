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
package org.youscope.plugin.custommicroplates;

import org.youscope.addon.microplate.MicroplateConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A customly defined microplate.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("custom-microplate")
public class CustomMicroplateConfiguration extends MicroplateConfiguration
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 7674884664568364109L;
	@XStreamAlias("custom-microplate-name")
	private String customMicroplateName = "unnamed";
	
	@Override
	public String getTypeIdentifier()
	{
		return CustomMicroplateManager.getCustomMicroplateTypeIdentifier(getCustomMicroplateName());
	}

	/**
	 * Sets the name the custom microplate, with which it is identified. Since used for saving, the name must be a valid file name, but without file name extension.
	 * @param customMicroplateName The name of the custom microplate.
	 */
	public void setCustomMicroplateName(String customMicroplateName)
	{
		this.customMicroplateName = customMicroplateName;
	}

	/**
	 * Returns the name of the custom microplate, with which it is identified. The ID is also used as the basename for saving the microplate.
	 * @return Name of the microplate template.
	 */
	public String getCustomMicroplateName()
	{
		return customMicroplateName;
	}
}
