package org.youscope.plugin.dropletmicrofluidics.tablecontroller;

import java.io.Serializable;

/**
 * State of the observer.
 * @author Moritz Lang
 *
 */
class ControllerState implements Serializable
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -5670488844311377421L;

	private long lastExecutionTime = -1;
	private double integralError = 0;
	public long getLastExecutionTime() {
		return lastExecutionTime;
	}
	public void setLastExecutionTime(long lastExecutionTime) {
		this.lastExecutionTime = lastExecutionTime;
	}
	public double getIntegralError() {
		return integralError;
	}
	public void setIntegralError(double integralError) {
		this.integralError = integralError;
	}
}
