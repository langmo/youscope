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

import org.youscope.addon.microscopeaccess.ReadOnlyPropertyInternal;
import org.youscope.common.microscope.ReadOnlyProperty;

/**
 * @author Moritz Lang
 * 
 */
class ReadOnlyPropertyRMI extends PropertyRMI implements ReadOnlyProperty
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 3130755396734654776L;

	/**
	 * Constructor.
	 * @throws RemoteException
	 */
	ReadOnlyPropertyRMI(ReadOnlyPropertyInternal readOnlyProperty, int accessID) throws RemoteException
	{
		super(readOnlyProperty, accessID);
	}
}
