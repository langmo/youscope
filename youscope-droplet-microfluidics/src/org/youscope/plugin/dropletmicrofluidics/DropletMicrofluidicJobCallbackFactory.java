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
package org.youscope.plugin.dropletmicrofluidics;

import org.youscope.addon.callback.CallbackAddonFactory;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.callback.Callback;
import org.youscope.common.callback.CallbackCreationException;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * Callback providing an UI for droplet based microfluidic measurements.
 * @author Moritz Lang
 */
public class DropletMicrofluidicJobCallbackFactory implements CallbackAddonFactory
{
    @Override
	public synchronized Callback createCallback(String typeIdentifier, YouScopeClient client,
			YouScopeServer server) throws CallbackCreationException {
		if(!DropletMicrofluidicJobCallback.TYPE_IDENTIFIER.equals(typeIdentifier))
		{
			throw new CallbackCreationException("Construction of callbacks with type identifiers "+typeIdentifier+" not supported by this factory.");
		}
		return new DropletMicrofluidicJobCallbackImpl(client);
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
