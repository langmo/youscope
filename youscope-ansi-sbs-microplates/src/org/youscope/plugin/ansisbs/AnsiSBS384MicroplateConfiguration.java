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
package org.youscope.plugin.ansisbs;

import org.youscope.addon.microplate.MicroplateConfiguration;
import org.youscope.common.configuration.YSConfigAlias;
import org.youscope.common.configuration.YSConfigIcon;

/**
 * 384 well microplate (ANSI/SBS 1-2004 through ANSI/SBS 4-2004).
 * @author Moritz Lang
 *
 */
@YSConfigAlias("384 well microplate (ANSI/SBS 1-2004 through ANSI/SBS 4-2004)")
@YSConfigIcon("icons/map.png")
public class AnsiSBS384MicroplateConfiguration extends MicroplateConfiguration
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 3032928883305729311L;
	/**
	 * Type identifier.
	 */
	public static final String TYPE_IDENTIFIER = "YouScope.microplate.AnsiSBS384Microplate";
	
	@Override
	public String getTypeIdentifier() {
		return TYPE_IDENTIFIER;
	}

	

}
