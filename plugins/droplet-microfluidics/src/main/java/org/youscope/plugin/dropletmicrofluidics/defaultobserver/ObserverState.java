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

import java.io.Serializable;

import org.youscope.common.ExecutionInformation;
import org.youscope.common.resource.ResourceException;

/**
 * State of the observer.
 * @author Moritz Lang
 *
 */
class ObserverState implements Serializable
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -5670488844311377425L;

	private long currentEvaluation = 0;
	private int nextDroplet = 0;
	private int numDroplets = 0;
	private double[] estimatedOffsets = null;
	private double observerMean = -1;
	private double observerIndividual = -1;
	public void registerDroplet(DefaultObserverConfiguration configuration) throws ResourceException
	{
		if(estimatedOffsets != null)
			throw new ResourceException("All droplets must be registered before controller starts.");
		numDroplets++;
		if(numDroplets == 1)
		{
			observerIndividual = configuration.getObserverIndividual();
			observerMean = configuration.getObserverMean();
			if(observerIndividual < 0 || observerMean < 0)
				throw new ResourceException("Observer learn speeds for individual and mean droplet heights must be bigger or equal to zero.");
		}
		else
		{
			if(observerIndividual != configuration.getObserverIndividual() || observerMean != configuration.getObserverMean())
				throw new ResourceException("Observer learn speeds for individual and mean droplet heights must be the same for all droplets of a given microfluidic chip.");
		}
	}
	public double getObserverMean()
	{
		return observerMean;
	}
	public double getObserverIndividual()
	{
		return observerIndividual;
	}
	public int getNumDroplets()
	{
		return numDroplets;
	}
	public double[] getEstimatedOffsets()
	{
		if(estimatedOffsets == null)
		{
			estimatedOffsets = new double[numDroplets];
			for(int i=0; i< estimatedOffsets.length; i++)
				estimatedOffsets[i] = 0;
		}
		return estimatedOffsets;
	}
	public void setEstimatedOffsets(double[] estimatedOffsets) throws ResourceException
	{
		if(estimatedOffsets==null || estimatedOffsets.length != numDroplets)
			throw new ResourceException("Number of estimated offsets must be "+Integer.toString(numDroplets)+".");
		for(int i=0; i< estimatedOffsets.length; i++)
			this.estimatedOffsets[i] = estimatedOffsets[i];
	}
	public int getNextDropletID(ExecutionInformation executionInformation) throws ResourceException
	{
		if(numDroplets <= 0)
			throw new ResourceException("No droplet registered");
		if(executionInformation.getEvaluationNumber() < currentEvaluation)
		{
			throw new ResourceException("Current droplet iteration is "+Long.toString(currentEvaluation+1)+", however, got droplet height report corresponding to evaluation "+Long.toString(executionInformation.getEvaluationNumber()+1)+". All droplets must be scanned equally often and in the same order.");
		}
		else if(executionInformation.getEvaluationNumber() == currentEvaluation)
		{
			int returnVal = nextDroplet;
			nextDroplet++;
			if(nextDroplet > numDroplets)
				throw new ResourceException("Only " + Integer.toString(numDroplets) + " droplets registered, however, droplet height for droplet #" + Integer.toString(returnVal+1) + " reported.");
			return returnVal;
		}
		else 
		{
			if(nextDroplet != numDroplets)
			{
				throw new ResourceException("Number of registered droplets is " + Integer.toString(numDroplets)+", however, only " + Integer.toString(nextDroplet) + " droplet heights were reported in evaluation "+ Long.toString(currentEvaluation+1)+".");
			}
			currentEvaluation = executionInformation.getEvaluationNumber();
			nextDroplet=1;
			return 0;
		}
	}
}
