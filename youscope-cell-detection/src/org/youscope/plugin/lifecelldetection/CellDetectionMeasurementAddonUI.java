/**
 * 
 */
package org.youscope.plugin.lifecelldetection;

import javax.swing.Icon;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.addon.measurement.MeasurementAddonUIAdapter;
import org.youscope.addon.measurement.pages.DescriptionPage;
import org.youscope.addon.measurement.pages.GeneralSettingsPage;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.ImageLoadingTools;
import org.youscope.common.measurement.Measurement;

/**
 * @author Moritz Lang
 */
class CellDetectionMeasurementAddonUI extends MeasurementAddonUIAdapter<CellDetectionMeasurementConfiguration>
{
	/**
	 * Constructor.
	 * @param microscope Interface to the microscope.
	 * @param client Interface to the client.
	 * @throws AddonException 
	 */
	CellDetectionMeasurementAddonUI(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
		
		setTitle("Life Cell Detection Measurement");
		String description = "Continuously takes images in a given channel and detects the cells in it.<br>If selected, an image is automatically created highlighting all detected cells.";
		Icon image = ImageLoadingTools.getResourceIcon("org/youscope/plugin/lifecelldetection/images/life-cell-detection.jpg", "Life Cell Detection Measurement");
		addPage(new DescriptionPage(null, description, image, null));
		
		addPage(new GeneralSettingsPage<CellDetectionMeasurementConfiguration>(client, CellDetectionMeasurementConfiguration.class));
		addPage(new StartAndEndConfigurationPage(client, server));
		addPage(new ImagingConfigurationPage(client, server));
	}
	
	static ComponentMetadataAdapter<CellDetectionMeasurementConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<CellDetectionMeasurementConfiguration>(CellDetectionMeasurementConfiguration.TYPE_IDENTIFIER, 
				CellDetectionMeasurementConfiguration.class, 
				Measurement.class, "Cell-Detection", new String[]{"misc"},
				"A measurement providing several algorithms to segment microscopy images, and to extract information on the detected cells.", "icons/smiley-mr-green.png");
	}
}
