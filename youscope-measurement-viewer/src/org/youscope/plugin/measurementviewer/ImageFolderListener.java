/**
 * 
 */
package org.youscope.plugin.measurementviewer;

import java.util.EventListener;

/**
 * @author Moritz Lang
 *
 */
interface ImageFolderListener extends EventListener
{
	public void showFolder(ImageFolderNode folder);
}
