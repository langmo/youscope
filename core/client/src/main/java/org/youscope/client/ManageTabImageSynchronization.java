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
import java.util.Vector;

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
class ManageTabImageSynchronization extends ManageTabElement
{
	/**
	 * Serial Version UID.
	 */
	private static final long				serialVersionUID	= 7128286456427832550L;
	private String[]						deviceNames			= new String[0];
	private boolean[]						deviceSynchronized	= new boolean[0];
	private boolean							settingsChanged		= false;
	private final JTable					imageSynchroTable;
	private final ImageSynchroTableModel	imageSynchroTableModel;

	ManageTabImageSynchronization()
	{
		imageSynchroTableModel = new ImageSynchroTableModel();
		imageSynchroTable = new JTable(imageSynchroTableModel);
		imageSynchroTable.setRowSelectionAllowed(true);
		imageSynchroTable.setColumnSelectionAllowed(false);
		imageSynchroTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		DescriptionPanel descriptionPanel = new DescriptionPanel("Description", "Before taking an image, YouScope waits until all devices belonging to the channel the image is made to, as well as the shutter (if any), have stopped moving.\nHowever, there might be additional devices which might not have finished moving before an image is taken, and, thus, produce artifacts in the image.\nIf you experience such artifacts, try to add the respective device to the image synchronization list. All devices in this list are waited to stop moving before taking any image.\n"
				+ "Note that this might not be sufficient in the case when the moving device is neither supporting handshaking nor polling. In this case you have to add an additional device delay.");
		
		setOpaque(false);
		setLayout(new BorderLayout(5, 5));
		JScrollPane scrollPane = new JScrollPane(descriptionPanel);
		scrollPane.setPreferredSize(new Dimension(400, 150));
		add(scrollPane, BorderLayout.NORTH);
		add(new JScrollPane(imageSynchroTable), BorderLayout.CENTER);
	}

	@Override
	public void initializeContent()
	{
		settingsChanged = false;

		Vector<String> loadedDevices = new Vector<String>();
		String[] synchronizedOnes;
		try
		{
			synchronizedOnes = YouScopeClientImpl.getMicroscope().getMicroscopeConfiguration().getImageSynchronizationDevices();
			for(Device device : YouScopeClientImpl.getMicroscope().getDevices())
			{
				loadedDevices.addElement(device.getDeviceID());
			}
		}
		catch(Exception e)
		{
			ClientSystem.err.println("Could not obtain image synchronization devices.", e);
			deviceNames = new String[0];
			deviceSynchronized = new boolean[0];
			imageSynchroTableModel.fireTableDataChanged();
			return;
		}
		deviceNames = loadedDevices.toArray(new String[loadedDevices.size()]);
		deviceSynchronized = new boolean[deviceNames.length];
		for(String synchronizedOne : synchronizedOnes)
		{
			int idx = loadedDevices.indexOf(synchronizedOne);
			if(idx < 0)
				continue;
			deviceSynchronized[idx] = true;
		}
		imageSynchroTableModel.fireTableDataChanged();
	}

	@Override
	public boolean storeContent()
	{
		if(settingsChanged)
		{
			Vector<String> changedDevices = new Vector<String>();
			for(int i = 0; i < deviceSynchronized.length; i++)
			{
				if(deviceSynchronized[i])
					changedDevices.addElement(deviceNames[i]);
			}
			try
			{
				YouScopeClientImpl.getMicroscope().getMicroscopeConfiguration().setImageSynchronizationDevices(changedDevices.toArray(new String[changedDevices.size()]));
			}
			catch(Exception e)
			{
				ClientSystem.err.println("Could not set image synchronization devices.", e);
			}
		}
		return settingsChanged;
	}

	private class ImageSynchroTableModel extends AbstractTableModel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -2836711115561383139L;

		private String[]			columnNames			= {"Device Name", "Synchronized"};

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
			return Boolean.class;
		}

		@Override
		public Object getValueAt(int row, int col)
		{
			if(col == 0)
				return deviceNames[row];
			return deviceSynchronized[row];
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
			if(col == 0 || row < 0 || row >= deviceSynchronized.length || !(aValue instanceof Boolean))
				return;
			settingsChanged = true;
			deviceSynchronized[row] = (Boolean)aValue;
		}
	}
}
