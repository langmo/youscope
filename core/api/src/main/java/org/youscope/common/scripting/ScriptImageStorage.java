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
package org.youscope.common.scripting;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author langmo
 */
public interface ScriptImageStorage extends Remote
{
    /**
     * Returns the pixels of the last image made.
     * 
     * @return pixels of image.
     * @throws RemoteException
     */
    public Object getLastImagePixels() throws RemoteException;

    /**
     * Waits for new image data to arrive and returns the pixels of the newly arrived image. Makes
     * only sense if job was started asynchrony.
     * 
     * @param timeout Maximal time in ms to wait.
     * @return pixels of image.
     * @throws RemoteException
     */
    public Object getNewImagePixels(int timeout) throws RemoteException;

    /**
     * Gets the width of the last image.
     * 
     * @return Width of image.
     * @throws RemoteException
     */
    public int getLastImageWidth() throws RemoteException;

    /**
     * Gets the heigt of the last image.
     * 
     * @return Height of image.
     * @throws RemoteException
     */
    public int getLastImageHeight() throws RemoteException;

    /**
     * Gets bit depth of last image.
     * 
     * @return Bit depth.
     * @throws RemoteException
     */
    public int getLastImageBitDepth() throws RemoteException;

    /**
     * Returns how many bytes correspond to one pixel. Thus, gives - together with
     * getLastImageCoding() - information on to which data type the result of getLastImagePixels()
     * has to be converted.
     * 
     * @return Bytes per pixel.
     * @throws RemoteException
     */
    public int getLastImageBytesPerPixel() throws RemoteException;

    /**
     * Returns a string describing the data type to which the result of getLastImagePixels() has to
     * be converted.
     * 
     * @return Data type of pixels.
     * @throws RemoteException
     */
    public String getLastImageCoding() throws RemoteException;
}
