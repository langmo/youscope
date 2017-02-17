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

import java.awt.Dimension; 
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeriesCollection;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.clientinterfaces.YouScopeFrameListener;
import org.youscope.common.PositionInformation;
import org.youscope.common.table.ColumnView;
import org.youscope.common.table.Table;
import org.youscope.common.table.TableException;
import org.youscope.common.table.TableListener;
import org.youscope.common.table.TableProducer;



/**
 * Frame which displays incoming data from a table data producer as a plot.
 * @author Moritz Lang
 *
 */
class TableDataVisualizerFrame
{
	private final TableProducer tableDataProducer; 
	private TableListener tableDataListener;
	
	private int currentDataNum = 0; 
	private final String xColumnName;
	private final PlotColumnHandler[] yColumnHandler;
	
	private final XYSeriesCollection plotsCollection = new XYSeriesCollection();
	
	private final TableDataPlotType plotType;
	private final YouScopeClient client;
	private final String identityColumnName;
	
	/**
	 * Enumeration indicating choices of what should be plotted on the x-Axis.
	 * @author Moritz Lang
	 *
	 */
	public enum XAxisType
	{
		/**
		 * Time, in minutes.
		 */
		TimeMin
		{
			@Override
			public String toString()
			{
				return "Time (min)";
			}
		},
		/**
		 * Evaluation number.
		 */
		Evaluation
		{
			@Override
			public String toString()
			{
				return "Evaluation (-)";
			}
		},
		/**
		 * The value of a specific field in the table data.
		 */
		TableColumnValue
		{
			@Override
			public String toString()
			{
				return "Table Column Value (-)";
			}
		},
	}
	
