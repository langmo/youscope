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
package org.youscope.plugin.livemodifiablejob;

import java.rmi.RemoteException;

import org.youscope.addon.callback.CallbackAddonFactory;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.callback.Callback;
import org.youscope.common.callback.CallbackCreationException;
import org.youscope.serverinterfaces.YouScopeServer;

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
