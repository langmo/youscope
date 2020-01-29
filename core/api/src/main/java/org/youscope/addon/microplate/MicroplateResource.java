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
