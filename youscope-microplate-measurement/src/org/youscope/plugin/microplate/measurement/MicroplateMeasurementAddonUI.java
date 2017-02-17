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
package org.youscope.plugin.microplate.measurement;



import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.addon.measurement.MeasurementAddonUIAdapter;
import org.youscope.addon.measurement.pages.MetadataPage;
import org.youscope.addon.measurement.pages.StartAndEndSettingsPage;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.measurement.Measurement;
import org.youscope.serverinterfaces.YouScopeServer;


/**
 * @author Moritz Lang
 *
 */
class MicroplateMeasurementAddonUI extends MeasurementAddonUIAdapter<MicroplateMeasurementConfiguration>
{
	/**
	 * Constructor.
	 * @param server YouScope server.
	 * @param YouScope client.
	 * @throws AddonException 
	 */
	MicroplateMeasurementAddonUI(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
		
		setTitle("Microplate Measurement");
		
		addPage(new MetadataPage<>(client));
		addPage(new GeneralSettingsPage(client));
		addPage(new MicroplatePage(client));
		addPage(new WellSelectionPage(client, server));  
		addPage(new PathPage(client, server));
		addPage(new StartAndEndSettingsPage(client, server));
		addPage(new ImagingProtocolPage(client, server));
		addPage(new MiscPage(client, server)); 
	}
	static ComponentMetadataAdapter<MicroplateMeasurementConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<MicroplateMeasurementConfiguration>(MicroplateMeasurementConfiguration.TYPE_IDENTIFIER, 
				MicroplateMeasurementConfiguration.class, 
				Measurement.class, "Microplate Measurement", new String[0], 
				"Takes images/executes a certain set of jobs at the activated well/tile of a microplate or microplate-like object like microfluidic channels. From a list of pre-defined microplate layouts, an appropriate layout can be chosen such that not all imaging positions have to be manually defined. Alternatively, a custom microplate layout can be defined.",
				"icons/map.png");
	}
}
