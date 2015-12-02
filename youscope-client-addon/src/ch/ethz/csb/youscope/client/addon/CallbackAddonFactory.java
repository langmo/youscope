/**
 * 
 */
package ch.ethz.csb.youscope.client.addon;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.measurement.callback.Callback;
import ch.ethz.csb.youscope.shared.measurement.callback.CallbackCreationException;

/**
 * A factory to create measurement callbacks.
 * Per measurement, there exist only one instance of each implementation of CallbackAddonFactory. Thus, singletons (per measurement) can be realized by
 * always returning the same callback object.
 * @author Moritz Lang
 */
public interface CallbackAddonFactory
{
	/**
     * Returns a new measurement callback for the given type identifier.
     * @param typeIdentifier The ID for which a callback should be created.
     * @param measurement Measurement which creates the callback.
     * @param client Interface to allow the addon to communicate with the client.
     * @param server Interface to the server.
     * @return The created addon for a given type identifier.
     * @throws CallbackCreationException Thrown if callback type identifier is not supported by this factory, or if any general error occurred during the callback creation.
     */
    Callback createCallback(String typeIdentifier, YouScopeClient client, YouScopeServer server) throws CallbackCreationException;

    /**
	 * Returns a list of all callback type identifiers supported by this factory.
	 * 
	 * @return List of supported callback types.
	 */
	String[] getSupportedTypeIdentifiers();

	/**
	 * Returns true if this factory supports callback with the given type identifier, false otherwise.
	 * @param typeIdentifier The ID of the callback type for which it should be queried if this factory supports its construction.
	 * @return True if this factory supports creating callback with the given ID, false otherwise.
	 */
	boolean isSupportingTypeIdentifier(String typeIdentifier);
}
