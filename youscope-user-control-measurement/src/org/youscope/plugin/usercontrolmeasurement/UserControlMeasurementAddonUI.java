/**
 * 
 */
package org.youscope.plugin.usercontrolmeasurement;

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
class UserControlMeasurementAddonUI extends MeasurementAddonUIAdapter<UserControlMeasurementConfiguration>
{
	/**
	 * Constructor.
	 * @param server YouScope server.
	 * @param YouScope client.
	 * @throws AddonException 
	 */
	UserControlMeasurementAddonUI(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
		
		setTitle("User Control Measurement");
		
		addPage(new StartPage());
		addPage(new GeneralSettingsPage(client, server));
		addPage(new MonitorPage(client, server));
	}
	
	static ComponentMetadataAdapter<UserControlMeasurementConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<UserControlMeasurementConfiguration>(UserControlMeasurementConfiguration.TYPE_IDENTIFIER, 
				UserControlMeasurementConfiguration.class, 
				Measurement.class, "User Control Measurement", new String[0], "icons/user-worker-boss.png");
	}
}
