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
package org.youscope.uielements;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrameListener;
import org.youscope.clientinterfaces.PropertyProvider;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.image.ImageListener;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementException;
import org.youscope.serverinterfaces.MeasurementProvider;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * A panel to configure, start and stop a live stream.
 * @author Moritz Lang
 *
 */
public class LiveStreamPanel extends ImagePanel {

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 5555935386353363014L;
	private final YouScopeClient client;
	private final YouScopeServer server;
	
	private final ChannelControl channelControl;
	private final StartStopControl startStopControl;
	private final Object measurementControlLock = new Object();
	private volatile Measurement measurement = null;
	private volatile ImageHandler imageHandler = null;
	private volatile boolean streamRunning = false;
	
	private final Object fullScreenLock = new Object();
	private JFrame fullScreenFrame = null;
	private volatile boolean fullScreenOn = false;
	
	private boolean autostart = false;
	/**
	 * Constructor.
	 * @param client YouScope client.
	 * @param server YouScope server.
	 */
	public LiveStreamPanel(YouScopeClient client, YouScopeServer server) 
	{
		super(client);
		setNoImageText("Press start to start imaging");
		setTitle("LiveStream");
		this.client = client;
		this.server = server;
		
		channelControl = new ChannelControl(client, server);
		channelControl.addActionListener(new ActionListener()
		{
			@Override
				public void actionPerformed(ActionEvent e) 
				{
					if(streamRunning)
						startLiveStream();
				}
		});
		startStopControl = new StartStopControl();
		
		setUserChoosesAutoAdjustContrast(true);
		insertControl("Imaging", channelControl, 0);
		addControl("Control", startStopControl);
		
		loadSettings(client.getPropertyProvider());
	}
	
