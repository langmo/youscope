/**
 * 
 */
package org.youscope.plugin.composedimaging;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.addon.measurement.MeasurementAddonUIAdapter;
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
		
		addPage(new StartPage());
		addPage(new GeneralSettingsPage(client, server));
		addPage(new StartAndEndConfigurationPage(client, server));
		addPage(new ImagingConfigurationPage(client, server));
		addPage(new AreaConfigurationPage(client, server));
	}
	
	static ComponentMetadataAdapter<ComposedImagingMeasurementConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<ComposedImagingMeasurementConfiguration>(ComposedImagingMeasurementConfiguration.TYPE_IDENTIFIER, 
				ComposedImagingMeasurementConfiguration.class, 
				Measurement.class, "Composed Imaging Measurement", new String[]{"misc"}, "icons/layers-group.png");
	}
}
