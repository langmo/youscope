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
package org.youscope.plugin.livestream;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.rmi.RemoteException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.microscope.CameraDevice;
import org.youscope.common.microscope.Channel;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.util.ImageConvertException;
import org.youscope.common.util.ImageTools;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.StandardFormats;

/**
 * A panel containing the UI to view and control a continuous measurement.
 * @author Moritz Lang
 */
class ContinousMeasurementAndControlsPanel extends JPanel
{

	/**
	 * Serial Version UID.
	 */
	private static final long			serialVersionUID		= -1418077134453605763L;

	private static double				exposure				= 50.0;
	
	private  boolean shouldRun = true;

	private static int				imagingPeriod			= 100;

	private static boolean			increaseContrast		= true;
	private static boolean			autoContrast			= true;
	
	private static String				lastChannelGroup			= "";
	private static String				lastChannel				= "";
	private final JComboBox<String>					channelGroupField		= new JComboBox<String>();
	private final JComboBox<String>					channelField			= new JComboBox<String>();
	
	private static String					lastCamera			= "";
	private final JLabel 					cameraLabel = new JLabel("Camera:");
	private final JComboBox<String>					cameraField		= new JComboBox<String>();
	
	private JFormattedTextField		exposureField			= new JFormattedTextField(StandardFormats.getDoubleFormat());

	private JFormattedTextField		imagingPeriodField		= new JFormattedTextField(StandardFormats.getIntegerFormat());

	private ContinousMeasurementPanel	mainPanel;

	private JCheckBox					increaseContrastField	= new JCheckBox("Increase Contrast.", increaseContrast);
	private JCheckBox					autoContrastField		= new JCheckBox("Auto-Adjust Contrast.", autoContrast);

	private HistogramPlot				histogram				= new HistogramPlot();

	private YouScopeClient	client;
	private YouScopeServer				server;
	
	private final JButton startStopButton = new JButton("Stop");

