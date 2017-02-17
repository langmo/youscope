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

import org.youscope.addon.microscopeaccess.FloatPropertyInternal;
import org.youscope.common.microscope.FloatProperty;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;

/**
 * @author Moritz Lang
 */
class FloatPropertyRMI extends PropertyRMI implements FloatProperty
{

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = -974065582584845178L;

    private final FloatPropertyInternal floatProperty;

    /**
     * Constructor.
     * 
     * @throws RemoteException
     */
    FloatPropertyRMI(FloatPropertyInternal floatProperty, int accessID) throws RemoteException
    {
        super(floatProperty, accessID);
        this.floatProperty = floatProperty;
    }

    @Override
	public float getFloatValue() throws MicroscopeException, NumberFormatException,
            InterruptedException, RemoteException
    {
        return floatProperty.getFloatValue();
    }

    @Override
	public void setValue(float value) throws MicroscopeException, MicroscopeLockedException,
            InterruptedException, RemoteException
    {
        floatProperty.setValue(value, accessID);
    }

    @Override
	public void setValueRelative(float offset) throws MicroscopeException, RemoteException,
            MicroscopeLockedException, NumberFormatException, InterruptedException
    {
        floatProperty.setValueRelative(offset, accessID);
    }

    @Override
	public float getLowerLimit() throws RemoteException
    {
        return floatProperty.getLowerLimit();
    }

    @Override
	public float getUpperLimit() throws RemoteException
    {
        return floatProperty.getUpperLimit();
    }
}
