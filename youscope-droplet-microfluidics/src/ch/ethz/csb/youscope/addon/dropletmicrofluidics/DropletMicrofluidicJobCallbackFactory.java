/**
 * 
 */
package ch.ethz.csb.youscope.addon.dropletmicrofluidics;

import ch.ethz.csb.youscope.client.addon.CallbackAddonFactory;
import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.measurement.callback.Callback;
import ch.ethz.csb.youscope.shared.measurement.callback.CallbackCreationException;

/**
 * Callback providing an UI for droplet based microfluidic measurements.
 * @author Moritz Lang
 */
public class DropletMicrofluidicJobCallbackFactory implements CallbackAddonFactory
{
    private DropletMicrofluidicJobCallbackImpl callback = null;
	@Override
	public synchronized Callback createCallback(String typeIdentifier, YouScopeClient client,
			YouScopeServer server) throws CallbackCreationException {
		if(!DropletMicrofluidicJobCallback.TYPE_IDENTIFIER.equals(typeIdentifier))
		{
			throw new CallbackCreationException("Construction of callbacks with type identifiers "+typeIdentifier+" not supported by this factory.");
		}
		if(callback != null)
			return callback;
		callback = new DropletMicrofluidicJobCallbackImpl(client);
		return callback;
	}

	@Override
	public String[] getSupportedTypeIdentifiers() {
		return new String[]{DropletMicrofluidicJobCallback.TYPE_IDENTIFIER};
	}

	@Override
	public boolean isSupportingTypeIdentifier(String typeIdentifier) {
		return DropletMicrofluidicJobCallback.TYPE_IDENTIFIER.equals(typeIdentifier);
	}
}
