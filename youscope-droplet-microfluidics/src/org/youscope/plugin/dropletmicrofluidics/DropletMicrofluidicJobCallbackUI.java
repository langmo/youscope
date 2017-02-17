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
package org.youscope.plugin.dropletmicrofluidics;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.ExecutionInformation;
import org.youscope.common.PositionInformation;
import org.youscope.common.callback.CallbackException;
import org.youscope.common.table.Table;
import org.youscope.common.table.TableEntry;
import org.youscope.common.table.TableException;

class DropletMicrofluidicJobCallbackUI
{
    private final YouScopeClient client;
    private volatile YouScopeFrame frame = null;
    private int numRegistered = 0;
    private final ArrayList<String> dropletNames = new ArrayList<String>();
    private final CurrentObservationPanel currentObservationPanel;
    private final AllDropletsPanel alldropletsPanel;
    private final DeltaFlowPanel deltaFlowPanel;
    private final AllFlowsPanel allFlowsPanel;
    private final int chipID;
    
    private volatile boolean receivedData = false;
    private final int[] connectedSyringes;
    
    DropletMicrofluidicJobCallbackUI(final YouScopeClient client, int chipID, int[] connectedSyringes)
    {
        this.client = client;
        this.chipID = chipID;
        this.connectedSyringes = connectedSyringes;
        currentObservationPanel = new CurrentObservationPanel();
        alldropletsPanel = new AllDropletsPanel();
        deltaFlowPanel = new DeltaFlowPanel();
        allFlowsPanel = new AllFlowsPanel();
    }
    
    private class CurrentObservationPanel
    {
    	private final ChartPanel chartPanel;
    	final XYSeries predictionCurve = new XYSeries("Predicted");
    	final XYSeries observationCurve = new XYSeries("Observed");
    	final XYSeries meanCurve = new XYSeries("Mean");
    	CurrentObservationPanel()
    	{
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
                                    
            NumberAxis xAxis = new NumberAxis("Iteration");
            xAxis.setAutoRangeIncludesZero(false);
            NumberAxis yAxis = new NumberAxis("Droplet height offset (um)");
            yAxis.setAutoRangeIncludesZero(false);

            XYSeriesCollection plotsCollection = new XYSeriesCollection();
            predictionCurve.add(0, 0);
    		plotsCollection.addSeries(predictionCurve);
    		plotsCollection.addSeries(observationCurve);
    		plotsCollection.addSeries(meanCurve);
    		renderer.setSeriesShapesVisible(0, false);
    		renderer.setSeriesShapesVisible(1, false);
    		renderer.setSeriesShapesVisible(2, false);
            
            XYPlot plot = new XYPlot(plotsCollection, xAxis, yAxis, renderer);
            JFreeChart chart = new JFreeChart(plot);
            chart.setTextAntiAlias(true);
            chartPanel = new ChartPanel(chart);
            chartPanel.setDisplayToolTips(true);
            chartPanel.setDomainZoomable(true);
            chartPanel.setMouseWheelEnabled(true);
            chartPanel.setMouseZoomable(true);
            chartPanel.setRangeZoomable(true);
          
            chartPanel.setFillZoomRectangle(true);
            chartPanel.setZoomAroundAnchor(true);
            chartPanel.setMaximumDrawHeight(1500);
            chartPanel.setMaximumDrawWidth(2000);
    	}
    	
    	void addObservation(double currentX, double nextX, double nextEstimate, double measuredOffset, double meanOffset)
    	{
    		predictionCurve.add(nextX, nextEstimate);
	    	observationCurve.add(currentX, measuredOffset);
	    	meanCurve.add(currentX, meanOffset);
    	}
    }
    
    private class DeltaFlowPanel
    {
    	private final ChartPanel chartPanel;
    	final XYSeries deltaFlowCurve = new XYSeries("Delta flow");
    	DeltaFlowPanel()
    	{
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
                                    
            NumberAxis xAxis = new NumberAxis("Iteration");
            xAxis.setAutoRangeIncludesZero(false);
            NumberAxis yAxis = new NumberAxis("Delta flow (ul/min)");
            yAxis.setAutoRangeIncludesZero(false);

            XYSeriesCollection plotsCollection = new XYSeriesCollection();
            plotsCollection.addSeries(deltaFlowCurve);
    		renderer.setSeriesShapesVisible(0, false);
    		
            XYPlot plot = new XYPlot(plotsCollection, xAxis, yAxis, renderer);
            JFreeChart chart = new JFreeChart(plot);
            chart.setTextAntiAlias(true);
            chartPanel = new ChartPanel(chart);
            chartPanel.setDisplayToolTips(true);
            chartPanel.setDomainZoomable(true);
            chartPanel.setMouseWheelEnabled(true);
            chartPanel.setMouseZoomable(true);
            chartPanel.setRangeZoomable(true);
          
            chartPanel.setFillZoomRectangle(true);
            chartPanel.setZoomAroundAnchor(true);
            chartPanel.setMaximumDrawHeight(1500);
            chartPanel.setMaximumDrawWidth(2000);
            chartPanel.getChart().removeLegend();
    	}
    	void addDeltaFlow(double currentX, double deltaFlow)
    	{
    		deltaFlowCurve.add(currentX, deltaFlow);
    	}
    }
    
