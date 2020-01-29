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
package org.youscope.plugin.dropletmicrofluidics.flexiblecontroller;

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
import org.youscope.uielements.DoubleTextField;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.ImageLoadingTools;
import org.youscope.uielements.PeriodField;
import org.youscope.uielements.TimeUnit;

/**
 * Table to set when which syringe is active.
 * @author Moritz Lang
 *
 */
class FlexibleSyringeTable extends JPanel
{
	/**
	 * Serial Version UID
	 */
	private static final long	serialVersionUID	= -147210452248761222L;
	private final JTable syringeTable;
	private final SyringeTableModel syringeTableModel;
	private int[] connectedSyringes;
	private ArrayList<FlexibleSyringeTableRow> rows = new ArrayList<FlexibleSyringeTableRow>();
	private final YouScopeClient client;
	public FlexibleSyringeTable(YouScopeClient client, final int[] connectedSyringes)
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
        syringeTable.setDefaultRenderer(Double.class, editor);
        syringeTable.setDefaultRenderer(SyringeControlState.class, editor);
        syringeTable.setDefaultEditor(Long.class, editor);
        syringeTable.setDefaultEditor(SyringeControlState.class, editor);
        syringeTable.setDefaultEditor(Double.class, editor);
        JScrollPane syringeTableScrollPane = new JScrollPane(syringeTable);
        syringeTableScrollPane.setPreferredSize(new Dimension(250, 70));
        syringeTableScrollPane.setMinimumSize(new Dimension(10, 10));
        add(syringeTableScrollPane, BorderLayout.CENTER);
        
