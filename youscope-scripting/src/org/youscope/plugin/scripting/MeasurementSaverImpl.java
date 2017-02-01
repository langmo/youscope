/**
 * 
 */
package org.youscope.plugin.scripting;

import java.rmi.RemoteException;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.image.ImageListener;
import org.youscope.common.measurement.MeasurementConfiguration;
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
	public MeasurementConfiguration getConfiguration()
	{
		return null;
	}

	@Override
	public void setConfiguration(MeasurementConfiguration configuration) throws ComponentRunningException
	{
		// Do nothing.
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
