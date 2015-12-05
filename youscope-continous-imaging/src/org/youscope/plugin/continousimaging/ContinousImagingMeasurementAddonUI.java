/**
 * 
 */
package org.youscope.plugin.continousimaging;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.addon.measurement.MeasurementAddonUIAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.measurement.Measurement;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 */
class ContinousImagingMeasurementAddonUI extends MeasurementAddonUIAdapter<ContinousImagingMeasurementConfiguration>
{
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
		
		addPage(new StartPage());
		addPage(new GeneralSettingsPage(client, server));
		addPage(new StartAndEndSettingsPage(client, server));
		addPage(new ImagingDefinitionPage(client, server));

	}
	
	static ComponentMetadataAdapter<ContinousImagingMeasurementConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<ContinousImagingMeasurementConfiguration>(ContinousImagingMeasurementConfiguration.TYPE_IDENTIFIER, 
				ContinousImagingMeasurementConfiguration.class, 
				Measurement.class, "Continuous Imaging Measurement", new String[0], "icons/camcorder.png");
	}
}
