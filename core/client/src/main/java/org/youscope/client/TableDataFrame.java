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
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.clientinterfaces.YouScopeFrameListener;
import org.youscope.common.PositionInformation;
import org.youscope.common.table.Table;
import org.youscope.common.table.TableDefinition;
import org.youscope.common.table.TableListener;
import org.youscope.common.table.TableProducer;

/**
 * Frame to display produced table data of a job during the execution of a measurement
 * @author Moritz Lang
 *
 */
class TableDataFrame
{
	private TableListener tableDataListener = null;

    private final TableProducer tableDataProducer; 

    private JTable tableDataTable;

    private TableDataModel tableDataModel;
    
    private TableDataPlotterPanel tablePlotter;
    
    /**
     * The columns of the table. Initialize to zero columns at startup, since only known after first element arrived.
     */
    private final String frameTitel;
    private final YouScopeClient client;
    private final PositionInformation positionInformation;
    /**
     * Constructor.
     * @param tableDataProducer The job which is producing the table data.
     * @param positionInformation The position in which the data is produced.
     * @param client Interface to the YouScope client.
     */
    public TableDataFrame(TableProducer tableDataProducer, PositionInformation positionInformation, YouScopeClient client)
    {
    	this.tableDataProducer = tableDataProducer;
    	this.client = client;
    	this.positionInformation = positionInformation;
    	
    	// Create title
    	String frameTitel = "";
    	frameTitel+= positionInformation.toString();
        
        if(frameTitel.length() > 0)
    		frameTitel+=", ";
        try
        {
        	frameTitel += tableDataProducer.getProducedTableDefinition().getTableName();
        } 
        catch (Exception e1)
        {
            client.sendError("Could not obtain table data description from image producer. Substituting default string.", e1);
            frameTitel+= "Unknown Table Data";
        }
        
        this.frameTitel = frameTitel;
    }
    
    /**
	 * Creates the UI in the frame.
	 * @param frame
	 */
	public void createUI(YouScopeFrame frame)
	{		
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setMaximizable(true);
		frame.setTitle(frameTitel);
        
        class MicroscopeTableDataListener extends UnicastRemoteObject implements TableListener
        {
            /**
             * Serial Version UID.
             */
            private static final long serialVersionUID = 1112739907507416542L;

            MicroscopeTableDataListener() throws RemoteException
            {
                super();
            }

            @Override
            public void newTableProduced(final Table table)
			{
            	// Start new thread to process table data.
            	Thread thread = new Thread(new Runnable()
            	{
            		@Override
                    public void run()
                    {
                        setTable(table);
                    }
            	}, "Table data processor");
                thread.start();
			}
        }
        try
        {
            tableDataListener = new MicroscopeTableDataListener();
        } 
        catch (RemoteException e1)
        {
        	client.sendError("Could not construct table data listener for visualization.", e1);
            tableDataListener = null;
        }

        frame.addFrameListener(new YouScopeFrameListener()
            {

                @Override
                public void frameClosed()
                {
                    if (tableDataListener == null)
                        return;
                    try
                    {
                        TableDataFrame.this.tableDataProducer.removeTableListener(tableDataListener);
                    } 
                    catch (Exception e)
                    {
                    	client.sendError("Could not remove table data listener for visualization.", e);
                    } 
                }

                @Override
                public void frameOpened()
                {
                    if (tableDataListener == null)
                        return;
                    try
                    {
                    	TableDataFrame.this.tableDataProducer.addTableListener(tableDataListener);
                    } 
                    catch (Exception e)
                    {
                    	client.sendError("Could not add table data listener for visualization.", e);
                    } 
                }
            });
        TableDefinition tableDefinition;
        try
		{
			tableDefinition = TableDataFrame.this.tableDataProducer.getProducedTableDefinition();
		}
		catch(Exception e)
		{
			client.sendError("Could not obtain table data information form table producing job.", e);
			tableDefinition = null;
		}
        
     // Center Panel
        JPanel centralPanel = new JPanel(new BorderLayout(2, 2));
        tableDataModel = new TableDataModel(tableDefinition);
        tableDataTable = new JTable(tableDataModel);
        
        tableDataTable.setRowSelectionAllowed(true);
        tableDataTable.setColumnSelectionAllowed(true);
        tableDataTable.setSurrendersFocusOnKeystroke(true);
        tableDataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        //tableDataTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        tableDataTable.setAutoCreateColumnsFromModel(true);
        JScrollPane tableDataScrollPane = new JScrollPane(tableDataTable);
        tableDataScrollPane.setMinimumSize(new Dimension(100, 50));
        centralPanel.add(tableDataScrollPane, BorderLayout.CENTER);
        centralPanel.setPreferredSize(new Dimension(500, 200));
        
        tablePlotter = new TableDataPlotterPanel(positionInformation, tableDataProducer, tableDefinition, client);
        
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(centralPanel, BorderLayout.CENTER);
        contentPane.add(tablePlotter, BorderLayout.SOUTH);        
        
        frame.setContentPane(contentPane);
        frame.pack();
    }
    
