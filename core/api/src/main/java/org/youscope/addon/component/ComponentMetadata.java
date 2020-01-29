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
package org.youscope.addon.component;

import org.youscope.addon.AddonMetadata;
import org.youscope.common.Component;
import org.youscope.common.configuration.Configuration;

/**
 * Provides metadata about a given measurement component.
 * @author Moritz Lang
 * @param <C> The class of configuration of this measurement component.
 *
 */
public interface ComponentMetadata<C extends Configuration>  extends AddonMetadata
{
    /**
     * Returns the class of the configuration of the measurement component.
     * The class should have a public no-argument constructor, such that newInstance() can be called. 
     * @return Configuration class of component.
     */
    Class<C> getConfigurationClass();
	
	/**
     * Returns the interface of the measurement component created when the configuration is parsed. 
     * This should not be the implementation of the component, but rather
     * a public interface implementing Remote.
     * @return Component interface.
     */
    Class<? extends Component> getComponentInterface();
}
