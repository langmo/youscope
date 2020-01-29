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
package org.youscope.plugin.multicamerastream;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.TitledBorder;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.clientinterfaces.YouScopeFrameListener;
import org.youscope.common.PositionInformation;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.image.ImageListener;
import org.youscope.common.job.basicjobs.ContinuousImagingJob;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementException;
import org.youscope.common.microscope.Channel;
import org.youscope.common.task.Task;
import org.youscope.common.util.ImageConvertException;
import org.youscope.common.util.ImageTools;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.IntegerTextField;
import org.youscope.uielements.StandardFormats;

/**
 * @author Moritz Lang
 *
 */
class MultiStreamFrame implements YouScopeFrameListener
{
	private final static double DEFAULT_EXPOSURE = 20.0;
	private final static int DEFAULT_PERIOD = 100;
	
	private static String				lastConfigGroup			= "";
	private static String				lastChannel				= "";
	private static int lastPeriod = DEFAULT_PERIOD;
	
	private final YouScopeServer server;
	private final YouScopeClient client;
	private final String[] cameras;
	private final ImageFieldAndControl[] imageFields;
	private final double[] exposures;
	
	private volatile Measurement measurement = null;
    private volatile ImageHandler imageHandler = null;
	
	private final JComboBox<String>					configGroupField		= new JComboBox<String>();
	private final JComboBox<String>					channelField			= new JComboBox<String>();
	private final IntegerTextField periodField = new IntegerTextField(lastPeriod);
	
	private class ImageHandler extends Thread
    {
    	private volatile boolean shouldRun = true;
    	
    	private final ImageEvent<?>[] nextImages;
    	ImageHandler()
    	{
    		nextImages = new ImageEvent[imageFields.length];
    	}
    	
    	private class ImageListenerImpl extends UnicastRemoteObject implements ImageListener
    	{
    		/**
			 * Serial Version UID.
			 */
			private static final long	serialVersionUID	= -4935351939442377119L;

			/**
    		 * Constructor
			 * @throws RemoteException
			 */
			protected ImageListenerImpl() throws RemoteException
			{
				super();
			}

			@Override
    		public void imageMade(ImageEvent<?> e) throws RemoteException
    		{
				String camera = e.getCamera();
	        	if(camera == null || camera.compareToIgnoreCase("unknown") == 0)
	        	{
	        		try
					{
						stopMeasurement();
					}
					catch(RemoteException | MeasurementException e1)
					{
						client.sendError("Measurement could not be stopped.", e1);
					}
					client.sendError("Stopped measurement since it was not detectable by which camera an image was taken. Check the camera drivers and ensure, that they set the \"Camera\" metadata tag correctly.");
					return;
	        	}
	        	for(int i=0; i < imageFields.length; i++)
	        	{
	        		if(camera.compareTo(imageFields[i].getImageField().getCamera()) == 0)
	    			{
	        			synchronized(ImageHandler.this)
	        			{
	        				nextImages[i] = e;
	        				ImageHandler.this.notifyAll();
	        			}
	        			
	    				return;
	    			}
	        	}
	        	
	        	// When we are here, we received an image from a camera which shouldn't be imaging...
	        	try
				{
					stopMeasurement();
				}
				catch(RemoteException | MeasurementException e1)
				{
					client.sendError("Measurement could not be stopped.", e1);
				}
	        	client.sendError("Received an image from camera \"" + camera + "\", although it shouldn't be imaging. Check if all cameras which are not taking part in the parallel imaging stopped imaging, or if the device driver of the respective camera sets the \"Camera\" metadata tag correctly.");
				
				
				
    			
    		}
    	}
    	private volatile ImageListenerImpl listener = null;
		
		
		synchronized void stopListening()
		{
			shouldRun = false;
			this.notifyAll();
		}
		
		ImageListener startListening() throws RemoteException
		{
			start();
			if(listener == null)
				listener = new ImageListenerImpl();
			return listener;
		}
    	
		@Override
		public void run()
		{
			while(shouldRun)
			{
				// update images if available
				for(int i=0; i<nextImages.length; i++)
				{
					ImageEvent<?> image = null;
					synchronized(this)
					{
						image = nextImages[i];
						nextImages[i] = null;
					}
					if(image != null)
					{
						imageFields[i].setImage(image);
					}
				}
				
				// wait that at least one image arrives.
				outer: while(shouldRun)
				{
					for(ImageEvent<?> e : nextImages)
					{
						if(e != null)
							break outer;
					}
					synchronized(this)
					{
						try
						{
							this.wait();
						}
						catch(InterruptedException e)
						{
							client.sendError("Microscope image processor was interrupted.", e);
							return;
						}
					}
				}
				
			}
			
		}
		
    }
	
	
	
