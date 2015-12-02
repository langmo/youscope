/**
 * 
 */
package ch.ethz.csb.youscope.client;

import java.util.EventListener;

import ch.ethz.csb.youscope.shared.microscope.DeviceSettingDTO;

/**
 * @author langmo
 */
interface DeviceChangeListener extends EventListener
{
    void deviceSettingChanged(DeviceSettingDTO setting);
}
