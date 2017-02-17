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
package org.youscope.plugin.composedimaging;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.addon.measurement.MeasurementAddonUIAdapter;
import org.youscope.addon.measurement.pages.GeneralSettingsPage;
import org.youscope.addon.measurement.pages.MetadataPage;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.measurement.Measurement;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 */
class ComposedImagingMeasurementAddonUI extends MeasurementAddonUIAdapter<ComposedImagingMeasurementConfiguration>
{
	/**
	 * Constructor.
	 * @param client YouScope client.
	 * @param server YouScope server.
	 * @throws AddonException 
	 */
	ComposedImagingMeasurementAddonUI(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
		
		setTitle("Composed Imaging Measurement");
		addPage(new MetadataPage<>(client));
		addPage(new GeneralSettingsPage<ComposedImagingMeasurementConfiguration>(client, ComposedImagingMeasurementConfiguration.class));
		addPage(new StartAndEndConfigurationPage(client, server));
		addPage(new ImagingConfigurationPage(client, server));
		addPage(new AreaConfigurationPage(client, server));
	}
	
	static ComponentMetadataAdapter<ComposedImagingMeasurementConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<ComposedImagingMeasurementConfiguration>(ComposedImagingMeasurementConfiguration.TYPE_IDENTIFIER, 
				ComposedImagingMeasurementConfiguration.class, 
				Measurement.class, "Composed Imaging Measurement", new String[]{"misc"},
				"Takes partly overlapping images on a rectangular grid, such that the resulting images can be stitched together using a 3rd party algorithm (stitching is not part of the measurement).",
				"icons/layers-group.png");
	}
}
