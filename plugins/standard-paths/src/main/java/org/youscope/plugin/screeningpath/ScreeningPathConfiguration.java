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
package org.youscope.plugin.screeningpath;

import org.youscope.addon.pathoptimizer.PathOptimizerConfiguration;
import org.youscope.common.configuration.YSConfigAlias;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Path optimized for screening of rectangular microplates with all wells/tiles selected.
 * @author Moritz Lang
 *
 */
@YSConfigAlias("Screening Path")
@XStreamAlias("screening-path")
public class ScreeningPathConfiguration extends PathOptimizerConfiguration
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 3061118882205729335L;
	/**
	 * Type identifier.
	 */
	public static final String TYPE_IDENTIFIER = "YouScope.path.ScreeningPath";

	@Override
	public String getTypeIdentifier() {
		return TYPE_IDENTIFIER;
	}

	

}
