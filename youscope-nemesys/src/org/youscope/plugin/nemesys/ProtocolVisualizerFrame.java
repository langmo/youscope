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
package org.youscope.plugin.nemesys;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.PeriodField;
import org.youscope.uielements.TimeUnit;



/**
 * Frame which displays a Nemesys protocol.
 * @author Moritz Lang
 *
 */
class ProtocolVisualizerFrame
{
	private final XYSeriesCollection plotsCollection = new XYSeriesCollection();
	private NumberAxis xAxis = null;
	
	private final YouScopeClient client;
	
	private final String script;
	private final String scriptEngine;
	private final String[] flowRateUnits;
	private final double[] flowRateMax;
	private final PeriodField startTimeField = new PeriodField();
	private final PeriodField endTimeField = new PeriodField();
	private final PeriodField timeStepField = new PeriodField();

	public ProtocolVisualizerFrame(YouScopeClient client, String[] flowRateUnits, double[] flowRateMax,  String script, String scriptEngine)
    {
		this.client = client;
		this.script = script;
		this.scriptEngine = scriptEngine;
		this.flowRateUnits = flowRateUnits;
		this.flowRateMax = flowRateMax;
		
    }
	/**
	 * Creates the UI in the frame.
	 * @param frame
	 */
	public void createUI(final YouScopeFrame frame)
	{
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setMaximizable(true);
        frame.setTitle("Nemesys Protocol Visualized");
        
       	XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        
       	String xString = "Time (s)"; 
        String yString = "Flow Rate";
        
        xAxis = new NumberAxis(xString);
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
        
        DynamicPanel settingsPanel = new DynamicPanel();
        settingsPanel.setBorder(new TitledBorder("Plot Settings"));
        settingsPanel.add(new JLabel("Start Time:"));
        startTimeField.setDuration(0);
        settingsPanel.add(startTimeField);
        
        settingsPanel.add(new JLabel("End Time:"));
		endTimeField.setDuration(10*60*1000);
		settingsPanel.add(endTimeField);
		
		settingsPanel.add(new JLabel("Time Step:"));
		timeStepField.setDuration(1000);
		settingsPanel.add(timeStepField);
		
		JButton calculateButton = new JButton("Actualize Plot");
		calculateButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				final int startTime = startTimeField.getDuration();
				final int endTime = endTimeField.getDuration();
				final int timeStep = timeStepField.getDuration();
				if(startTime >= endTime)
				{
					client.sendError("Start time must be smaller than end time.");
					return;
				}
				else if(timeStep > endTime-startTime)
				{
					client.sendError("Time step size must be at least equal the difference between start and end time.");
					return;
				}
				
				frame.startLoading();
				new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							actualizePlot(startTime, endTime, timeStep);
						}
						catch(NemesysException e)
						{
							client.sendError("Error while creating plot for Nemesys protocol.", e);
						}
						
