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
package org.youscope.plugin.controller;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.youscope.addon.component.ComponentAddonUI;
import org.youscope.addon.component.ComponentMetadata;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.table.ColumnDefinition;
import org.youscope.common.table.TableConsumerConfiguration;
import org.youscope.uielements.ComponentComboBox;
import org.youscope.uielements.DynamicPanel;

class OutputConfigurationPanel extends DynamicPanel
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 5001188084125383941L;
	private Component addonConfigurationPanel = null;
	private ComponentAddonUI<? extends JobConfiguration> currentAddon = null;
	private final YouScopeClient client;
	private final YouScopeFrame frame;
	private final ControllerJobConfiguration controllerConfiguration;
	private final ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();
	private final ComponentComboBox<JobConfiguration> componentBox;
	public OutputConfigurationPanel(ControllerJobConfiguration controllerConfiguration, YouScopeClient client, YouScopeFrame frame)
	{
		this.client = client;
		this.frame = frame;
		this.controllerConfiguration = controllerConfiguration;
		
		add(new JLabel("Output job:"));
		componentBox = new ComponentComboBox<JobConfiguration>(client, JobConfiguration.class, TableConsumerConfiguration.class);
		if(controllerConfiguration.getInputJob() != null)
			componentBox.setSelectedElement(controllerConfiguration.getInputJob().getTypeIdentifier());
		add(componentBox);
		componentBox.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) {
				displayAddonConfiguration(componentBox.getSelectedElement());
			}
		});
		displayAddonConfiguration(componentBox.getSelectedElement());
	}
	
	void addActionListener(ActionListener listener)
	{
		actionListeners.add(listener);
	}
	void removeActionListener(ActionListener listener)
	{
		actionListeners.remove(listener);
	}
	
	private Component createErrorUI(String message, Exception exception)
	{
		if(exception != null)
			message += "\n\n"+exception.getMessage();
		JTextArea textArea = new JTextArea(message);
		textArea.setEditable(false);
		JPanel errorPane = new JPanel(new BorderLayout());
		errorPane.add(new JLabel("An error occured:"), BorderLayout.NORTH);
		errorPane.add(new JScrollPane(textArea), BorderLayout.CENTER);
		return errorPane;
	}
	void commitChanges(ControllerJobConfiguration configuration)
	{	    	
		configuration.setOutputJob(currentAddon == null ? null : currentAddon.getConfiguration());
	}
	ColumnDefinition<?>[] getOutputColumns()
	{
		if(currentAddon == null)
		{
			return null;
		}
		JobConfiguration job;
		try {
			job = currentAddon.getConfiguration();
		} catch (Exception e) {
			client.sendError("Cannot get information on data consumed by output job.", e);
			return null;
		}
		
		if(job instanceof TableConsumerConfiguration)
		{
			return ((TableConsumerConfiguration)job).getConsumedTableDefinition().getColumnDefinitions();
		}
		client.sendError("Selected output job does not consume table data.");
		return null;
	}
	private void displayAddonConfiguration(ComponentMetadata<? extends JobConfiguration> outputOption)
	{
		if(currentAddon != null)
		{
			ComponentMetadata<?> metadata = currentAddon.getAddonMetadata();
			if(metadata!= null && outputOption!= null && metadata.getTypeIdentifier().equals(outputOption.getTypeIdentifier()))
				return;
			currentAddon = null;
		}
		if(addonConfigurationPanel != null)
			remove(addonConfigurationPanel);
		if(outputOption == null)
		{
			addonConfigurationPanel = createErrorUI("No controller input jobs available.", null);
			addFill(addonConfigurationPanel);
			revalidate();
			if(frame.isVisible())
				frame.pack();
			fireJobChanged();
			return;
		}
		try 
		{
			currentAddon = client.getAddonProvider().createComponentUI(outputOption.getTypeIdentifier(), JobConfiguration.class);
			
			JobConfiguration currentConfiguration = controllerConfiguration.getOutputJob();
			
			if(currentConfiguration != null && currentConfiguration.getTypeIdentifier().equals(outputOption.getTypeIdentifier()))
			{
				currentAddon.setConfiguration(currentConfiguration);
			}
			addonConfigurationPanel = currentAddon.toPanel(frame);
		} 
		catch (Exception e) 
		{
			addonConfigurationPanel = createErrorUI("Error loading controller output job configuration interface.", e);
			addFill(addonConfigurationPanel);
			client.sendError("Error loading controller output job configuration interface.",  e);
			revalidate();
			if(frame.isVisible())
				frame.pack();
			fireJobChanged();
			return;
		}
		
		addFill(addonConfigurationPanel);
		revalidate();
		if(frame.isVisible())
			frame.pack();
		fireJobChanged();
	}
	private void fireJobChanged()
	{
		for(ActionListener listener : actionListeners)
		{
			listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRST, "job changed"));
		}
	}
}
