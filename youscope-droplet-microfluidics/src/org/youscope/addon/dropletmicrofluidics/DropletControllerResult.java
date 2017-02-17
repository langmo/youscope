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
package org.youscope.addon.dropletmicrofluidics;

import java.io.Serializable;

/**
 * Result of a droplet based microfluidics controller.
 * @author Moritz Lang
 *
 */
public class DropletControllerResult implements Serializable, Cloneable {

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 4711191654255897853L;
	private double[] flowRates;
	private final double deltaFlow;
	/**
	 * Constructor.
	 * @param flowRates The flow rates of the flow units, in ul/min
	 * @param deltaFlow Difference of target inflow, respectively outflow rates, and actual outflow rate to correct for droplet height (i.e. controller output).
	 */
	public DropletControllerResult(double[] flowRates, double deltaFlow) 
	{
		this.flowRates = flowRates;
		this.deltaFlow = deltaFlow;
	}
	
	@Override
	public DropletControllerResult clone()
	{
		DropletControllerResult clone;
		try {
			clone = (DropletControllerResult) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Clone not supported", e); // will not happen.
		}
		clone.flowRates = new double[flowRates.length];
		System.arraycopy(flowRates, 0, clone.flowRates, 0, flowRates.length);
		return clone;
	}

	/**
	 * Returns the difference between target flow rate and actual flow rates. This difference is the controller output,
	 * i.e. the flow rate difference chosen to adjust the droplet heights.
	 * @return Delta flow rate in ul/min.
	 */
	public double getDeltaFlow()
	{
		return deltaFlow;
	}
	
	/**
	 * Returns the flow rates in ul/min.
	 * @return flow rates in ul/min.
	 */
	public double[] getFlowRates()
	{
		double[] returnVal = new double[flowRates.length];
		System.arraycopy(flowRates, 0, returnVal, 0, flowRates.length);
		return returnVal;
	}
	
	/**
	 * Returns the flow rate in ul/min of the given flow unit.
	 * @param flowUnit ID of the flow unit.
	 * @return Flow rate in ul/min
	 * @throws IndexOutOfBoundsException Thrown if flowUnit ID small zero or greater equal {@link #getNumFlowUnits()}
	 */
	public double getFlowRate(int flowUnit) throws IndexOutOfBoundsException
	{
		return flowRates[flowUnit];
	}
	
	/**
	 * Returns the number of flow units.
	 * @return Number of flow units.
	 */
	public int getNumFlowUnits()
	{
		return flowRates.length;
	}
}
