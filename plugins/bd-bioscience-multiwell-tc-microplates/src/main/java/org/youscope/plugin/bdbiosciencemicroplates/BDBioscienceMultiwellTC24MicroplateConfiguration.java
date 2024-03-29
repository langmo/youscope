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
package org.youscope.plugin.bdbiosciencemicroplates;

import org.youscope.addon.microplate.MicroplateConfiguration;
import org.youscope.common.configuration.YSConfigAlias;
import org.youscope.common.configuration.YSConfigIcon;

/**
 * 24 well microplate (BD Bioscience - Multiwell TC Plate).
 * @author Moritz Lang
 *
 */
@YSConfigAlias("24 well microplate (BD Bioscience - Multiwell TC Plate)")
@YSConfigIcon("icons/map.png")
public class BDBioscienceMultiwellTC24MicroplateConfiguration extends MicroplateConfiguration
{
	
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -4533762908164999175L;
	/**
	 * Type identifier.
	 */
	public static final String TYPE_IDENTIFIER = "YouScope.microplate.BDBioscienceMultiwellTC24Microplate";
	
	@Override
	public String getTypeIdentifier() {
		return TYPE_IDENTIFIER;
	}

	

}
