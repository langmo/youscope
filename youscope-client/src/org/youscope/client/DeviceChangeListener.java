/**
 * 
 */
package org.youscope.client;

import java.util.EventListener;

import org.youscope.common.microscope.DeviceSetting;

/**
 * @author langmo
 */
interface DeviceChangeListener extends EventListener
{
    void deviceSettingChanged(DeviceSetting setting);
}
