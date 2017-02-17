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
package org.youscope.plugin.continuationmeasurement;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.youscope.addon.ConfigurationManagement;
import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.configuration.Configuration;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.IntegerTextField;
import org.youscope.uielements.PeriodField;

class SelectMeasurementPanel extends DynamicPanel 
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 7931514904649793034L;
	private final JTextField folderField = new JTextField();
	private final JTextField configFileField = new JTextField();

	private final IntegerTextField imageNumberField = new IntegerTextField(1);
	private final PeriodField previousRuntimeField = new PeriodField();
	final YouScopeClient client;
	private final ArrayList<SelectionListener> listeners = new ArrayList<>();
	public SelectMeasurementPanel(YouScopeClient client, String lastFolder, String lastConfigFile) 
	{
		this.client = client;
		/**
		 *  You might find out that stuff by some kind of wizzard, but since I am lazy there is no wizard, yet, 
		 *  and one has to enter the stuff by hand.
		 *  You might e.g. want to only ask for the path for the config file. By having the config file, you can construct the
		 *  previous save settings. With the previous save settings, you can query the location of the image table. Then, you can load the
		 *  image table and search for the highest image number (=delta-1) and the time when the last image was taken (=last runtime). Similarly, you can
		 *  reconstruct the base folder of the measurement by asking the old save settings for their base folder, and compare it with the folder where you found the config.
		 */
		// internally, we count zero based, but for the user we count one based.
		imageNumberField.setMinimalValue(1);
		imageNumberField.setValue(100);
		if(lastFolder == null)
			lastFolder = (String) getClient().getPropertyProvider().getProperty(StandardProperty.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER);
		folderField.setText(lastFolder == null ? "" : lastFolder);
		previousRuntimeField.setDuration(3600*1000);
		configFileField.setText(lastConfigFile == null ? "" : lastConfigFile);
		
		add(new JLabel("Directory of measurement which should be appended:"));
		JPanel folderPanel = new JPanel(new BorderLayout(5, 0));
		folderPanel.add(folderField, BorderLayout.CENTER);
		if(getClient().isLocalServer())
		{
			JButton openFolderChooser = new JButton("Edit");
			openFolderChooser.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					JFileChooser fileChooser = new JFileChooser(folderField.getText());
					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int returnVal = fileChooser.showDialog(null, "Open");
					if(returnVal == JFileChooser.APPROVE_OPTION)
					{
						folderField.setText(fileChooser.getSelectedFile().getAbsolutePath());
					}
				}
			});
			folderPanel.add(openFolderChooser, BorderLayout.EAST);
		}
		add(folderPanel);
		
		add(new JLabel("Config file of measurement which should be appended:"));
		JPanel configFilePanel = new JPanel(new BorderLayout(5, 0));
		configFilePanel.add(configFileField, BorderLayout.CENTER);
		if(getClient().isLocalServer())
		{
			JButton openFolderChooser = new JButton("Edit");
			openFolderChooser.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					JFileChooser fileChooser = new JFileChooser(configFileField.getText());
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					int returnVal = fileChooser.showDialog(null, "Open");
					if(returnVal == JFileChooser.APPROVE_OPTION)
					{
						configFileField.setText(fileChooser.getSelectedFile().getAbsolutePath());
					}
				}
			});
			configFilePanel.add(openFolderChooser, BorderLayout.EAST);
		}
		add(configFilePanel);
		
		add(new JLabel("Image number to proceed with:"));
		add(imageNumberField);
		add(new JLabel("Runtime of previous measurement:"));
		add(previousRuntimeField);
		
		addFillEmpty();
		
        JButton appendButton = new JButton("Append Measurement");
        appendButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				appendMeasurement();
			}
		});
        add(appendButton);
        
		
	}
	private YouScopeClient getClient()
	{
		return client;
	}
	
	private void appendMeasurement()
	{
		MeasurementConfiguration lastConfig;
		try {
			Configuration lastConfigTemp = ConfigurationManagement.loadConfiguration(configFileField.getText());
			if(!(lastConfigTemp instanceof MeasurementConfiguration))
			{
				getClient().sendError("Provided configuration is not a measurement configuration.");
				return;
			}
			lastConfig = (MeasurementConfiguration) lastConfigTemp;
		} catch (IOException e) {
			getClient().sendError("Could not open last config.", e);
			return;
		}
		String folder = folderField.getText();
		long deltaEvaluation = imageNumberField.getValue()-1;
		long previousRuntime = previousRuntimeField.getDurationLong();
		for(SelectionListener listener : listeners)
		{
			listener.selectionMade(lastConfig, folder, deltaEvaluation, previousRuntime);
		}
	}
	
	public static interface SelectionListener
	{
		void selectionMade(MeasurementConfiguration lastConfig, String folder, long deltaEvaluation, long previousRuntime);
	}
	public void addSelectionListener(SelectionListener listener)
	{
		listeners.add(listener);
	}
	public void removeSelectionListener(SelectionListener listener)
	{
		listeners.remove(listener);
	}
}
