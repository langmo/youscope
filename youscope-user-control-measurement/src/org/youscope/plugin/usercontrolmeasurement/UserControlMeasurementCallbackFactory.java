/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.youscope.plugin.usercontrolmeasurement;


import java.rmi.RemoteException;

import org.youscope.addon.callback.CallbackAddonFactory;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.callback.Callback;
import org.youscope.common.callback.CallbackCreationException;
import org.youscope.serverinterfaces.YouScopeServer;

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
