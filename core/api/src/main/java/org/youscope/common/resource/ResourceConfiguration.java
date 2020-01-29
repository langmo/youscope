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
package org.youscope.common.resource;

import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.ConfigurationException;

/**
 * Base class of all configurations of measurement resources.
 * @author Moritz Lang
 *
 */
public abstract class ResourceConfiguration implements Configuration 
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -7383728961240600655L;

	@Override
	public void checkConfiguration() throws ConfigurationException {
		// do nothing.
		
	}
}
