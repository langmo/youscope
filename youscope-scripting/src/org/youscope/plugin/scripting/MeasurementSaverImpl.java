/**
 * 
 */
package org.youscope.plugin.scripting;

import java.rmi.RemoteException;

import org.youscope.common.ImageEvent;
import org.youscope.common.ImageListener;
import org.youscope.common.configuration.MeasurementConfiguration;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.MeasurementSaveSettings;
import org.youscope.common.measurement.MeasurementSaver;
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
	public void setSaveSettings(MeasurementSaveSettings saveSettings) throws MeasurementRunningException
	{
		// Do nothing.
	}

	@Override
	public MeasurementSaveSettings getSaveSettings()
	{
		return null;
	}

	@Override
	public MeasurementConfiguration getConfiguration()
	{
		return null;
	}

	@Override
	public void setConfiguration(MeasurementConfiguration configuration) throws MeasurementRunningException
	{
		// Do nothing.
	}

	@Override
	public void imageMade(ImageEvent<?> e)
	{
		// Do nothing.
	}

	@Override
	public String getLastMeasurementFolder()
	{
		return null;
	}

	@Override
	public TableListener getSaveTableDataListener(String tableSaveName)
	{
		return this;
	}

	@Override
	public void newTableProduced(Table table) throws RemoteException {
		// do nothing.
		
	}
}
