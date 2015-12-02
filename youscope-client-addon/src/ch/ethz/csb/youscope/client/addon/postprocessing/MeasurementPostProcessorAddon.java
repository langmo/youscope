/**
 * 
 */
package ch.ethz.csb.youscope.client.addon.postprocessing;

import ch.ethz.csb.youscope.client.addon.YouScopeFrame;

/**
 * @author Moritz Lang
 *
 */
public interface MeasurementPostProcessorAddon
{
	/**
	 * Is called when the post-processor should start to do whatever it is made to do.
     * The post-processor should either provide some possibilities for the user to interact with it
     * (e.g. to configure or stop the post-processor doing whatever it does), which should be initialized in the frame,
     * or a message or something similar that the processor is started, which should then be initialized in the frame.
     * However, the frame should not be made visible.
     * @param frame The frame in which the graphical elements of the post-processor should be initialized.
     * 
     */
    void createUI(YouScopeFrame frame);
}
