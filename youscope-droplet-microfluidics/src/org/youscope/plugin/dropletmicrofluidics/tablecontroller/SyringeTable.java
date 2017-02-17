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
package org.youscope.plugin.dropletmicrofluidics.tablecontroller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.AbstractCellEditor;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.util.TextTools;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.ImageLoadingTools;
import org.youscope.uielements.PeriodField;
import org.youscope.uielements.TimeUnit;

/**
 * Table to set when which syringe is active.
 * @author Moritz Lang
 *
 */
class SyringeTable extends JPanel
{
	/**
	 * Serial Version UID
	 */
	private static final long	serialVersionUID	= -847210452248761222L;
	private final JTable syringeTable;
	private final SyringeTableModel syringeTableModel;
	private int[] connectedSyringes = new int[0];
	private ArrayList<SyringeTableRow> rows = new ArrayList<SyringeTableRow>();
	private final YouScopeClient client;
	public SyringeTable(YouScopeClient client, final int[] connectedSyringes)
	{
		super(new BorderLayout(0, 0));
		this.client = client;
		this.connectedSyringes = connectedSyringes;
		this.syringeTableModel = new SyringeTableModel();
		this.syringeTable = new JTable(syringeTableModel);
		syringeTable.setAutoCreateColumnsFromModel(true);
		syringeTable.setRowSelectionAllowed(true);
        syringeTable.setColumnSelectionAllowed(false);
        syringeTable.setSurrendersFocusOnKeystroke(true);
        syringeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        syringeTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        syringeTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        SyringeTableEditor editor = new SyringeTableEditor();
        syringeTable.setDefaultRenderer(Long.class, editor);
        syringeTable.setDefaultRenderer(SyringeState.class, editor);
        syringeTable.setDefaultEditor(Long.class, editor);
        syringeTable.setDefaultEditor(SyringeState.class, editor);
        JScrollPane syringeTableScrollPane = new JScrollPane(syringeTable);
        syringeTableScrollPane.setPreferredSize(new Dimension(250, 70));
        syringeTableScrollPane.setMinimumSize(new Dimension(10, 10));
        add(syringeTableScrollPane, BorderLayout.CENTER);
        
        // Up, down, add and remove Buttons
        Icon addButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--plus.png", "Add Time Point");
        Icon deleteButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--minus.png", "Remove Time Point");
        JButton newPositionButton;
        if (addButtonIcon == null)
            newPositionButton = new JButton("Add Row");
        else
            newPositionButton = new JButton("Add Row", addButtonIcon);
        newPositionButton.setHorizontalAlignment(SwingConstants.LEFT);
        newPositionButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	addRow();
                }
            });
        JButton deletePositionButton;
        if (deleteButtonIcon == null)
            deletePositionButton = new JButton("Delete Row");
        else
            deletePositionButton = new JButton("Delete Row", deleteButtonIcon);
        deletePositionButton.setHorizontalAlignment(SwingConstants.LEFT);
        deletePositionButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    int row = syringeTable.getSelectedRow();
                    if (row < 0)
                        return;
                    rows.remove(row);
                    syringeTableModel.fireTableRowsDeleted(row, row);
                }
            });        
        DynamicPanel buttonPanel = new DynamicPanel();
        buttonPanel.add(newPositionButton);
        buttonPanel.add(deletePositionButton);
        buttonPanel.add(new JPanel());
        buttonPanel.addFillEmpty();
        
        add(buttonPanel, BorderLayout.EAST);
	}
	
	private void addRow()
	{
        SyringeTableRow row;
        if(rows.size() > 0)
        {
        	row = rows.get(rows.size()-1).clone();
        	row.setStartTimeMS(row.getStartTimeMS()+60*60*1000);
        }
        else
        {
        	SyringeState[] syringeStates = new SyringeState[connectedSyringes.length];
        	for(int i=0; i<syringeStates.length; i++)
        	{
        		syringeStates[i] = i==0 ? SyringeState.INFLOW : SyringeState.INACTIVE;
        	}
        	row = new SyringeTableRow(0, syringeStates);
        	
        }
        rows.add(row);	
        
        syringeTableModel.fireTableDataChanged();
	}
	
	public void setConnectedSyringes(int[] connectedSyringes)
	{
		if(connectedSyringes.length == this.connectedSyringes.length)
			return;
		ArrayList<SyringeTableRow> newRows = new ArrayList<SyringeTableRow>();
		this.connectedSyringes = connectedSyringes;
		for(SyringeTableRow row : rows)
		{
			if(row.getNumSyringes() != connectedSyringes.length)
			{
				SyringeState[] oldStates = row.getSyringeStates();
				SyringeState[] newStates = new SyringeState[connectedSyringes.length];
				int i=0;
				for(; i< newStates.length && i<oldStates.length; i++)
				{
					newStates[i] = oldStates[i];
				}
				for(; i<newStates.length; i++)
				{
					newStates[i] = SyringeState.INACTIVE;
				}
				newRows.add(new SyringeTableRow(row.getStartTimeMS(), newStates));
			}
			else
			{
				newRows.add(row.clone());
			}
		}
		this.rows = newRows;
		syringeTableModel.fireTableStructureChanged();
	}
	
	void setRows(SyringeTableRow[] syringeTableRows)
	{
		rows.clear();
		
		if(syringeTableRows == null)
		{
			addRow();
			return;
		}
		
		if(syringeTableRows.length == 0)
		{
			syringeTableModel.fireTableDataChanged();
			return;
		}
		Arrays.sort(syringeTableRows);
		boolean firstError = true;
		for(SyringeTableRow row : syringeTableRows)
		{
			if(row.getNumSyringes() != connectedSyringes.length)
			{
				if(firstError)
				{
					client.sendError("Configuration has " + Integer.toString(row.getNumSyringes()) + " syringes defined, while currently only "+
								Integer.toString(connectedSyringes.length) + " syringes are defined. Trying to take over as much information about which syringe should be on when as possible, however, syringe table needs manual check.");
					firstError = false;
				}
				SyringeState[] oldStates = row.getSyringeStates();
				SyringeState[] newStates = new SyringeState[connectedSyringes.length];
				int i=0;
				for(; i< newStates.length && i<oldStates.length; i++)
				{
					newStates[i] = oldStates[i];
				}
				for(; i<newStates.length; i++)
				{
					newStates[i] = SyringeState.INACTIVE;
				}
				rows.add(new SyringeTableRow(row.getStartTimeMS(), newStates));
			}
			else
			{
				rows.add(row.clone());
			}
		}
		syringeTableModel.fireTableDataChanged();
	}
	SyringeTableRow[] getRows()
	{
		return rows.toArray(new SyringeTableRow[rows.size()]);
	}
	
	private class SyringeTableModel extends AbstractTableModel
    {
        /**
         * Serial Version UID.
         */
        private static final long serialVersionUID = -1111111115571333139L;

        SyringeTableModel()
        {
           // do nothing.
        }

        @Override
        public String getColumnName(int col)
        {
        	if(col == 0)
        		return "Time";
        	if(col>=getColumnCount())
        		return "[INVALID COLUMN]";
			return "Syringe " + Integer.toString(connectedSyringes[col-1]+1);
        }

        @Override
        public int getRowCount()
        {
            return rows.size();
        }

        @Override
        public int getColumnCount()
        {
            return connectedSyringes.length+1;
        }

        @Override
        public Class<?> getColumnClass(int column)
        {
        	if(column == 0)
        		return Long.class;
			return SyringeState.class;
        }

        @Override
        public Object getValueAt(int row, int col)
        {
        	if(col == 0)
        		return (rows.get(row).getStartTimeMS());
			return rows.get(row).getSyringeState(col-1);
        }
        @Override
        public boolean isCellEditable(int row, int col)
        {
        	return true;
        }
        @Override
        public void setValueAt(Object value, int row, int col) {
			
        	if(value instanceof Long && col == 0)
        	{
        		rows.get(row).setStartTimeMS((Long)value);
        		if(row > 0)
        		{
        			if(rows.get(row).getStartTimeMS() < rows.get(row-1).getStartTimeMS())
        			{
        				Collections.sort(rows);
        				fireTableDataChanged();
        			}
        		}
        		if(row < rows.size()-1)
        		{
        			if(rows.get(row).getStartTimeMS() > rows.get(row+1).getStartTimeMS())
        			{
        				Collections.sort(rows);
        				fireTableDataChanged();
        			}
        		}
        	}
    		else if(value instanceof SyringeState && col > 0)
        		rows.get(row).setSyringeState(col-1,(SyringeState)value);
		}
    }
	
	private class SyringeTableEditor extends AbstractCellEditor implements
	    TableCellEditor, TableCellRenderer
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = -1118092155595116115L;
		
		private final Color INACTIVE_COLOR = new Color(0.5f, 0.5f, 0.0f);
		private final Color INFLOW_COLOR = new Color(0.0f, 0.5f, 0.0f);
		private final Color OUTFLOW_COLOR = new Color(0.0f, 0.0f, 0.5f);
		private final Color TIME_COLOR = new Color(0.3f, 0.3f, 0.3f);
		
		private JComboBox<SyringeState> valuesField = new JComboBox<SyringeState>(SyringeState.values());
		private PeriodField periodField = new PeriodField(new TimeUnit[]{TimeUnit.SECOND, TimeUnit.MINUTE, TimeUnit.HOUR, TimeUnit.DAY});
		private Component lastComponent = null;
		
		private final JLabel label = new JLabel("");
		SyringeTableEditor()
		{
			label.setOpaque(true);
			valuesField.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					fireEditingStopped();
				}
			});
			valuesField.setBorder(new EmptyBorder(0, 0, 0, 0));
		}
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
				return periodField.getDurationLong();
			}
			else if(lastComponent instanceof JComboBox)
			{
				JComboBox<?> valuesField = (JComboBox<?>)lastComponent;
				
				return valuesField.getSelectedItem();
		        
			}
		    return 0.0;
		}
		
		// @Override
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
		{
			if(column == 0)
			{
				
				periodField.setDuration(rows.get(row).getStartTimeMS());
				
				lastComponent=periodField;
				return periodField;
			}
			
			valuesField.setSelectedItem(rows.get(row).getSyringeState(column - 1));
			lastComponent = valuesField;
			return valuesField;
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object object,
		        boolean isSelected, boolean hasFocus, int row, int column)
		{
			String text;
			if(column == 0 && object instanceof Long)
				text = TextTools.toDurationString((Long)object);
			else
				text = object.toString();
			
			label.setText(" "+text);
		    if (isSelected)
		    {
		    	label.setForeground(Color.WHITE);
		    	if(object instanceof SyringeState)
		    	{
		    		SyringeState value = (SyringeState)object;
		    		switch(value)
		    		{
		    		case INACTIVE:
		    			label.setBackground(INACTIVE_COLOR);
		    			break;
		    		case INFLOW:
		    			label.setBackground(INFLOW_COLOR);
		    			break;
		    		case OUTFLOW:
		    			label.setBackground(OUTFLOW_COLOR);
		    			break;
		    		default:
						// should not happen
						label.setBackground(Color.RED);
						break;
		    		}
		    	}
		    	else
		    	{
		    		label.setBackground(TIME_COLOR);
		    	}
		    }
		    else
		    {
		    	label.setBackground(Color.WHITE);
		    	if(object instanceof SyringeState)
		    	{
		    		SyringeState value = (SyringeState)object;
		    		switch(value)
		    		{
		    		case INACTIVE:
		    			label.setForeground(INACTIVE_COLOR);
		    			break;
		    		case INFLOW:
		    			label.setForeground(INFLOW_COLOR);
		    			break;
		    		case OUTFLOW:
		    			label.setForeground(OUTFLOW_COLOR);
		    			break;
		    		default:
						// should not happen
						label.setForeground(Color.RED);
						break;
		    		}
		    	}
		    	else
		    	{
		    		label.setForeground(TIME_COLOR);
		    	}
		    }
		                
		    return label;
		}
	}
}
