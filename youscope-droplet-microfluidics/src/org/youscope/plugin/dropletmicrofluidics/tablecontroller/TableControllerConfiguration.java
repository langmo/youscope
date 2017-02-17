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
package org.youscope.plugin.dropletmicrofluidics.tablecontroller;

import org.youscope.addon.dropletmicrofluidics.DropletControllerConfiguration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.YSConfigAlias;
import org.youscope.common.configuration.YSConfigDoubleRange;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Configuration for the droplet based microfluidic measurements based on a syringe table.
 * @author Moritz Lang
 *
 */
@YSConfigAlias("Table controller")
@XStreamAlias("droplet-table-controller")
public class TableControllerConfiguration extends DropletControllerConfiguration
{
 
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -7518731968362660759L;
	
	@YSConfigAlias("target flow rate (ul/min)")
	@YSConfigDoubleRange(minValue=0.0)
	@XStreamAlias("target-flow-rate-ul-min")
	private double targetFlowRate = 4.0;
	
	@YSConfigAlias("maximal delta flow for controller (ul/min)")
	@YSConfigDoubleRange(minValue=0.0)
	@XStreamAlias("max-delta-flow-rate-ul-min")
	private double maxDeltaFlowRate = 2.0;
	
	@YSConfigAlias("Time constant of controller's proportional part (ms)")
	@YSConfigDoubleRange(minValue=0.0)
	@XStreamAlias("time-constant-p-ms")
	private long timeConstantProportional = 15*60*1000;
	
	@YSConfigAlias("Time constant of controller's integral part (ms)")
	@YSConfigDoubleRange(minValue=0.0)
	@XStreamAlias("time-constant-integral")
	private long timeConstantIntegral = 10*60*60*1000;
	
	@XStreamAlias("syringe-table")
	private SyringeTableRow[] syringeTableRows = null;
	
	@XStreamAlias("correct-by-outflow")
	private boolean correctByOutflow = false;
	
	@XStreamAlias("ratio-height-to-volume-um-ul")
	private double ratioHeightToVolume = 8.5;
	
	/**
	 * True if droplet height is corrected by varying outflow, false if varying inflow.
	 * @return true if corrected by outflow.
	 */
	public boolean isCorrectByOutflow() {
		return correctByOutflow;
	}

	/**
	 * True if droplet height is corrected by varying outflow, false if varying inflow.
	 * @param correctByOutflow true if corrected by outflow.
	 */
	public void setCorrectByOutflow(boolean correctByOutflow) {
		this.correctByOutflow = correctByOutflow;
	}

	/**
	 * Returns the (estimated) ratio between droplet height in um and the droplet volume in ul.
	 * @return ratio height to volume.
	 */
	public double getRatioHeightToVolume() {
		return ratioHeightToVolume;
	}

	/**
	 * Sets the (estimated) ratio between droplet height in um and the droplet volume in ul.
	 * @param ratioHeightToVolume ratio height to volume.
	 */
	public void setRatioHeightToVolume(double ratioHeightToVolume) {
		this.ratioHeightToVolume = ratioHeightToVolume;
	}

	/**
	 * The identifier for this configuration.
	 */
	public static final String	TYPE_IDENTIFIER	= "YouScope.DropletMicrofluidics.TableController";
	
	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}

	/**
	 * Returns the target flow rate in ul/min.
	 * @return target flow rate.
	 */
	public double getTargetFlowRate() {
		return targetFlowRate;
	}

	/**
	 * Sets the target flow rate in ul/min
	 * @param targetRate flow rate in ul/min
	 */
	public void setTargetFlowRate(double targetRate) {
		this.targetFlowRate = targetRate;
	}
	
	/**
	 * Maximal flow rate in ul/min for the correction of drolet height. Must be smaller than {@link #getTargetFlowRate()}.
	 * @return maximal correction flow rate.
	 */
	public double getMaxDeltaFlowRate() {
		return maxDeltaFlowRate;
	}

	/**
	 * Maximal flow rate in ul/min for the correction of drolet height. Must be smaller than {@link #getTargetFlowRate()}.
	 * @param maxDeltaFlowRate maximal correction flow rate.
	 */
	public void setMaxDeltaFlowRate(double maxDeltaFlowRate) {
		this.maxDeltaFlowRate = maxDeltaFlowRate;
	}

	/**
	 * Returns the time constant (in ms) of the proportional part of the controller.
	 * @return time constant of proportional part.
	 */
	public long getTimeConstantProportional() {
		return timeConstantProportional;
	}

	/**
	 * Sets the time constant (in ms) of the proportional part of the controller.
	 * @param timeConstantProportional time constant of proportional part.
	 */
	public void setTimeConstantProportional(long timeConstantProportional) {
		this.timeConstantProportional = timeConstantProportional;
	}

	/**
	 * Returns the time constant (in ms) of the integral part of the controller.
	 * @return time constant of integral part.
	 */
	public long getTimeConstantIntegral() {
		return timeConstantIntegral;
	}

	/**
	 * Sets the time constant (in ms) of the integral part of the controller.
	 * 
	 * @param timeConstantIntegral time constant of integral part.
	 */
	public void setTimeConstantIntegral(long timeConstantIntegral) {
		this.timeConstantIntegral = timeConstantIntegral;
	}

	/**
	 * Returns a table defining when which syringe should act as an outflow or inflow syringe.
	 * @return syringe table.
	 */
	public SyringeTableRow[] getSyringeTableRows() {
		return syringeTableRows;
	}

	/**
	 * Sets a table defining when which syringe should act as an outflow or inflow syringe.
	 * @param syringeTableRows syringe table.
	 */
	public void setSyringeTableRows(SyringeTableRow[] syringeTableRows) {
		this.syringeTableRows = syringeTableRows;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		super.checkConfiguration();
		if(syringeTableRows == null || syringeTableRows.length == 0)
    		throw new ConfigurationException("Add at least one row to the syringe table.");
    	if(syringeTableRows[0].getNumSyringes() <=0)
    		throw new ConfigurationException("No flow units available.");
    	for(int i = 0; i < syringeTableRows.length; i++)
    	{
    		SyringeState[] states = syringeTableRows[i].getSyringeStates();
    		boolean atLeastOne = false;
    		for(SyringeState state : states)
    		{
    			atLeastOne = atLeastOne || (correctByOutflow && state == SyringeState.OUTFLOW) || (!correctByOutflow && state == SyringeState.INFLOW);
    		}
    		if(!atLeastOne && correctByOutflow)
    			throw new ConfigurationException("Row " + Integer.toString(i+1) + " of syringe table does not define any outflow syringe, however, droplet height should be corrected by outflow.");
    		else if(!atLeastOne && !correctByOutflow)
    			throw new ConfigurationException("Row " + Integer.toString(i+1) + " of syringe table does not define any inflow syringe, however, droplet height should be corrected by inflow.");
    	}
    	if(targetFlowRate <= 0)
    		throw new ConfigurationException("Target flow rate must be strictly greater than zero. Use evaporation controller for zero target flow rate.");
    	else if(maxDeltaFlowRate <= 0)
    		throw new ConfigurationException("Maximal delta flow rate must be strictly greater than zero.");
    	else if(maxDeltaFlowRate > targetFlowRate)
    		throw new ConfigurationException("Maximal delta flow rate used for droplet height correction must be smaller or equal to the target flow rate.");
    	
		
	}


}
