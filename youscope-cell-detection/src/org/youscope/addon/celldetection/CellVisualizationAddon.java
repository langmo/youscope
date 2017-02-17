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

/**
 * A cell visualization addon is a class supporting to visualize previously detected cells in microscope images by generating new images.
 * @author Moritz Lang
 *
 */
public interface CellVisualizationAddon extends Resource
{
	/**
	 * Visualizes the cell in this image according to the configuration of the addon.
	 * @param image The image in which the cells should be visualized.
	 * @param detectionResult The output of a detection algorithm for this image.
	 * @return An image, in which the detected cells are highlighted in one or the other way.
	 * @throws CellVisualizationException
	 * @throws RemoteException 
	 */
	public ImageEvent<?> visualizeCells(ImageEvent<?> image, CellDetectionResult detectionResult) throws CellVisualizationException, RemoteException;
}
