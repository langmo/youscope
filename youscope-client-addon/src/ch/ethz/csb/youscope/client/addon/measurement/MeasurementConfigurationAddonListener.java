/**
 * 
 */
package ch.ethz.csb.youscope.client.addon.measurement;

import java.util.EventListener;

import ch.ethz.csb.youscope.shared.configuration.MeasurementConfiguration;

/**
 * Listener which should be called by measurement configuration addons when configuration of a measurement finished.
 * @author Moritz Lang
 */
public interface MeasurementConfigurationAddonListener extends EventListener
{
    /**
     * Should be invoked when the configuration is finished.
     * @param configuration The finished configuration.
     */
    void measurementConfigurationFinished(MeasurementConfiguration configuration);
}