	MultiStreamFrame(YouScopeFrame frame, YouScopeServer server, YouScopeClient client, String[] cameras)
	{
		this.server = server;
		this.client = client;
		this.cameras = cameras;
		
		frame.setClosable(true);
		frame.setMaximizable(true);
		frame.setResizable(true);
		frame.setTitle("YouScope Stream XD");
		frame.addFrameListener(this);
		
		exposures = new double[cameras.length];
		for(int i=0; i < exposures.length; i++)
		{
			exposures[i] = DEFAULT_EXPOSURE;
		}
		
		imageFields = new ImageFieldAndControl[cameras.length];
    	for(int i = 0; i < cameras.length; i++)
    	{
    		imageFields[i] = new ImageFieldAndControl(new ImageField(cameras[i]));
    	}
    	Arrays.sort(imageFields);
    	
    	// Make an approximately square layout
    	int horizontalElements = (int) Math.ceil(Math.sqrt(imageFields.length));
    	int verticalElements = (int) Math.ceil(((double)imageFields.length) / horizontalElements);
    	// Create panel with image fields
    	JPanel imageFieldsPanel = new JPanel(new GridLayout(verticalElements, horizontalElements));
    	for(ImageFieldAndControl field : imageFields)
    	{
    		imageFieldsPanel.add(field);
    	}
    	
    	// top panel to choose channel
    	JPanel topPanel = new JPanel(new FlowLayout());
		topPanel.add(new JLabel("Channel Group:"));
		topPanel.add(configGroupField);
		topPanel.add(new JLabel("Channel:"));
		topPanel.add(channelField);
		topPanel.add(new JLabel("Imaging Period:"));
		periodField.setMinimalValue(0);
		topPanel.add(periodField);
		
		// Load state
		loadConfigGroupNames();
		configGroupField.setSelectedItem(lastConfigGroup);
		if(configGroupField.getSelectedItem() != null)
			lastConfigGroup = configGroupField.getSelectedItem().toString();
		loadChannels();
		channelField.setSelectedItem(lastChannel);
		if(channelField.getSelectedItem() != null)
			lastChannel = channelField.getSelectedItem().toString();
		
		channelField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				String channel = (String)channelField.getSelectedItem();
				String configGroup = (String)configGroupField.getSelectedItem();
				if(channel == null)
					return;
				
				lastChannel = channel;
				lastConfigGroup = configGroup;
				try
				{
					startMeasurement();
				}
				catch (RemoteException e)
				{
					MultiStreamFrame.this.client.sendError("Could not start continuous measurement.", e);
				    return;
				}
			}
		});
		periodField.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				lastPeriod = periodField.getValue();
				try
				{
					startMeasurement();
				}
				catch (RemoteException e)
				{
					MultiStreamFrame.this.client.sendError("Could not start continuous measurement.", e);
				    return;
				}
			}
			
		});
		
		configGroupField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				loadChannels();
			}
		});
    	
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(topPanel, BorderLayout.NORTH);
		contentPane.add(imageFieldsPanel, BorderLayout.CENTER);
		contentPane.setPreferredSize(new Dimension(800, 600));
				
    	frame.setContentPane(contentPane);
    	frame.pack();
		frame.setVisible(true);
		
		try
		{
			startMeasurement();
		}
		catch (RemoteException e)
		{
			client.sendError("Could not start continuous measurement.", e);
		    return;
		}
	}
	
	private synchronized void stopMeasurement() throws RemoteException, MeasurementException
	{
		if(measurement != null)
		{
			measurement.stopMeasurement(false);
			
			measurement = null;
		}
		if(imageHandler != null)
		{
			imageHandler.stopListening();
			imageHandler = null;
		}
	}
	
	private synchronized void startMeasurement() throws RemoteException
	{
		try
		{
			stopMeasurement();
		}
		catch(Exception e)
		{
			client.sendError("Could not initialize parallel continuous measurement.", e);
			return;
		}
		
		measurement = server.getMeasurementProvider().createMeasurement();
		imageHandler = new ImageHandler();
		try
		{
			measurement.setName("Parallel Continuous Imaging");
			measurement.setLockMicroscopeWhileRunning(false);

			// Create continuous pulling job.
			ContinuousImagingJob job = server.getComponentProvider(null).createJob(new PositionInformation(), ContinuousImagingJob.DEFAULT_TYPE_IDENTIFIER, ContinuousImagingJob.class);
			job.setCameras(cameras);
			job.setExposures(exposures);
			job.setBurstImaging(false);
			job.addImageListener(imageHandler.startListening());
			job.setChannel(lastConfigGroup, lastChannel);

			// Add a task for the continuous pulling job.
			Task task = measurement.addTask(lastPeriod, false, 0);
			task.addJob(job);
			measurement.startMeasurement();
		}
		catch(Exception e)
		{
			client.sendError("Could not initialize parallel continuous measurement.", e);
			return;
		}
	}
	
	private void loadConfigGroupNames()
	{
		String[] configGroupNames = null;
		try
		{
			configGroupNames = server.getMicroscope().getChannelManager().getChannelGroupIDs();
		}
		catch(Exception e)
		{
			client.sendError("Could not obtain config group names.", e);
		}

		if(configGroupNames == null || configGroupNames.length <= 0)
		{
			configGroupNames = new String[] {""};
		}

		configGroupField.removeAllItems();
		for(String configGroupName : configGroupNames)
		{
			configGroupField.addItem(configGroupName);
		}
	}

	private void loadChannels()
	{
		String[] channelNames = null;

		Object selectedGroup = configGroupField.getSelectedItem();
		if(selectedGroup != null && selectedGroup.toString().length() > 0)
		{
			try
			{
				Channel[] channels = server.getMicroscope().getChannelManager().getChannels(selectedGroup.toString());
				channelNames = new String[channels.length];
				for(int i=0; i<channels.length; i++)
				{
					channelNames[i] = channels[i].getChannelID();
				}
			}
			catch(Exception e)
			{
				client.sendError("Could not obtain channel names of microscope.", e);
			}
		}

		if(channelNames == null || channelNames.length <= 0)
		{
			channelNames = new String[] {""};
		}

		channelField.removeAllItems();
		for(String channelName : channelNames)
		{
			channelField.addItem(channelName);
		}
	}
	
	private class ImageFieldAndControl extends JPanel implements Comparable<ImageFieldAndControl>
	{

		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= 1186561339715993221L;
		private final JFormattedTextField exposureField = new JFormattedTextField(StandardFormats.getDoubleFormat());
		private final ImageField imageField;
		private final HistogramPlot histogram = new HistogramPlot();
		private final JCheckBox increaseContrastField = new JCheckBox("Increase Contrast.", true);
	    private final JCheckBox autoContrastField = new JCheckBox("Auto-Adjust Contrast.", true);
		ImageFieldAndControl(ImageField imageField)
		{
			this.imageField = imageField;
			exposureField.setValue(DEFAULT_EXPOSURE);
			
			exposureField.addActionListener(new RestartActionListener());
			
			setBorder(new TitledBorder(imageField.toString()));
			setLayout(new BorderLayout());
			
			// Control panel
			GridBagLayout elementsLayout = new GridBagLayout();
			JPanel elementsPanel = new JPanel(elementsLayout);
	        GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
	        addConfElement(new JLabel("Exposure (ms):"), elementsLayout, newLineConstr, elementsPanel);
	        addConfElement(exposureField, elementsLayout, newLineConstr, elementsPanel);
	        addConfElement(increaseContrastField, elementsLayout, newLineConstr, elementsPanel);
	        addConfElement(autoContrastField, elementsLayout, newLineConstr, elementsPanel);
	        addConfElement(histogram, elementsLayout, newLineConstr, elementsPanel);
	        addConfElement(new JPanel(), elementsLayout, StandardFormats.getBottomContstraint(), elementsPanel);
						
	        increaseContrastField.addActionListener(new ActionListener()
		        {
					@Override
					public void actionPerformed(ActionEvent e)
					{
						histogram.setAutoAdjusting(increaseContrastField.isSelected() && autoContrastField.isSelected());
						autoContrastField.setVisible(increaseContrastField.isSelected());
					}
		        });
	        autoContrastField.addActionListener(new ActionListener()
	        {
				@Override
				public void actionPerformed(ActionEvent e)
				{
					histogram.setAutoAdjusting(increaseContrastField.isSelected() && autoContrastField.isSelected());
				}
	        });
	        
			JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imageField, new JScrollPane(elementsPanel));
	        splitPane.setOneTouchExpandable(true);
	        splitPane.setDividerLocation(1.0);
	        splitPane.setResizeWeight(1.0);
	        add(splitPane, BorderLayout.CENTER);
		}
		void setImage(ImageEvent<?> event)
	    {
			int[][] bins;
			try
			{
				bins = ImageTools.getHistogram(event, Math.max(histogram.getWidth(), 100));
			}
			catch (ImageConvertException e)
			{
				client.sendError("Could not generate histogram for image.", e);
				return;
			}
			histogram.setBins(bins);
			
			BufferedImage image;
            try
            {
            	if(increaseContrastField.isSelected())
            	{
            		double[] minMax = histogram.getMinMax();
            		image = ImageTools.getScaledMicroscopeImage(event, (float)minMax[0], (float)minMax[1]);
            	}
            	else
            	{
            		image = ImageTools.getMicroscopeImage(event);
            	}
            }
        	catch (ImageConvertException e)
			{
        		client.sendError("Could not process image!", e);
				return;
			}
        	
        	imageField.setImage(image);
	    }
		ImageField getImageField()
		{
			return imageField;
		}
		class RestartActionListener implements ActionListener
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					// Find corresponding camera
					int cameraNum = 0;
					for(; cameraNum < cameras.length; cameraNum++)
					{
						if(imageField.getCamera().compareTo(cameras[cameraNum]) == 0)
						{
							break;
						}
					}
					
					MultiStreamFrame.this.exposures[cameraNum] = ((Number)exposureField.getValue()).doubleValue();
					startMeasurement();
				}
				catch (RemoteException e1)
				{
					client.sendError("Could not restart continuous measurement.", e1);
				    return;
				}
			}
		}
		@Override
		public int compareTo(ImageFieldAndControl o)
		{
			return getImageField().compareTo(o.getImageField());
		}
	}
	
	protected class ImageField extends JComponent implements Comparable<ImageField>
    {
        /**
         * Serial Version UID.
         */
        private static final long serialVersionUID = 3857578873912009525L;

        private BufferedImage image = null;
        
        private String camera;
        
        ImageField(String camera)
        {
        	this.camera = camera;
        }
        public String getCamera()
        {
        	return camera;
        }
        public synchronized void setImage(BufferedImage image)
        {
        	this.image = image;
        	repaint();
        }
        @Override
        public String toString()
        {
        	String retVal = "";
        	if(camera == null)
        		retVal += "Unknown Camera";
        	else
        		retVal += camera;
        	return retVal;
        }
        
        @Override
		public void paintComponent(Graphics grp)
        {
        	BufferedImage image;
        	int width;
            int height;
        	synchronized(this)
        	{
        		image = this.image;
	        	width = this.getWidth();
	            height = this.getHeight();
        	}
            if (image == null)
            {
            	
                String text = "No data available.";
            	// get metrics from the graphics
                FontMetrics metrics = grp.getFontMetrics();
                // get the height of a line of text in this font and render context
                int textHeight = metrics.getHeight();
                // get the advance of my text in this font and render context
                int textWidth = metrics.stringWidth(text);
                grp.drawString(text, (width - textWidth) / 2, (height - textHeight) / 2);
                return;
            }

            double imageWidth = image.getWidth(null);
            double imageHeight = image.getHeight(null);
            if (width / imageWidth > height / imageHeight)
            {
                imageWidth = imageWidth * height / imageHeight;
                imageHeight = height;
            } else
            {
                imageHeight = imageHeight * width / imageWidth;
                imageWidth = width;
            }

            // draw the image
            grp.drawImage(image, (int) (getWidth() - imageWidth) / 2,
                    (int) (getHeight() - imageHeight) / 2, (int) imageWidth, (int) imageHeight,
                    this);
        }
		
		@Override
		public int compareTo(ImageField arg0)
		{
			// Now sort depending on camera
			if(camera == null)
				return 0;
			return camera.compareToIgnoreCase(arg0.getCamera());
		}
    }

	@Override
	public void frameClosed()
	{
		try
		{
			stopMeasurement();
		}
		catch (RemoteException | MeasurementException e)
		{
			client.sendError("Could not finish streaming measurement.", e);
		}
	}
	private static void addConfElement(Component component, GridBagLayout layout,
            GridBagConstraints constr, JPanel panel)
    {
        layout.setConstraints(component, constr);
        panel.add(component);
    }
	
	@Override
	public void frameOpened()
	{
		// Do nothing.
	}
}
