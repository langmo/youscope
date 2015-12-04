/**
 * 
 */
package org.youscope.plugin.scripting;

import java.io.File;
import java.util.EventListener;

/**
 * @author langmo
 *
 */
interface EditFileListener extends EventListener
{
	public void editFile(File file);
}
