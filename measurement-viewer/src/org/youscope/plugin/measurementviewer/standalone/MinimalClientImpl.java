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
package org.youscope.plugin.measurementviewer.standalone;

import org.youscope.clientinterfaces.ClientAddonProvider;
import org.youscope.clientinterfaces.MetadataDefinitionManager;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.clientinterfaces.PropertyProvider;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementConfiguration;

class MinimalClientImpl implements YouScopeClient { 

	private final MinimalPropertyProvider properties = new MinimalPropertyProvider();
	
	void close()
	{
		properties.saveProperties();
	}
 
	@Override
	public PropertyProvider getPropertyProvider() {
		return properties;
	}

	@Override
	public YouScopeFrame createFrame() {
		return new MinimalFrameImpl();
	}

	@Override
	public MeasurementConfiguration[] getLastSavedMeasurements() {
		return new MeasurementConfiguration[0];
	}

	@Override
	public void sendError(String message, Throwable error) {
		System.err.println(message+(error == null ? "" : " - " + error.getMessage()));
	}

	@Override
	public void sendError(String message) { 
		sendError(message, null);
	}
 
	@Override
	public void sendMessage(String message) {
		System.err.println(message);
	} 

	@Override
	public boolean isLocalServer() {
		return true;
	}

	@Override
	public boolean editMeasurement(MeasurementConfiguration configuration) {
		return false;
	}

	@Override
	public Measurement initializeMeasurement(MeasurementConfiguration configuration) {
		return null;
	}

	@Override
	public boolean initializeMeasurement(Measurement measurement) {
		return false;
	}

	@Override
	public ClientAddonProvider getAddonProvider() {
		return null;
	}

	@Override
	public MetadataDefinitionManager getMeasurementMetadataProvider() {
		return new MinimalMeasurementMetadataProvider();
	}

}
