/**
 * 
 */
package org.youscope.plugin.composedimaging;

import javax.swing.ImageIcon;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.addon.measurement.MeasurementAddonUIAdapter;
import org.youscope.addon.measurement.pages.DescriptionPage;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.measurement.Measurement;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.ImageLoadingTools;

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
		
		String description = "A composed imaging measurement is used to to take pictures in a two dimensional spatial array.</p><p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">The pictures are taken with a given overlap such that they can be composed afterwards.\n\nThis measurement type only takes the images, the stitching has to be done by an appropriate external program.";
		ImageIcon image = ImageLoadingTools.getResourceIcon("org/youscope/plugin/composedimaging/images/composed-imaging.jpg", "Composed Measurement");
		addPage(new DescriptionPage(null, description, image, null));
		
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
