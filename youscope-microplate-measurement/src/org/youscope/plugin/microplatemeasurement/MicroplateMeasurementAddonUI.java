/**
 * 
 */
package org.youscope.plugin.microplatemeasurement;


import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.addon.measurement.MeasurementAddonUIAdapter;
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
		
		addPage(new StartPage());
		addPage(new GeneralSettingsPage(client, server));
		addPage(new MicroplatePage(client));
		addPage(new WellSelectionPage(client, server));
		addPage(new StartAndEndSettingsPage(client, server));
		addPage(new ImagingProtocolPage(client, server));
		addPage(new MiscPage(client, server));
	}
	static ComponentMetadataAdapter<MicroplateMeasurementConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<MicroplateMeasurementConfiguration>(MicroplateMeasurementConfiguration.TYPE_IDENTIFIER, 
				MicroplateMeasurementConfiguration.class, 
				Measurement.class, "Microplate Measurement", new String[0], "icons/map.png");
	}
}
