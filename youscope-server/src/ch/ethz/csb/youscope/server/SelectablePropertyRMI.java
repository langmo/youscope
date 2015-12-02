/**
 * 
 */
package ch.ethz.csb.youscope.server;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.server.microscopeaccess.SelectablePropertyInternal;
import ch.ethz.csb.youscope.shared.microscope.SelectableProperty;

/**
 * @author Moritz Lang
 */
class SelectablePropertyRMI extends PropertyRMI implements SelectableProperty
{

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = 7230759464316017918L;

    private final SelectablePropertyInternal selectableProperty;

    /**
     * Constructor.
     * 
     * @throws RemoteException
     */
    SelectablePropertyRMI(SelectablePropertyInternal selectableProperty, int accessID)
            throws RemoteException
    {
        super(selectableProperty, accessID);
        this.selectableProperty = selectableProperty;
    }

    @Override
	public String[] getAllowedPropertyValues() throws RemoteException
    {
        return selectableProperty.getAllowedPropertyValues();
    }

}
