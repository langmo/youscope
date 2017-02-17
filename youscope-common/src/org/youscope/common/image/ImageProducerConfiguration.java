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


/**
 * Interface each job configuration should implement to indicate that its corresponding job produces images.
 * By implementing this interface one accepts the contract that each job created by the respective configuration also implements the ImageProducer interface.
 * 
 * By implementing this interface one furthermore allows for validity checks, e.g. if the images produced by several different jobs are not tried to be saved in one common file.
 * 
 * @author Moritz Lang
 * 
 */
public interface ImageProducerConfiguration
{
	/**
	 * Should return the names of all images produced by this job and which should be saved to disk.
	 * Allows to check YouScope if two jobs will later on try to save their images to the same file, thus erasing each other's result.
	 * Only names of images which are stored by YouScope's standard storage algorithm have to be returned.
	 * 
	 * @return Array of all image names produced by this job configuration, or null.
	 */
	public String[] getImageSaveNames();

	/**
	 * Returns the number of images which get produced per evaluation of a job initialized with this configuration.
	 * Typically, this is one. However, some jobs can produce more than one image, and the number of the produced images may or may not depend on the configuration.
	 * Some jobs might even produce no images at all for certain configurations (but usually for other configurations they produce images, otherwise this interface would not make sense to implement).
	 * 
	 * @return Number of produced images. Zero indicate that no images are produced by this job type at all. Negative numbers indicate that the number of images is not known, or that it might even
	 *         vary.
	 */
	int getNumberOfImages();
}
