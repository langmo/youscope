/**
 * 
 */
package org.youscope.client;

import java.util.EventListener;

import org.youscope.common.microscope.DeviceSettingDTO;

/**
 * @author langmo
 */
interface DeviceChangeListener extends EventListener
{
    void deviceSettingChanged(DeviceSettingDTO setting);
}
