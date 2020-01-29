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
package org.youscope.plugin.brentfocussearch;

import java.rmi.RemoteException;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.apache.commons.math3.optim.univariate.UnivariatePointValuePair;
import org.youscope.addon.focussearch.FocusSearchOracle;
import org.youscope.addon.focussearch.FocusSearchResource;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.resource.ResourceAdapter;
import org.youscope.common.resource.ResourceException;

class BrentFocusSearchAddon extends ResourceAdapter<BrentFocusSearchConfiguration> implements FocusSearchResource
{

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 7144091958406916252L;


	BrentFocusSearchAddon(PositionInformation positionInformation, BrentFocusSearchConfiguration configuration) throws ConfigurationException, RemoteException
	{
		super(positionInformation, configuration, BrentFocusSearchConfiguration.CONFIGURATION_ID,BrentFocusSearchConfiguration.class, "Brent focus search");
	}
	

	@Override
	public double runAutofocus(final FocusSearchOracle oracle)
			throws ResourceException, RemoteException {
		assertInitialized();
		BrentFocusSearchConfiguration configuration = getConfiguration();
		if(oracle == null)
			throw new ResourceException("Focus search oracle is null.");
		final int maxSearchSteps =configuration.getMaxSearchSteps();
		double focusLowerBound = configuration.getFocusLowerBound();
		double focusUpperBound = configuration.getFocusUpperBound();
		double focusTolerance = configuration.getTolerance();
		
		class OptimizationException extends RuntimeException
		{
			/**
			 * Serial Version UID.
			 */
			private static final long serialVersionUID = 7111732041188942146L;

			public OptimizationException(double focusPosition, Exception e)
			{
				super("Could not get focus score for relative focus position " + Double.toString(focusPosition)+ ".", e);
			}
		}
		UnivariateFunction focusFunction = new UnivariateFunction() 
		{

	         @Override
			public double value(double relFocusPosition) throws OptimizationException 
	         {
	            try 
	            {
	               return oracle.getFocusScore(relFocusPosition);
	            } 
	            catch (Exception e) 
	            {
	               throw new OptimizationException(relFocusPosition, e);
	            }
	         }
	      };
		
		BrentOptimizer brentOptimizer = new BrentOptimizer(Math.sqrt(Math.ulp(1.0)), focusTolerance, new ConvergenceChecker<UnivariatePointValuePair>()
				{
					// own convergence checker, which acts on top of normal convergence criteria. This convergence checker simply signals convergence
					// whenever maximal numbe of iterations is reached.
					@Override
					public boolean converged(int iteration, UnivariatePointValuePair previous, UnivariatePointValuePair current) {
						/**
						 * +2 because:
						 * - Iterations start counting at zero.
						 * - We are interested in function evaluations, whereas we here get the number of iterations. The Sobel algorithm does
						 *   one initial guess before starting the first function evaluation.
						 */
						return iteration + 2 >= maxSearchSteps; 
					}
			
				});
		UnivariatePointValuePair solution;
		try
		{
			solution = brentOptimizer.optimize(new UnivariateObjectiveFunction(focusFunction),
					MaxEval.unlimited(), // We stop after maxSearchSteps iterations by convergence checker.
					GoalType.MAXIMIZE,
					new SearchInterval(focusLowerBound, focusUpperBound));
		}
		catch(OptimizationException e)
		{
			throw new ResourceException("Brent focus optimization failed.", e);
		}
	    return solution.getPoint();
	}
}
