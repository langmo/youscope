/**
 * 
 */
package org.youscope.plugin.multicameracontinuousimaging;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.addon.measurement.MeasurementAddonUIAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.measurement.Measurement;
import org.youscope.serverinterfaces.YouScopeServer;

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
		
		addPage(new StartPage());
		addPage(new GeneralSettingsPage(client, server));
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
