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

import org.youscope.addon.AddonException;
import org.youscope.common.Component;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * Interface which has to be implemented if the creation of a component, given its configuration, should be done
 * differently from the default mechanism.
 * @author Moritz Lang
 * @param <C> The configuration class consumed for the creation.
 * @param <T> Public interface of created component.
 *
 */
public interface CustomAddonCreator<C extends Configuration, T extends Component>
{
	/**
	 * Creates an component for the given configuration. The created component must be in agreement with the component interface type returned in the metadata.
     * 
	 * @param positionInformation The position in the measurement hierarchy where the addon should be constructed.
	 * @param configuration The configuration of the addon.
	 * @param constructionContext The context of the construction, providing information and functionality in the construction of the addon.
	 * @return The constructed component.
	 * @throws ConfigurationException Thrown if the configuration is invalid.
	 * @throws AddonException Thrown if an error occurred during the construction.
	 */
	public T createCustom(PositionInformation positionInformation, C configuration, ConstructionContext constructionContext) throws ConfigurationException, AddonException;
	
	/**
	 * Returns the public interface of the component which gets created.
	 * @return Interface of created component.
	 */
	public Class<T> getComponentInterface();
}
