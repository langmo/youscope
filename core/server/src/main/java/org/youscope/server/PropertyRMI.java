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
import java.rmi.server.UnicastRemoteObject;

import org.youscope.addon.microscopeaccess.PropertyInternal;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.Property;
import org.youscope.common.microscope.PropertyType;

/**
 * @author langmo
 */
abstract class PropertyRMI extends UnicastRemoteObject implements Property
{
	/**
	 * Serial Version UID.
	 */
	private static final long		serialVersionUID	= 5401100467596480263L;

	private final PropertyInternal	property;

	protected final int				accessID;

	/**
	 * Constructor.
	 * 
	 * @throws RemoteException
	 */
	PropertyRMI(PropertyInternal property, int accessID) throws RemoteException
	{
		super();
		this.property = property;
		this.accessID = accessID;
	}

	@Override
	public String getPropertyID()
	{
		return property.getPropertyID();
	}

	@Override
	public PropertyType getType()
	{
		return property.getType();
	}

	@Override
	public String getValue() throws MicroscopeException, InterruptedException
	{
		return property.getValue();
	}

	@Override
	public boolean isEditable()
	{
		return property.isEditable();
	}

	@Override
	public String getDeviceID() throws RemoteException
	{
		return property.getDeviceID();
	}

	@Override
	public void setValue(String value) throws RemoteException, MicroscopeException, MicroscopeLockedException, InterruptedException, DeviceException
	{
		property.setValue(value, accessID);
	}
}
