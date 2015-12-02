/**
 * 
 */
package ch.ethz.csb.youscope.server.addon;


import java.util.UUID;

import javax.script.ScriptEngineManager;

import ch.ethz.csb.youscope.shared.MessageListener;
import ch.ethz.csb.youscope.shared.measurement.ComponentProvider;
import ch.ethz.csb.youscope.shared.measurement.MeasurementSaver;
import ch.ethz.csb.youscope.shared.measurement.callback.CallbackProvider;

/**
 * @author langmo
 * 
 */
public interface ConstructionContext
{
	/**
	 * An interface with which images produced by image producing jobs can be saved.
	 * @return The image supervision.
	 */
	MeasurementSaver getMeasurementSaver();
	
	/**
	 * Returns a provider with which measurement components, such as jobs and resources, can be created given the corresponding configurations.
	 * @return Provider to create components.
	 */
	ComponentProvider getComponentProvider();
	
	/**
	 * Returns the script engine manager of the server.
	 * @return The script engine manager of the server.
	 */
	ScriptEngineManager getScriptEngineManager();
	
	/**
	 * Returns a message listener which can be used for logging/displaying short status messages or errors similar to System.out and System.err.
	 * @return message listener for logging.
	 */
	MessageListener getLogger();
	
	/**
	 * Returns an interface with which callbacks to the client can be obtained.
	 * @return callback provider.
	 */
	CallbackProvider getCallbackProvider();
	
	/**
	 * Returns the unique identifier of the measurement in which the component is created.
	 * @return Unique ID of created measurement.
	 */
	UUID getMeasurementUUID();
}
