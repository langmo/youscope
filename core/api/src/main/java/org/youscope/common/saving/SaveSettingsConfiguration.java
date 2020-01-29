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
package org.youscope.common.saving;

import org.youscope.common.resource.ResourceConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Subclasses of this abstract class represent the configuration of a folder structure into which measurement data should be stored into.
 * @author Moritz Lang
 *
 */
@XStreamAlias("save-settings")
public abstract class SaveSettingsConfiguration extends ResourceConfiguration 
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -1725372967132338811L;
}
