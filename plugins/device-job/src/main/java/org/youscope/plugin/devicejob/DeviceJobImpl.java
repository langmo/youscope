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
package org.youscope.plugin.devicejob;

import java.rmi.RemoteException;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.job.JobAdapter;
import org.youscope.common.job.JobException;
import org.youscope.common.job.basicjobs.DeviceSettingJob;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.SettingException;
import org.youscope.common.table.Table;
import org.youscope.common.table.TableDefinition;
import org.youscope.common.table.TableException;

/**
 * @author Moritz Lang
 */
class DeviceJobImpl extends JobAdapter implements DeviceSettingJob
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -115321069638111069L;

	private DeviceSetting[]	settings			= new DeviceSetting[0];

	private volatile Table tableToEvaluate = null;
	
	public DeviceJobImpl(PositionInformation positionInformation) throws RemoteException
	{
		super(positionInformation);
	}

	@Override
	public synchronized void setDeviceSettings(DeviceSetting[] settings) throws ComponentRunningException
	{
		assertRunning();
		if(settings == null)
		{
			this.settings = new DeviceSetting[0];
			return;
		}

		this.settings = new DeviceSetting[settings.length];
		for(int i = 0; i < settings.length; i++)
		{
			this.settings[i] = settings[i].clone();
		}

	}

	@Override
	public DeviceSetting[] getDeviceSettings()
	{
		return settings;
	}

	@Override
	public void clearDeviceSettings() throws ComponentRunningException
	{
		DeviceJobImpl.this.setDeviceSettings(null);
	}

	@Override
	public void addDeviceSetting(String device, String property, String value) throws ComponentRunningException
	{
		assertRunning();
		DeviceSetting[] newSettings = new DeviceSetting[settings.length + 1];
		System.arraycopy(settings, 0, newSettings, 0, settings.length);
		DeviceSetting newSetting = new DeviceSetting();
		newSetting.setDeviceProperty(device, property);
		newSetting.setValue(value);
		newSettings[newSettings.length - 1] = newSetting;
		setDeviceSettings(newSettings);
	}

	
	@Override
	public void initializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		super.initializeJob(microscope, measurementContext);
		tableToEvaluate = null;
	}
	
	@Override
	public void uninitializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		super.uninitializeJob(microscope, measurementContext);
		tableToEvaluate = null;
	}
	
	@Override
	public void runJob(ExecutionInformation executionInformation, Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		if(settings != null)
		{
			try
			{
				microscope.applyDeviceSettings(settings);
			}
			catch(SettingException e)
			{
				throw new JobException("Device settings invalid.", e);
			}
			catch(MicroscopeLockedException e)
			{
				throw new JobException("Device settings could not be set.", e);
			}
			catch(MicroscopeException e)
			{
				throw new JobException("Device settings caused error in device driver.", e);
			}
		}
		if(Thread.interrupted())
			throw new InterruptedException();
		Table tableToEvaluate;
		synchronized(this)
		{
			tableToEvaluate = this.tableToEvaluate;
			 this.tableToEvaluate = null;
		}
		if(tableToEvaluate != null)
		{
			DeviceSetting[] settings = new DeviceSetting[tableToEvaluate.getNumRows()];
			for(int row = 0;row<tableToEvaluate.getNumRows(); row++)
			{
				try {
					settings[row] = new DeviceSetting(
							tableToEvaluate.getValue(row, DeviceTable.COLUMN_DEVICE.getColumnName(),String.class),
							tableToEvaluate.getValue(row, DeviceTable.COLUMN_PROPERTY.getColumnName(),String.class),
							tableToEvaluate.getValue(row, DeviceTable.COLUMN_VALUE.getColumnName(),String.class));
				} catch (Exception e) {
					throw new JobException("Could not interpret values in consumed table in row "+ Integer.toString(row)+".", e);
				}
			}
			try
			{
				microscope.applyDeviceSettings(settings);
			}
			catch(SettingException e)
			{
				throw new JobException("Device settings invalid.", e);
			}
			catch(MicroscopeLockedException e)
			{
				throw new JobException("Device settings could not be set.", e);
			}
			catch(MicroscopeException e)
			{
				throw new JobException("Device settings caused error in device driver.", e);
			}
		}
	}

	@Override
	protected String getDefaultName()
	{
		DeviceSetting[] settings = this.settings;
		String text = "Device Settings(";
		for(int i = 0; i < settings.length; i++)
		{
			if(i > 0)
				text += ", ";
			text += settings[i].getDevice() + "." + settings[i].getProperty() + "=" + settings[i].getStringValue();
		}
		return text;
	}

	@Override
	public synchronized void consumeTable(Table table) throws RemoteException, TableException
	{
		tableToEvaluate = table.toTable(getConsumedTableDefinition());
		
	}

	@Override
	public TableDefinition getConsumedTableDefinition() throws RemoteException {
		return DeviceTable.getTableDefinition();
	}
}
