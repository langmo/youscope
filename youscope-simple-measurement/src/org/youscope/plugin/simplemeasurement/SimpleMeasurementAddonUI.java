/**
 * 
 */
package org.youscope.plugin.simplemeasurement;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.addon.measurement.MeasurementAddonUIAdapter;
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

		addPage(new StartPage());
		addPage(new GeneralSettingsPage(client, server));
		addPage(new StartAndEndSettingsPage(client, server));
		addPage(new ImagingProtocolPage(client, server));
		addPage(new MiscPage(client, server));
	}
	static ComponentMetadataAdapter<SimpleMeasurementConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<SimpleMeasurementConfiguration>(SimpleMeasurementConfiguration.TYPE_IDENTIFIER, 
				SimpleMeasurementConfiguration.class, 
				Measurement.class, "Simple Measurement", new String[0], "icons/paper-plane.png");
	}
}
