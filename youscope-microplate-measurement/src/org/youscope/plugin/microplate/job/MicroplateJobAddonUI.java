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
package org.youscope.plugin.microplate.job;



import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.job.Job;
import org.youscope.serverinterfaces.YouScopeServer;


/**
 * @author Moritz Lang
 *
 */
class MicroplateJobAddonUI extends JobAddonUIAdapter<MicroplateJobConfiguration>
{
	/**
	 * Constructor.
	 * @param server YouScope server.
	 * @param YouScope client.
	 * @throws AddonException 
	 */
	MicroplateJobAddonUI(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
		
		setTitle("Microplate Job");
		addPage(new MicroplatePage(client));
		addPage(new WellSelectionPage(client, server));  
		addPage(new PathPage(client, server));
		addPage(new ImagingProtocolPage(client, server));
	}
	static ComponentMetadataAdapter<MicroplateJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<MicroplateJobConfiguration>(MicroplateJobConfiguration.TYPE_IDENTIFIER, 
				MicroplateJobConfiguration.class, 
				Job.class, 
				"Microplate", 
				new String[]{"containers"},
				"A job realizing most of the functionality of a microplate measurement, but which can be combined with other jobs in a different kind of measurement. Takes images/executes a certain set of child jobs at the activated positions of a microplate or microplate-like object like microfluidic channels.",
				"icons/map.png");
	}
}
