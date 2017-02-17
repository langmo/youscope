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
package org.youscope.plugin.openbis;

import java.rmi.RemoteException;

import org.youscope.addon.serveraddon.ServerAddon;

/**
 * Addon allowing to transfer measurement data (images, meta-data) to OpenBIS.
 * @author Moritz Lang
 *
 */
public interface OpenBISAddon extends ServerAddon
{
	/**
	 * Transfers a measurement to OpenBIS and saves it there under openBISUser/projectIdentifier/measurementIdentifier. If overwrite == false, an OpenBISException is thrown if a measurement with the
	 * given folder structure does already exist, otherwise, it gets overwritten.
	 * The transfer is done in a separate thread. However, the transfer is initialized (i.e. the connection to OpenBIS using rsync) is established in the current thread, as well as initial validity checks.
	 * The transfer is done via rsync, using ssh. An not password protected SSH authentification file is assumed to be located in the openbis directory under:
	 * openbis/.ssh/id_dsa or openbis/.ssh/id_rsa (depending on the encryption). The authentification file must be in the OpenSSH format (NOT in the Putty format).
	 * The connection via ssh is done to sshUser@sshServer:sshDirectory.
	 * @param sshUser The user name for the SSH connection.
	 * @param sshServer The SSH server name (i.e.: the OpenBIS server).
	 * @param sshDirectory The directory configured as the drop box on the OpenBIS server, e.g. "~/servers/datastore_server/data/microscope". This folder must be the same as configured in OpenBIS for incoming microscopy files.
	 * @param openBISUser Identifier of the OpenBIS user. The user name has to be already configured using the OpenBIS UI. Usually different then the SSH user name.
	 * @param projectIdentifier Identifier of the project the measurement belongs to. The project identifier has to be already configured under the user name using the OpenBIS UI.
	 * @param measurementIdentifier An identifier for the measurement. The measurement does NOT have to be configured before. 
	 * @param measurementFolder The folder on the YouScope server where the measurement data is saved.
	 * @param overwrite True if an existing measurement in OpenBIS should be overwritten, false if not.
	 * @throws RemoteException
	 * @throws OpenBISException Thrown if transfer could not be initialized.
	 */
	public void transferMeasurement(String sshUser, String sshServer, String sshDirectory, String openBISUser, String projectIdentifier, String measurementIdentifier, String measurementFolder, boolean overwrite) throws RemoteException, OpenBISException;
	
	/**
	 * Tries to interrupt a running transfer of data. Does not necessarily do any cleanup of already transferred data.
	 * The interruption of the transfer is done in a separate thread, while this function returns immediately. If the transfer
	 * could be finished successfully, one can determine with the respective listeners.
	 * Does nothing if no transfer is currently active.
	 * @throws RemoteException
	 */
	public void cancelTransfer() throws RemoteException;
	
	/**
	 * Returns true if there is a currently active transfer taking place, and false otherwise.
	 * @return True if currently data is transferred.
	 * @throws RemoteException
	 */
	public boolean isTransferring() throws RemoteException;
	
	/**
	 * Adds a listener which gets informed about the current state of the transfer.
	 * @param listener Listener to add.
	 * @throws RemoteException
	 */
	public void addTransferListener(OpenBISListener listener) throws RemoteException;
	
	/**
	 * Removes a previously added listener.
	 * @param listener Listener to remove.
	 * @throws RemoteException
	 */
	public void removeTransferListener(OpenBISListener listener) throws RemoteException;
}
