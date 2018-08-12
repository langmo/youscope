/*******************************************************************************
 * Copyright (c) 2018 Moritz Lang.
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
package org.youscope.plugin.focusviewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentMetadata;
import org.youscope.addon.focusscore.FocusScoreConfiguration;
import org.youscope.addon.focusscore.FocusScoreResource;
import org.youscope.addon.tool.ToolAddonUIAdapter;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.addon.tool.ToolMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrameListener;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.image.ImageListener;
import org.youscope.common.job.Job;
import org.youscope.common.job.basicjobs.ContinuousImagingJob;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.SimpleMeasurementContext;
import org.youscope.common.microscope.MeasurementProcessingListener;
import org.youscope.common.task.Task;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.SubConfigurationPanel;

/**
 * @author Moritz Lang
 *
 */
class FocusViewerTool extends ToolAddonUIAdapter
{	
	private ContinuousImagingJob monitoredJob = null;
	private ImageProcessor imageProcessor = null;
	private JLabel scoreField = new JLabel("LiveStream not started!");
	private SubConfigurationPanel<FocusScoreConfiguration> focusScoreAlgorithmPanel = null;
	private FocusScoreResource currentAlgorithm = null;
	private boolean changeAlgorithm = false;
	private MeasurementContext measurementContext = null;
	private final XYSeries plotData = new XYSeries("Focus Scores");
	private class ImageProcessor extends Thread
    {
    	private volatile ImageEvent<?> nextImage;
    	private volatile boolean shouldRun = true;
    	private class MonitoredJobListener extends UnicastRemoteObject implements ImageListener
    	{
    		/**
			 * Serial Version UID.
			 */
			private static final long	serialVersionUID	= -4115351939442377119L;

			/**
    		 * Constructor
			 * @throws RemoteException
			 */
			protected MonitoredJobListener() throws RemoteException
			{
				super();
			}

			@Override
    		public void imageMade(ImageEvent<?> e) throws RemoteException
    		{
    			synchronized(ImageProcessor.this)
    			{
    				nextImage = e;
    				ImageProcessor.this.notifyAll();
    			}
    		}
    	}
    	private MonitoredJobListener monitoredJobListener = null;		
		
    	synchronized ImageListener getImageListener() throws RemoteException
    	{
    		if(monitoredJobListener == null)
				monitoredJobListener = new MonitoredJobListener();
    		return monitoredJobListener;
    	}
    	
		synchronized void stopProcessing()
		{
			shouldRun = false;
			this.notifyAll();
		}
		
		synchronized void startProcessing()
		{
			if(!shouldRun)
				return;
			start();
		}
    	
