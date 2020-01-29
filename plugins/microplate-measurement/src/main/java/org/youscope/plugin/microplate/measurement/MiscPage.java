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
package org.youscope.plugin.microplate.measurement;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.youscope.addon.measurement.MeasurementAddonUIPage;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DoubleTextField;
import org.youscope.uielements.DynamicPanel;

class MiscPage extends MeasurementAddonUIPage<MicroplateMeasurementConfiguration>
{

	/**
	 * Serial Verision UID.
	 */
	private static final long				serialVersionUID		= 885352612109223078L;
	
	private final JCheckBox 							allowEditsField = new JCheckBox("Allow measurement to be edited while running (experimental).");
	private final JCheckBox 							storeStatisticsField = new JCheckBox("Gather statistics about job runtimes.");
	private final JLabel							statisticsFileFieldLabel		= new JLabel("Statistics file name (without extension):");
	private final JTextField						statisticsFileField				= new JTextField("statistics");

	private final JComboBox<MicroplateMeasurementConfiguration.ZeroPositionType> zeroPositionTypeField		= new JComboBox<>(MicroplateMeasurementConfiguration.ZeroPositionType.values());
	private final DoubleTextField zeroPositionXField = new DoubleTextField(0);
	private final DoubleTextField zeroPositionYField = new DoubleTextField(0);
	private final DoubleTextField zeroPositionFocusField = new DoubleTextField(0);
	
	private final YouScopeServer server;
	private final YouScopeClient client;
	
	private String focusDevice = null;
	private String stageDevice = null;
	/**
	 * Constructor.
	 * @param client
	 * @param server
	 */
	MiscPage(YouScopeClient client, YouScopeServer server)
	{
		this.server = server;
		this.client = client;
	}

	@Override
	public void loadData(MicroplateMeasurementConfiguration configuration)
	{
		String statisticsFileName = configuration.getStatisticsFileName();
		if(statisticsFileName == null)
		{
			storeStatisticsField.setSelected(false);
			statisticsFileFieldLabel.setVisible(false);
			statisticsFileField.setVisible(false);
		}
		else
		{
			storeStatisticsField.setSelected(true);
			statisticsFileFieldLabel.setVisible(true);
			statisticsFileField.setVisible(true);
			statisticsFileField.setText(statisticsFileName);			
		}
		allowEditsField.setSelected(configuration.isAllowEditsWhileRunning());
		
		zeroPositionTypeField.setSelectedItem(configuration.getZeroPositionType());
		XYAndFocusPosition zeroPosition = configuration.getZeroPosition();
		if(zeroPosition != null)
		{
			zeroPositionXField.setValue(zeroPosition.getX());
			zeroPositionYField.setValue(zeroPosition.getY());
			if(!Double.isNaN(zeroPosition.getFocus()))
			{
				zeroPositionFocusField.setValue(zeroPosition.getFocus());
			}
		}
		if(configuration.getFocusConfiguration() != null)
			focusDevice = configuration.getFocusConfiguration().getFocusDevice();
		else
			focusDevice = null;
		stageDevice = configuration.getStageDevice();
		for(ActionListener listener : zeroPositionTypeField.getActionListeners())
		{
			listener.actionPerformed(new ActionEvent(zeroPositionTypeField, ActionEvent.ACTION_FIRST, "reloaded"));
		}
	}

	@Override
	public boolean saveData(MicroplateMeasurementConfiguration configuration)
	{
		if(storeStatisticsField.isSelected())
		{
			configuration.setStatisticsFileName(statisticsFileField.getText());
		}
		else
		{
			configuration.setStatisticsFileName(null);
		}
		configuration.setAllowEditsWhileRunning(allowEditsField.isSelected());
		
		MicroplateMeasurementConfiguration.ZeroPositionType zeroPositionType = (MicroplateMeasurementConfiguration.ZeroPositionType)zeroPositionTypeField.getSelectedItem();
		configuration.setZeroPositionType(zeroPositionType);
		if(zeroPositionType == MicroplateMeasurementConfiguration.ZeroPositionType.CUSTOM)
		{
			configuration.setZeroPosition(new XYAndFocusPosition(zeroPositionXField.getValue(), zeroPositionYField.getValue(), configuration.getFocusConfiguration() != null ? zeroPositionFocusField.getValue() : Double.NaN));
		}
		else
			configuration.setZeroPosition(null);
		return true;
	}

	@Override
	public void setToDefault(MicroplateMeasurementConfiguration configuration)
	{
		// Do nothing.
	}

	@Override
	public String getPageName()
	{
		return "Misc";
	}

	@Override
	public void createUI(final YouScopeFrame frame)
	{
		setLayout(new BorderLayout());
		
		DynamicPanel mainPanel = new DynamicPanel();
		mainPanel.add(allowEditsField);
		mainPanel.add(storeStatisticsField);
		mainPanel.add(statisticsFileFieldLabel);
		mainPanel.add(statisticsFileField);
		storeStatisticsField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				statisticsFileFieldLabel.setVisible(storeStatisticsField.isSelected());
				statisticsFileField.setVisible(storeStatisticsField.isSelected());
				fireSizeChanged();
			}
		});
		
		mainPanel.add(new JLabel("Action after each iteration, and at start and end of measurement:"));
		mainPanel.add(zeroPositionTypeField);
		final JLabel zeroPositionXYLabel = new JLabel("Zero position x/y (um):");
		mainPanel.add(zeroPositionXYLabel);
		final JPanel zeroPositionXYPanel = new JPanel(new GridLayout(1,2));
		zeroPositionXYPanel.add(zeroPositionXField);
		zeroPositionXYPanel.add(zeroPositionYField);
		mainPanel.add(zeroPositionXYPanel);
		final JLabel zeroPositionFocusLabel = new JLabel("Zero focus position (um):");
		mainPanel.add(zeroPositionFocusLabel);
		mainPanel.add(zeroPositionFocusField);
		final JButton currentPositionButton = new JButton("Current Position");
		currentPositionButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Point2D.Double currentPosition;
				try
				{
					currentPosition = server.getMicroscope().getStageDevice(stageDevice).getPosition();
				}
				catch(Exception e1)
				{
					client.sendError("Could not obtain current postion of stage "+stageDevice+".", e1);
					return;
				}
				zeroPositionXField.setValue(currentPosition.getX());
				zeroPositionYField.setValue(currentPosition.getY());
				if(focusDevice != null)
				{
					double currentFocus;
					try
					{
						currentFocus = server.getMicroscope().getFocusDevice(focusDevice).getFocusPosition();
					}
					catch(Exception e1)
					{
						client.sendError("Could not obtain current postion of focus device "+focusDevice+".", e1);
						return;
					}
					zeroPositionFocusField.setValue(currentFocus);
				}
			}
		});
		mainPanel.add(currentPositionButton);
		zeroPositionTypeField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean visible = (MicroplateMeasurementConfiguration.ZeroPositionType)zeroPositionTypeField.getSelectedItem() == MicroplateMeasurementConfiguration.ZeroPositionType.CUSTOM;
				zeroPositionXYLabel.setVisible(visible);
				zeroPositionXYPanel.setVisible(visible);
				currentPositionButton.setVisible(visible);
				
				zeroPositionFocusLabel.setVisible(visible && focusDevice != null);
				zeroPositionFocusField.setVisible(visible && focusDevice != null);
			}
		});
		
		mainPanel.addFillEmpty();
		add(mainPanel, BorderLayout.CENTER);
		setBorder(new TitledBorder("Miscellaneous"));
	}
}
