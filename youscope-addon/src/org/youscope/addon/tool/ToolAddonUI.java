/**
 * 
 */
package org.youscope.addon.tool;

import org.youscope.clientinterfaces.YouScopeFrame;

/**
 * @author langmo Interface for addons which implement custom jobs.
 */
public interface ToolAddonUI
{
	/**
	 * Is called when the tool should start to do whatever it is made to do.
     * The tool should either provide some possibilities for the user to interact with it
     * (e.g. to stop the tool doing whatever it does), which should be initialized in the frame,
     * or a message or something similar that the tool is started, which should then be initialized in the frame.
     * However, the frame should not be made visible.
     * @param frame The frame in which the graphical elements of the tool should be initialized.
     * 
     */
    void createUI(YouScopeFrame frame);
}
