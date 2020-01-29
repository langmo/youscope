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

/**
 * Indicates that a given job produces images, and allows to add a listener to obtain produced images.
 * @author langmo
 */
public interface ImageProducer extends Remote
{
	/**
	 * Adds a listener which is invoked if the camera of the microscope made a new picture.
	 * A listener can always be added, even if the respective measurement is already running.
	 * 
	 * @param listener Listener which detects new pictures.
	 * @throws RemoteException
	 */
	void addImageListener(ImageListener listener) throws RemoteException;

	/**
	 * Removes a previously added image listener.
	 * 
	 * @param listener Listener which was previously added.
	 * @throws RemoteException
	 */
	void removeImageListener(ImageListener listener) throws RemoteException;

	/**
	 * Returns a short string describing the images which are made by this job.
	 * 
	 * @return Description of the image.
	 * @throws RemoteException
	 */
	String getImageDescription() throws RemoteException;

	/**
	 * Returns the number of images which get produced per evaluation of the respective job.
	 * Typically, this is one. However, some jobs can produce more than one image, and the number of the produced images may or may not depend on the configuration.
	 * Some jobs might even produce no images at all for certain configurations (but usually for other configurations they produce images, otherwise this interface would not make sense to implement).
	 * 
	 @return Number of produced images. Zero indicate that no images are produced by this job type at all. Negative numbers indicate that the number of images is not known, or that it might even vary.
	 * @throws RemoteException
	 */
	int getNumberOfImages() throws RemoteException;
}
