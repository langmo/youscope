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
package org.youscope.addon.serveraddon;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * An addon with specialized functionality which cannot be adequately represented by the other mechanisms of YouScope.
 * This interface should be implemented by a service provider on the server side, and can be queried by
 * any addon on the client (and server) side. Therefore, the specific interface of the addon has to be known by
 * both, the client and the server side.
 * It is highly recommended to construct a specialized interface for the addon which inherits this interface, over which
 * server and client communicate, instead of letting the client addon know implementation details about the addon on the server side.
 * Furthermore, we recommend to design the interface functions such that they are as independent from the specific addon implementation
 * as possible.
 * 
 * @author Moritz Lang
 * 
 */
public interface ServerAddon extends Remote
{
	/**
	 * Should return a short, human readable name for the addon.
	 * The name should describe the functionality provided by the addon, rather then details of the specific implementation.
	 * Thus, two addons implementing the same interface should also return the same name, as well as different versions of the same addon.
	 * This function is for information only, and its functionality should not be used to predict any properties of the addon.
	 * @return The name of the interface.
	 * @throws RemoteException
	 */
	public String getAddonName() throws RemoteException;

	/**
	 * Should return the version of the addon. Every change in the implementation of the addon should lead to a different (increasing) version of the addon.
	 * 
	 * Remark: Please note that a Java interface represents a contract between the service provider and the service consumer.
	 * By "simply" changing an interface, this contract is most often broken, which may (and often will) lead to unexpected behavior,
	 * if the client and the server are using different versions of the interface. In general, after publishing (i.e. making available) an interface,
	 * it should not be changed anymore. Instead, a new interface should be created. If the functionality of the old interface is a
	 * strict subset of the one of the new interface, the new interface should inherit from the old one.
	 * @return Version of the addon.
	 * @throws RemoteException
	 */
	public float getAddonVersion() throws RemoteException;

	/**
	 * Should return a human readable description of the general purpose of the interface, about the provided
	 * functionality, specific properties of the implementation, as well as about the person/company which designed the addon and how where it can be obtained
	 * (e.g. a download address). Furthermore, if there are different versions of interfaces representing the same
	 * or similar addons, this should be stated, as well as compatibility issues with older versions.
	 * This function is for information only, and its functionality should not be used to predict any properties of the addon.
	 * @return Human readable description of the interface.
	 * @throws RemoteException
	 */
	public String getAddonDescription() throws RemoteException;

	/**
	 * Should return a unique ID of the specific implementation of this interface.
	 * Thus, two addons implementing the same interface should return a different ID. Furthermore, the ID
	 * should also be different for different versions of the addon.
	 * The ID should follow the pattern "YourIdentifier.AddonName" (e.g. "YouScope.Livestream"
	 * or "Bush_GW.WMDFinder"). However, a client should not parse this name to obtain information about the addon,
	 * as well as not assume that the addon follows this pattern.
	 * 
	 * @return ID Unique ID of the addon.
	 * @throws RemoteException
	 */
	public String getAddonID() throws RemoteException;
}