		@Override
		public void run()
		{
			int nextImageIdx = 1;
			while(shouldRun)
			{
				// get image.
				ImageEvent<?> image = null;
				synchronized(this)
				{
					while(shouldRun && nextImage == null)
					{
						try
						{
							this.wait();
						}
						catch(InterruptedException e)
						{
							sendErrorMessage("Error while waiting for next image to calculate focus score for.", e);
							return;
						}
					}
					if(!shouldRun)
						return;
					image = nextImage;
					nextImage = null;
				}
				
				// process image
				String messageTemp;
				double scoreTemp;
				try
				{
					scoreTemp =calculateScore(image);			
					messageTemp = String.format ("%.8f", scoreTemp);
				}
				catch(@SuppressWarnings("unused") Exception e)
				{
					messageTemp = "Settings invalid!";
					scoreTemp = Double.NaN;
				}
				final double score = scoreTemp;
				final int currentImage = nextImageIdx++;
				final String message = messageTemp;
				
				Runnable runner = new Runnable()
				{
					@Override
					public void run()
					{
						scoreField.setText(message);
						if(!Double.isNaN(score))
						{
							plotData.add(currentImage, score);
						}
					}
				};
				if(SwingUtilities.isEventDispatchThread())
					runner.run();
				else
					SwingUtilities.invokeLater(runner);
			}
			
		}
		
    }
	double calculateScore(ImageEvent<?> image) throws Exception
	{
		FocusScoreResource focusAlgorithm;
		boolean shouldChange;
		synchronized(FocusViewerTool.this)
		{
			focusAlgorithm = currentAlgorithm;
			shouldChange = changeAlgorithm || focusAlgorithm == null;
			changeAlgorithm = false;
		}
		if(shouldChange)
		{
			if(currentAlgorithm != null)
			{
				currentAlgorithm.uninitialize(measurementContext);
				currentAlgorithm = null;
			}
			FocusScoreConfiguration configuration = focusScoreAlgorithmPanel.getConfiguration();
			if(configuration == null)
				throw new Exception("Configuration empty");
			try
			{
				configuration.checkConfiguration();
			}
			catch(ConfigurationException e)
			{
				throw new Exception("Configuration invalid", e);
			}
			focusAlgorithm = getServer().getComponentProvider(null).createComponent(new PositionInformation(), configuration, FocusScoreResource.class);
			focusAlgorithm.initialize(measurementContext);
			synchronized(FocusViewerTool.this)
			{
				if(!changeAlgorithm)
					currentAlgorithm = focusAlgorithm;
			}
		}
		return focusAlgorithm.calculateScore(image);
	}
	
	
	/**
	 * Constructor.
	 * @param client Interface to the YouScope client.
	 * @param server Interface to the YouScope server.
	 * @throws AddonException 
	 */
	public FocusViewerTool(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
	}
	public final static String TYPE_IDENTIFIER = "YouScope.FocusViewer";
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "Focus Score", null, "A tool continuously showing the focus score for the currently running LiveStream.",
				"icons/contrast-low.png");
	}
	private final MeasurementProcessingListener measurementListener = new MeasurementProcessingListener()
	{
		
		@Override
		public void measurementQueueChanged() throws RemoteException
		{
			// do nothing.
		}
		
		@Override
		public void measurementProcessingStopped() throws RemoteException
		{
			unregisterMeasurement();
			
		}
		
		@Override
		public void currentMeasurementChanged() throws RemoteException
		{
			registerMeasurement(getServer().getCurrentMeasurement());
			
		}
	};
	@Override
	public java.awt.Component createUI()
	{
		setMaximizable(false);
		setResizable(true);
		setTitle("Focus Viewer");
		setShowCloseButton(true);
		
		addFrameListener(new YouScopeFrameListener()
		{
			@Override
			public void frameOpened()
			{
				synchronized(FocusViewerTool.this)
				{
					imageProcessor = new ImageProcessor();
					try
					{
						measurementContext = new SimpleMeasurementContext();
						getServer().addMeasurementProcessingListener(measurementListener);
						registerMeasurement(getServer().getCurrentMeasurement());
					}
					catch (RemoteException e)
					{
						sendErrorMessage("Could not start calculation of image scores.", e);
						return;
					}
					imageProcessor.startProcessing();
					
					
				}
			}
			
			@Override
			public void frameClosed()
			{
				synchronized(FocusViewerTool.this)
				{
					imageProcessor.stopProcessing();
					try
					{
						getServer().removeMeasurementProcessingListener(measurementListener);
					}
					catch (@SuppressWarnings("unused") RemoteException e)
					{
						// do nothing.
					}
					unregisterMeasurement();
				}
			}
		});
		
		FocusScoreConfiguration defaultConfig = null;
		try
		{
			ComponentMetadata<? extends FocusScoreConfiguration> metadata = getClient().getAddonProvider().getComponentMetadata("YouScope.VarianceFocusScore", FocusScoreConfiguration.class);
			if(metadata != null)
			{
				Class<? extends FocusScoreConfiguration> configClass = metadata.getConfigurationClass();
				if(configClass != null)
					defaultConfig = configClass.newInstance();
				
			}
		}
		catch (@SuppressWarnings("unused") AddonException | InstantiationException | IllegalAccessException e)
		{
			// do nothing. Selecting any score algorithm should be fine, too.
		}
		
		DynamicPanel algorithmPanel = new DynamicPanel();
		focusScoreAlgorithmPanel = new SubConfigurationPanel<FocusScoreConfiguration>(null, defaultConfig, FocusScoreConfiguration.class, getClient(), getContainingFrame());
		focusScoreAlgorithmPanel.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				synchronized (FocusViewerTool.this)
				{
					changeAlgorithm = true;
				}
				
			}
		});
		algorithmPanel.add(focusScoreAlgorithmPanel);
		algorithmPanel.setBorder(new TitledBorder("Focus score algorithm"));
		
		DynamicPanel currentScorePanel = new DynamicPanel();
		scoreField.setHorizontalAlignment(SwingConstants.RIGHT);
		scoreField.setMinimumSize(new Dimension(150, 20));
		scoreField.setPreferredSize(new Dimension(150, 20));
		currentScorePanel.addRight(scoreField);
		currentScorePanel.setBorder(new TitledBorder("Current Score"));
		
		DynamicPanel leftPanel = new DynamicPanel();
		leftPanel.add(currentScorePanel);
		leftPanel.add(algorithmPanel);
		leftPanel.addFillEmpty();
		
	    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
	    String xString = "Time";
	    String yString = "Focus Score";
	    
	    NumberAxis xAxis = new NumberAxis(xString);
	    xAxis.setAutoRangeIncludesZero(false);
	    NumberAxis yAxis = new NumberAxis(yString);
	    yAxis.setAutoRangeIncludesZero(false);

	    XYSeriesCollection plotsCollection = new XYSeriesCollection();
	    plotData.setMaximumItemCount(50);
	    plotsCollection.addSeries(plotData);
	    
	    XYPlot plot = new XYPlot(plotsCollection, xAxis, yAxis, renderer);
	    JFreeChart chart = new JFreeChart("Focus Score History", scoreField.getFont(), plot, false);
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
		
		
        // End initializing
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(leftPanel, BorderLayout.WEST);
		contentPane.add(chartPanel, BorderLayout.CENTER);
		return contentPane;
	}
	synchronized void registerMeasurement(Measurement measurement)
	{
		unregisterMeasurement();
		if(measurement == null)
			return;
		// try to find out if this is a continuous imaging job and add as image listener if so. Otherwise, do nothing.
		try
		{
			Task[] tasks = measurement.getTasks();
			if(tasks.length <=0)
				return;
			Job[] jobs = tasks[0].getJobs();
			if(jobs.length <= 0)
				return;
			if(!(jobs[0] instanceof ContinuousImagingJob))
				return;
			monitoredJob = (ContinuousImagingJob)jobs[0];
			monitoredJob.addImageListener(imageProcessor.getImageListener());
		}
		catch (RemoteException e)
		{
			sendErrorMessage("Could not register image listener for focus score calculation.", e);
			monitoredJob = null;
		}
	}
	synchronized void unregisterMeasurement()
	{
		if(monitoredJob == null)
			return;
		try
		{
			monitoredJob.removeImageListener(imageProcessor.getImageListener());
		}
		catch (RemoteException e)
		{
			sendErrorMessage("Could not unregister image listener for focus score calculation.", e);
		}
		monitoredJob = null;
	}
}
