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
     * Sets the number of flow units available.
     * @param numFlowUnits Number of flow units.
     */
    public void setNumFlowUnits(int numFlowUnits);
}
