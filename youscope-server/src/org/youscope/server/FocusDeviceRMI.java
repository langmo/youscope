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
package org.youscope.server;

import java.rmi.RemoteException;

import org.youscope.addon.microscopeaccess.FocusDeviceInternal;
import org.youscope.common.microscope.FocusDevice;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;

/**
 * @author langmo
 */
class FocusDeviceRMI extends DeviceRMI implements FocusDevice
{

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = -7624825693540201850L;

    private FocusDeviceInternal focusDevice;

    FocusDeviceRMI(FocusDeviceInternal focusDevice, int accessID) throws RemoteException
    {
        super(focusDevice, accessID);
        this.focusDevice = focusDevice;
    }

    @Override
	public double getFocusPosition() throws MicroscopeException, InterruptedException
    {
        return focusDevice.getFocusPosition();
    }

    @Override
	public void setFocusPosition(double position) throws MicroscopeLockedException,
            MicroscopeException, InterruptedException
    {
        focusDevice.setFocusPosition(position, accessID);

    }

    @Override
	public void setRelativeFocusPosition(double offset) throws MicroscopeLockedException,
            RemoteException, MicroscopeException, InterruptedException
    {
        focusDevice.setRelativeFocusPosition(offset, accessID);
    }
}
