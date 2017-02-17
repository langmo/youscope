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
package org.youscope.server;

import java.rmi.RemoteException;

import org.youscope.addon.microscopeaccess.StringPropertyInternal;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.StringProperty;

/**
 * @author Moritz Lang
 * 
 */
class StringPropertyRMI extends PropertyRMI implements StringProperty
{
	/**
	 * Serial Version UID.
	 */
	private static final long				serialVersionUID	= 2756352088850819009L;
	private final StringPropertyInternal	stringProperty;

	/**
	 * Constructor.
	 * @throws RemoteException
	 */
	StringPropertyRMI(StringPropertyInternal stringProperty, int accessID) throws RemoteException
	{
		super(stringProperty, accessID);
		this.stringProperty = stringProperty;
	}

	@Override
	public void setValue(String value) throws RemoteException, MicroscopeException, MicroscopeLockedException, InterruptedException
	{
		stringProperty.setValue(value, accessID);
	}
}
