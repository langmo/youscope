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

import org.youscope.addon.microscopeaccess.IntegerPropertyInternal;
import org.youscope.common.microscope.IntegerProperty;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;

/**
 * @author Moritz Lang
 */
class IntegerPropertyRMI extends PropertyRMI implements IntegerProperty
{

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = 6895767112006206165L;

    private final IntegerPropertyInternal integerProperty;

    /**
     * Constructor.
     * 
     * @throws RemoteException
     */
    IntegerPropertyRMI(IntegerPropertyInternal integerProperty, int accessID)
            throws RemoteException
    {
        super(integerProperty, accessID);
        this.integerProperty = integerProperty;
    }

    @Override
	public int getIntegerValue() throws MicroscopeException, NumberFormatException,
            InterruptedException, RemoteException
    {
        return integerProperty.getIntegerValue();
    }

    @Override
	public void setValue(int value) throws MicroscopeException, MicroscopeLockedException,
            InterruptedException, RemoteException
    {
        integerProperty.setValue(value, accessID);
    }

    @Override
	public void setValueRelative(int offset) throws MicroscopeException, MicroscopeLockedException,
            NumberFormatException, InterruptedException, RemoteException
    {
        integerProperty.setValueRelative(offset, accessID);
    }

    @Override
	public int getLowerLimit() throws RemoteException
    {
        return integerProperty.getLowerLimit();
    }

    @Override
	public int getUpperLimit() throws RemoteException
    {
        return integerProperty.getUpperLimit();
    }
}
