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
package org.youscope.addon.dropletmicrofluidics;

import org.youscope.addon.component.ComponentAddonUI;

/**
 * Interface for addons which allow the configuration of droplet based microfluidic controllers.
 * @author Moritz Lang 
 * @param <C> The configuration class to edit.
 */
public interface DropletControllerConfigurationAddon<C extends DropletControllerConfiguration> extends ComponentAddonUI<C>
{
    /**
     * Sets the IDs (zero based) of the syringes which are connected to the chip controlled by this controller.
     * @param connectedSyringes IDs of connected syringes.
     */
    public void setConnectedSyringes(int[] connectedSyringes);
}
