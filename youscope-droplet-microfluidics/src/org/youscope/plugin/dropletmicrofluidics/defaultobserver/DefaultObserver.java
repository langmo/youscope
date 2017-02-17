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

import java.rmi.RemoteException;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;
import org.youscope.addon.dropletmicrofluidics.DropletObserverResource;
import org.youscope.addon.dropletmicrofluidics.DropletObserverResult;
import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.resource.ResourceAdapter;
import org.youscope.common.resource.ResourceException;

class DefaultObserver extends ResourceAdapter<DefaultObserverConfiguration> implements DropletObserverResource
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -9044352984010930940L;
	public DefaultObserver(PositionInformation positionInformation, DefaultObserverConfiguration configuration)
					throws ConfigurationException, RemoteException {
		super(positionInformation, configuration, DefaultObserverConfiguration.TYPE_IDENTIFIER, DefaultObserverConfiguration.class, "Droplet-based microfluidics observer");
	}
	@Override
	public DropletObserverResult runObserver(ExecutionInformation executionInformation, MeasurementContext measurementContext,
			double dropletOffset, int microfluidicChipID) throws ResourceException, RemoteException 
	{
		assertInitialized();
		ObserverState state = loadState(measurementContext, microfluidicChipID);
		int l = state.getNextDropletID(executionInformation);
		
		double[] x = state.getEstimatedOffsets();
		double c0 = state.getObserverMean();
		double c1 = state.getObserverIndividual();
		int N = state.getNumDroplets(); // number of droplets
		double pi = Math.PI;
		double y = x[l]; // estimated offset for current droplet
		double h = dropletOffset; // observed offset
		
		// Transform states by discrete Fourier transform (DFT).
		Complex[] z = new Complex[N];
		for(int i=0; i<N; i++)
		{
			z[i] = new Complex(0);
			for(int k=0; k<N; k++)
			{
				z[i] = z[i].add(ComplexUtils.polar2Complex(1.0/N, -2.0*pi*k/N*i).multiply(x[k]));
			}
		}
		
		// Update step
		// Formula in Matlab: zp = z + ct/N .* exp(-2*pi*j * K.' * l/N) * (h-y);
		for(int i=0; i<N; i++)
		{
			double c;
			if(i == 0)
				c=c0;
			else
				c=c1;
			z[i] = z[i].add(ComplexUtils.polar2Complex(c/N, -2.0*pi*i/N*l).multiply(h-y));
		}
		
		// Transform states back by inverse discrete Fourier transformation (IDFT).
		for(int i=0; i<N; i++)
		{
			Complex xi = new Complex(0);
			for(int k=0; k<N; k++)
			{
				xi = xi.add(ComplexUtils.polar2Complex(1.0, 2.0*pi*k/N*i).multiply(z[k]));
			}
			x[i] = xi.getReal();
		}
		
		// Save result
		state.setEstimatedOffsets(x);
		saveState(state, measurementContext, microfluidicChipID);
		
		sendMessage("Observed droplet offset of " + Integer.toString((int)dropletOffset) + "um, estimated before "+Integer.toString((int)y) + "um. Changed mean droplet offset estimate to "+Integer.toString((int)z[0].getReal())+"um.");
		
		return new DropletObserverResult(l, x);
	}
	private static String getStateIdentifier(int microfluidicChipID)
	{
		return DefaultObserverConfiguration.TYPE_IDENTIFIER+".Chip"+Integer.toString(microfluidicChipID);
	}
	private ObserverState loadState(MeasurementContext measurementContext,  int microfluidicChipID) throws RemoteException
	{
		String identifier = getStateIdentifier(microfluidicChipID);
		ObserverState observerState = measurementContext.getProperty(identifier, ObserverState.class);
		if(observerState == null)
			observerState = new ObserverState();
		return observerState;
	}
	private void saveState(ObserverState state, MeasurementContext measurementContext,  int microfluidicChipID) throws RemoteException
	{
		String identifier = getStateIdentifier(microfluidicChipID);
		measurementContext.setProperty(identifier, state);
	}
	
	@Override
	public void registerDroplet(MeasurementContext measurementContext, int microfluidicChipID)
			throws ResourceException, RemoteException {
		ObserverState state = loadState(measurementContext, microfluidicChipID);
		state.registerDroplet(getConfiguration());
		saveState(state, measurementContext, microfluidicChipID);
		
	}
	@Override
	public void unregisterDroplet(MeasurementContext measurementContext, int microfluidicChipID)
			throws ResourceException, RemoteException {
		// Do nothing; Observer is anyway reseted when measurement finishes.
		
	}
}
