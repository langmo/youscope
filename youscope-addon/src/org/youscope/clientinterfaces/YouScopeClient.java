/**
 * 
 */
package org.youscope.clientinterfaces;

import javax.script.ScriptEngineFactory;

import org.youscope.addon.measurement.MeasurementAddonFactory;
import org.youscope.addon.microplate.MicroplateAddonFactory;
import org.youscope.addon.postprocessing.PostProcessorAddonFactory;
import org.youscope.addon.tool.ToolAddonFactory;
import org.youscope.common.configuration.MeasurementConfiguration;
import org.youscope.common.measurement.Measurement;


/**
 * Interface through which an addon can access the client.
 * @author langmo
 *
 */
public interface YouScopeClient
{
	
	/** 
	 * Returns an interface through which an addon can save properties as well as access the properties of the YouScope client.
	 * @return Interface to save and load properties.
	 */
	YouScopeProperties getProperties();
	/**
	 * Creates a new content window. The content window is usually
	 * either a frame or an internal frame (default).  
	 * @return New Frame.
	 */
	YouScopeFrame createFrame();
	
	/**
	 * Returns all known measurement addons.
	 * @return Iterable through all known measurement addons.
	 */
	Iterable<MeasurementAddonFactory> getMeasurementAddons();
	
	/**
     * Returns a list of the last saved mesurements.
     * 
     * @return The list of measurements.
     */
    MeasurementConfiguration[] getLastSavedMeasurements();
    
	/**
	 * Returns all known measurement post processor addons.
	 * @return Iterable through all known measurement post proccessor addons.
	 */
	Iterable<PostProcessorAddonFactory> getMeasurementPostProcessorAddons();

	/**
	 * Returns all known tool addons.
	 * @return Iterable through all known tool addons.
	 */
	Iterable<ToolAddonFactory> getToolAddons();
	
	/**
	 * Returns all known microplate types.
	 * @return Iterable through all known microplate types.
	 */
	Iterable<MicroplateAddonFactory> getMicroplateTypeAddons();
	
	/**
	 * Returns the microplate type addon with the given ID, or null if this addon is unknown.
	 * @param addonID The ID of the microplate type.
	 * @return A factory to create the addon, or null if the addon ID is unknown.
	 */
	MicroplateAddonFactory getMicroplateTypeAddon(String addonID);
    
	/**
	 * Returns the measurement addon with the given ID, or null if this addon is unknown.
	 * @param addonID The ID of the addon.
	 * @return A factory to create the addon, or null if the addon ID is unknown.
	 */
    MeasurementAddonFactory getMeasurementAddon(String addonID);
    /**
	 * Returns the measurement post processing addon with the given ID, or null if this addon is unknown.
	 * @param addonID The ID of the addon.
	 * @return A factory to create the addon, or null if the addon ID is unknown.
	 */
    PostProcessorAddonFactory getMeasurementPostProcessorAddon(String addonID);
    /**
	 * Returns the tool addon with the given ID, or null if this addon is unknown.
	 * @param addonID The ID of the addon.
	 * @return A factory to create the addon, or null if the addon ID is unknown.
	 */
    ToolAddonFactory getToolAddon(String addonID);
    
	/**
	 * Notifies the client that an error occurred, such that the client can notify the user in the
	 * client specific way.
	 * @param message Description of the error.
	 * @param error The error.
	 */
	void sendError(String message, Throwable error);
	
	/**
	 * Notifies the client that an error occurred, such that the client can notify the user in the
	 * client specific way.
	 * @param message Description of the error.
	 */
	void sendError(String message);
	
	/**
	 * Sends a human readable message to the client, which (may) be displayed to the user in the
	 * client specific way.
	 * @param message The message to send to the client.
	 */
	void sendMessage(String message);
	
	/**
	 * Returns true if the microscope is connected to this computer, and false, if the microscope is on a different computer.
	 * @return True if the microscope is connected to this computer, and false, if the microscope is on a different computer.
	 */
	boolean isLocalServer();
	
	/**
	 * Returns all script engine factories supported by the client.
	 * @return Iterable through all script engines.
	 */
	Iterable<ScriptEngineFactory> getScriptEngineFactories();
	
	/**
	 * Returns a script engine factory with the given name. 
	 * 
	 * Remark: engine names are case sensitive.
	 * 
	 * @param engineName The name of the script engine factory. Same as factory.getEngineName().
	 * @return script engine factory for given name, or null, if no script engine is supported for the given name.
	 */
	ScriptEngineFactory getScriptEngineFactory(String engineName);
	
	/**
	 * Opens the default editor to manipulate the measurement configuration.
	 * @param configuration The configuration of the measurement to edit.
	 * @return True if editor could be opened, wrong otherwise.
	 */
	boolean editMeasurement(MeasurementConfiguration configuration);
	
	/**
	 * Initializes the measurement and shows the measurement control frame.
	 * @param configuration The configuration of the measurement.
	 * @return The initialized measurement, or null if not successful.
	 */
	Measurement initializeMeasurement(MeasurementConfiguration configuration);
	
	/**
	 * Opens the measurement control frame for a measurement.
	 * @param measurement The measurement to control.
	 * @return True if successful, false otherwise.
	 */
	boolean initializeMeasurement(Measurement measurement);
	
	/**
	 * Returns a class with which client addons can be constructed.
	 * @return client addon provider.
	 */
	ClientAddonProvider getAddonProvider();
}
