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
package org.youscope.plugin.scripting;

import java.rmi.RemoteException;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.image.ImageListener;
import org.youscope.common.saving.MeasurementFileLocations;
import org.youscope.common.saving.MeasurementSaver;
import org.youscope.common.saving.SaveSettings;
import org.youscope.common.table.Table;
import org.youscope.common.table.TableListener;

/**
 * Object pretending to save a measurement. Implemented to be able to debug a measurement.
 * @author Moritz Lang
 *
 */
class MeasurementSaverImpl implements MeasurementSaver, ImageListener, TableListener
{

	@Override
	public ImageListener getSaveImageListener(String imageSaveName)
	{
		return this;
	}

	@Override
	public void setSaveSettings(SaveSettings saveSettings) throws ComponentRunningException
	{
		// Do nothing.
	}

	@Override
	public SaveSettings getSaveSettings()
	{
		return null;
	}

	@Override
	public void imageMade(ImageEvent<?> e)
	{
		// Do nothing.
	}

	@Override
	public TableListener getSaveTableListener(String tableSaveName)
	{
		return this;
	}

	@Override
	public void newTableProduced(Table table) throws RemoteException {
		// do nothing.
		
	}

	@Override
	public MeasurementFileLocations getLastMeasurementFileLocations() throws RemoteException {
		return null;
	}
}
