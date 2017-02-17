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
package org.youscope.plugin.dropletmicrofluidics.evaporationController;

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
@YSConfigAlias("Evaporation controller")
@XStreamAlias("droplet-evaporation-controller")
public class EvaporationControllerConfiguration extends DropletControllerConfiguration
{
 
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -1518731968362660759L;
	
	@YSConfigAlias("maximal delta flow for controller (ul/min)")
	@YSConfigDoubleRange(minValue=0.0)
	@XStreamAlias("max-delta-flow-rate-ul-min")
	private double maxDeltaFlowRate = 2.0;
	
	@YSConfigAlias("Time constant of controller's proportional part (ms)")
	@YSConfigDoubleRange(minValue=0.0)
	@XStreamAlias("time-constant-p-min")
	private long timeConstantProportional = 15*60*1000;
	
	@YSConfigAlias("Time constant of controller's integral part (ms)")
	@YSConfigDoubleRange(minValue=0.0)
	@XStreamAlias("time-constant-integral")
	private long timeConstantIntegral = 10*60*60*1000;
	
	@XStreamAlias("use-syringe")
	private boolean[] useSyringe = null;
	
	@XStreamAlias("ratio-height-to-volume-um-ul")
	private double ratioHeightToVolume = 8.5;

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
	public static final String	TYPE_IDENTIFIER	= "YouScope.DropletMicrofluidics.EvaporationController";
	
	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}
	
	/**
	 * Maximal flow rate in ul/min for the correction of drolet height. 
	 * @return maximal correction flow rate.
	 */
	public double getMaxDeltaFlowRate() {
		return maxDeltaFlowRate;
	}

	/**
	 * Maximal flow rate in ul/min for the correction of drolet height. 
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
	 * Returns the syringes which should be used to correct evaporation.
	 * @return syringes which should be used.
	 */
	public boolean[] getUseSyringe() {
		return useSyringe;
	}

	/**
	 * Sets the syringes which should be used to correct evaporation.
	 * @param useSyringe syringes which should be used.
	 */
	public void setUseSyringe(boolean[] useSyringe) {
		this.useSyringe = useSyringe;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		super.checkConfiguration();
		
		boolean atLeastOne = false;
    	for(int i=0; i<useSyringe.length; i++)
    	{
    		atLeastOne = atLeastOne || useSyringe[i];
    	}
    	if(!atLeastOne)
    		throw new ConfigurationException("Select at least one flow unit to compensate for evaporation.");
    	if(maxDeltaFlowRate <= 0)
    		throw new ConfigurationException("Maximal allowed delta flow must be stricly greater than zero.");
	}
}
