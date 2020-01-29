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

import org.youscope.addon.microscopeaccess.SelectablePropertyInternal;
import org.youscope.common.microscope.SelectableProperty;

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
