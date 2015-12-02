/**
 * 
 */
package ch.ethz.csb.youscope.client.addon.measurement;

import javax.swing.ImageIcon;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.shared.YouScopeServer;

/**
 * @author langmo
 */
public interface MeasurementConfigurationAddonFactory
{

    /**
     * Returns a new measurement configuration addon for the given ID, or null if addon does not support the configuration of measurements witht the given ID.
     * @param ID The ID for which a measurement should be created.
     * @param client Interface to allow the addon to communicate with the client.
     * @param server Interface to the server.
     * 
     * @return The created addon to configure a measurement.
     */
    MeasurementConfigurationAddon createMeasurementConfigurationAddon(String ID, YouScopeClient client, YouScopeServer server);

    /**
	 * Returns a list of all measurement configuration types supported by this addon
	 * 
	 * @return List of supported configurations.
	 */
	String[] getSupportedConfigurationIDs();

	/**
	 * Returns true if this addon supports measurement configurations with the given ID, false otherwise.
	 * @param ID The ID of the measurement configuration for which it should be querried if this addon supports its construction.
	 * @return True if this addon supports measurement configurations with the given ID, false otherwise.
	 */
	boolean supportsConfigurationID(String ID);

    /**
     * Should return a short human readable name of the measurement which corresponds to the given ID. If the addon does
     * not support the configuration of measurements with the given ID, null should be returned.
     * The name may or may not consist of sub-strings separated by slashes (e.g. "Misc/Foo"). The last string corresponds to the base name.
     * In this case, a client may or may not display some kind of folder structure to navigate to a given addon.
     * @param ID The ID of the measurement for which the human readable name should be returned.
     * 
     * @return Human readable name of the measurement.
     */
    String getMeasurementName(String ID);
    
    /**
     * Returns an icon for this measurement, or null, if this measurement does not have an own icon.
     * @param ID The ID of the measurement for which the icon should be returned.
     * @return Icon for the measurement, or null.
     */
    ImageIcon getMeasurementIcon(String ID);
}
