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
package org.youscope.addon.measurement.pages;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.youscope.addon.measurement.MeasurementAddonUIPage;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.MeasurementMetadataPanel;

/**
 * A page with which the measurement name, a measurement description, and other metadata can be set. 
 * @author Moritz Lang
 *
 * @param <T> Measurement configuration type
 */
public class MetadataPage<T extends MeasurementConfiguration> extends MeasurementAddonUIPage<T>
{

	/**
	 * Serial Verision UID.
	 */
	private static final long				serialVersionUID		= 885352612109223078L;

	private final JTextField nameField = new JTextField("unnamed");
	private final JTextArea descriptionField = new JTextArea();
	private final MeasurementMetadataPanel metadataPanel;
	/**
	 * Constructor.
	 * @param client YouScope client object.
	 */
	public MetadataPage(YouScopeClient client)
	{
		metadataPanel = new MeasurementMetadataPanel(client, null);
	}

	@Override
	public void loadData(MeasurementConfiguration configuration)
	{
		nameField.setText(configuration.getName());	
		descriptionField.setText(configuration.getDescription());
		metadataPanel.setMetadataProperties(configuration.getMetadataProperties());
	}

	@Override
	public boolean saveData(MeasurementConfiguration configuration)
	{
		configuration.setName(nameField.getText());
		configuration.setDescription(descriptionField.getText());
		configuration.setMetadataProperties(metadataPanel.getMetadataProperties());
		return true;
	}

	@Override
	public void setToDefault(MeasurementConfiguration configuration)
	{
		configuration.setMetadataProperties(metadataPanel.getMetadataProperties());
	}

	@Override
	public String getPageName()
	{
		return "Measurement Metadata";
	}

	@Override
	public void createUI(YouScopeFrame frame)
	{
		DynamicPanel mainPanel = new DynamicPanel();
		
		mainPanel.add(new JLabel("Name:"));
		mainPanel.add(nameField);
		mainPanel.add(new JLabel("Metadata:"));
		mainPanel.add(metadataPanel);
		mainPanel.add(new JLabel("Description:"));
		JScrollPane descriptionScrollPane = new JScrollPane(descriptionField);
		descriptionScrollPane.setPreferredSize(new Dimension(600, 200));
		descriptionScrollPane.setMinimumSize(new Dimension(100, 75));
		mainPanel.addFill(descriptionScrollPane);
		setLayout(new BorderLayout());
		add(mainPanel, BorderLayout.CENTER);
		setBorder(new TitledBorder("Measurement Metadata"));
	}
}
