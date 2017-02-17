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
package org.youscope.plugin.fluigent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.ArrayList;

import javax.swing.AbstractCellEditor;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.youscope.uielements.DoubleTextField;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.ImageLoadingTools;
import org.youscope.uielements.PeriodField;
import org.youscope.uielements.TimeUnit;

/**
 * Simple Fluigent control configuration using a time table
 * @author Moritz Lang
 *
 */
class SimpleTimingTable extends JPanel
{
	/**
	 * Serial Version UID
	 */
	private static final long	serialVersionUID	= -847210455548761222L;
	private final JTable timingTable;
	private final TimingTableModel timingTableModel;
	private String[] flowRateUnits;
	private final ArrayList<SyringeTableRow> timings = new ArrayList<SyringeTableRow>();
	public SimpleTimingTable(String[] flowRateUnits)
	{
		super(new BorderLayout(0, 0));
		setOpaque(false);
		this.flowRateUnits = flowRateUnits;
		this.timingTableModel = new TimingTableModel();
		this.timingTable = new JTable(timingTableModel);
		timingTable.setAutoCreateColumnsFromModel(true);
		timingTable.setRowSelectionAllowed(true);
        timingTable.setColumnSelectionAllowed(false);
        timingTable.setSurrendersFocusOnKeystroke(true);
        timingTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        timingTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        timingTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        TimingTableEditor editor = new TimingTableEditor();
        timingTable.setDefaultRenderer(Double.class, editor);
        timingTable.setDefaultEditor(Double.class, editor);
        JScrollPane zSlicesScrollPane = new JScrollPane(timingTable);
        zSlicesScrollPane.setPreferredSize(new Dimension(250, 70));
        zSlicesScrollPane.setMinimumSize(new Dimension(10, 10));
        add(zSlicesScrollPane, BorderLayout.CENTER);
        
        // Up, down, add and remove Buttons
        Icon upButtonIcon = ImageLoadingTools.getResourceIcon("icons/arrow-090.png", "Move Upwards");
        Icon downButtonIcon = ImageLoadingTools.getResourceIcon("icons/arrow-270.png", "Move Downwards");
        Icon addButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--plus.png", "Add Time Point");
        Icon deleteButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--minus.png", "Remove Time Point");
        JButton upButton;
        if (upButtonIcon == null)
            upButton = new JButton("Move Up");
        else
            upButton = new JButton("Move Up", upButtonIcon);
        upButton.setHorizontalAlignment(SwingConstants.LEFT);
        JButton downButton;
        if (downButtonIcon == null)
            downButton = new JButton("Move Down");
        else
            downButton = new JButton("Move Down", downButtonIcon);
        downButton.setHorizontalAlignment(SwingConstants.LEFT);
        upButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    moveUpDown(true);
                }
            });
        downButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    moveUpDown(false);
                }
            });

        JButton newPositionButton;
        if (addButtonIcon == null)
            newPositionButton = new JButton("Add Timing");
        else
            newPositionButton = new JButton("Add Timing", addButtonIcon);
        newPositionButton.setHorizontalAlignment(SwingConstants.LEFT);
        newPositionButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    addRows(new long[]{0});
                }
            });
        JButton deletePositionButton;
        if (deleteButtonIcon == null)
            deletePositionButton = new JButton("Delete Timing");
        else
            deletePositionButton = new JButton("Delete Position", deleteButtonIcon);
        deletePositionButton.setHorizontalAlignment(SwingConstants.LEFT);
        deletePositionButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    int row = timingTable.getSelectedRow();
                    if (row < 0)
                        return;
                    timings.remove(row);
                    timingTableModel.fireTableRowsDeleted(row, row);
                }
            });        
        DynamicPanel buttonPanel = new DynamicPanel();
        buttonPanel.add(newPositionButton);
        buttonPanel.add(deletePositionButton);
        JPanel emptyPanel = new JPanel();
        emptyPanel.setOpaque(false);
        buttonPanel.add(emptyPanel);
        buttonPanel.add(upButton);
        buttonPanel.add(downButton);
        buttonPanel.addFillEmpty();
        
        add(buttonPanel, BorderLayout.EAST);

	}
	
	void setTimings(SyringeTableRow[] newTimings) throws ResourceException
	{
		timings.clear();
		
		if(newTimings.length == 0)
		{
			timingTableModel.fireTableDataChanged();
			return;
		}
		if(newTimings[0].flowRates.length != flowRateUnits.length)
		{
			timingTableModel.fireTableDataChanged();
			throw new ResourceException("Fluigent protocol which should be loaded has the wrong number of flow units.");
		}
		for(SyringeTableRow newTiming : newTimings)
		{
			timings.add(newTiming);
		}
		timingTableModel.fireTableDataChanged();
	}
	SyringeTableRow[] getTimings()
	{
		return timings.toArray(new SyringeTableRow[timings.size()]);
	}
	
	void setFlowRateUnits(String[] flowRateUnits)
	{
		boolean sizeChanged;
		if(this.flowRateUnits.length == flowRateUnits.length)
		{
			sizeChanged = false;
		}
		else
		{
			sizeChanged = true;
		}
		this.flowRateUnits = flowRateUnits;
		
		if(sizeChanged)
		{
			SyringeTableRow[] oldTimings = timings.toArray(new SyringeTableRow[timings.size()]);
			timings.clear();
			for(SyringeTableRow oldTiming : oldTimings)
			{
				SyringeTableRow newTiming = new SyringeTableRow(oldTiming.time, flowRateUnits.length);
				for(int i=0; i<oldTiming.flowRates.length && i < flowRateUnits.length; i++)
				{
					newTiming.flowRates[i] = oldTiming.flowRates[i];
				}
				timings.add(newTiming);
			}
		}
		timingTableModel.fireTableStructureChanged();
	}
	
	private void addRows(long[] rowTimes)
	{
		for(long rowTime : rowTimes)
		{
			timings.add(new SyringeTableRow(rowTime, flowRateUnits.length));
		}
        timingTableModel.fireTableRowsInserted(timings.size() - rowTimes.length,  timings.size() - 1);
	}
	
	private void moveUpDown(boolean moveUp)
    {
        int idx = timingTable.getSelectedRow();
        if (idx == -1 || (moveUp && idx == 0) || (!moveUp && idx + 1 >= timings.size()))
            return;
        int newIdx;
        if (moveUp)
            newIdx = idx - 1;
        else
            newIdx = idx + 1;
        SyringeTableRow z = timings.get(idx);
        timings.remove(idx);
        timings.add(newIdx, z);
        timingTableModel.fireTableDataChanged();
        timingTable.setRowSelectionInterval(newIdx, newIdx);
    }
	
	private class TimingTableModel extends AbstractTableModel
    {
        /**
         * Serial Version UID.
         */
        private static final long serialVersionUID = -2811111115571333139L;

        TimingTableModel()
        {
           // do nothing.
        }

        @Override
        public String getColumnName(int col)
        {
        	if(col == 0)
        		return "Time (s)";
			return "Flow Syringe " + Integer.toString(col) + " (ul/min)";
        }

        @Override
        public int getRowCount()
        {
            return timings.size();
        }

        @Override
        public int getColumnCount()
        {
            return flowRateUnits.length+1;
        }

        @Override
        public Class<?> getColumnClass(int column)
        {
        	return Double.class;
        }

        @Override
        public Object getValueAt(int row, int col)
        {
        	if(col == 0)
        		return (timings.get(row).time) / 1000.0;
			return timings.get(row).flowRates[col-1];
        }
        @Override
        public boolean isCellEditable(int row, int col)
        {
        	return true;
        }
        @Override
        public void setValueAt(Object value, int row, int col) {
			
        	if(value instanceof Number)
        	{
        		if(col == 0)
            		timings.get(row).time = (long)(((Number)value).doubleValue() * 1000.0);
            	else
            		timings.get(row).flowRates[col-1] = ((Number)value).doubleValue();
        	}
		}
    }
	
	private class TimingTableEditor extends AbstractCellEditor implements
	    TableCellEditor, TableCellRenderer
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = -1118092155595116115L;
		
		private Component lastComponent = null;
		@Override
		public Object getCellEditorValue()
		{
			if(lastComponent == null)
				return "";
			else if(lastComponent instanceof PeriodField)
			{
				PeriodField periodField =(PeriodField)lastComponent;
				try
				{
					periodField.commitEdit();
				}
				catch(@SuppressWarnings("unused") ParseException e)
				{
					// do nothing.
				}
				return (periodField.getDurationLong())/1000.0;
			}
			else if(lastComponent instanceof DoubleTextField)
			{
				DoubleTextField valuesField = (DoubleTextField)lastComponent;
				try
				{
					valuesField.commitEdit();
				}
				catch(@SuppressWarnings("unused") ParseException e)
				{
					// do nothing.
				}
				return valuesField.getValue();
		        
			}
		    return 0.0;
		}
		
		// @Override
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
		{
			
			if(column == 0)
			{
				PeriodField periodField = new PeriodField(new TimeUnit[]{TimeUnit.SECOND, TimeUnit.MINUTE, TimeUnit.HOUR});
				periodField.setDuration(timings.get(row).time);
				
				lastComponent=periodField;
				return periodField;
			}
			DoubleTextField valuesField = new DoubleTextField();
			valuesField.setValue(timings.get(row).flowRates[column - 1]);
			
			valuesField.setBorder(new EmptyBorder(0, 0, 0, 0));
			valuesField.setMargin(new Insets(0, 0, 0, 0));
			lastComponent = valuesField;
			return valuesField;
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object object,
		        boolean isSelected, boolean hasFocus, int row, int column)
		{
			JLabel label = new JLabel(object.toString());
		    label.setOpaque(true);
		                
		    if (isSelected)
		    {
		        label.setBackground(table.getSelectionBackground());
		        label.setForeground(table.getSelectionForeground());
		    }
		    else
		    {
		    	label.setForeground(Color.BLACK);
		    	label.setBackground(Color.WHITE);
		    }
		                
		    return label;
		}
	}
}
