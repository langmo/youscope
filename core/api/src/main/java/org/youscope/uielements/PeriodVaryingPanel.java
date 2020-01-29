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
package org.youscope.uielements;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.youscope.common.task.VaryingPeriodConfiguration;

/**
 * @author langmo
 * 
 */
public class PeriodVaryingPanel extends JPanel
{

	/**
	 * Serial Version UID.
	 */
	private static final long			serialVersionUID	= -5607845444555622850L;

	private ArrayList<long[]>				periodVaryingData	= new ArrayList<long[]>();

	private PeriodVaryingDataModel	periodVaryingDataModel;

	private JTable					periodVaryingDataTable;

	private static final String		addButtonFile		= "icons/block--plus.png";

	private static final String		deleteButtonFile	= "icons/block--minus.png";

	private static final String		upButtonFile		= "icons/arrow-090.png";

	private static final String		downButtonFile		= "icons/arrow-270.png";

	private ImageIcon					addButtonIcon		= null;
	private ImageIcon					deleteButtonIcon	= null;
	private ImageIcon					upButtonIcon		= null;
	private ImageIcon					downButtonIcon		= null;

	/**
	 * Constructor.
	 */
	public PeriodVaryingPanel()
	{
		super(new BorderLayout());

		GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
		GridBagConstraints bottomConstr = StandardFormats.getBottomContstraint();

		// Initialize table for varying periods
		periodVaryingDataModel = new PeriodVaryingDataModel();
		periodVaryingDataTable = new JTable(periodVaryingDataModel)
		{
			/**
			 * Serializable Version UID.
			 */
			private static final long	serialVersionUID	= 5624975353082301719L;
			@Override
			public TableCellRenderer getCellRenderer(int row, int column)
			{
				final TableCellRenderer cellRenderer = super.getCellRenderer(row, column);// passiveRenderer;
				return new TableCellRenderer()
				{

					@Override
					public Component getTableCellRendererComponent(JTable table, Object value,
							boolean isSelected, boolean hasFocus, int row, int column) 
					{
						Component component = cellRenderer.getTableCellRendererComponent(table, value,
								isSelected, hasFocus, row, column);
						component.setEnabled(isCellEditable(row, column));
						return component;
					}
			
				};
			}
		};

		periodVaryingDataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		periodVaryingDataTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
		periodVaryingDataTable.setFillsViewportHeight(true);

		loadIcons();

		JButton addButton = new JButton(addButtonIcon);
		addButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				long[] datum = {5000, 1};
				periodVaryingData.add(datum);
				periodVaryingDataModel.fireTableDataChanged();
				periodVaryingDataTable.setRowSelectionInterval(periodVaryingData.size() - 1, periodVaryingData.size() - 1);
			}
		});
		JButton removeButton = new JButton(deleteButtonIcon);
		removeButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int row = periodVaryingDataTable.getSelectedRow();
				if(row >= 0 && row < periodVaryingData.size())
				{
					periodVaryingData.remove(row);
					periodVaryingDataModel.fireTableDataChanged();
				}
			}
		});
		JButton upButton = new JButton(upButtonIcon);
		upButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int row = periodVaryingDataTable.getSelectedRow();
				if(row > 0 && row < periodVaryingData.size())
				{
					long[] datum = periodVaryingData.get(row);
					periodVaryingData.remove(row);
					periodVaryingData.add(row - 1, datum);
					periodVaryingDataModel.fireTableDataChanged();
					periodVaryingDataTable.setRowSelectionInterval(row - 1, row - 1);
				}
			}
		});
		JButton downButton = new JButton(downButtonIcon);
		downButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int row = periodVaryingDataTable.getSelectedRow();
				if(row >= 0 && row < periodVaryingData.size() - 1)
				{
					long[] datum = periodVaryingData.get(row);
					periodVaryingData.remove(row);
					periodVaryingData.add(row + 1, datum);
					periodVaryingDataModel.fireTableDataChanged();
					periodVaryingDataTable.setRowSelectionInterval(row + 1, row + 1);
				}
			}
		});

		GridBagLayout elementsLayout = new GridBagLayout();
		JPanel buttonPanel = new JPanel(elementsLayout);
		StandardFormats.addGridBagElement(addButton, elementsLayout, newLineConstr, buttonPanel);
		StandardFormats.addGridBagElement(removeButton, elementsLayout, newLineConstr, buttonPanel);
		StandardFormats.addGridBagElement(upButton, elementsLayout, newLineConstr, buttonPanel);
		StandardFormats.addGridBagElement(downButton, elementsLayout, newLineConstr, buttonPanel);
		StandardFormats.addGridBagElement(new JPanel(), elementsLayout, bottomConstr, buttonPanel);

		// Whole table
		add(buttonPanel, BorderLayout.EAST);
		add(new JScrollPane(periodVaryingDataTable), BorderLayout.CENTER);

		// Preinitialize model with some example data.
		long[] datum1 = {5000, 1};
		long[] datum2 = {6000, 1};
		periodVaryingData.add(datum1);
		periodVaryingData.add(datum2);
	}

	private void loadIcons()
	{
		// Load icons
		try
		{
			URL addButtonURL = getClass().getClassLoader().getResource(addButtonFile);
			if(addButtonURL != null)
				addButtonIcon = new ImageIcon(addButtonURL, "Add Duration");

			URL deleteButtonURL = getClass().getClassLoader().getResource(deleteButtonFile);
			if(deleteButtonURL != null)
				deleteButtonIcon = new ImageIcon(deleteButtonURL, "Delete Duration");

			URL upButtonURL = getClass().getClassLoader().getResource(upButtonFile);
			if(upButtonURL != null)
				upButtonIcon = new ImageIcon(upButtonURL, "Move upwards");

			URL downButtonURL = getClass().getClassLoader().getResource(downButtonFile);
			if(downButtonURL != null)
				downButtonIcon = new ImageIcon(downButtonURL, "Move downwards");

		}
		catch(@SuppressWarnings("unused") Exception e)
		{
			// Do nothing.
		}
	}

	/**
	 * Sets the period configuration.
	 * @param period period configuration.
	 */
	public void setPeriod(VaryingPeriodConfiguration period)
	{
		long[] periods = period.getPeriods();
		periodVaryingData.clear();
		for(int i = 0; i < periods.length; i++)
		{
			long periodLength = periods[i];
			long numPeriods = 1;
			for(int j = i + 1; j < periods.length; j++)
			{
				if(periods[j] == periodLength)
				{
					i = j;
					numPeriods++;
				}
				else
					break;
			}
			long[] datum = {periodLength, numPeriods};
			periodVaryingData.add(datum);
		}
	}

	/**
	 * Returns the period configuration.
	 * @return The period configuration.
	 */
	public VaryingPeriodConfiguration getPeriod()
	{
		VaryingPeriodConfiguration period = new VaryingPeriodConfiguration();
		period.setStartTime(0);
		Vector<Long> periodsTemp = new Vector<Long>();
		for(long[] datum : periodVaryingData)
		{
			for(int i = 0; i < datum[1]; i++)
			{
				periodsTemp.add(datum[0]);
			}
		}
		long[] periods = new long[periodsTemp.size()];
		int i = 0;
		for(long datum : periodsTemp)
		{
			periods[i++] = datum;
		}
		
		period.setPeriods(periods);
		return period;
	}

	class PeriodVaryingDataModel extends AbstractTableModel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -5398301321964417528L;

		@Override
		public int getColumnCount()
		{
			return 4;
		}

		@Override
		public int getRowCount()
		{
			return periodVaryingData.size() + 1;
		}

		@Override
		public Object getValueAt(int row, int col)
		{
			if(col >= getColumnCount() || col < 0 || row < 0 || row >= getRowCount())
				return null;
			else if(row < periodVaryingData.size())
			{
				long[] datum = periodVaryingData.get(row);
				if(col == 0)
				{
					return "Period " + Integer.toString(row + 1) + ":";
				}
				else if(col < 3)
				{
					return datum[col - 1];
				}
				else
				{
					return datum[0] * datum[1];
				}
			}
			else
			{
				// Last row = sum of time
				if(col == 0)
					return "Total:";
				else if(col < 3)
					return "—";
				else
				{
					int time = 0;
					for(long[] datum : periodVaryingData)
					{
						time += datum[0] * datum[1];
					}
					return time;
				}
			}
		}

		@Override
		public String getColumnName(int col)
		{
			if(col == 0)
				return "Element";
			else if(col == 1)
				return "Period (ms)";
			else if(col == 2)
				return "Repetitions";
			else
				return "Total Time (ms)";
		}

		@Override
		public boolean isCellEditable(int row, int col)
		{
			if((col < 3 && col > 0 && row < getRowCount() - 1))
				return true;
			return false;
		}

		@Override
		public Class<?> getColumnClass(int col)
		{
			if(col == 0)
				return String.class;
			else if(col == 1)
				return Long.class;
			else if(col == 2)
				return Long.class;
			else
				return Long.class;
		}

		@Override
		public void setValueAt(Object value, int row, int col)
		{
			if(row < periodVaryingData.size())
			{
				periodVaryingData.get(row)[col - 1] = ((Number)value).longValue();
				fireTableCellUpdated(row, 3);
			}
			else
			{
				return;
			}
			fireTableCellUpdated(getRowCount()-1, 3);
		}
	}
}
