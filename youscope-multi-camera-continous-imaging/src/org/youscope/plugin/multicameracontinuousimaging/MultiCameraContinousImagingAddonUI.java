/**
 * 
 */
package org.youscope.plugin.multicameracontinuousimaging;

import javax.swing.Icon;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.addon.measurement.MeasurementAddonUIAdapter;
import org.youscope.addon.measurement.pages.DescriptionPage;
import org.youscope.addon.measurement.pages.GeneralSettingsPage;
import org.youscope.addon.measurement.pages.MetadataPage;
import org.youscope.addon.measurement.pages.StartAndEndSettingsPage;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.measurement.Measurement;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.ImageLoadingTools;

/**
 * @author Moritz Lang
 */
class MultiCameraContinousImagingAddonUI extends MeasurementAddonUIAdapter<MultiCameraContinousImagingConfiguration>
{
	/**
	 * Constructor.
	 * @param microscope Interface to the microscope.
	 * @param client Interface to the client.
	 * @throws AddonException 
	 */
	MultiCameraContinousImagingAddonUI(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
		
		setTitle("Multi-Cam Imaging");
		
		String description = "A multi-camera continuous imaging measurement is used to (rapidly) take images with several cameras in parallel at the current position every given period.\n\n"+
				"One can select the channel, the exposure time and the imaging period. Instead of choosing an imaging period, one can also choose to \"bulk image\", which means to image as fast as possible.";
		Icon image = ImageLoadingTools.getResourceIcon("org/youscope/plugin/multicameracontinuousimaging/images/continous-imaging.jpg", "Multi-Camera Measurement");
		addPage(new DescriptionPage(null, description, image, null));
		addPage(new MetadataPage<>(client));
		addPage(new GeneralSettingsPage<MultiCameraContinousImagingConfiguration>(client, MultiCameraContinousImagingConfiguration.class));
		addPage(new StartAndEndSettingsPage(client, server));
		addPage(new ImagingDefinitionPage(client, server));
	}
	
	static ComponentMetadataAdapter<MultiCameraContinousImagingConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<MultiCameraContinousImagingConfiguration>(MultiCameraContinousImagingConfiguration.TYPE_IDENTIFIER, 
				MultiCameraContinousImagingConfiguration.class, 
				Measurement.class, "Multi-Cam Imaging", new String[]{"misc"});
	}	
}
