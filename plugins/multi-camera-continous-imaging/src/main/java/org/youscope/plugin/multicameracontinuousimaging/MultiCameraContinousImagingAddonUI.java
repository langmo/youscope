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
package org.youscope.plugin.multicameracontinuousimaging;

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
class MultiCameraContinousImagingAddonUI extends MeasurementAddonUIAdapter<MultiCameraContinousImagingConfiguration>
{
	/**
	 * Constructor.
	 * @param microscope Interface to the microscope.
	 * @param client Interface to the client.
	 * @throws AddonException 
	 */
	MultiCameraContinousImagingAddonUI(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
		
		setTitle("Multi-Cam Imaging");
		
		addPage(new MetadataPage<>(client));
		addPage(new GeneralSettingsPage<MultiCameraContinousImagingConfiguration>(client, MultiCameraContinousImagingConfiguration.class));
		addPage(new StartAndEndSettingsPage(client, server));
		addPage(new ImagingDefinitionPage(client, server));
	}
	
	static ComponentMetadataAdapter<MultiCameraContinousImagingConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<MultiCameraContinousImagingConfiguration>(MultiCameraContinousImagingConfiguration.TYPE_IDENTIFIER, 
				MultiCameraContinousImagingConfiguration.class, 
				Measurement.class, "Multi-Cam Imaging", new String[]{"misc"});
	}	
}
