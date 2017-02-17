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
package org.youscope.plugin.multicameraandcolorstream;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

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
import org.youscope.common.measurement.MeasurementException;
import org.youscope.common.microscope.CameraDevice;
import org.youscope.common.microscope.Channel;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.util.ImageConvertException;
import org.youscope.common.util.ImageTools;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.IntegerTextField;
import org.youscope.uielements.StandardFormats;

/**
 * @author Moritz Lang
 * 
 */
class MultiStreamAndControlsPanel extends JPanel
{
	/**
	 * SerializableVersion UID.
	 */
	private static final long			serialVersionUID		= 6155151406737671100L;

	private static final int NUM_COLORS = 3;
	
	private static double[]			lastExposures			= {20.0, 20.0, 20.0};
	private static int lastPeriod = 100;

	private static boolean[]			lastIncreaseContrasts	= {true, true, true};
	private static boolean[]			lastAutoContrasts		= {true, true, true};
	private String[]			lastCameras		= {null, null, null};

	private static String			lastConfigGroup		= null;
	private static String			lastChannel			= null;

	private static boolean[]			lastActivated			= {false, false, false};
	private JCheckBox[]				lastActivatedFields		= new JCheckBox[NUM_COLORS];

	private JComboBox<?>[]				cameraFields		= new JComboBox<?>[NUM_COLORS];
	
	private JComboBox<String>				configGroupField		= null;

	private JComboBox<String>				channelField			= null;

	private JFormattedTextField[]		exposureFields			= new JFormattedTextField[NUM_COLORS];

	private IntegerTextField		periodField		= new IntegerTextField(lastPeriod);
	
	private JFormattedTextField[]		deltaXFields		= new JFormattedTextField[NUM_COLORS];
	private JFormattedTextField[]		deltaYFields		= new JFormattedTextField[NUM_COLORS];
	private JFormattedTextField[]		deltaPhiFields		= new JFormattedTextField[NUM_COLORS];
	
	private double[] lastDeltaXs = {0,0,0};
	private double[] lastDeltaYs = {0,0,0};
	private double[] lastDeltaPhis = {0,0,0};

	private MultiStreamPanel			mainPanel;

	private JCheckBox[]				increaseContrastFields	= new JCheckBox[NUM_COLORS];
	private JCheckBox[]				autoContrastFields		= new JCheckBox[NUM_COLORS];

	private HistogramPlot[]			histograms				= new HistogramPlot[NUM_COLORS];

	private YouScopeClient	client;
	private YouScopeServer				server;

