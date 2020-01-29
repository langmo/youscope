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
package org.youscope.plugin.simplemeasurement;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.addon.measurement.MeasurementAddonUIAdapter;
import org.youscope.addon.measurement.pages.StartAndEndSettingsPage;
import org.youscope.addon.measurement.pages.GeneralSettingsPage;
import org.youscope.addon.measurement.pages.MetadataPage;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.measurement.Measurement;
import org.youscope.serverinterfaces.YouScopeServer;


/**
 * @author langmo
 *
 */
class SimpleMeasurementAddonUI extends MeasurementAddonUIAdapter<SimpleMeasurementConfiguration>
{
	/**
	 * Constructor.
	 * @param server YouScope server.
	 * @param YouScope client.
	 * @throws AddonException 
	 */
	SimpleMeasurementAddonUI(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
		setTitle("Simple Measurement");

		addPage(new MetadataPage<SimpleMeasurementConfiguration>(client));
		addPage(new GeneralSettingsPage<SimpleMeasurementConfiguration>(client, SimpleMeasurementConfiguration.class));
		addPage(new StartAndEndSettingsPage(client, server));
		addPage(new ImagingProtocolPage(client, server));
		addPage(new MiscPage(client, server));
	}
	static ComponentMetadataAdapter<SimpleMeasurementConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<SimpleMeasurementConfiguration>(SimpleMeasurementConfiguration.TYPE_IDENTIFIER, 
				SimpleMeasurementConfiguration.class, 
				Measurement.class, "Simple Measurement", new String[0], 
				"The most simple customizable type of measurement in YouScope. A certain list of jobs (elementary or composed actions like moving the stage or taking an image) is defined, which are executed once or several times with a fixed frequency or in burst mode.", 
				"icons/paper-plane.png");
	}
}
