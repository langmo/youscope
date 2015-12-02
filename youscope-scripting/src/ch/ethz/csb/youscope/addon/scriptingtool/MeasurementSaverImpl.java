/**
 * 
 */
package ch.ethz.csb.youscope.addon.scriptingtool;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.shared.ImageEvent;
import ch.ethz.csb.youscope.shared.ImageListener;
import ch.ethz.csb.youscope.shared.configuration.MeasurementConfiguration;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.MeasurementSaveSettings;
import ch.ethz.csb.youscope.shared.measurement.MeasurementSaver;
import ch.ethz.csb.youscope.shared.table.Table;
import ch.ethz.csb.youscope.shared.table.TableListener;

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
	public void imageMade(ImageEvent e)
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
