package org.youscope.plugin.travelingsalesman.blossom;

import java.util.EventListener;

/**
 * A listener which gets informed about the individual steps of the blossom algorithm.
 * @author Moritz Lang
 *
 */
public interface BlossomListener extends EventListener 
{
	/**
	 * Gets invoked if primal update was performed.
	 * @param type Type of primal update performed.
	 */
	public void primalUpdate(PrimalUpdateType type);
	
	/**
	 * Gets invoked if dual update was performed.
	 * @param dualChange Change of the dual variables in current tree.
	 */
	public void dualUpdate(double dualChange);
}
