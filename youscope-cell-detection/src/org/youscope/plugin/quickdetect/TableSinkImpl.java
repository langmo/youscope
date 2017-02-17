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

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.youscope.common.ExecutionInformation;
import org.youscope.common.PositionInformation;
import org.youscope.common.table.Table;
import org.youscope.common.table.TableException;

class TableSinkImpl extends UnicastRemoteObject implements TableSink {

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -6115859509834763738L;
	final Table table;
	TableSinkImpl(long creationTime, PositionInformation positionInformation, ExecutionInformation executionInformation) throws RemoteException {
		super();
		table = new Table(QuickDetectTable.getTableDefinition(), creationTime, positionInformation, executionInformation);
	}
	@Override
	public void addRow(Integer cellID, Integer quantID, Double xpos, Double ypos, Double area, Double fluorescence) throws RemoteException, TableException
	{
		table.addRow(cellID, quantID, xpos, ypos, area, fluorescence);
	}

}