	private final XAxisType xAxisType;
	private final PositionInformation positionInformation;
	/**
	 * Constructor
	 * @param positionInformation Position of component producing tables in measurement.
	 * @param tableDataProducer The job which produces the data.
	 * @param xAxisType Enumeration indicating what should be plotted on the x-axis (time, evaluation number, ...). 
	 * @param xColumnName The column of the table which should be plotted on the x-axis. Value is only active if xAxisType = TableColumnValue is selected.
	 * @param yColumnNames The columns plotted on the y-axes.
	 * @param plotType The type of the plot.
	 * @param identityColumnName the column used for identifying the data which is belonging to one curve in the plot. Value ignored by everything but TableDataPlotType.LineIdentity.
	 * @param client Interface to the YouScope client.
	 */
	public TableDataVisualizerFrame(PositionInformation positionInformation, TableProducer tableDataProducer, XAxisType xAxisType, String xColumnName, String[] yColumnNames, TableDataPlotType plotType, String identityColumnName, YouScopeClient client)
    {
		this.tableDataProducer = tableDataProducer;
		this.positionInformation = positionInformation;
		this.xAxisType = xAxisType;
		this.xColumnName = xColumnName;
		yColumnHandler = new PlotColumnHandler[yColumnNames.length];
		for(int i=0; i<yColumnNames.length; i++)
		{
			yColumnHandler[i] = new PlotColumnHandler(yColumnNames[i], plotsCollection);
		}
		this.plotType = plotType;
		this.client = client;
		this.identityColumnName = identityColumnName;
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

		// Create title
        String title = positionInformation.toString();
        try
        {
        	String tableName = tableDataProducer.getProducedTableDefinition().getTableName();
        	if(tableName.length() > 0 && title.length()>0)
            	title = title+", "+tableName;
        	else if(tableName.length() > 0)
        		title = tableName;
        }
        catch (@SuppressWarnings("unused") Exception e1)
        {
            // do nothing.
        }
        if(title.length()>0)
        	title+=": ";
        title += "Plot of ";
        for(int i=0; i<yColumnHandler.length; i++)
        {
        	if(i>0)
        	{
        		title += ", ";
        		if(i==yColumnHandler.length-1)
        			title+="and ";
        	}
        	title+=yColumnHandler[i];
        }
        frame.setTitle(title);
        
        class MicroscopeTableDataListener extends UnicastRemoteObject implements TableListener
        {
            /**
             * Serial Version UID.
             */
            private static final long serialVersionUID = 1112739907507416542L;

            private volatile boolean errorHappened = false;
            MicroscopeTableDataListener() throws RemoteException
            {
                super();
            }

            @Override
			public void newTableProduced(final Table table)
			{
            	if(errorHappened)
            		return;
            	Thread thread = new Thread(new Runnable()
                {
                	@Override
                    public void run()
                    {
                        try {
							addTableData(table);
						} catch (TableException e) {
							errorHappened = true;
							client.sendError("Error in visualizing table data.", e);
						}
                    }
                }, "Table Data Processor");
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
                    	TableDataVisualizerFrame.this.tableDataProducer.removeTableListener(tableDataListener);
                    } 
                    catch (Exception e)
                    {
                    	TableDataVisualizerFrame.this.client.sendError("Could not remove table data listener for visualization.", e);
                    } 
                }

                @Override
                public void frameOpened()
                {
                    if (tableDataListener == null)
                        return;
                    try
                    {
                    	TableDataVisualizerFrame.this.tableDataProducer.addTableListener(tableDataListener);
                    } 
                    catch (Exception e)
                    {
                    	TableDataVisualizerFrame.this.client.sendError("Could not add table data listener for visualization.", e);
                    } 
                }
            });
        
        XYItemRenderer renderer;
        if(plotType == TableDataPlotType.Scatter)
        {
	        XYDotRenderer dotRenderer = new XYDotRenderer();
	        dotRenderer.setDotHeight(5);
	        dotRenderer.setDotWidth(5);
	        renderer = dotRenderer;
        }
        else
        {
        	XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer();
        	renderer = lineRenderer;
        }

        String xString;
        if(xAxisType == XAxisType.TableColumnValue && xColumnName != null)
        	xString = xColumnName;
        else
        	xString = xAxisType.toString();
         
        String yString;
        if(yColumnHandler.length == 1)
        {
        	yString = yColumnHandler[0].toString();
        }
        else
        {
        	yString = "Value";
        }
        
        NumberAxis xAxis = new NumberAxis(xString);
        xAxis.setAutoRangeIncludesZero(false);
        NumberAxis yAxis = new NumberAxis(yString);
        yAxis.setAutoRangeIncludesZero(false);

        XYPlot plot = new XYPlot(plotsCollection, xAxis, yAxis, renderer);
        JFreeChart chart = new JFreeChart(plot);
        chart.setTextAntiAlias(true);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setDisplayToolTips(true);
        chartPanel.setDomainZoomable(true);
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setMouseZoomable(true);
        chartPanel.setRangeZoomable(true);
      
        chartPanel.setFillZoomRectangle(true);
        chartPanel.setZoomAroundAnchor(true);
        chartPanel.setMaximumDrawHeight(1500);
        chartPanel.setMaximumDrawWidth(2000);
        frame.setContentPane(chartPanel);
        frame.setSize(new Dimension(500, 500));
    }
	
	/**
	 * Adds the table data to the plot.
	 * @param table Table containing data to add.
	 * @throws TableException 
	 */
	public synchronized void addTableData(final Table table) throws TableException
    {
		currentDataNum++;
		if(table.getNumRows() == 0)
			return;
    	ColumnView<?> identityColumn;
    	if(plotType == TableDataPlotType.LineIdentity)
    	{
    		identityColumn = table.getColumnView(identityColumnName);
    	}
    	else
    		identityColumn = null;
    	
    	ColumnView<? extends Number> xColumn;
    	if(xAxisType == XAxisType.TableColumnValue)
    	{
    		xColumn = table.getColumnView(xColumnName, Number.class);
    	}
    	else
    		xColumn = null;
    	
    	ArrayList<ColumnView<? extends Number>> yColumns = new ArrayList<ColumnView<? extends Number>>(yColumnHandler.length);
    	for(int i=0; i<yColumnHandler.length; i++)
    	{
    		yColumns.add(table.getColumnView(yColumnHandler[i].getColumnName(), Number.class));
    	}
    	
    	long evaluationNumber = table.getExecutionInformation() == null ? currentDataNum : table.getExecutionInformation().getEvaluationNumber();
    	double timeMin = (table.getExecutionInformation() == null) ? currentDataNum : ((double)table.getCreationRuntime()) / 60000;
    	
    	
    	// get x values
    	double[] xVals = new double[table.getNumRows()];
    	for(int row=0; row<table.getNumRows(); row++)
    	{
    		if(xAxisType == XAxisType.Evaluation)
			{
				xVals[row]= evaluationNumber;
			}
			else if(xAxisType == XAxisType.TimeMin)
			{
				xVals[row]= timeMin;
			}
    		else if(xAxisType == XAxisType.TableColumnValue)
    		{
    			xVals[row] = xColumn.getValue(row).doubleValue();
    		}
    	}

    	// process data
    	for(int yColumnID=0; yColumnID<yColumns.size(); yColumnID++)
		{
    		ColumnView<? extends Number> columnView = yColumns.get(yColumnID);
    		double[] allValues = new double[table.getNumRows()];
    		int numValues = 0;
    		
    		for(int row=0; row<table.getNumRows(); row++)
        	{
    			if(columnView.get(row).isNull())
    				continue;
    			double yVal = columnView.get(row).getValue().doubleValue();
    			
    			
    			// Identity only for plot all values
        		String identityVal;
        		if(identityColumn == null)
        			identityVal = null;
        		else
        		{
        			identityVal = identityColumn.get(row).getValueAsString().trim();
        			if(identityVal == null || identityVal.length() <= 0)
        				continue;
        		}
        		
        		
    			if(plotType == TableDataPlotType.LineMean || plotType == TableDataPlotType.LineMedian)
    			{
    				allValues[numValues] = yVal;
    				numValues++;
    			}
    			else
    				yColumnHandler[yColumnID].addDate(xVals[row], yVal, identityVal);
        	}
    		if(numValues > 0 && plotType == TableDataPlotType.LineMean)
    		{
    			double meanValue = 0;
    			for(int k=0; k<numValues; k++)
    			{
    				meanValue += allValues[k];
    			}
    			yColumnHandler[yColumnID].addDate(xVals[0], meanValue / numValues, null);
    		}
    		else if(numValues > 0 && plotType == TableDataPlotType.LineMedian)
    		{
    			double[] sortArray = new double[numValues];
    			System.arraycopy(allValues, 0, sortArray, 0, numValues);
    			Arrays.sort(sortArray);
    			if(numValues % 2 == 1)
    			{
    				yColumnHandler[yColumnID].addDate(xVals[0], sortArray[numValues / 2], null);
    			}
    			else
    			{
    				yColumnHandler[yColumnID].addDate(xVals[0], (sortArray[numValues / 2 - 1] + sortArray[numValues / 2]) / 2, null);
    			}
    		}
		} 	
    }
}
