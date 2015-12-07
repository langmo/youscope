/**
 * 
 */
package org.youscope.uielements;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.youscope.common.configuration.VaryingPeriod;

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

	protected Vector<int[]>				periodVaryingData	= new Vector<int[]>();

	protected int						periodVaryingPause	= 0;

	protected PeriodVaryingDataModel	periodVaryingDataModel;

	protected JTable					periodVaryingDataTable;

	protected static final String		addButtonFile		= "icons/block--plus.png";

	protected static final String		deleteButtonFile	= "icons/block--minus.png";

	protected static final String		upButtonFile		= "icons/arrow-090.png";

	protected static final String		downButtonFile		= "icons/arrow-270.png";

	protected ImageIcon					addButtonIcon		= null;
	protected ImageIcon					deleteButtonIcon	= null;
	protected ImageIcon					upButtonIcon		= null;
	protected ImageIcon					downButtonIcon		= null;

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

			private PassiveCellRenderer	passiveRenderer		= new PassiveCellRenderer();

			@Override
			public TableCellRenderer getCellRenderer(int row, int column)
			{
				if(isCellEditable(row, column))
				{
					return super.getCellRenderer(row, column);
				}
				return passiveRenderer;
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
				int[] datum = {5000, 1};
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
					periodVaryingData.removeElementAt(row);
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
					int[] datum = periodVaryingData.elementAt(row);
					periodVaryingData.removeElementAt(row);
					periodVaryingData.insertElementAt(datum, row - 1);
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
					int[] datum = periodVaryingData.elementAt(row);
					periodVaryingData.removeElementAt(row);
					periodVaryingData.insertElementAt(datum, row + 1);
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
		int[] datum1 = {5000, 1};
		int[] datum2 = {6000, 1};
		periodVaryingData.add(datum1);
		periodVaryingData.add(datum2);
	}

	protected void loadIcons()
	{
		// Load icons
		try
		{
			URL addButtonURL = getClass().getClassLoader().getResource(addButtonFile);
			if(addButtonURL != null)
				addButtonIcon = new ImageIcon(addButtonURL, "Add Job");

			URL deleteButtonURL = getClass().getClassLoader().getResource(deleteButtonFile);
			if(deleteButtonURL != null)
				deleteButtonIcon = new ImageIcon(deleteButtonURL, "Delete Job");

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
	public void setPeriod(VaryingPeriod period)
	{
		int[] periods = period.getPeriods();
		periodVaryingData.clear();
		for(int i = 0; i < periods.length; i++)
		{
			int periodLength = periods[i];
			int numPeriods = 1;
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
			int[] datum = {periodLength, numPeriods};
			periodVaryingData.add(datum);
		}
		periodVaryingPause = period.getBreakTime();
	}

	/**
	 * Returns the period configuration.
	 * @return The period configuration.
	 */
	public VaryingPeriod getPeriod()
	{
		VaryingPeriod period = new VaryingPeriod();
		period.setStartTime(0);
		Vector<Integer> periodsTemp = new Vector<Integer>();
		for(int[] datum : periodVaryingData)
		{
			for(int i = 0; i < datum[1]; i++)
			{
				periodsTemp.add(datum[0]);
			}
		}
		int[] periods = new int[periodsTemp.size()];
		int i = 0;
		for(int datum : periodsTemp)
		{
			periods[i++] = datum;
		}
		
		period.setPeriods(periods);
		period.setBreakTime(periodVaryingPause);
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
			return periodVaryingData.size() + 2;
		}

		@Override
		public Object getValueAt(int row, int col)
		{
			if(col >= getColumnCount() || col < 0 || row < 0 || row >= getRowCount())
				return null;
			else if(row < periodVaryingData.size())
			{
				int[] datum = periodVaryingData.elementAt(row);
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
			else if(row == periodVaryingData.size())
			{
				// "Pause" row
				if(col == 0)
					return "Pause:";
				else if(col < 3)
					return "";
				else
					return periodVaryingPause;
			}
			else
			{
				// Last row = sum of time
				if(col == 0)
					return "Total:";
				else if(col < 3)
					return "";
				else
				{
					int time = periodVaryingPause;
					for(int[] datum : periodVaryingData)
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
			if((col < 3 && col > 0 && row < getRowCount() - 2) || (col == 3 && row == getRowCount() - 2))
				return true;
			return false;
		}

		@Override
		public Class<?> getColumnClass(int c)
		{
			return getValueAt(0, c).getClass();
		}

		@Override
		public void setValueAt(Object value, int row, int col)
		{
			if(row < periodVaryingData.size())
			{
				periodVaryingData.elementAt(row)[col - 1] = (Integer)value;
				fireTableCellUpdated(row, 3);
			}
			else
			{
				periodVaryingPause = (Integer)value;
			}
			fireTableCellUpdated(row, col);
			fireTableCellUpdated(getRowCount(), 3);
		}
	}

	protected class PassiveCellRenderer extends JLabel implements TableCellRenderer
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= 5045544589881926980L;

		public PassiveCellRenderer()
		{
			setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int column)
		{
			setBackground(Color.LIGHT_GRAY);
			setBorder(new EmptyBorder(2, 2, 2, 2));
			setText(table.getValueAt(row, column).toString());
			setFont(getFont().deriveFont(Font.BOLD));
			if(column == 3)
				setHorizontalAlignment(SwingConstants.RIGHT);
			else
				setHorizontalAlignment(SwingConstants.LEFT);
			return this;
		}
	}
}
