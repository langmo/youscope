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
