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
/**
 * 
 */
package org.youscope.plugin.continuationmeasurement;

import java.rmi.RemoteException;
import java.util.ServiceLoader;

import org.youscope.addon.AddonException;
import org.youscope.addon.measurement.MeasurementAddonFactory;
import org.youscope.addon.measurement.MeasurementInitializer;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.common.task.Task;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author langmo
 * 
 */
class ContinuationMeasurementInitializer implements MeasurementInitializer<ContinuationMeasurementConfiguration>
{

	@Override
	public void initializeMeasurement(Measurement measurement, ContinuationMeasurementConfiguration configuration, ConstructionContext constructionContext) throws ConfigurationException, AddonException
	{
		MeasurementConfiguration encapsulatedConfiguration = configuration.getEncapsulatedConfiguration();
		if(encapsulatedConfiguration == null)
			throw new ConfigurationException("No measurement configuration encapsulated.");
		String encapsulatedTypeIdentifier = encapsulatedConfiguration.getTypeIdentifier();
		
		// Initialize encapsulated configuration.
		MeasurementAddonFactory encapsulatedAddonFactory = null;
		ServiceLoader<MeasurementAddonFactory> addonFactories = ServiceLoader.load(MeasurementAddonFactory.class, ContinuationMeasurementInitializer.class.getClassLoader());
		for(MeasurementAddonFactory addon : addonFactories)
		{
			if(addon.isSupportingTypeIdentifier(encapsulatedTypeIdentifier))
				encapsulatedAddonFactory = addon;
		}
		if (encapsulatedAddonFactory == null)
            throw new AddonException("Type of encapsulated measurement configuration (" + encapsulatedTypeIdentifier + ") unknown.");
		encapsulatedAddonFactory.initializeMeasurement(measurement, encapsulatedConfiguration, constructionContext);
		
		// Change initial frame numbers
		try {
			for(Task task : measurement.getTasks())
			{
				task.setInitialExecutionNumber(configuration.getDeltaEvaluationNumber());
			}
		} 
		catch (RemoteException | ComponentRunningException | IllegalArgumentException e) {
			throw new AddonException("Could not set initial execution number of tasks.", e);
		}
		try {
			measurement.setInitialRuntime(configuration.getPreviousRuntime());
		} catch (RemoteException | ComponentRunningException | IllegalArgumentException e1) {
			throw new AddonException("Could not set initial runtime of measurement.", e1);
		}
		try {
			measurement.getSaver().setSaveSettings(new EncapsulatedSaveSettings(measurement.getSaver().getSaveSettings(), configuration.getMeasurementFolder()));
		} catch (ComponentRunningException | RemoteException e) {
			throw new AddonException("Could not encapsulate save settings.", e);
		}
	}
}
