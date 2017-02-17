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
import java.awt.GridLayout;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.youscope.common.microscope.StateDevice;
import org.youscope.uielements.DescriptionPanel;

/**
 * @author Moritz Lang
 * 
 */
class ManageTabStateLabels extends ManageTabElement
{
	/**
	 * Serial Version UID.
	 */
	private static final long			serialVersionUID	= 6006790148233585883L;
	private final JList<String>					devicesField		= new JList<String>();
	private final JTable				deviceLabelsTable;
	private final StateLabelsTableModel	deviceLabelsTableModel;
	private boolean						actualizing			= false;
	private String						currentDevice		= null;
	private String[]					deviceLabels		= new String[0];
	private boolean						labelsChanged		= false;
	private boolean somethingChanged = false;

	ManageTabStateLabels()
	{
		devicesField.addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if(actualizing)
					return;
				showDeviceLabels(devicesField.getSelectedValue() == null ? null : devicesField.getSelectedValue().toString());
			}
		});
		devicesField.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		deviceLabelsTableModel = new StateLabelsTableModel();
		deviceLabelsTable = new JTable(deviceLabelsTableModel);

		JPanel splitPanel = new JPanel(new GridLayout(1, 2, 2, 2));
		splitPanel.setOpaque(false);
		
		JPanel deviceSelectionPanel = new JPanel(new BorderLayout());
		deviceSelectionPanel.setOpaque(false);
		deviceSelectionPanel.setBorder(new TitledBorder("Step 1: Select Device"));
		deviceSelectionPanel.add(new JScrollPane(devicesField), BorderLayout.CENTER);
		splitPanel.add(deviceSelectionPanel);

		JPanel deviceLabelsPanel = new JPanel(new BorderLayout());
		deviceLabelsPanel.setOpaque(false);
		deviceLabelsPanel.setBorder(new TitledBorder("Step 2: Define State Labels"));
		deviceLabelsPanel.add(new JScrollPane(deviceLabelsTable), BorderLayout.CENTER);
		splitPanel.add(deviceLabelsPanel);
		
		DescriptionPanel descriptionPanel = new DescriptionPanel("Description", "Some devices have several states which can be accessed by their index. However, changing the state of a device by the index might be not intuitive and error-prone.\nTherefore, it is possible to assign each state to a human readable name, e.g. the name and wavelength of a filter. This name can then be used in YouScope instead of the index to change the state of the device.");
		
		setOpaque(false);
		setLayout(new BorderLayout(5, 5));
		JScrollPane scrollPane = new JScrollPane(descriptionPanel);
		scrollPane.setPreferredSize(new Dimension(400, 150));
		add(scrollPane, BorderLayout.NORTH);
		add(splitPanel, BorderLayout.CENTER);
	}

	private class StateLabelsTableModel extends AbstractTableModel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -2836711115571383124L;

		private String[]			columnNames			= {"State", "Label"};

		@Override
		public String getColumnName(int col)
		{
			return columnNames[col];
		}

		@Override
		public int getRowCount()
		{
			return deviceLabels.length;
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
			return String.class;
		}

		@Override
		public Object getValueAt(int row, int col)
		{
			if(col == 0)
				return "#" + Integer.toString(row + 1);
			return deviceLabels[row];
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
			if(col == 0 || row < 0 || row >= deviceLabels.length)
				return;
			deviceLabels[row] = aValue.toString();
			labelsChanged = true;
			somethingChanged = true;
		}

	}

	private void showDeviceLabels(String deviceName)
	{
		if(labelsChanged && currentDevice != null && (deviceName == null || !deviceName.equals(currentDevice)))
		{
			// Update current device labels.
			try
			{
				YouScopeClientImpl.getMicroscope().getStateDevice(currentDevice).setStateLabels(deviceLabels);
			}
			catch(Exception e)
			{
				ClientSystem.err.println("Could not get state labels for device " + deviceName + ".", e);
				deviceLabels = new String[0];
				currentDevice = null;
			}
		}
		labelsChanged = false;
		currentDevice = deviceName;
		if(deviceName == null)
		{
			deviceLabels = new String[0];
		}
		else
		{
			try
			{
				deviceLabels = YouScopeClientImpl.getMicroscope().getStateDevice(deviceName).getStateLabels();
				deviceLabelsTableModel.fireTableDataChanged();
			}
			catch(Exception e)
			{
				ClientSystem.err.println("Could not get state labels for device " + deviceName + ".", e);
				deviceLabels = new String[0];
				currentDevice = null;
			}
		}

	}

	@Override
	public void initializeContent()
	{
		actualizing = true;
		String[] deviceNames;
		try
		{
			StateDevice[] devices = YouScopeClientImpl.getMicroscope().getStateDevices();
			deviceNames = new String[devices.length];
			for(int i = 0; i < devices.length; i++)
			{
				deviceNames[i] = devices[i].getDeviceID();
			}
		}
		catch(Exception e)
		{
			ClientSystem.err.println("Could not obtain name of state devices.", e);
			deviceNames = new String[0];
		}
		devicesField.setListData(deviceNames);
		actualizing = false;
		if(deviceNames.length > 0)
			devicesField.setSelectedIndex(0);
	}

	@Override
	public boolean storeContent()
	{
		showDeviceLabels(null);
		return somethingChanged;
	}
}