	private volatile boolean doNotFire = false;
	
	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws RemoteException
	 * @throws InterruptedException
	 * @throws MicroscopeException
	 */
	public ContinousMeasurementAndControlsPanel(YouScopeClient client, YouScopeServer server) throws RemoteException, InterruptedException, MicroscopeException
	{
		this.client = client;
		this.server = server;

		// Get button images
		String rotateClockwiseFile = "icons/arrow-circle-225-left.png";
		String rotateCounterClockwiseFile = "icons/arrow-circle.png";
		String flipHorizontalFile = "icons/arrow-continue-000-top.png";
		String flipVerticalFile = "icons/arrow-continue-090.png";

		ImageIcon rotateClockwiseIcon = null;
		ImageIcon rotateCounterClockwiseIcon = null;
		ImageIcon flipHorizontalIcon = null;
		ImageIcon flipVerticalIcon = null;

		try
		{
			URL rotateClockwiseURL = getClass().getClassLoader().getResource(rotateClockwiseFile);
			if(rotateClockwiseURL != null)
				rotateClockwiseIcon = new ImageIcon(rotateClockwiseURL, "Rotate Clockwise");
			URL rotateCounterClockwiseURL = getClass().getClassLoader().getResource(rotateCounterClockwiseFile);
			if(rotateCounterClockwiseURL != null)
				rotateCounterClockwiseIcon = new ImageIcon(rotateCounterClockwiseURL, "Rotate Counter Clockwise");

			URL flipHorizontalURL = getClass().getClassLoader().getResource(flipHorizontalFile);
			if(flipHorizontalURL != null)
				flipHorizontalIcon = new ImageIcon(flipHorizontalURL, "Flip Horizontal");
			URL flipVerticalURL = getClass().getClassLoader().getResource(flipVerticalFile);
			if(flipVerticalURL != null)
				flipVerticalIcon = new ImageIcon(flipVerticalURL, "Flip Vertical");
		}
		catch(@SuppressWarnings("unused") Exception e)
		{
			// Do nothing.
		}

		setLayout(new BorderLayout());

		GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();

		GridBagConstraints newLineNotFillConstr = new GridBagConstraints();
		newLineNotFillConstr.gridwidth = GridBagConstraints.REMAINDER;
		newLineNotFillConstr.anchor = GridBagConstraints.NORTHWEST;
		newLineNotFillConstr.gridx = 0;
		newLineNotFillConstr.weightx = 0;

		GridBagLayout settingsLayout = new GridBagLayout();
		JPanel settingsPanel = new JPanel(settingsLayout);

		// Imaging Panel
		GridBagLayout imagingSettingsLayout = new GridBagLayout();
		JPanel imagingSettingsPanel = new JPanel(imagingSettingsLayout);
		imagingSettingsPanel.setBorder(new TitledBorder(new EtchedBorder(), "Imaging"));
		StandardFormats.addGridBagElement(cameraLabel, imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
		StandardFormats.addGridBagElement(cameraField, imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
		StandardFormats.addGridBagElement(new JLabel("Channel Group:"), imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
		StandardFormats.addGridBagElement(channelGroupField, imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
		StandardFormats.addGridBagElement(new JLabel("Channel:"), imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
		StandardFormats.addGridBagElement(channelField, imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
		StandardFormats.addGridBagElement(new JLabel("Exposure (ms):"), imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
		StandardFormats.addGridBagElement(exposureField, imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
		StandardFormats.addGridBagElement(new JLabel("Imaging Period (ms):"), imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
		StandardFormats.addGridBagElement(imagingPeriodField, imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
		StandardFormats.addGridBagElement(imagingSettingsPanel, settingsLayout, newLineConstr, settingsPanel);

		// Contrast Panel
		GridBagLayout contrastSettingsLayout = new GridBagLayout();
		JPanel contrastSettingsPanel = new JPanel(contrastSettingsLayout);
		contrastSettingsPanel.setBorder(new TitledBorder(new EtchedBorder(), "Contrast"));
		StandardFormats.addGridBagElement(increaseContrastField, contrastSettingsLayout, newLineConstr, contrastSettingsPanel);
		StandardFormats.addGridBagElement(autoContrastField, contrastSettingsLayout, newLineConstr, contrastSettingsPanel);
		increaseContrastField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				increaseContrast = increaseContrastField.isSelected();
				mainPanel.setIncreaseContrast(increaseContrast);
				histogram.setAutoAdjusting(increaseContrastField.isSelected() && autoContrastField.isSelected());
				autoContrastField.setVisible(increaseContrastField.isSelected());
			}
		});
		autoContrastField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				histogram.setAutoAdjusting(increaseContrastField.isSelected() && autoContrastField.isSelected());
			}
		});
		StandardFormats.addGridBagElement(histogram, contrastSettingsLayout, newLineConstr, contrastSettingsPanel);
		StandardFormats.addGridBagElement(contrastSettingsPanel, settingsLayout, newLineConstr, settingsPanel);

		// Rotate and Flip Panel
		JPanel rotateAndFlipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		rotateAndFlipPanel.setBorder(new TitledBorder(new EtchedBorder(), "Rotating and Flipping"));
		JButton rotateClockwise;
		if(rotateClockwiseIcon == null)
			rotateClockwise = new JButton("rotate clockwise");
		else
			rotateClockwise = new JButton(rotateClockwiseIcon);
		rotateClockwise.setMargin(new Insets(1, 1, 1, 1));
		rotateClockwise.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				mainPanel.rotateClockwise();
			}

		});
		rotateAndFlipPanel.add(rotateClockwise);
		JButton rotateCounterClockwise;
		if(rotateCounterClockwiseIcon == null)
			rotateCounterClockwise = new JButton("rotate counter clockwise");
		else
			rotateCounterClockwise = new JButton(rotateCounterClockwiseIcon);
		rotateCounterClockwise.setMargin(new Insets(1, 1, 1, 1));
		rotateCounterClockwise.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				mainPanel.rotateCounterClockwise();
			}

		});
		rotateAndFlipPanel.add(rotateCounterClockwise);
		JButton flipHorizontalButton;
		if(flipHorizontalIcon == null)
			flipHorizontalButton = new JButton("flip horizontal");
		else
			flipHorizontalButton = new JButton(flipHorizontalIcon);
		flipHorizontalButton.setMargin(new Insets(1, 1, 1, 1));
		flipHorizontalButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				mainPanel.flipHorizontal();
			}

		});
		rotateAndFlipPanel.add(flipHorizontalButton);
		JButton flipVerticalButton = new JButton("flip vertical");
		if(flipVerticalIcon == null)
			flipVerticalButton = new JButton("flip vertical");
		else
			flipVerticalButton = new JButton(flipVerticalIcon);
		flipVerticalButton.setMargin(new Insets(1, 1, 1, 1));
		flipVerticalButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				mainPanel.flipVertical();
			}

		});
		rotateAndFlipPanel.add(flipVerticalButton);
		// TODO: Pixel intensities do not work with these types of transformations. Make them work and allow flipping and rotating of images again.
		//StandardFormats.addGridBagElement(rotateAndFlipPanel, settingsLayout, newLineConstr, settingsPanel);
		
		startStopButton.setText(shouldRun ? "Stop" : "Start");
		startStopButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if(shouldRun)
				{
					stopMeasurement();
					startStopButton.setText("Start");
					shouldRun = false;
				}
				else
				{
					startMeasurement();
					startStopButton.setText("Stop");
					shouldRun = true;
				}
			}
		});
		StandardFormats.addGridBagElement(startStopButton, settingsLayout, newLineConstr, settingsPanel);
		

		// Load state
		if(loadCameras())
		{
			cameraField.setVisible(true);
			cameraLabel.setVisible(true);
			cameraField.setSelectedItem(lastCamera);
		}
		else
		{
			cameraField.setVisible(false);
			cameraLabel.setVisible(false);
		}
		if(cameraField.getSelectedItem() != null)
			lastCamera = cameraField.getSelectedItem().toString();
		
		loadChannelGroupNames();
		channelGroupField.setSelectedItem(lastChannelGroup);
		if(channelGroupField.getSelectedItem() != null)
			lastChannelGroup = channelGroupField.getSelectedItem().toString();
		loadChannels();
		channelField.setSelectedItem(lastChannel);
		if(channelField.getSelectedItem() != null)
			lastChannel = channelField.getSelectedItem().toString();

		exposureField.setValue(exposure);
		imagingPeriodField.setValue(imagingPeriod);

		increaseContrastField.setSelected(increaseContrast);
		autoContrastField.setSelected(autoContrast);
		histogram.setAutoAdjusting(increaseContrast && autoContrast);
		autoContrastField.setVisible(increaseContrast);

		exposureField.addActionListener(new ConfigurationChangeListener());
		imagingPeriodField.addActionListener(new ConfigurationChangeListener());
		channelField.addActionListener(new ConfigurationChangeListener());
		cameraField.addActionListener(new ConfigurationChangeListener());
		channelGroupField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				loadChannels();
				measurementConfigurationChanged();
			}
		});

		// Load main panel
		mainPanel = new ContinousMeasurementPanel(client, server, lastCamera, imagingPeriod, lastChannelGroup, lastChannel, exposure, increaseContrast)
		{

			/**
			 * Serial Version UID.
			 */
			private static final long	serialVersionUID	= -1418017134453605763L;

			@Override
			public void newImage(ImageEvent<?> event)
			{
				int[][] bins;
				double[] minMax;
				try
				{
					bins = ImageTools.getHistogram(event, Math.max(histogram.getWidth(), 100));
					histogram.setBins(bins);
					minMax = histogram.getMinMax();
				}
				catch(ImageConvertException e)
				{
					minMax = new double[] {0.0, 1.0};
					ContinousMeasurementAndControlsPanel.this.client.sendError("Could not generate histogram for image.", e);
				}

				mainPanel.setCutoff((float)minMax[0], (float)minMax[1]);
				super.newImage(event);
			}
		};
		// MouseMicroscopeControl mouseControl = new MouseMicroscopeControl();
		// mainPanel.addMouseListener(mouseControl);
		// mainPanel.addMouseMotionListener(mouseControl);

		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		rightPanel.add(settingsPanel, BorderLayout.NORTH);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainPanel, new JScrollPane(rightPanel));
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation((splitPane.getMaximumDividerLocation() - 80));
		splitPane.setResizeWeight(1.0);
		add(splitPane, BorderLayout.CENTER);
	}

	private boolean loadCameras()
	{
		String[] cameraNames = null;
		try
		{
			CameraDevice[] cameras = server.getMicroscope().getCameraDevices();
			cameraNames = new String[cameras.length];
			for(int i=0; i< cameras.length; i++)
			{
				cameraNames[i] = cameras[i].getDeviceID();
			}
		}
		catch(RemoteException e)
		{
			client.sendError("Could not obtain names of cameras.", e);
		}
		
		if(cameraNames == null || cameraNames.length <= 0)
		{
			cameraNames = new String[] {""};
		}
		
		cameraField.removeAllItems();
		for(String cameraName : cameraNames)
		{
			cameraField.addItem(cameraName);
		}
		return cameraNames.length > 1;
	}
	
	private void loadChannelGroupNames()
	{
		String[] channelGroupNames = null;
		try
		{
			channelGroupNames = server.getMicroscope().getChannelManager().getChannelGroupIDs();
		}
		catch(Exception e)
		{
			client.sendError("Could not obtain config group names.", e);
		}

		if(channelGroupNames == null || channelGroupNames.length <= 0)
		{
			channelGroupNames = new String[] {""};
		}

		channelGroupField.removeAllItems();
		for(String configGroupName : channelGroupNames)
		{
			channelGroupField.addItem(configGroupName);
		}
	}

	private void loadChannels()
	{
		String[] channelNames = null;

		Object selectedGroup = channelGroupField.getSelectedItem();
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

		doNotFire = true;
		channelField.removeAllItems();
		for(String channelName : channelNames)
		{
			channelField.addItem(channelName);
		}
		doNotFire = false;
	}

	/* private class MouseMicroscopeControl extends MouseAdapter implements Runnable,
	 * MouseMotionListener
	 * {
	 * private boolean currentlyDragging = false;
	 * 
	 * private boolean mouseDown = false;
	 * 
	 * private Point lastProcessedPosition = null;
	 * 
	 * private Point currentPosition = null;
	 * 
	 * @Override
	 * public void run()
	 * {
	 * synchronized (this)
	 * {
	 * if (currentlyDragging)
	 * return;
	 * currentlyDragging = true;
	 * }
	 * int period = ((Number) imagingPeriodField.getValue()).intValue();
	 * while (mouseDown)
	 * {
	 * double diffX;
	 * double diffY;
	 * synchronized (this)
	 * {
	 * if (currentPosition == null
	 * || lastProcessedPosition == null)
	 * return;
	 * 
	 * diffX = currentPosition.getX()
	 * - lastProcessedPosition.getX();
	 * diffY = currentPosition.getY()
	 * - lastProcessedPosition.getY();
	 * lastProcessedPosition = currentPosition;
	 * }
	 * if (diffX != 0 || diffY != 0)
	 * {
	 * setRelativePosition(diffX * 0.1 * moveStepSize, -diffY
	 * 0.1 * moveStepSize);
	 * }
	 * try
	 * {
	 * Thread.sleep(period);
	 * }
	 * catch (InterruptedException e)
	 * {
	 * client.sendError("Mouse dragging disturbed.", e);
	 * return;
	 * }
	 * }
	 * synchronized (this)
	 * {
	 * currentlyDragging = false;
	 * }
	 * }
	 * 
	 * @Override
	 * public void mouseExited(MouseEvent e)
	 * {
	 * synchronized (this)
	 * {
	 * mouseDown = false;
	 * }
	 * setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	 * }
	 * 
	 * @Override
	 * public void mousePressed(MouseEvent e)
	 * {
	 * synchronized (this)
	 * {
	 * if (currentlyDragging == true)
	 * return;
	 * mouseDown = true;
	 * lastProcessedPosition = e.getPoint();
	 * currentPosition = e.getPoint();
	 * }
	 * setCursor(new Cursor(Cursor.MOVE_CURSOR));
	 * Thread thread = new Thread(this);
	 * thread.start();
	 * }
	 * 
	 * @Override
	 * public void mouseReleased(MouseEvent e)
	 * {
	 * synchronized (this)
	 * {
	 * mouseDown = false;
	 * }
	 * setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	 * }
	 * 
	 * @Override
	 * public void mouseDragged(MouseEvent arg0)
	 * {
	 * synchronized (this)
	 * {
	 * currentPosition = arg0.getPoint();
	 * }
	 * }
	 * 
	 * @Override
	 * public void mouseMoved(MouseEvent arg0)
	 * {
	 * // Event of no interest -> Do nothing
	 * 
	 * }
	 * } */

	private ContinousMeasurementPanel getImagePanel()
	{
		return mainPanel;
	}

	private class ConfigurationChangeListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			measurementConfigurationChanged();
		}
	}
	
	private void measurementConfigurationChanged()
	{
		if(doNotFire)
			return;
		
		String channel = channelField.getSelectedItem().toString();
		String configGroup = channelGroupField.getSelectedItem().toString();
		String camera = cameraField.getSelectedItem().toString();
		
		if(channel == null || configGroup == null)
			return;
		
		exposure = ((Number)exposureField.getValue()).doubleValue();
		imagingPeriod = ((Number)imagingPeriodField.getValue()).intValue();
		
		lastChannel = channel;
		lastChannelGroup = configGroup;
		lastCamera = camera;

		mainPanel.stopMeasurement();
		mainPanel.setCamera(camera);
		mainPanel.setExposure(exposure);
		mainPanel.setChannel(lastChannelGroup, lastChannel);
		mainPanel.setImagingPeriod(imagingPeriod);
		if(shouldRun)
			mainPanel.startMeasurement();
	}

	/**
     * Returns if measurement is currently running or not.
     * @return TRUE if measurement is running, false otherwise.
     */
    public boolean isMeasurementRunning()
    {
    	return getImagePanel().isMeasurementRunning();
    }
	
	/**
	 * Starts the continuous measurement.
	 */
	public void startMeasurement()
	{
		getImagePanel().startMeasurement();
	}

	/**
	 * Stops the measurement. Does nothing if measurement is not running.
	 * The call immediately returns, even if the stopping of the measurement takes longer.
	 */
	public void stopMeasurement()
	{
		getImagePanel().stopMeasurement();
	}

	/**
	 * Stops the measurement and waits until it is finished.
	 */
	public void stopMeasurementAndWait()
	{
		getImagePanel().stopMeasurement();
	}
}
