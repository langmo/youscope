package org.youscope.addon.microplate;

import java.rmi.RemoteException;

import org.youscope.common.microplate.MicroplateLayout;
import org.youscope.common.resource.Resource;
import org.youscope.common.resource.ResourceException;

/**
 * Configuration of the tile/well layout defining where images should be taken. Typically, represents a microplate.
 * @author Moritz Lang
 *
 */
public interface MicroplateResource extends Resource 
{    
    /**
     * Returns the layout of the microplate.
     * @return Layout of microplate.
     * @throws ResourceException
     * @throws RemoteException
     */
	MicroplateLayout getMicroplateLayout() throws ResourceException, RemoteException;
}