        // Up, down, add and remove Buttons
        Icon addButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--plus.png", "Add Row");
        Icon deleteButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--minus.png", "Remove Row");
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
		FlexibleSyringeTableRow newRow;
		if(rows.size() > 0)
		{
			newRow = rows.get(rows.size()-1).clone();
			newRow.setStartTimeMS(newRow.getStartTimeMS()+60*60*1000);
		}
		else
		{
			newRow = new FlexibleSyringeTableRow(0, connectedSyringes.length);
			if(connectedSyringes.length > 0)
			{
				newRow.setTargetFlowRate(0, 4);
				newRow.setMaxDeltaFlowRate(2);
				newRow.setSyringeControlState(0, SyringeControlState.POSITIVE);
			}
		}
        rows.add(newRow);
        syringeTableModel.fireTableDataChanged();
	}
	
	public void setConnectedSyringes(int[] connectedSyringes)
	{
		if(connectedSyringes.length == this.connectedSyringes.length)
			return;
		ArrayList<FlexibleSyringeTableRow> newRows = new ArrayList<FlexibleSyringeTableRow>();
		this.connectedSyringes = connectedSyringes;
		for(FlexibleSyringeTableRow row : rows)
		{
			if(row.getNumSyringes() != connectedSyringes.length)
			{
				FlexibleSyringeTableRow newRow = new FlexibleSyringeTableRow(row.getStartTimeMS(), connectedSyringes.length);
				newRow.setMaxDeltaFlowRate(row.getMaxDeltaFlowRate());
				for(int i=0; i<connectedSyringes.length && i<row.getNumSyringes(); i++)
				{
					newRow.setSyringeControlState(i, row.getSyringeControlState(i));
					newRow.setTargetFlowRate(i, row.getTargetFlowRate(i));
				}
				newRows.add(newRow);
			}
			else
			{
				newRows.add(row.clone());
			}
		}
		this.rows = newRows;
		syringeTableModel.fireTableStructureChanged();
	}
	
	void setRows(FlexibleSyringeTableRow[] syringeTableRows)
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
		for(FlexibleSyringeTableRow row : syringeTableRows)
		{
			if(row.getNumSyringes() != connectedSyringes.length)
			{
				if(firstError)
				{
					client.sendError("Configuration has " + Integer.toString(row.getNumSyringes()) + " syringes defined, while currently only "+
								Integer.toString(connectedSyringes.length) + " syringes are defined. Trying to take over as much information about which syringe should be on when as possible, however, syringe table needs manual check.");
					firstError = false;
				}
				
				FlexibleSyringeTableRow newRow = new FlexibleSyringeTableRow(row.getStartTimeMS(), connectedSyringes.length);
				newRow.setMaxDeltaFlowRate(row.getMaxDeltaFlowRate());
				for(int i=0; i<connectedSyringes.length && i<row.getNumSyringes(); i++)
				{
					newRow.setSyringeControlState(i, row.getSyringeControlState(i));
					newRow.setTargetFlowRate(i, row.getTargetFlowRate(i));
				}
				rows.add(newRow);
			}
			else
			{
				rows.add(row.clone());
			}
		}
		syringeTableModel.fireTableDataChanged();
	}
	FlexibleSyringeTableRow[] getRows()
	{
		return rows.toArray(new FlexibleSyringeTableRow[rows.size()]);
	}
	
	private class SyringeTableModel extends AbstractTableModel
    {
        /**
         * Serial Version UID.
         */
        private static final long serialVersionUID = -1111111111111333139L;

        SyringeTableModel()
        {
           // do nothing.
        }

        @Override
        public String getColumnName(int col)
        {
        	if(col == 0)
        		return "Time";
        	if(col == 1)
        		return "Max delta flow (ul/min)";
        	if(col>=getColumnCount())
        		return "[INVALID COLUMN]";
        	int syrID = (col-2)/2;
        	if((col-2)%2 == 0)
        		return "Flow Syringe " +Integer.toString(connectedSyringes[syrID]+1);
			return "Control Syringe" +Integer.toString(connectedSyringes[syrID]+1);
        }

        @Override
        public int getRowCount()
        {
            return rows.size();
        }

        @Override
        public int getColumnCount()
        {
            return connectedSyringes.length*2+2;
        }

        @Override
        public Class<?> getColumnClass(int column)
        {
        	if(column == 0)
        		return Long.class;
        	else if(column == 1)
        		return Double.class;
        	else if(column % 2 == 0)
        		return Double.class;
        	else
        		return SyringeControlState.class;
        }

        @Override
        public Object getValueAt(int row, int col)
        {
        	if(col == 0)
        		return (rows.get(row).getStartTimeMS());
        	else if(col == 1)
        		return rows.get(row).getMaxDeltaFlowRate();
        	else
        	{
        		int syrID = (col-2)/2;
            	if((col-2)%2 == 0)
            		return rows.get(row).getTargetFlowRate(syrID);
				return rows.get(row).getSyringeControlState(syrID);
        	}
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
    		else if(value instanceof Double && col == 1)
    		{
    			rows.get(row).setMaxDeltaFlowRate((Double)value);
    		}
    		else if(col >= 2)
    		{
    			int syrID = (col-2)/2;
            	if((col-2)%2 == 0 && value instanceof Double)
            		rows.get(row).setTargetFlowRate(syrID, (Double) value);
            	else if((col-2)%2 == 1 && value instanceof SyringeControlState)
            		rows.get(row).setSyringeControlState(syrID, (SyringeControlState)value);
    		}
		}
    }
	
	private class SyringeTableEditor extends AbstractCellEditor implements
	    TableCellEditor, TableCellRenderer
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = -1118092155595116115L;
		
		private final Color FIXED_COLOR = new Color(0.5f, 0.5f, 0.0f);
		private final Color POSITIVE_COLOR = new Color(0.0f, 0.5f, 0.0f);
		private final Color NEGATIVE_COLOR = new Color(0.0f, 0.0f, 0.5f);
		private final Color TIME_COLOR = new Color(0.3f, 0.3f, 0.3f);
		
		private JComboBox<SyringeControlState> controlField = new JComboBox<SyringeControlState>(SyringeControlState.values());
		private PeriodField periodField = new PeriodField(new TimeUnit[]{TimeUnit.SECOND, TimeUnit.MINUTE, TimeUnit.HOUR, TimeUnit.DAY});
		private DoubleTextField flowField = new DoubleTextField();
		private Component lastComponent = null;
		
		private final JLabel label = new JLabel("");
		SyringeTableEditor()
		{
			label.setOpaque(true);
			flowField.addActionListener(new ActionListener() 
			{
				@Override
				public void actionPerformed(ActionEvent e) {
					fireEditingStopped();
				}
			});
			controlField.addActionListener(new ActionListener() 
			{
				@Override
				public void actionPerformed(ActionEvent e) {
					fireEditingStopped();
				}
			});
			controlField.setBorder(new EmptyBorder(0, 0, 0, 0));
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
			else if(lastComponent instanceof DoubleTextField)
			{
				return ((DoubleTextField)lastComponent).getValue();
			}
			else
				return "";
		}
		
		// @Override
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
		{
			if(value instanceof Long)
			{
				periodField.setDuration((Long)value);
				lastComponent=periodField;
				return periodField;
			}
			else if(value instanceof Double)
			{
				flowField.setValue(value);
				lastComponent=flowField;
				return flowField;
			}
			else if(value instanceof SyringeControlState)
			{
				controlField.setSelectedItem(value);
				lastComponent = controlField;
				return controlField;
			}
			return null;
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object object,
		        boolean isSelected, boolean hasFocus, int row, int column)
		{
			String text;
			if(object instanceof Long)
				text = TextTools.toDurationString((Long)object);
			else if(object instanceof Double)
				text = object.toString()+" ul/min";
			else
				text = object.toString();
			
			label.setText(" "+text);
		    if (isSelected)
		    {
		    	label.setForeground(Color.WHITE);
		    	if(column >= 2 && (column-2)/2 < rows.get(row).getNumSyringes())
		    	{
		    		SyringeControlState value = rows.get(row).getSyringeControlState((column-2)/2);
		    		switch(value)
		    		{
		    		case FIXED:
		    			label.setBackground(FIXED_COLOR);
		    			break;
		    		case POSITIVE:
		    			label.setBackground(POSITIVE_COLOR);
		    			break;
		    		case NEGATIVE:
		    			label.setBackground(NEGATIVE_COLOR);
		    			break;
					default:
						// Should not happen.
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
		    	if(column >= 2 && (column-2)/2 < rows.get(row).getNumSyringes())
		    	{
		    		SyringeControlState value = rows.get(row).getSyringeControlState((column-2)/2);
		    		switch(value)
		    		{
		    		case FIXED:
		    			label.setForeground(FIXED_COLOR);
		    			break;
		    		case POSITIVE:
		    			label.setForeground(POSITIVE_COLOR);
		    			break;
		    		case NEGATIVE:
		    			label.setForeground(NEGATIVE_COLOR);
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