    private class AllFlowsPanel
    {
    	private final ChartPanel chartPanel;
    	private XYSeries[] flowsPlots = null;
    	private XYLineAndShapeRenderer renderer;
    	private XYSeriesCollection plotsCollection;
    	AllFlowsPanel()
    	{
            renderer = new XYLineAndShapeRenderer();
                                    
            NumberAxis xAxis = new NumberAxis("Iteration");
            xAxis.setAutoRangeIncludesZero(false);
            NumberAxis yAxis = new NumberAxis("Flow Rate (ul/min)");
            yAxis.setAutoRangeIncludesZero(false);

            plotsCollection = new XYSeriesCollection();
  
    		XYPlot plot = new XYPlot(plotsCollection, xAxis, yAxis, renderer);
            JFreeChart chart = new JFreeChart(plot);
            chart.setTextAntiAlias(true);
            chartPanel = new ChartPanel(chart);
            chartPanel.setDisplayToolTips(true);
            chartPanel.setDomainZoomable(true);
            chartPanel.setMouseWheelEnabled(true);
            chartPanel.setMouseZoomable(true);
            chartPanel.setRangeZoomable(true);
          
            chartPanel.setFillZoomRectangle(true);
            chartPanel.setZoomAroundAnchor(true);
            chartPanel.setMaximumDrawHeight(1500);
            chartPanel.setMaximumDrawWidth(2000);
    	}
    	void addAll(double currentX, double[] flowRates)
    	{
    		if(flowsPlots == null)
    		{
    			flowsPlots = new XYSeries[flowRates.length];
    			for(int i=0; i<flowRates.length; i++)
    			{
    				if(connectedSyringes == null || i>=connectedSyringes.length)
    					flowsPlots[i] = new XYSeries("Flow Unit " + Integer.toString(i+1));
    				else
    					flowsPlots[i] = new XYSeries("Flow Unit " + Integer.toString(connectedSyringes[i]+1));
    				plotsCollection.addSeries(flowsPlots[i]);
    				renderer.setSeriesShapesVisible(i, false);
    			}
    		}
    		for(int i=0; i<flowRates.length && i < flowsPlots.length; i++)
    		{
    			flowsPlots[i].add(currentX, flowRates[i]);
    		}
    	}
    }
    
    private class AllDropletsPanel
    {
    	private final ChartPanel chartPanel;
    	private XYSeries[] dropletHeightPlots = null;
    	private XYLineAndShapeRenderer renderer;
    	private XYSeriesCollection plotsCollection;
    	AllDropletsPanel()
    	{
            renderer = new XYLineAndShapeRenderer();
                                    
            NumberAxis xAxis = new NumberAxis("Iteration");
            xAxis.setAutoRangeIncludesZero(false);
            NumberAxis yAxis = new NumberAxis("Estimated offset (um)");
            yAxis.setAutoRangeIncludesZero(false);

            plotsCollection = new XYSeriesCollection();
  
    		XYPlot plot = new XYPlot(plotsCollection, xAxis, yAxis, renderer);
            JFreeChart chart = new JFreeChart(plot);
            chart.setTextAntiAlias(true);
            chartPanel = new ChartPanel(chart);
            chartPanel.setDisplayToolTips(true);
            chartPanel.setDomainZoomable(true);
            chartPanel.setMouseWheelEnabled(true);
            chartPanel.setMouseZoomable(true);
            chartPanel.setRangeZoomable(true);
          
            chartPanel.setFillZoomRectangle(true);
            chartPanel.setZoomAroundAnchor(true);
            chartPanel.setMaximumDrawHeight(1500);
            chartPanel.setMaximumDrawWidth(2000);
            chartPanel.getChart().getLegend().setItemFont(new Font(Font.SANS_SERIF, Font.PLAIN, 8));
    	}
    	void addAll(double currentX, double[] offsets)
    	{
    		if(dropletHeightPlots == null)
    		{
    			dropletHeightPlots = new XYSeries[offsets.length];
    			for(int i=0; i<offsets.length; i++)
    			{
    				String name;
    				if(i < dropletNames.size())
    					name = dropletNames.get(i);
    				else
    					name = "Droplet " + Integer.toString(i+1);
    				dropletHeightPlots[i] = new XYSeries(name);
    				plotsCollection.addSeries(dropletHeightPlots[i]);
    				renderer.setSeriesShapesVisible(i, false);
    			}
    		}
    		for(int i=0; i<offsets.length && i < dropletHeightPlots.length; i++)
    		{
    			dropletHeightPlots[i].add(currentX, offsets[i]);
    		}
    	}
    }
    
    
    private synchronized void setupUI()
    {
        if(frame != null)
        {
        	frame.setVisible(true);
        	return;
        }
        frame = client.createFrame();
    	
        frame.setTitle("Droplet microfluidics, Chip " + Integer.toString(chipID));
        frame.setClosable(false);

        JPanel mainPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        mainPanel.add(currentObservationPanel.chartPanel);
        mainPanel.add(alldropletsPanel.chartPanel);
        mainPanel.add(deltaFlowPanel.chartPanel);
        mainPanel.add(allFlowsPanel.chartPanel);
        
        frame.setContentPane(mainPanel);
        frame.setSize(new Dimension(600, 480));
        frame.setVisible(true);
    }
    
