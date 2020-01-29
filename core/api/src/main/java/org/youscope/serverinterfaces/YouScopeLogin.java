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
package org.youscope.serverinterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Moritz Lang
 */
public interface YouScopeLogin extends Remote
{
    /**
     * When the right password is provided, this interface grants access to the microscope.
     * 
     * @param password The password. Should be the same as the one set in the startup of the server.
     * @return Main interface of the microscope.
     * @throws RemoteException
     * @throws SecurityException Thrown if authentification failed.
     */
    public YouScopeServer login(String password) throws RemoteException, SecurityException;

}
