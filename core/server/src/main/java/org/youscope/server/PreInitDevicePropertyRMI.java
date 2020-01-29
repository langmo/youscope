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

import org.youscope.addon.microscopeaccess.PreInitDevicePropertyInternal;
import org.youscope.common.microscope.MicroscopeDriverException;
import org.youscope.common.microscope.PreInitDeviceProperty;
import org.youscope.common.microscope.PropertyType;

/**
 * @author langmo
 */
class PreInitDevicePropertyRMI extends UnicastRemoteObject implements PreInitDeviceProperty
{

	/**
	 * Serial Version UID.
	 */
	private static final long					serialVersionUID	= -2204579569726358661L;

	private final PreInitDevicePropertyInternal	deviceProperty;

	/**
	 * Constructor.
	 * @param deviceProperty 
	 * @param accessID  
	 * @throws RemoteException 
	 */
	public PreInitDevicePropertyRMI(PreInitDevicePropertyInternal deviceProperty, int accessID) throws RemoteException
	{
		super();
		this.deviceProperty = deviceProperty;
	}

	@Override
	public String getPropertyID() throws MicroscopeDriverException
	{
		return deviceProperty.getPropertyID();
	}

	@Override
	public PropertyType getType() throws MicroscopeDriverException
	{
		return deviceProperty.getType();
	}

	@Override
	public String[] getAllowedPropertyValues() throws MicroscopeDriverException
	{
		return deviceProperty.getAllowedPropertyValues();
	}

	@Override
	public String getDefaultValue() throws MicroscopeDriverException
	{
		return deviceProperty.getDefaultValue();
	}
}
