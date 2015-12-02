/**
 * 
 */
package ch.ethz.csb.youscope.addon.livemodifiablejob;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.client.addon.CallbackAddonFactory;
import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.measurement.callback.Callback;
import ch.ethz.csb.youscope.shared.measurement.callback.CallbackCreationException;

/**
 * @author Moritz Lang
 */
public class LiveModifiableJobCallbackFactory implements CallbackAddonFactory
{
    private LiveModifiableJobCallbackImpl callback = null;
	
	@Override
	public String[] getSupportedTypeIdentifiers() {
		return new String[]{LiveModifiableJobCallbackImpl.TYPE_IDENTIFIER};
	}

	@Override
	public boolean isSupportingTypeIdentifier(String typeIdentifier) {
		return LiveModifiableJobCallback.TYPE_IDENTIFIER.equals(typeIdentifier);
	}

	@Override
	public synchronized Callback createCallback(String typeIdentifier,
			YouScopeClient client, YouScopeServer server) throws CallbackCreationException {
		if(!isSupportingTypeIdentifier(typeIdentifier))
			throw new CallbackCreationException("Factory does not support type identifier "+typeIdentifier+".");
		if(callback != null)
			return callback;
		try {
			callback = new LiveModifiableJobCallbackImpl(client, server);
		} catch (RemoteException e) {
			throw new CallbackCreationException("Could not create callback due to remote exception.", e);
		}
		return callback;
	}
}
