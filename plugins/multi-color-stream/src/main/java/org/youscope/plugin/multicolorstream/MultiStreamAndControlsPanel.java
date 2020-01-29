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
package org.youscope.plugin.multicolorstream;

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
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.microscope.Channel;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.util.ImageConvertException;
import org.youscope.common.util.ImageTools;
import org.youscope.serverinterfaces.YouScopeServer;
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
	private static final long			serialVersionUID		= 6155451406737671100L;

	private static double[]			lastExposures			= {20.0, 100.0, 100.0, 100.0};

	private static int				imagingPeriod			= 100;

	private static boolean[]			lastIncreaseContrasts	= {true, true, true, true};
	private static boolean[]			lastAutoContrasts		= {true, true, true, true};

	private static String[]			lastConfigGroups		= {"", null, null, null};
	private static String[]			lastChannels			= {"", null, null, null};

	private static boolean[]			lastActivated			= {true, false, false, false};
	private JCheckBox[]				lastActivatedFields		= new JCheckBox[4];

	private JComboBox<?>[]				configGroupFields		= new JComboBox<?>[4];

	private JComboBox<?>[]				channelFields			= new JComboBox<?>[4];

	private JFormattedTextField[]		exposureFields			= new JFormattedTextField[4];

	private JFormattedTextField		imagingPeriodField		= new JFormattedTextField(StandardFormats.getIntegerFormat());

	private MultiStreamPanel			mainPanel;

	private JCheckBox[]				increaseContrastFields	= new JCheckBox[4];
	private JCheckBox[]				autoContrastFields		= new JCheckBox[4];

	private HistogramPlot[]			histograms				= new HistogramPlot[4];

	private final JSlider	brightFieldSlieder		= new JSlider(0, 100, 50);
	private static double lastBrightFieldOpacity = 0.5;
	private final JLabel brightFieldLabel = new JLabel(Double.toString(lastBrightFieldOpacity * 100.0) + "%", SwingConstants.RIGHT);
	
        
	private YouScopeClient	client;
	private YouScopeServer				server;

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
		StandardFormats.addGridBagElement(new JLabel("Imaging Period (ms):"), generalSettingsLayout, newLineConstr, generalSettingsPanel);
		StandardFormats.addGridBagElement(imagingPeriodField, generalSettingsLayout, newLineConstr, generalSettingsPanel);
		StandardFormats.addGridBagElement(new JLabel("Bright-Field Opacity:"), generalSettingsLayout, newLineConstr, generalSettingsPanel);
		brightFieldSlieder.setValue((int)(lastBrightFieldOpacity * 100.0));
		brightFieldSlieder.setPaintTicks(true);
		brightFieldSlieder.setSnapToTicks(true);
		brightFieldSlieder.setMinorTickSpacing(1);
		brightFieldSlieder.setMajorTickSpacing(10);
		brightFieldSlieder.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent arg0)
			{
				lastBrightFieldOpacity = brightFieldSlieder.getValue() / 100.0;
				brightFieldLabel.setText(Double.toString(lastBrightFieldOpacity * 100.0) + "%");
				mainPanel.setBrightFieldOpacity(lastBrightFieldOpacity);
			}
		});
		StandardFormats.addGridBagElement(brightFieldSlieder, generalSettingsLayout, newLineConstr, generalSettingsPanel);
		StandardFormats.addGridBagElement(brightFieldLabel, generalSettingsLayout, newLineConstr, generalSettingsPanel);
		StandardFormats.addGridBagElement(generalSettingsPanel, settingsLayout, newLineConstr, settingsPanel);

		// Different channels
		String[] configGroupNames = loadConfigGroupNames();
		final String[] channelIDNames = {"Bright-Field", "Red", "Green", "Blue"};
		JPanel cascadePanel = new JPanel(new GridLayout(2, 2, 2, 2));
		for(int i = 0; i < 4; i++)
		{
			GridBagLayout imagingSettingsLayout = new GridBagLayout();
			JPanel imagingSettingsPanel = new JPanel(imagingSettingsLayout);
			imagingSettingsPanel.setBorder(new TitledBorder(new EtchedBorder(), channelIDNames[i]));
			lastActivatedFields[i] = new JCheckBox("Activate " + channelIDNames[i]);
			StandardFormats.addGridBagElement(lastActivatedFields[i], imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
			StandardFormats.addGridBagElement(new JLabel("Channel Group:"), imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
			configGroupFields[i] = new JComboBox<String>(configGroupNames);
			StandardFormats.addGridBagElement(configGroupFields[i], imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
			StandardFormats.addGridBagElement(new JLabel("Channel:"), imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
			channelFields[i] = new JComboBox<String>();
			StandardFormats.addGridBagElement(channelFields[i], imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
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
			if(i>0)
			{
				// By default, only show the upper 20%
				histograms[i].setAutoAdjustmentCuttoffs(0.8, 0.01);
			}
			StandardFormats.addGridBagElement(histograms[i], imagingSettingsLayout, newLineConstr, imagingSettingsPanel);

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
			protected void newImage(int imageNo, ImageEvent<?> event)
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
				super.newImage(imageNo, event);
			}
		};

		// Load state
		for(int i = 0; i < configGroupFields.length; i++)
		{
			if(lastConfigGroups[i] != null)
			{
				configGroupFields[i].setSelectedItem(lastConfigGroups[i]);
			}
			loadChannels(i);
			if(lastChannels[i] != null)
			{
				channelFields[i].setSelectedItem(lastChannels[i]);
			}
			exposureFields[i].setValue(lastExposures[i]);
			exposureFields[i].addActionListener(new ConfigurationChangeListener());

			class ConfigGroupActionListener implements ActionListener
			{
				private final int	channelID;

				ConfigGroupActionListener(int channelID)
				{
					this.channelID = channelID;
				}

				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					loadChannels(channelID);
				}
			}
			configGroupFields[i].addActionListener(new ConfigGroupActionListener(i));
			channelFields[i].addActionListener(new ConfigurationChangeListener());

			lastActivatedFields[i].setSelected(lastActivated[i]);
			lastActivatedFields[i].addActionListener(new ConfigurationChangeListener());

			increaseContrastFields[i].setSelected(lastIncreaseContrasts[i]);
			autoContrastFields[i].setSelected(lastAutoContrasts[i]);
			histograms[i].setAutoAdjusting(lastIncreaseContrasts[i] && lastAutoContrasts[i]);
			autoContrastFields[i].setVisible(lastIncreaseContrasts[i]);
		}
		imagingPeriodField.setValue(imagingPeriod);
		imagingPeriodField.addActionListener(new ConfigurationChangeListener());

		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		rightPanel.add(settingsPanel, BorderLayout.NORTH);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainPanel, new JScrollPane(rightPanel));
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation((splitPane.getMaximumDividerLocation() - 160));
		splitPane.setResizeWeight(1.0);
		add(splitPane, BorderLayout.CENTER);
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

	private void loadChannels(int channelID)
	{
		String[] channelNames = null;

		Object selectedGroup = configGroupFields[channelID].getSelectedItem();
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

		channelFields[channelID].removeAllItems();
		for(String channelName : channelNames)
		{
			@SuppressWarnings("unchecked")
			JComboBox<String> temp = ((JComboBox<String>)channelFields[channelID]);
			temp.addItem(channelName);
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
		for(int i = 0; i < 4; i++)
		{
			lastActivated[i] = lastActivatedFields[i].isSelected();
			lastExposures[i] = ((Number)exposureFields[i].getValue()).doubleValue();

			lastChannels[i] = (String)channelFields[i].getSelectedItem();
			lastConfigGroups[i] = (String)configGroupFields[i].getSelectedItem();

			if(lastActivated[i])
			{
				mainPanel.setExposure(i, lastExposures[i]);
				mainPanel.setChannel(i, lastConfigGroups[i], lastChannels[i]);
			}
			else
			{
				mainPanel.setChannel(i, null, null);
			}
		}
		imagingPeriod = ((Number)imagingPeriodField.getValue()).intValue();
		mainPanel.setImagingPeriod(imagingPeriod);
		mainPanel.setBrightFieldOpacity(lastBrightFieldOpacity);

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
}
