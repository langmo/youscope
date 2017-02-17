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
package org.youscope.plugin.dropletmicrofluidics.flexiblecontroller;

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
