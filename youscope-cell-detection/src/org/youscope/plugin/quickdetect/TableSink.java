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
package org.youscope.plugin.quickdetect;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for Matlab.
 * @author Moritz Lang
 *
 */
public interface TableSink extends Remote 
{
	/**
	 * @param cellID
	 * @param quantID
	 * @param xpos
	 * @param ypos
	 * @param area
	 * @param fluorescence
	 * @throws RemoteException
	 * @throws Exception 
	 */
	public void addRow(Integer cellID, Integer quantID, Double xpos, Double ypos, Double area, Double fluorescence) throws RemoteException, Exception;
}
