/**
 * 
 */
package org.youscope.plugin.dropletmicrofluidics;

import org.youscope.addon.callback.CallbackAddonFactory;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.measurement.callback.Callback;
import org.youscope.common.measurement.callback.CallbackCreationException;
import org.youscope.serverinterfaces.YouScopeServer;

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
