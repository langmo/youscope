/**
 * 
 */
package org.youscope.common.configuration;

import java.io.Serializable;

/**
 * Marker interface that a given object is a YouScope configuration file.
 * Configuration files in YouScope can be saved and loaded, exported and
 * imported to XML, and are serializable and cloneable. By implementing this
 * interface, the respective class guarantees that it fulfills these
 * requirements. 
 * Any class implementing this interface should have a public no-arguments constructor.
 * Furthermore, it is expected to have meaningful XStream annotations.
 * 
 * @author Moritz Lang
 * 
 */
public interface Configuration extends Cloneable, Serializable 
{
	/**
	 * Returns a unique identifier for this configuration type.
	 * The identifier is used to determine factories and addons to construct and configure jobs, measurements, etc., given
	 * a corresponding configuration. Make sure that the ID returned is unique.
	 * The returned identifier should conform to "XXX.YYY", where XXX stands for an identifier of the organizational
	 * unit or the person who created the configuration (e.g. ETH, Smith, Cindarella), and YYY stands for an identifier
	 * of the specific kind of configuration (e.g. AutofocusJob, PlatePosition, MicroplateMeasurement). Optionally, additional hierarchy levels can be
	 * defined by additional separators ., e.g. "YouScope.AutofocusJob.Version1.1"
	 * The identifier should not contain spaces, non-ASCII characters, or colons (except for hierarchy level separation). Identifiers should start with
	 * a capital character, i.e. A-Z. 
	 * Make sure to not use the same identifier for different configurations, since this might result in unexpected behavior or errors.
	 * Make
	 * @return Unique identifier of the configuration type.
	 */
	public String getTypeIdentifier();
	
	/**
     * Checks the current state of the configuration. If the configuration state is valid, the method returns normally.
     * If not, a {@link ConfigurationException} is thrown describing the error, and what has to be changed in the configuration, by the user,
     * such that it becomes valid. This error can then e.g. be displayed to the user
     * (see ConfigurationAddonTools.displayConfigurationInvalid(ConfigurationException, YouScopeClient).
     * @throws ConfigurationException
     */
    void checkConfiguration() throws ConfigurationException;
}