    boolean isReceivedData()
    {
    	return receivedData;
    }
    void dropletMeasured(ExecutionInformation executionInformation, Table table) throws CallbackException
	{
    	receivedData = true;
    	try
    	{
	    	int numDroplets = 0;
	    	double deltaFlow = table.getColumnView(DropletMicrofluidicTable.COLUMN_DELTA_FLOW).getValue(0);
	    	double meanOffset = table.getColumnView(DropletMicrofluidicTable.COLUMN_DROPLETS_MEAN_OFFSET).getValue(0);
	    	double measuredOffset = table.getColumnView(DropletMicrofluidicTable.COLUMN_CURRENT_DROPLET_MEASURED_OFFSET).getValue(0);
	    	int currentDroplet = table.getColumnView(DropletMicrofluidicTable.COLUMN_CURRENT_DROPLET_ID).getValue(0);
	    	double[] estimatedHeightsTemp = new double[table.getNumRows()];
	    	for(TableEntry<? extends Double> entry : table.getColumnView(DropletMicrofluidicTable.COLUMN_DROPLET_ESTIMATED_OFFSET))
	    	{
	    		if(!entry.isNull())
	    		{
	    			estimatedHeightsTemp[numDroplets] = entry.getValue();
	    			numDroplets++;
	    		}
	    	}
	    	double[] estimatedHeights = new double[numDroplets];
	    	System.arraycopy(estimatedHeightsTemp, 0, estimatedHeights, 0, numDroplets);
	    	
	    	int nextDroplet = (currentDroplet+1) % numDroplets;
	    	double nextEstimate = estimatedHeights[nextDroplet];
	    	double currentX = executionInformation.getEvaluationNumber() + ((double)currentDroplet)/numDroplets;
	    	double nextX =  executionInformation.getEvaluationNumber() + ((double)currentDroplet+1)/numDroplets;
	    	
	    	double[] flowsTemp = new double[table.getNumRows()];
	    	int numFlowUnits = 0;
	    	for(TableEntry<? extends Double> entry : table.getColumnView(DropletMicrofluidicTable.COLUMN_FLOW_UNIT_FLOW_RATE))
	    	{
	    		if(!entry.isNull())
	    		{
	    			flowsTemp[numFlowUnits] = entry.getValue();
	    			numFlowUnits++;
	    		}
	    	}
	    	double[] flowRates = new double[numFlowUnits];
	    	System.arraycopy(flowsTemp, 0, flowRates, 0, numFlowUnits);
	    	
	    	currentObservationPanel.addObservation(currentX, nextX, nextEstimate, measuredOffset, meanOffset);
	    	alldropletsPanel.addAll(currentX, estimatedHeights);
	    	deltaFlowPanel.addDeltaFlow(currentX, deltaFlow);
	    	allFlowsPanel.addAll(currentX, flowRates);
    	}
    	catch(TableException e)
    	{
    		throw new CallbackException("Could not process droplet microfluidics table.", e);
    	}
	}

	synchronized void initializeCallback(Serializable... arguments) throws RemoteException, CallbackException {
		numRegistered++;
		String dropletName;
		if(arguments == null || arguments.length < 1 || arguments[0]==null || !(arguments[0] instanceof PositionInformation) || ((PositionInformation)arguments[0]).getWell() == null)
		{
			dropletName = "Droplet " + Integer.toString(numRegistered); 
		}
		else
		{
			dropletName = "Droplet " + ((PositionInformation)arguments[0]).getWell().getWellName();
		}
		dropletNames.add(dropletName);
		
		setupUI();
	}

	synchronized void uninitializeCallback() throws RemoteException, CallbackException {
		numRegistered--;
		receivedData = true; // use anyway a new singleton.
		if(numRegistered == 0 && frame != null)
		{
			frame.setClosable(true);
		}
	}
}
