/**
 * 
 */
package org.youscope.serverinterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author langmo
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