    private synchronized void setTable(Table table)
    {
    	tableDataModel.addTable(table);
    	tablePlotter.setTableDefinition(table.getTableDefinition());
    }
        
    private class TableDataModel extends AbstractTableModel
    {
        /**
         * Serial Version UID.
         */
        private static final long serialVersionUID = -2836722115571383139L;
        private final String[] defaultColumns = {"Evaluation", "Measurement Time", "Absolute Time", "Well", "Position"};
        
        private TableDefinition tableDefinition;
        private String[] tableColumns;
        private String[][] tableData;
        private int firstRow=0;
        private int numRows = 0;
        private final static int MAX_ROWS = 100;
        private final SimpleDateFormat absoluteTimeFormat = new SimpleDateFormat("HH:mm:ss");
        TableDataModel(TableDefinition tableDefinition)
        {
        	setTableDefinition(tableDefinition);
        }
        private void setTableDefinition(TableDefinition tableDefinition)
        {
        	this.tableDefinition = tableDefinition;
        	String[] specificColumns = tableDefinition.getColumnNames();
        	tableColumns = new String[specificColumns.length+defaultColumns.length];
        	System.arraycopy(defaultColumns, 0, tableColumns, 0, defaultColumns.length);
        	System.arraycopy(specificColumns, 0, tableColumns, defaultColumns.length, specificColumns.length);
        	this.tableData = new String[MAX_ROWS][tableColumns.length];
        	this.numRows = 0;
        	this.firstRow = 0;
        }
        private boolean addTableData(Table table)
        {
        	boolean layoutChanged;
        	if(!tableDefinition.equals(table.getTableDefinition()))
			{
        		setTableDefinition(table.getTableDefinition());
				layoutChanged = true;
			}
        	else
        		layoutChanged = false;
        	for(int row =0; row<table.getNumRows(); row++)
        	{
        		int idx;
        		if(numRows == MAX_ROWS)
        		{
        			idx = firstRow;
        			firstRow = (firstRow+1) % MAX_ROWS;
        		}
        		else
        		{
        			idx = firstRow+numRows;
        			numRows++;
        		}
        		tableData[idx][0] = table.getExecutionInformation() == null ? "" : table.getExecutionInformation().getEvaluationString();
        		long timeInS = table.getCreationRuntime()/1000;
        		tableData[idx][1] = String.format("%dh %02dmin %02ds", timeInS / 3600, (timeInS % 3600) / 60, (timeInS % 60)); 
        		tableData[idx][2] = absoluteTimeFormat.format(new Date(table.getCreationTime()));
        		tableData[idx][3] = (table.getPositionInformation() != null && table.getPositionInformation().getWell() != null) ? table.getPositionInformation().getWell().getWellName() : "";
        		tableData[idx][4] = table.getPositionInformation() != null ? table.getPositionInformation().getPositionsString() : "";
        		for(int i = 0; i<table.getNumColumns(); i++)
        		{
        			tableData[idx][defaultColumns.length+i] = table.getEntry(row, i).getValueAsString(); 
        		} 
        	}
        	return layoutChanged;
        	
        }
        private void addTable(final Table table)
        {
        	Runnable runner = new Runnable()
			{
				@Override
				public void run() 
				{
					if(addTableData(table))
						fireTableStructureChanged();
					else
						fireTableDataChanged();
					
				}
			};
			if(SwingUtilities.isEventDispatchThread())
				runner.run();
			else
				SwingUtilities.invokeLater(runner);
        }

        @Override
        public String getColumnName(int col)
        {
        	if(col >= 0 && col < tableColumns.length)
        		return tableColumns[col];
			return "Invalid Column";
        }

        @Override
        public int getRowCount()
        {
        	return numRows;
        }

        @Override
        public int getColumnCount()
        {
        	return tableColumns.length;
        }

        @Override
        public Class<?> getColumnClass(int column)
        {
        	return String.class;
        }

        @Override
        public Object getValueAt(int row, int col)
        {
        	if(col >= 0 && col < tableColumns.length)
        		return tableData[(firstRow+row)%MAX_ROWS][col];
        	return "Invalid Cell";
        }
        @Override
        public boolean isCellEditable(int row, int col)
        {
        	return false;
        }
        @Override
        public void setValueAt(Object value, int row, int col) 
        {
        	// function not supported.
		}
    }
}
