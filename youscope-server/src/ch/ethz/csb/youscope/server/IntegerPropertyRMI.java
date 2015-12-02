/**
 * 
 */
package ch.ethz.csb.youscope.server;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.server.microscopeaccess.IntegerPropertyInternal;
import ch.ethz.csb.youscope.shared.microscope.IntegerProperty;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeException;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeLockedException;

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
