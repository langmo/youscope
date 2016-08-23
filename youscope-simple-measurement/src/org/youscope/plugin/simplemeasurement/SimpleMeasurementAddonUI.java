/**
 * 
 */
package org.youscope.plugin.simplemeasurement;

import javax.swing.ImageIcon;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.addon.measurement.MeasurementAddonUIAdapter;
import org.youscope.addon.measurement.pages.DescriptionPage;
import org.youscope.addon.measurement.pages.StartAndEndSettingsPage;
import org.youscope.addon.measurement.pages.GeneralSettingsPage;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.measurement.Measurement;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.ImageLoadingTools;


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

		String description = "A simple measurement is the easiest type of measurement in YouScope. With it, one can perform an imaging protocol at the current stage position one or several times.\n\n" +
				"To configure a simple measurement, the imaging protocol consisting of several subelements, called jobs, has to be defined, as well as the timing of the measurement (if more than one itertion through the protocol is intended).\n" +
				"One job thereby corresponds to a single step of the imaging protocol, like taking a bright-field or a green fluorescence image.";
		ImageIcon image = ImageLoadingTools.getResourceIcon("org/youscope/plugin/simplemeasurement/images/simpleMeasurement.jpg", "Simple Measurement");
		addPage(new DescriptionPage(null, description, image, null)); 
		
		addPage(new GeneralSettingsPage<SimpleMeasurementConfiguration>(client, SimpleMeasurementConfiguration.class));
		addPage(new StartAndEndSettingsPage(client, server));
		addPage(new ImagingProtocolPage(client, server));
		addPage(new MiscPage(client, server));
	}
	static ComponentMetadataAdapter<SimpleMeasurementConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<SimpleMeasurementConfiguration>(SimpleMeasurementConfiguration.TYPE_IDENTIFIER, 
				SimpleMeasurementConfiguration.class, 
				Measurement.class, "Simple Measurement", new String[0], 
				"The most simple customizable type of measurement in YouScope. A certain list of jobs (elementary or composed actions like moving the stage or taking an image) is defined, which are executed once or several times with a fixed frequency or in burst mode.", 
				"icons/paper-plane.png");
	}
}
