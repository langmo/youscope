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
