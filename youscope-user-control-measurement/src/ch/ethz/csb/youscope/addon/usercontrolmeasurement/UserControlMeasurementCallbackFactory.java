/**
 * 
 */
package ch.ethz.csb.youscope.addon.usercontrolmeasurement;


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
public class UserControlMeasurementCallbackFactory implements CallbackAddonFactory
{
	@Override
	public Callback createCallback(String typeIdentifier,
			YouScopeClient client, YouScopeServer server) throws CallbackCreationException {
		if(UserControlMeasurementCallbackImpl.TYPE_IDENTIFIER.equals(typeIdentifier))
		{
			try {
				return new UserControlMeasurementCallbackImpl(client, server);
			} catch (RemoteException e) {
				throw new CallbackCreationException("Remote error while creating callback.", e);
			}
		}
		throw new CallbackCreationException("Factory does not support creation of callbacks with type identifiers " + typeIdentifier+".");
	}

	@Override
	public String[] getSupportedTypeIdentifiers() {
		return new String[]{UserControlMeasurementCallbackImpl.TYPE_IDENTIFIER};
	}

	@Override
	public boolean isSupportingTypeIdentifier(String typeIdentifier) {
		return UserControlMeasurementCallbackImpl.TYPE_IDENTIFIER.equals(typeIdentifier);
	}
}
