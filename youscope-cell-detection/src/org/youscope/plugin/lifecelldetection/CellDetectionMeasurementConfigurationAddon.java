/**
 * 
 */
package org.youscope.plugin.lifecelldetection;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.addon.measurement.MeasurementAddonUIAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.common.measurement.Measurement;

/**
 * @author Moritz Lang
 */
class CellDetectionMeasurementConfigurationAddon extends MeasurementAddonUIAdapter<CellDetectionMeasurementConfiguration>
{
	/**
	 * Constructor.
	 * @param microscope Interface to the microscope.
	 * @param client Interface to the client.
	 * @throws AddonException 
	 */
	CellDetectionMeasurementConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
		
		setTitle("Continuous Cell Detection Measurement");
		
		addPage(new StartPage());
		addPage(new GeneralSettingsPage(client, server));
		addPage(new StartAndEndConfigurationPage(client, server));
		addPage(new ImagingConfigurationPage(client, server));
	}
	
	static ComponentMetadataAdapter<CellDetectionMeasurementConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<CellDetectionMeasurementConfiguration>(CellDetectionMeasurementConfiguration.TYPE_IDENTIFIER, 
				CellDetectionMeasurementConfiguration.class, 
				Measurement.class, "Cell-Detection", new String[]{"misc"}, "icons/smiley-mr-green.png");
	}
}
