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
package org.youscope.common.image;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.EventListener;

/**
 * Listener which gets notified about produced images.
 * 
 * @author Moritz Lang
 */
public interface ImageListener extends Remote, EventListener
{
    /**
     * Method gets invoked if new image is available.
     * 
     * @param image Object containing the pixel data, as well as the image metadata.
     * @throws RemoteException
     */
    void imageMade(ImageEvent<?> image) throws RemoteException;
}
