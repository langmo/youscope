/**
 * 
 */
package ch.ethz.csb.youscope.server.addon.measurement;


/**
 * @author langmo
 * 
 */
public interface MeasurementConstructionAddonFactory
{
	/**
	 * Returns a new measurement construction addon for the given ID, or null if addon does not support the configuration of measurements with the given ID.
     * @param ID The ID for which a measurement should be created..
	 * 
	 * @return New measurement construction addon, or null.
	 */
	MeasurementConstructionAddon createMeasurementConstructionAddon(String ID);

	/**
	 * Returns a list of all measurement configuration types supported by this addon
	 * 
	 * @return List of supported configurations.
	 */
	String[] getSupportedConfigurationIDs();

	/**
	 * Returns true if this addon supports measurement configurations with the given ID, false otherwise.
	 * @param ID The ID of the measurement configuration for which it should be queried if this addon supports its construction.
	 * @return True if this addon supports measurement configurations with the given ID, false otherwise.
	 */
	boolean supportsConfigurationID(String ID);
}
