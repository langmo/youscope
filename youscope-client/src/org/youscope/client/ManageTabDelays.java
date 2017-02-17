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
package org.youscope.client;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.youscope.common.microscope.Device;
import org.youscope.uielements.DescriptionPanel;

/**
 * @author Moritz Lang
 *
 */
class ManageTabDelays extends ManageTabElement
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 1128286456427832550L;
	private String[] deviceNames = new String[0];
	private Double[] deviceDelays = new Double[0];
	private boolean settingsChanged = false;
	private final JTable deviceDelayTable;
	private final DeviceDelayTableModel deviceDelayTableModel;
	ManageTabDelays()
	{
		deviceDelayTableModel = new DeviceDelayTableModel();
		deviceDelayTable = new JTable(deviceDelayTableModel);
		deviceDelayTable.setRowSelectionAllowed(true);
		deviceDelayTable.setColumnSelectionAllowed(false);
		deviceDelayTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		deviceDelayTable.setOpaque(false);
		setOpaque(false);
		setLayout(new BorderLayout(5, 5));
		DescriptionPanel descriptionPanel = new DescriptionPanel("Description", "Many devices support either a handshaking mechanism or polling to determine if they are currently changing their state. However, some devices don't.\nIf you experience that images are taken albeit a device was still changing its state, try to add a delay larger or equal to the maximal time the device needs to perform a state change. Whenever changing the state of the device, YouScope will wait at least the given time span before proceeding.\nNote that if the device supports handshaking or polling, YouScope will use this mechanism for synchronization first. However, if the polling/handshaking takes less time than the explicit delay, YouScope will afterwards wait for the time difference. Thus, the explicit delays represent the minimal time YouScope should wait for a device, and the actual time waited might be higher (but never lower) than the explicit delay.");
		JScrollPane scrollPane = new JScrollPane(descriptionPanel);
		scrollPane.setPreferredSize(new Dimension(400, 150));
		add(scrollPane, BorderLayout.NORTH);
		JScrollPane scrollPane2 = new JScrollPane(deviceDelayTable);
		scrollPane2.setOpaque(false);
		add(scrollPane2, BorderLayout.CENTER);
	}

	@Override
	public void initializeContent()
	{
		settingsChanged = false;
		
		try
		{
			Device[] devices = YouScopeClientImpl.getMicroscope().getDevices();
			deviceNames = new String[devices.length];
			deviceDelays = new Double[devices.length];
			for(int i=0; i<devices.length; i++)
			{
				deviceNames[i] = devices[i].getDeviceID();
				deviceDelays[i] = devices[i].getExplicitDelay();
			}
		}
		catch(Exception e)
		{
			ClientSystem.err.println("Could not obtain list of delayed devices.", e);
			deviceNames = new String[0];
			deviceDelays = new Double[0];
		}
		deviceDelayTableModel.fireTableDataChanged();
	}

	@Override
	public boolean storeContent()
	{
		if(settingsChanged)
		{
			for(int i= 0; i<deviceNames.length; i++)
			{
				try
				{
					YouScopeClientImpl.getMicroscope().getDevice(deviceNames[i]).setExplicitDelay(deviceDelays[i]);
				}
				catch(Exception e)
				{
					ClientSystem.err.println("Could not set device delay of device " + deviceNames[i] + " to " + deviceDelays[i].toString() + " ms.", e);
				}
			}
		}
		return settingsChanged;
	}
	
	private class DeviceDelayTableModel extends AbstractTableModel
    {
        /**
         * Serial Version UID.
         */
        private static final long serialVersionUID = -2836711115561383139L;

        private String[] columnNames =
            { "Device Name", "Delay (ms)"};

        @Override
        public String getColumnName(int col)
        {
            return columnNames[col];
        }

        @Override
        public int getRowCount()
        {
            return deviceNames.length;
        }

        @Override
        public int getColumnCount()
        {
            return columnNames.length;
        }

        @Override
        public Class<?> getColumnClass(int column)
        {
        	if(column == 0)
        		return String.class;
			return Double.class;
        }

        @Override
        public Object getValueAt(int row, int col)
        {
        	if(col == 0)
        		return deviceNames[row];
			return deviceDelays[row];
        }
        @Override
        public boolean isCellEditable(int row, int col)
        {
        	if(col == 0)
        		return false;
			return true;
        }
        @Override
        public void setValueAt(Object aValue, int row, int col)
        {
        	if(col == 0 || row < 0 || row >= deviceDelays.length || !(aValue instanceof Double))
        		return;
        	settingsChanged = true;
        	deviceDelays[row] = (Double)aValue;
        }
    }
}
