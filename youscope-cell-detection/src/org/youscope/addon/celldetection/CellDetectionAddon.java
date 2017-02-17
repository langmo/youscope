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
package org.youscope.addon.celldetection;

import java.rmi.RemoteException;

import org.youscope.common.image.ImageEvent;
import org.youscope.common.resource.Resource;
import org.youscope.common.table.TableProducer;

/**
 * A cell detection addon is a class supporting to detect cells in microscope images.
 * @author Moritz Lang
 *
 */
public interface CellDetectionAddon extends Resource, TableProducer
{
	/**
	 * Detects the cell in the detectionImage image according to the configuration of the addon.
	 * Same as calling detectCells(detectionImage, new ImageEvent[0]).
	 * @param detectionImage The image which should be analyzed.
	 * @return The result of the cell detection algorithm.
	 * @throws CellDetectionException
	 * @throws RemoteException 
	 */
	public CellDetectionResult detectCells(ImageEvent<?> detectionImage) throws CellDetectionException, RemoteException;
	
	/**
	 * Detects the cell in the detectionImage image according to the configuration of the addon.
	 * The additional images in quantificationImages are used to quantify e.g. the fluorescence level in a certain channel.
	 * Note that not all algorithms support quantification of further images. These algorithms should implement this method, however, are not
	 * required to use the images for quantification...
	 * @param detectionImage The image which should be analyzed.
	 * @param quantificationImages Images which should be quantified.
	 * @return The result of the cell detection algorithm.
	 * @throws CellDetectionException
	 * @throws RemoteException 
	 */
	public CellDetectionResult detectCells(ImageEvent<?> detectionImage, ImageEvent<?>[] quantificationImages) throws CellDetectionException, RemoteException;
}
