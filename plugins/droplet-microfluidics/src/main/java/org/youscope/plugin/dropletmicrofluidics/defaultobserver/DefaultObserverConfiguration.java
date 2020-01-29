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
package org.youscope.plugin.dropletmicrofluidics.defaultobserver;

import org.youscope.addon.dropletmicrofluidics.DropletObserverConfiguration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.YSConfigAlias;
import org.youscope.common.configuration.YSConfigDoubleRange;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Configuration for observer based on discrete Fourier transformation.
 * @author Moritz Lang
 *
 */
@YSConfigAlias("Droplet observer")
@XStreamAlias("droplet-default-observer")
public class DefaultObserverConfiguration extends DropletObserverConfiguration
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -2815430198403970866L;
	/**
	 * Type identifier.
	 */
	public static final String TYPE_IDENTIFIER = "YouScope.DropletDefaultObserver";
	
	@YSConfigAlias("Individual droplet's height learn speed (0-1)")
	@YSConfigDoubleRange(minValue=0.0,maxValue=1.0)
	@XStreamAlias("observer-individual")
	private double observerIndividual = 0.7;
	
	@YSConfigAlias("Mean droplet's height learn speed (0-inf)")
	@YSConfigDoubleRange(minValue=0.0)
	@XStreamAlias("observer-mean")
	private double observerMean = 1.5;
	
	@Override
	public String getTypeIdentifier() 
	{
		return TYPE_IDENTIFIER;
	}
	
	/**
	 * Returns learn rate constant of individual droplet heights.
	 * @return Individual droplet height learn rate constant (0-1).
	 */
	public double getObserverIndividual() {
		return observerIndividual;
	}

	/**
	 * Sets the learn rate constant of individual droplet heights.
	 * @param observerIndividual Individual droplet height learn rate constant (0-1).
	 */
	public void setObserverIndividual(double observerIndividual) {
		this.observerIndividual = observerIndividual;
	}

	/**
	 * Returns learn rate constant of mean droplet heights.
	 * @return Mean droplet height learn rate constant (0-2).
	 */
	public double getObserverMean() {
		return observerMean;
	}

	/**
	 * Sets the learn rate constant of mean droplet heights.
	 * @param observerMean Mean droplet height learn rate constant (0-2).
	 */
	public void setObserverMean(double observerMean) {
		this.observerMean = observerMean;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		super.checkConfiguration();
		
		if(observerMean < 0)
			throw new ConfigurationException("Observer mean droplet height learn speed must be bigger or equal to zero.");
		if(observerIndividual < 0 || observerIndividual > 1)
			throw new ConfigurationException("Individual droplet learn speed must be between zero and one.");
	}
	
	
}
