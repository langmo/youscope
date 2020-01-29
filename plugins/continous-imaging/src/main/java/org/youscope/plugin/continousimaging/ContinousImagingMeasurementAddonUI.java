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
package org.youscope.plugin.continousimaging;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.addon.measurement.MeasurementAddonUIAdapter;
import org.youscope.addon.measurement.pages.GeneralSettingsPage;
import org.youscope.addon.measurement.pages.MetadataPage;
import org.youscope.addon.measurement.pages.StartAndEndSettingsPage;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.measurement.Measurement;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 */
class ContinousImagingMeasurementAddonUI extends MeasurementAddonUIAdapter<ContinousImagingMeasurementConfiguration>
{
	private final static String DESCRIPTION = "A continuous imaging measurement is used to (rapidly) take images at the current position every given period.\n\n"+
			"One can select the channel, the exposure time and the imaging period. Instead of choosing an imaging period, one can also choose to \"bulk image\", which means to image as fast as possible.";
	/**
	 * Constructor.
	 * @param server YouScope server.
	 * @param client YouScope client.
	 * @throws AddonException 
	 */
	ContinousImagingMeasurementAddonUI(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
		
		setTitle("Continuous Imaging Measurement");
		
		
		addPage(new MetadataPage<>(client));
		addPage(new GeneralSettingsPage<ContinousImagingMeasurementConfiguration>(client, ContinousImagingMeasurementConfiguration.class)); 
		addPage(new StartAndEndSettingsPage(client, server));
		addPage(new ImagingDefinitionPage(client, server));

	}
	
	static ComponentMetadataAdapter<ContinousImagingMeasurementConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<ContinousImagingMeasurementConfiguration>(ContinousImagingMeasurementConfiguration.TYPE_IDENTIFIER, 
				ContinousImagingMeasurementConfiguration.class, 
				Measurement.class, "Continuous Imaging Measurement", new String[0], 
				DESCRIPTION,
				"icons/camcorder.png");
	}
}
