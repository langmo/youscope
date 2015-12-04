/**
 * 
 */
package org.youscope.plugin.waitforuser;


import java.rmi.RemoteException;

import org.youscope.addon.callback.CallbackAddonFactory;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.measurement.callback.Callback;
import org.youscope.common.measurement.callback.CallbackCreationException;
import org.youscope.serverinterfaces.YouScopeServer;

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
