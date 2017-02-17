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
package org.youscope.addon.component;

import java.util.EventListener;

import org.youscope.common.configuration.Configuration;

/**
 * Listener which should be called when a configuration addon finished its configuration.
 * @author Moritz Lang
 * @param <T> Type of the configuration done by the addon.
 */
public interface ComponentAddonUIListener<T extends Configuration> extends EventListener
{
	/**
     * Should be invoked when the configuration is finished.
     * @param configuration The finished configuration.
     */
    void configurationFinished(T configuration);
}
