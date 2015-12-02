/**
 * 
 */
package ch.ethz.csb.youscope.client.addon;

import java.util.EventListener;

/**
 * @author langmo
 * 
 */
public interface YouScopeFrameListener extends EventListener
{
	/**
	 * Activated when the frame is closing.
	 */
	public void frameClosed();

	/**
	 * Activated when frame is opened/displayed.
	 */
	public void frameOpened();
}
