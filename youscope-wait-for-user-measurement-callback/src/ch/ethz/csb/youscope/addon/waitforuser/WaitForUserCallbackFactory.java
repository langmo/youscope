/**
 * 
 */
package ch.ethz.csb.youscope.addon.waitforuser;


import java.rmi.RemoteException;

import ch.ethz.csb.youscope.client.addon.CallbackAddonFactory;
import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.measurement.callback.Callback;
import ch.ethz.csb.youscope.shared.measurement.callback.CallbackCreationException;

/**
 * @author Moritz Lang
 *
 */
public class WaitForUserCallbackFactory implements CallbackAddonFactory
{
	@Override
	public Callback createCallback(String typeIdentifier,
			YouScopeClient client, YouScopeServer server) throws CallbackCreationException {
		if(WaitForUserCallback.TYPE_IDENTIFIER.equals(typeIdentifier))
		{
			try {
				return new WaitForUserCallbackImpl(client);
			} catch (RemoteException e) {
				throw new CallbackCreationException("Remote exception while creating callback.", e);
			}
		}
		throw new CallbackCreationException("Factory does not support creation of callbacks with type identifier "+ typeIdentifier+".");
	}

	@Override
	public String[] getSupportedTypeIdentifiers() {
		return new String[]{WaitForUserCallback.TYPE_IDENTIFIER};
	}

	@Override
	public boolean isSupportingTypeIdentifier(String typeIdentifier) {
		return WaitForUserCallback.TYPE_IDENTIFIER.equals(typeIdentifier);
	}
}