						frame.endLoading();
					}
				}).start();
			}
		});
		settingsPanel.add(calculateButton);
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(settingsPanel, BorderLayout.SOUTH);
		mainPanel.add(chartPanel, BorderLayout.CENTER);
        
        frame.setContentPane(mainPanel);
        frame.setSize(new Dimension(500, 500));
    }
	
	/**
	 * A virtual Nemesys device control pretending to a script that a real Nemesys device is connected.
	 * @author Moritz Lang
	 *
	 */
	private class VirtualScriptCallback extends UnicastRemoteObject implements NemesysScriptCallback
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= 6314754666980275253L;
		private final double[] currentFlowRates;
		private final HashMap<String, String> states = new HashMap<String,String>();
		VirtualScriptCallback() throws RemoteException
		{
			currentFlowRates = new double[flowRateUnits.length];
			for(int i=0; i<currentFlowRates.length; i++)
			{
				currentFlowRates[i] = 0.0;
			}
		}
		
		@Override
		public int getNumberOfDosingUnits() throws RemoteException, NemesysException, InterruptedException
		{
			return flowRateUnits.length;
		}

		@Override
		public double getFlowRateMax(int dosingUnit) throws RemoteException, NemesysException, InterruptedException
		{
			if(dosingUnit < 0 || dosingUnit >= flowRateUnits.length)
				throw new NemesysException("The parameter dosingUnit must be bigger or equal to 0 and smaller than " + Integer.toString(flowRateUnits.length) + " (" + Integer.toString(flowRateUnits.length) + " dosing units connected to Nemesys device)");
			return flowRateMax[dosingUnit];
		}

		@Override
		public void setFlowRate(int dosingUnit, double flowRate) throws RemoteException, NemesysException, InterruptedException
		{
			if(dosingUnit < 0 || dosingUnit >= flowRateUnits.length)
				throw new NemesysException("The parameter dosingUnit must be bigger or equal to 0 and smaller than " + Integer.toString(flowRateUnits.length) + " (" + Integer.toString(flowRateUnits.length) + " dosing units connected to Nemesys device)");
			currentFlowRates[dosingUnit] = flowRate;
			
		}

		@Override
		public double getFlowRate(int dosingUnit) throws NemesysException
		{
			if(dosingUnit < 0 || dosingUnit >= flowRateUnits.length)
				throw new NemesysException("The parameter dosingUnit must be bigger or equal to 0 and smaller than " + Integer.toString(flowRateUnits.length) + " (" + Integer.toString(flowRateUnits.length) + " dosing units connected to Nemesys device)");
			return currentFlowRates[dosingUnit];
		}

		@Override
		public String getFlowUnit(int dosingUnit) throws RemoteException, NemesysException, InterruptedException
		{
			if(dosingUnit < 0 || dosingUnit >= flowRateUnits.length)
				throw new NemesysException("The parameter dosingUnit must be bigger or equal to 0 and smaller than " + Integer.toString(flowRateUnits.length) + " (" + Integer.toString(flowRateUnits.length) + " dosing units connected to Nemesys device)");
			return flowRateUnits[dosingUnit];
		}

		@Override
		public String getStateAsString(String state, String defaultValue) throws RemoteException
		{
			String value = states.get(state);
			if(value == null)
				return defaultValue;
			return value;
		}

		@Override
		public double getStateAsDouble(String state, double defaultValue) throws RemoteException, NumberFormatException
		{
			return Double.parseDouble(getStateAsString(state, Double.toString(defaultValue)));
		}

		@Override
		public int getStateAsInteger(String state, int defaultValue) throws RemoteException, NumberFormatException
		{
			double val = getStateAsDouble(state, defaultValue);
			if(Math.abs(val - ((int)val)) < 0.000000001)
				return (int)val;

			throw new NumberFormatException("State " + state + " has value " + Double.toString(val) + ", which is not an integer.");
		}

		@Override
		public void setState(String state, String value) throws RemoteException
		{
			states.put(state, value);
		}

		@Override
		public void setState(String state, int value) throws RemoteException
		{
			states.put(state, Integer.toString(value));
		}

		@Override
		public void setState(String state, double value) throws RemoteException
		{
			states.put(state, Double.toString(value));
		}
	}
	
	private void actualizePlot(int startTime, int endTime, int timeStep) throws NemesysException
	{
		int numTimeSteps = (int)Math.ceil(((double)(endTime-startTime)) / ((double)timeStep));
		TimeUnit timeUnit = endTimeField.getActiveTimeUnit();
		xAxis.setLabel("Time (" + timeUnit.toString() + ")");
		
		plotsCollection.removeAllSeries();
		XYSeries[] curves = new XYSeries[flowRateUnits.length];
        for(int i=0; i<curves.length; i++)
        {
        	curves[i] = new XYSeries("syringe" + Integer.toString(i+1) + ".flowRate (" + flowRateUnits[i] + ")");
        }
		
		// Load script engine.
		if(NemesysJobConfiguration.SCRIPT_ENGINE_TIMETABLE.equals(scriptEngine))
		{
			TimingScriptEngine timingTable;
			try
			{
				timingTable = new TimingScriptEngine(script);
			}
			catch(NemesysException e)
			{
				throw new NemesysException("Error while initializing Nemesys protocol.", e);
			}
			for(int i=0; i<numTimeSteps; i++)
			{
				int currentTime = startTime + i * timeStep;
				SyringeTableRow timing = timingTable.getActiveSettings(currentTime);
				if(timing == null)
				{
					// normally, this means that current flow rate should be kept. We here assume that IC of flow rate is zero.
					for(int j=0; j<curves.length; j++)
					{
						curves[j].add(timeUnit.toUnit(currentTime), 0.0);
					}
				}
				else
				{
					for(int j=0; j<curves.length; j++)
					{
						curves[j].add(timeUnit.toUnit(currentTime), timing.flowRates[j]);
					}
				}
			}
		}
		else
		{
			List<ScriptEngineFactory> factories = new ScriptEngineManager(NemesysJobImpl.class.getClassLoader()).getEngineFactories();
			ScriptEngineFactory theFactory = null;
			for(ScriptEngineFactory factory : factories)
			{
				if(factory.getEngineName().compareToIgnoreCase(scriptEngine)==0)
				{
					theFactory = factory;
					break;
				}
			}
			if(theFactory == null)
			{
				String message = "No local script engine with name " + scriptEngine + " is registered. Registered engines:\n";
				boolean first = true;
				for(ScriptEngineFactory factory : factories)
				{
					if(first)
						first = false;
					else
						message += ", ";
					message += factory.getEngineName();
				}
				throw new NemesysException(message);
			}			
			
			ScriptEngine localEngine = theFactory.getScriptEngine();
			if(localEngine == null)
				throw new NemesysException("Could not create local script engine with name " + scriptEngine + ".");
	
			// Set output writer of engine
			StringWriter scriptOutputListener = new StringWriter();
			localEngine.getContext().setWriter(scriptOutputListener);
			scriptOutputListener.flush();
			String message = scriptOutputListener.toString();
			if(message != null && message.length() > 0)
			{
				client.sendMessage("Nemesys script message: " + message);
				scriptOutputListener.getBuffer().setLength(0);
			}
			
			VirtualScriptCallback scriptCallback;
			try
			{
				scriptCallback = new VirtualScriptCallback();
			}
			catch(RemoteException e1)
			{
				throw new NemesysException("Error while communicating with script engine.", e1);
			}
			
			for(int i=0; i<numTimeSteps; i++)
			{
				int currentTime = startTime + i * timeStep;
				
				// Set controller algorithm variables
				localEngine.put("evaluationNumber", i);
				localEngine.put("evaluationTime", currentTime);
				localEngine.put("nemesys", scriptCallback);
				
				// Run controller algorithm.
				StringReader fileReader = null;
				BufferedReader bufferedReader = null;
				localEngine.getContext().setWriter(scriptOutputListener);
				try
				{
					fileReader = new StringReader(script);
					bufferedReader = new BufferedReader(fileReader);
					localEngine.eval(bufferedReader);
				}
				catch(ScriptException e)
				{
					throw new NemesysException("Nemesys script produced error.", e);
				}
				finally
				{
					if(fileReader != null)
						fileReader.close();
					if(bufferedReader != null)
					{
						try {
							bufferedReader.close();
						} catch (IOException e) {
							client.sendError("Could not close script file buffer.", e);
						}
					}
				}
				scriptOutputListener.flush();
				message = scriptOutputListener.toString();
				if(message != null && message.length() > 0)
				{
					client.sendMessage("Nemesys script message: " + message);
					scriptOutputListener.getBuffer().setLength(0);
				}
				
				for(int j=0; j<curves.length; j++)
				{
					curves[j].add(timeUnit.toUnit(currentTime), scriptCallback.getFlowRate(j));
				}
			}
		}
		for(int i=0; i<curves.length; i++)
        {
        	plotsCollection.addSeries(curves[i]);
        }
	}
}
