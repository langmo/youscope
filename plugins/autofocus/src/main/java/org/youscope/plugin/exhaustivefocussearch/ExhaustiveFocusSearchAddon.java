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
package org.youscope.plugin.exhaustivefocussearch;

import java.rmi.RemoteException;

import org.youscope.addon.focussearch.FocusSearchOracle;
import org.youscope.addon.focussearch.FocusSearchResource;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.resource.ResourceAdapter;
import org.youscope.common.resource.ResourceException;

/**
 * Implementation of an exhaustive search focus search algorithm.
 * @author Moritz Lang
 *
 */
class ExhaustiveFocusSearchAddon extends ResourceAdapter<ExhaustiveFocusSearchConfiguration> implements FocusSearchResource
{
	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -1378968222326190628L;

	ExhaustiveFocusSearchAddon(PositionInformation positionInformation, ExhaustiveFocusSearchConfiguration configuration) throws ConfigurationException, RemoteException
	{
		super(positionInformation, configuration, ExhaustiveFocusSearchConfiguration.CONFIGURATION_ID, ExhaustiveFocusSearchConfiguration.class, "Exhaustive focus search");
	}

	@Override
	public double runAutofocus(FocusSearchOracle oracle) throws ResourceException, RemoteException 
	{
		assertInitialized();
		ExhaustiveFocusSearchConfiguration configuration = getConfiguration();
		if(oracle == null)
			throw new ResourceException("Focus search oracle is null.");
		int numSearchSteps =configuration.getNumSearchSteps();
		double focusLowerBound = configuration.getFocusLowerBound();
		double focusUpperBound = configuration.getFocusUpperBound();
		double maxFocusScore = -1;
		double maxFocusPosition = Double.NaN;
		
		 
		for(int i=0; i<numSearchSteps; i++) 
		{
			double relFocusPosition = focusLowerBound + (focusUpperBound - focusLowerBound)/(numSearchSteps-1) * i;
			double focusScore = oracle.getFocusScore(relFocusPosition);
			if(focusScore > maxFocusScore)
			{
				maxFocusScore = focusScore;
				maxFocusPosition = relFocusPosition;
			}
		}
		if(!Double.isNaN(maxFocusPosition))
			return maxFocusPosition;
		throw new ResourceException("Could not find optimal focal plane.");
	}
}