	private static final String PROPERTY_LAST_DELTA_X = "YouScope.multiCameraAndColorStream.lastDeltaX";
	private static final String PROPERTY_LAST_DELTA_Y = "YouScope.multiCameraAndColorStream.lastDeltaY";
	private static final String PROPERTY_LAST_DELTA_PHIS = "YouScope.multiCameraAndColorStream.lastDeltaPhi";
	private static final String PROPERTY_LAST_CAMERAS = "YouScope.multiCameraAndColorStream.lastCamera";
	
	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws RemoteException
	 * @throws InterruptedException
	 * @throws MicroscopeException
	 */
	public MultiStreamAndControlsPanel(YouScopeClient client, YouScopeServer server) throws RemoteException, InterruptedException, MicroscopeException
	{
		this.client = client;
		this.server = server;
		
		for(int i=0;i<NUM_COLORS; i++)
		{
			lastDeltaXs[i] = client.getPropertyProvider().getProperty(PROPERTY_LAST_DELTA_X + Integer.toString(i), 0.0); 
			lastDeltaYs[i] = client.getPropertyProvider().getProperty(PROPERTY_LAST_DELTA_Y + Integer.toString(i), 0.0);
			lastDeltaPhis[i] = client.getPropertyProvider().getProperty(PROPERTY_LAST_DELTA_PHIS + Integer.toString(i), 0.0);
			lastCameras[i] = client.getPropertyProvider().getProperty(PROPERTY_LAST_CAMERAS + Integer.toString(i), (String)null);
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

		// General settings
		GridBagLayout generalSettingsLayout = new GridBagLayout();
		JPanel generalSettingsPanel = new JPanel(generalSettingsLayout);
		generalSettingsPanel.setBorder(new TitledBorder(new EtchedBorder(), "General Settings"));
		StandardFormats.addGridBagElement(new JLabel("Channel Group:"), generalSettingsLayout, newLineConstr, generalSettingsPanel);
		configGroupField = new JComboBox<String>(loadConfigGroupNames());
		StandardFormats.addGridBagElement(configGroupField, generalSettingsLayout, newLineConstr, generalSettingsPanel);
		StandardFormats.addGridBagElement(new JLabel("Channel:"), generalSettingsLayout, newLineConstr, generalSettingsPanel);
		channelField = new JComboBox<String>();
		StandardFormats.addGridBagElement(channelField, generalSettingsLayout, newLineConstr, generalSettingsPanel);
		
		StandardFormats.addGridBagElement(new JLabel("Imaging Period:"), generalSettingsLayout, newLineConstr, generalSettingsPanel);
		periodField.setMinimalValue(0);
		StandardFormats.addGridBagElement(periodField, generalSettingsLayout, newLineConstr, generalSettingsPanel);

		
		StandardFormats.addGridBagElement(generalSettingsPanel, settingsLayout, newLineConstr, settingsPanel);
		
		// Different colors
		final String[] channelIDNames = {"Red", "Yellow", "Blue"};
		String[] cameraNames = loadCameraNames();
		JPanel cascadePanel = new JPanel(new GridLayout(2, 2, 2, 2));
		for(int i = 0; i < NUM_COLORS; i++)
		{
			GridBagLayout imagingSettingsLayout = new GridBagLayout();
			JPanel imagingSettingsPanel = new JPanel(imagingSettingsLayout);
			imagingSettingsPanel.setBorder(new TitledBorder(new EtchedBorder(), channelIDNames[i]));
			lastActivatedFields[i] = new JCheckBox("Activate " + channelIDNames[i]);
			StandardFormats.addGridBagElement(lastActivatedFields[i], imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
			StandardFormats.addGridBagElement(new JLabel("Camera:"), imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
			cameraFields[i] = new JComboBox<String>(cameraNames);
			StandardFormats.addGridBagElement(cameraFields[i], imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
			StandardFormats.addGridBagElement(new JLabel("Exposure (ms):"), imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
			exposureFields[i] = new JFormattedTextField(StandardFormats.getDoubleFormat());
			StandardFormats.addGridBagElement(exposureFields[i], imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
			increaseContrastFields[i] = new JCheckBox("Increase Contrast.", lastIncreaseContrasts[i]);
			StandardFormats.addGridBagElement(increaseContrastFields[i], imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
			autoContrastFields[i] = new JCheckBox("Auto-Adjust Contrast.", lastAutoContrasts[i]);
			StandardFormats.addGridBagElement(autoContrastFields[i], imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
			class IncreaseContrastActionListener implements ActionListener
			{
				private final int	channelID;

				IncreaseContrastActionListener(int channelID)
				{
					this.channelID = channelID;
				}

				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					lastIncreaseContrasts[channelID] = increaseContrastFields[channelID].isSelected();
					mainPanel.setIncreaseContrast(channelID, lastIncreaseContrasts[channelID]);
					histograms[channelID].setAutoAdjusting(increaseContrastFields[channelID].isSelected() && autoContrastFields[channelID].isSelected());
					autoContrastFields[channelID].setVisible(increaseContrastFields[channelID].isSelected());
				}
			}
			increaseContrastFields[i].addActionListener(new IncreaseContrastActionListener(i));

			class AutoContrastActionListener implements ActionListener
			{
				private final int	channelID;

				AutoContrastActionListener(int channelID)
				{
					this.channelID = channelID;
				}

				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					histograms[channelID].setAutoAdjusting(increaseContrastFields[channelID].isSelected() && autoContrastFields[channelID].isSelected());
				}
			}
			autoContrastFields[i].addActionListener(new AutoContrastActionListener(i));
			histograms[i] = new HistogramPlot();
			StandardFormats.addGridBagElement(histograms[i], imagingSettingsLayout, newLineConstr, imagingSettingsPanel);

			class RotationAndTranslationActionListener implements ActionListener
			{
				private final int	channelID;

				RotationAndTranslationActionListener(int channelID)
				{
					this.channelID = channelID;
				}

				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					lastDeltaXs[channelID] = ((Number)deltaXFields[channelID].getValue()).doubleValue();
					lastDeltaYs[channelID] = ((Number)deltaYFields[channelID].getValue()).doubleValue();
					lastDeltaPhis[channelID] = ((Number)deltaPhiFields[channelID].getValue()).doubleValue();
					MultiStreamAndControlsPanel.this.mainPanel.setRotationAndTranslation(channelID, lastDeltaXs[channelID], lastDeltaYs[channelID], lastDeltaPhis[channelID]);
				}
			}
			RotationAndTranslationActionListener rotationAndTranslationActionListener = new RotationAndTranslationActionListener(i);
			StandardFormats.addGridBagElement(new JLabel("Horizontal Translation (Pixel):"), imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
			deltaXFields[i] = new JFormattedTextField(StandardFormats.getDoubleFormat());
			deltaXFields[i].addActionListener(rotationAndTranslationActionListener);
			StandardFormats.addGridBagElement(deltaXFields[i], imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
			StandardFormats.addGridBagElement(new JLabel("Vertical Translation (Pixel):"), imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
			deltaYFields[i] = new JFormattedTextField(StandardFormats.getDoubleFormat());
			deltaYFields[i].addActionListener(rotationAndTranslationActionListener);
			StandardFormats.addGridBagElement(deltaYFields[i], imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
			StandardFormats.addGridBagElement(new JLabel("Rotation (Degree):"), imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
			deltaPhiFields[i] = new JFormattedTextField(StandardFormats.getDoubleFormat());
			deltaPhiFields[i].addActionListener(rotationAndTranslationActionListener);
			StandardFormats.addGridBagElement(deltaPhiFields[i], imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
						
			cascadePanel.add(imagingSettingsPanel);
		}

		StandardFormats.addGridBagElement(cascadePanel, settingsLayout, newLineConstr, settingsPanel);

		// Load main panel
		mainPanel = new MultiStreamPanel(client, server)
		{

			/**
			 * Serial Version UID.
			 */
			private static final long	serialVersionUID	= -1118017134453605763L;

			@Override
			void processImage(int imageNo, ImageEvent<?> event)
			{
				int[][] bins;
				double[] minMax;
				try
				{
					bins = ImageTools.getHistogram(event, Math.max(histograms[imageNo].getWidth(), 100));
					histograms[imageNo].setBins(bins);
					minMax = histograms[imageNo].getMinMax();
				}
				catch(ImageConvertException e)
				{
					minMax = new double[] {0.0, 1.0};
					MultiStreamAndControlsPanel.this.client.sendError("Could not generate histogram for image.", e);
				}

				mainPanel.setCutoff(imageNo, (float)minMax[0], (float)minMax[1]);
				super.processImage(imageNo, event);
			}
		};

		// Load state
		if(lastConfigGroup != null)
		{
			configGroupField.setSelectedItem(lastConfigGroup);
		}
		loadChannels();
		if(lastChannel != null)
		{
			channelField.setSelectedItem(lastChannel);
		}
		configGroupField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				loadChannels();
			}
		});
		channelField.addActionListener(new ConfigurationChangeListener());
		periodField.addActionListener(new ConfigurationChangeListener());
		for(int i = 0; i < NUM_COLORS; i++)
		{
			
			exposureFields[i].setValue(lastExposures[i]);
			exposureFields[i].addActionListener(new ConfigurationChangeListener());

			lastActivatedFields[i].setSelected(lastActivated[i]);
			lastActivatedFields[i].addActionListener(new ConfigurationChangeListener());
			
			if(lastCameras[i] != null)
				cameraFields[i].setSelectedItem(lastCameras[i]);
			else if(i < cameraNames.length)
				cameraFields[i].setSelectedItem(cameraNames[i]);
			cameraFields[i].addActionListener(new ConfigurationChangeListener());

			increaseContrastFields[i].setSelected(lastIncreaseContrasts[i]);
			autoContrastFields[i].setSelected(lastAutoContrasts[i]);
			histograms[i].setAutoAdjusting(lastIncreaseContrasts[i] && lastAutoContrasts[i]);
			autoContrastFields[i].setVisible(lastIncreaseContrasts[i]);
			
			
			deltaXFields[i].setValue(lastDeltaXs[i]);
			deltaYFields[i].setValue(lastDeltaYs[i]);
			deltaPhiFields[i].setValue(lastDeltaPhis[i]);
			mainPanel.setRotationAndTranslation(i, lastDeltaXs[i], lastDeltaYs[i], lastDeltaPhis[i]);
		}

		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		rightPanel.add(settingsPanel, BorderLayout.NORTH);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainPanel, new JScrollPane(rightPanel));
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation((splitPane.getMaximumDividerLocation() - 160));
		splitPane.setResizeWeight(1.0);
		add(splitPane, BorderLayout.CENTER);
	}

	private String[] loadCameraNames()
	{
		String[] cameraNames = null;
		try
		{
			CameraDevice[] cameraDevices = server.getMicroscope().getCameraDevices();
			cameraNames = new String[cameraDevices.length];
			for(int i=0; i<cameraDevices.length; i++)
			{
				cameraNames[i] = cameraDevices[i].getDeviceID();
			}
		}
		catch(Exception e)
		{
			client.sendError("Could not obtain camera names.", e);
			cameraNames = null;
		}
		
		if(cameraNames == null)
		{
			cameraNames = new String[0];
		}
		return cameraNames;
	}
	
	private String[] loadConfigGroupNames()
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

		if(configGroupNames == null)
		{
			configGroupNames = new String[0];
		}
		return configGroupNames;
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

	private MultiStreamPanel getImagePanel()
	{
		return mainPanel;
	}

	private class ConfigurationChangeListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			stopMeasurement();
			startMeasurement();
		}
	}

	/**
	 * Starts the continuous measurement.
	 */
	public void startMeasurement()
	{
		lastChannel = (String)channelField.getSelectedItem();
		lastConfigGroup = (String)configGroupField.getSelectedItem();
		mainPanel.setChannel(lastConfigGroup, lastChannel);
		lastPeriod = periodField.getValue();
		mainPanel.setImagingPeriod(lastPeriod);
		
		for(int i = 0; i < NUM_COLORS; i++)
		{
			lastActivated[i] = lastActivatedFields[i].isSelected();
			lastExposures[i] = ((Number)exposureFields[i].getValue()).doubleValue();
			lastCameras[i] = (String)cameraFields[i].getSelectedItem();
	

			if(lastActivated[i])
			{
				mainPanel.setExposure(i, lastExposures[i]);
				mainPanel.setCamera(i, lastCameras[i]);
			}
			else
			{
				mainPanel.setCamera(i, null);
			}
		}
		
		try
		{
			getImagePanel().startMeasurement();
		}
		catch(RemoteException e)
		{
			client.sendError("Could not start measurement.", e);
		}
	}

	/**
	 * Stops the measurement. Does nothing if measurement is not running.
	 * The call immediately returns, even if the stopping of the measurement takes longer.
	 */
	public void stopMeasurement()
	{
		try
		{
			getImagePanel().stopMeasurement();
		}
		catch(RemoteException | MeasurementException e)
		{
			client.sendError("Could not stop measurement.", e);
		}
		
		for(int i=0;i<NUM_COLORS; i++)
		{
			client.getPropertyProvider().setProperty(PROPERTY_LAST_DELTA_X + Integer.toString(i), lastDeltaXs[i]); 
			client.getPropertyProvider().setProperty(PROPERTY_LAST_DELTA_Y + Integer.toString(i), lastDeltaYs[i]);
			client.getPropertyProvider().setProperty(PROPERTY_LAST_DELTA_PHIS + Integer.toString(i), lastDeltaPhis[i]);
			client.getPropertyProvider().setProperty(PROPERTY_LAST_CAMERAS + Integer.toString(i), lastCameras[i]);
		}
	}
}
