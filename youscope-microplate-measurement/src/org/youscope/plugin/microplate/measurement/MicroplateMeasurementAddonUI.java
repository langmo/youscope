/**
 * 
 */
package org.youscope.plugin.microplate.measurement;


import javax.swing.ImageIcon;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.addon.measurement.MeasurementAddonUIAdapter;
import org.youscope.addon.measurement.pages.DescriptionPage;
import org.youscope.addon.measurement.pages.StartAndEndSettingsPage;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.measurement.Measurement;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.ImageLoadingTools;


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
		
		String description = "A microplate measurement helps to perform an identical imaging protocol for several wells and/or positions in a microplate.\n\n" +
				"To configure such a measurement, the wells to be measured are selected and combined with the imaging protocol consisting of several subelements, called jobs.\n\n" +
				"One job thereby corresponds to a single step of the imaging protocol, like taking a bright-field or a green fluorescence image.";
		ImageIcon microplateMeasurementIcon = ImageLoadingTools.getResourceIcon("org/youscope/plugin/microplate/measurement/images/microplate-measurement.jpg", "Microplate Measurement");
		String imageLegend = "Flowchart of a microplate measurement.";
		addPage(new DescriptionPage(null, description, microplateMeasurementIcon, imageLegend));
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