	/**
	 * Set to true to show the LiveStream in full-screen mode. Set to fals again to stop full-sceen again.
	 * @param fullScreen True to start, false to stop full-screen.
	 */
	public void setFullScreen(final boolean fullScreen)
	{
		synchronized(fullScreenLock)
		{
			if(fullScreen == fullScreenOn)
				return;
			fullScreenOn = fullScreen;
		}
		
		Runnable runner = new Runnable() {
			
			@Override
			public void run() 
			{
				if(!fullScreen && fullScreenFrame != null)
				{
					GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0].setFullScreenWindow(null);
					fullScreenFrame.setVisible(false);
					fullScreenFrame.getContentPane().removeAll();
					if(frame != null)
					{
						frame.setContentPane(LiveStreamPanel.this);
					}
				}
				else if(fullScreen)
				{
					if(frame != null)
					{
						frame.setContentPane(new JLabel("<html>LiveStream is currently<br />in full-screen mode</html>"));
						try {
							Thread.sleep(100);
						} catch (@SuppressWarnings("unused") InterruptedException e) {
							// do nothing.
						}
					}
					if(fullScreenFrame == null)
					{
						fullScreenFrame = new JFrame("LiveStream");
						fullScreenFrame.setUndecorated(true);
						fullScreenFrame.setLayout(new BorderLayout());
						fullScreenFrame.addWindowListener(new WindowAdapter()
						{
							@Override
							public void windowClosed(WindowEvent arg0) {
								setFullScreen(false);
							}
				
							@Override
							public void windowClosing(WindowEvent arg0) {
								setFullScreen(false);
							}
						});
					}
					fullScreenFrame.getContentPane().removeAll();
					fullScreenFrame.getContentPane().add(LiveStreamPanel.this, BorderLayout.CENTER);
					fullScreenFrame.setVisible(true);
					
					GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0].setFullScreenWindow(fullScreenFrame);
				}
				startStopControl.updateButtons();
			}
		};
		if(SwingUtilities.isEventDispatchThread())
			runner.run();
		else
			SwingUtilities.invokeLater(runner);
	}
	
	
	@Override
	public void saveSettings(PropertyProvider properties)
	{
		super.saveSettings(properties);
		String camera = channelControl.getCamera();
    	String channelGroup = channelControl.getChannelGroup();
    	String channel = channelControl.getChannel();
    	double exposure = channelControl.getExposure();
    	int imagingPeriod = channelControl.getImagingPeriod();
    	
    	properties.setProperty(StandardProperty.PROPERTY_STREAM_LAST_CHANNEL, channel);
    	properties.setProperty(StandardProperty.PROPERTY_STREAM_LAST_CHANNEL_GROUP, channelGroup);
    	properties.setProperty(StandardProperty.PROPERTY_STREAM_LAST_CAMERA, camera);
    	properties.setProperty(StandardProperty.PROPERTY_STREAM_LAST_EXPOSURE, exposure);
    	properties.setProperty(StandardProperty.PROPERTY_STREAM_LAST_PERIOD, imagingPeriod);
	}
	@Override
	public void loadSettings(PropertyProvider properties)
	{
		super.loadSettings(properties);
		setAutoStartStream((Boolean) client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_AUTOSTART));
		
		if((boolean) client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_USE_DEFAULT_SETTINGS))
		{
			channelControl.setExposure((double)client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_DEFAULT_EXPOSURE));
			channelControl.setImagingPeriod((int)client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_DEFAULT_PERIOD));
			channelControl.setCamera((String) client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_DEFAULT_CAMERA));
			channelControl.setChannel((String)client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_DEFAULT_CHANNEL_GROUP), 
					(String)client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_DEFAULT_CHANNEL));
		}
		else
		{
			channelControl.setExposure((double)client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_LAST_EXPOSURE));
			channelControl.setImagingPeriod((int)client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_LAST_PERIOD));
			channelControl.setCamera((String) client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_LAST_CAMERA));
			channelControl.setChannel((String)client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_LAST_CHANNEL_GROUP), 
					(String)client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_LAST_CHANNEL));
		}
	}
	
	/**
	 * Returns a frame listener which can be added to the frame to which this panel is added, taking care that the LiveStream automatically
	 * stops when the frame closes (and, that it automatically starts when the frame opens, if autostart is set to true).
	 * @return Frame listener.
	 */
	@Override
	public YouScopeFrameListener getFrameListener()
	{
		final YouScopeFrameListener superListener = super.getFrameListener();
		return new YouScopeFrameListener() {
			
			@Override
			public void frameOpened() 
			{
				superListener.frameOpened();
				if(autostart)
					startLiveStream();
			}
			
			@Override
			public void frameClosed() {
				stopLiveStream();
				saveSettings(client.getPropertyProvider());
				superListener.frameClosed();
			}
		};
	}
	
	private class ImageHandler extends Thread
    {
    	private volatile ImageEvent<?> nextImage;
    	private volatile boolean shouldRun = true;
    	private class ImageListenerImpl extends UnicastRemoteObject implements ImageListener
    	{
    		/**
			 * Serial Version UID.
			 */
			private static final long	serialVersionUID	= -4135351939442377119L;

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
    			synchronized(ImageHandler.this)
    			{
    				nextImage = e;
    				ImageHandler.this.notifyAll();
    			}
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
							client.sendError("Microscope image processor was interrupted.", e);
							return;
						}
					}
					if(!shouldRun)
						return;
					image = nextImage;
					nextImage = null;
				}
				
				// process image
				setImage(image);
				
			}
			
		}
		
    }
	/**
	 * Control to select the channel and exposure times.
	 * @author Moritz Lang
	 *
	 */
	public static class ChannelControl extends DynamicPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = 6561875723795313098L;
		final CameraField cameraField;
		final ChannelField channelField;
		final DoubleTextField exposureField = new DoubleTextField();
		final IntegerTextField imagingPeriodField = new IntegerTextField();
		private final ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();
		/**
		 * Constructor.
		 * @param client YouScope client.
		 * @param server YouScope server.
		 */
		public ChannelControl(final YouScopeClient client, final YouScopeServer server)
		{
			cameraField = new CameraField(client, server);
			channelField = new ChannelField(client, server);
			
			exposureField.setMinimalValue(0);
			imagingPeriodField.setMinimalValue(0);
			
			ActionListener changeListener = new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					fireActionEvent();
				}
			};
			
			JLabel label;
			if(cameraField.isChoice())
			{
				label = new JLabel("Camera:");
				label.setForeground(Color.WHITE);
				add(label);
				add(cameraField);
				cameraField.addActionListener(changeListener);
			}
			
			label = new JLabel("Channel:");
			label.setForeground(Color.WHITE);
			add(label);
			add(channelField);
			channelField.addActionListener(changeListener);
			
			label = new JLabel("Exposure (ms):");
			label.setForeground(Color.WHITE);
			add(label);
			add(exposureField);
			exposureField.addActionListener(changeListener);
			
			label = new JLabel("Imaging Period (ms):");
			label.setForeground(Color.WHITE);
			add(label);
			add(imagingPeriodField);
			imagingPeriodField.addActionListener(changeListener);
		}
		
		/**
		 * Adds a listener which gets notified when channel configuration changed.
		 * @param listener Notifier to add.
		 */
		public void addActionListener(ActionListener listener)
		{
			synchronized (actionListeners) 
			{
				actionListeners.add(listener);
			}
		}
		/**
		 * Removes a previously added listener.
		 * @param listener Listener to remove.
		 */
		public void removeActionListener(ActionListener listener)
		{
			synchronized (actionListeners) 
			{
				actionListeners.remove(listener);
			}
		}
		
		private void fireActionEvent()
		{
			synchronized (actionListeners) 
			{
				for(ActionListener listener : actionListeners)
				{
					listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRST, "Channel settings changed"));
				}
			}
		}
		
		/**
		 * Returns the currently selected channel.
		 * @return Currently selected channel.
		 */
		public String getChannel()
		{
			return channelField.getChannel();
		}
		/**
		 * Sets the currently selected channel
		 * @param channelGroup channel group
		 * @param channel channel
		 */
		public void setChannel(String channelGroup, String channel)
		{
			channelField.setChannel(channelGroup, channel);
		}
		/**
		 * Returns the currently selected channel group.
		 * @return Currently selected channel group.
		 */
		public String getChannelGroup()
		{
			return channelField.getChannelGroup();
		}
		/**
		 * Returns the currently selected exposure time in ms.
		 * @return exposure time in ms.
		 */
		public double getExposure()
		{
			return exposureField.getValue();
		}
		/**
		 * Sets the currently selected exposure
		 * @param exposure exposure time in ms.
		 */
		public void setExposure(double exposure)
		{
			exposureField.setValue(exposure);
		}
		/**
		 * Returns the currently selected imaging period in ms.
		 * @return Imaging period in ms.
		 */
		public int getImagingPeriod()
		{
			return imagingPeriodField.getValue();
		}
		/**
		 * Sets the currently selected imaging period in ms.
		 * @param period Imaging period in ms.
		 */
		public void setImagingPeriod(int period)
		{
			imagingPeriodField.setValue(period);
		}
		/**
		 * Returns the currently selected camera.
		 * Note: the camera selection field will only be shown if more than one camera is available. This function
		 * nevertheless will return the camera name even if only one camera is available.
		 * @return Name of currently selected camera.
		 */
		public String getCamera()
		{
			return cameraField.getCameraDevice();
		}
		/**
		 * Sets the currently selected camera.
		 * Note: the camera selection field will only be shown if more than one camera is available. This function
		 * nevertheless will set the camera name even if only one camera is available.
		 * @param camera Name of currently selected camera.
		 */
		public void setCamera(String camera)
		{
			cameraField.setCamera(camera);
		}
	}
	
	/**
	 * If set to true, the stream automatically starts when the window is made visible.
	 * Note: the stream automatically stops if the frame is hidden.
	 * Has only an effect if #toFrame() is used.
	 * @param autostart true to automatically start imaging when frame shows up.
	 */
	public void setAutoStartStream(boolean autostart)
	{
		this.autostart = autostart;
	}
	
	/**
	 * Set to true to display a button with which the user can choose on him/herself if the LiveStream should be shown in full-screen mode. Default is false.
	 * @param userChooses True to display fullscreen button.
	 */
	public void setUserChoosesFullScreen(boolean userChooses)
	{
		GraphicsDevice[] screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		if(screens.length < 1)
			userChooses = false;
		else
		{
			userChooses = userChooses && screens[0].isFullScreenSupported();
		}
		
		startStopControl.fullScreenButton.setVisible(userChooses);
	}
	
	private class StartStopControl extends DynamicPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = -4369903187110180162L;
		private final String START_TEXT = "start imaging";
		private final String STOP_TEXT = "stop imaging";
		private final String SNAP_TEXT = "snap image";
		
		private final String START_FULLSCREEN_TEXT = "full-screen";
		private final String STOP_FULLSCREEN_TEXT = "stop full-screen";
		
		final JButton startStopButton = new JButton(START_TEXT);
		final JButton snapImageButton = new JButton(SNAP_TEXT);
		final JButton fullScreenButton = new JButton(START_FULLSCREEN_TEXT);
		
		final Icon startIcon = ImageLoadingTools.getResourceIcon("icons/control.png", "start stream");
		final Icon stopIcon = ImageLoadingTools.getResourceIcon("icons/control-stop.png", "stop stream");
		final Icon snapIcon = ImageLoadingTools.getResourceIcon("icons/picture--plus.png", "snap image");
		final Icon startFullScreenIcon = ImageLoadingTools.getResourceIcon("icons/monitor-image.png", "snap image");
		final Icon stopFullScreenIcon = ImageLoadingTools.getResourceIcon("icons/monitor-window-3d.png", "snap image");
		StartStopControl()
		{
			startStopButton.setText(START_TEXT);
			if(startIcon != null)
			{
				startStopButton.setIcon(startIcon);
			}
			startStopButton.setOpaque(false);
			startStopButton.setHorizontalAlignment(SwingConstants.LEFT);
			startStopButton.setToolTipText(START_TEXT);
			add(startStopButton);
			startStopButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					if(!streamRunning)
					{
						startLiveStream();
					}
					else
					{
						stopLiveStream();
					}
				}
			});
			
			snapImageButton.setText(SNAP_TEXT);
			if(snapIcon != null)
			{
				snapImageButton.setIcon(snapIcon);
			}
			snapImageButton.setOpaque(false);
			snapImageButton.setHorizontalAlignment(SwingConstants.LEFT);
			snapImageButton.setToolTipText(SNAP_TEXT);
			add(snapImageButton);
			snapImageButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					snapImage();
				}
			});
			
			fullScreenButton.setText(START_FULLSCREEN_TEXT);
			if(startFullScreenIcon != null)
			{
				fullScreenButton.setIcon(startFullScreenIcon);
			}
			fullScreenButton.setOpaque(false);
			fullScreenButton.setHorizontalAlignment(SwingConstants.LEFT);
			fullScreenButton.setToolTipText(START_FULLSCREEN_TEXT);
			add(fullScreenButton);
			fullScreenButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					if(fullScreenOn)
						setFullScreen(false);
					else
						setFullScreen(true);
				}
			});
			fullScreenButton.setVisible(false);
		}
		void updateButtons()
		{
			if(streamRunning)
			{
				startStopButton.setText(STOP_TEXT);
				if(stopIcon != null)
				{
					startStopButton.setIcon(stopIcon);
				}
				startStopButton.setToolTipText(STOP_TEXT);
				snapImageButton.setEnabled(false);
			}
			else
			{
				startStopButton.setText(START_TEXT);
				if(startIcon != null)
				{
					startStopButton.setIcon(startIcon);
				}
				startStopButton.setToolTipText(START_TEXT);
				snapImageButton.setEnabled(true);
			}
			if(fullScreenOn)
			{
				fullScreenButton.setText(STOP_FULLSCREEN_TEXT);
				if(stopFullScreenIcon != null)
				{
					fullScreenButton.setIcon(stopFullScreenIcon);
				}
				fullScreenButton.setToolTipText(STOP_FULLSCREEN_TEXT);
			}
			else
			{
				fullScreenButton.setText(START_FULLSCREEN_TEXT);
				if(startFullScreenIcon != null)
				{
					fullScreenButton.setIcon(startFullScreenIcon);
				}
				fullScreenButton.setToolTipText(START_FULLSCREEN_TEXT);
			}
		}
	}
	
	/**
     * Stops the live stream. Does nothing if live stream is not running.
     * The call immediately returns, even if the stopping of the live stream takes longer.
     */
    public void stopLiveStream()
    {
        synchronized (measurementControlLock)
        {
        	streamRunning = false;
            if (measurement != null)
            {
                try
                {
                    measurement.stopMeasurement(false);
                } 
                catch (RemoteException | MeasurementException e)
                {
                    client.sendError("Could not stop measurement.", e);
                }
                measurement = null;
            }
            if(imageHandler != null)
            {
            	imageHandler.stopListening();
                imageHandler = null;
            }
        }
        startStopControl.updateButtons();
    }

    /**
     * Snaps a single image with the current settings.
     */
    public void snapImage()
    {
    	ImageEvent<?> image;
    	synchronized(measurementControlLock)
    	{
    		if(measurement != null)
    			return;
	    	String camera = channelControl.getCamera();
	    	String channelGroup = channelControl.getChannelGroup();
	    	String channel = channelControl.getChannel();
	    	double exposure = channelControl.getExposure();
	    	try {
	    		image =server.getMicroscope().getCameraDevice(camera).makeImage(channelGroup, channel, exposure);
			} catch (Exception e) {
				client.sendError("Could not snap image.", e);
				image = null;
			}
    	}
    	if(image != null)
    		setImage(image);
    }
    
    /**
     * Starts the live stream. If the live stream is already running, it restarts it.
     */
    public void startLiveStream()
    {
    	synchronized(measurementControlLock)
    	{
	    	String camera = channelControl.getCamera();
	    	String channelGroup = channelControl.getChannelGroup();
	    	String channel = channelControl.getChannel();
	    	double exposure = channelControl.getExposure();
	    	int imagingPeriod = channelControl.getImagingPeriod();
	    	
	    	// stop any previous measurement
	    	if(streamRunning)
	    	{
	    		stopLiveStream();	
	    		// wait some additional time to reduce risk for snc errors.
	    		try {
					Thread.sleep(500);
				} catch (@SuppressWarnings("unused") InterruptedException e) {
					// do nothing...
				}
	    	}
	    	streamRunning = true;
	    	
	    	
	        // Create measurement on server
	        try
	        {
	            MeasurementProvider measurementFactory =server.getMeasurementProvider();
	            
                // Create measurement
            	imageHandler = new ImageHandler();
                measurement = measurementFactory.createContinuousMeasurement((camera == null || camera.length() < 1) ? null : camera, channelGroup, channel, imagingPeriod, exposure, imageHandler.startListening());
                
                // Start measurement
                measurement.startMeasurement();
	        } 
	        catch (Exception e)
	        {
	        	client.sendError("Could not create/start measurement", e);
	            measurement = null;
	            return;
	        }
    	}
    	startStopControl.updateButtons();
    }
}
