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
