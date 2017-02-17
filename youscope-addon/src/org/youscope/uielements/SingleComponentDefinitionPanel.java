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
import org.youscope.common.configuration.Configuration;
import org.youscope.common.table.TableProducerConfiguration;
import org.youscope.uielements.ComponentComboBox;
import org.youscope.uielements.DynamicPanel;

/**
 * Panel to select exactly one configuration of a list of supported configurations, and to configure this configuration.
 * @author Moritz Lang
 *
 * @param <C> Configuration type configurable by this panel.
 */
public class SingleComponentDefinitionPanel<C extends Configuration> extends DynamicPanel
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 5667588084125383941L;
	private Component addonConfigurationPanel = null;
	private ComponentAddonUI<? extends C> currentAddon = null;
	private final YouScopeClient client;
	private final YouScopeFrame frame;
	private final ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();
	private final C initialConfiguration;
	private final ComponentComboBox<C> componentBox;
	private final Class<C> configurationType;
	private final JLabel label;
	/**
	 * Constructor.
	 * @param configurationType Type of configurations which should be choosable.
	 * @param client YouScope client.
	 * @param parentFrame Frame containing this panel.
	 * @param requiredInterfaces additional interfaces each configuration has to implement to be choosable (e.g. {@link TableProducerConfiguration}.
	 */
	public SingleComponentDefinitionPanel(Class<C> configurationType, YouScopeClient client, YouScopeFrame parentFrame, Class<?>... requiredInterfaces)
	{
		this(configurationType, null, client, parentFrame, requiredInterfaces);
	}
	/**
	 * Constructor.
	 * @param configurationType Type of configurations which should be choosable.
	 * @param configuration Initial active configuration.
	 * @param client YouScope client.
	 * @param parentFrame Frame containing this panel.
	 * @param requiredInterfaces additional interfaces each configuration has to implement to be choosable (e.g. {@link TableProducerConfiguration}.
	 */
	public SingleComponentDefinitionPanel(Class<C> configurationType, C configuration, YouScopeClient client, YouScopeFrame parentFrame, Class<?>... requiredInterfaces)
	{
		this.client = client;
		this.frame = parentFrame;
		this.initialConfiguration = configuration;
		this.configurationType = configurationType;
		label = new JLabel("Type:");
		add(label);
		componentBox = new ComponentComboBox<C>(client, configurationType, requiredInterfaces);
		if(configuration != null)
			componentBox.setSelectedElement(configuration.getTypeIdentifier());
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
	
	/**
	 * Sets the label which is displayed to indicate the type choice.
	 * @param label Label to display.
	 */
	public void setLabel(String label)
	{
		this.label.setText(label);
	}
	
	/**
	 * Returns the current state of the configuration.
	 * @return Current configuration, or null if no configuration addon is available.
	 */
	public C getConfiguration()
	{
		if(currentAddon == null)
			return null;
		return currentAddon.getConfiguration();
	}
	
	/**
	 * Returns the type identifier of the currently active configuration, or null if no addon is available.
	 * @return Current configuration type identifier.
	 */
	public String getTypeIdentifier()
	{
		if(currentAddon == null)
			return null;
		return currentAddon.getAddonMetadata().getTypeIdentifier();
	}
	
	/**
	 * Returns the metadata of the currently active configuration, or null if no addon is available.
	 * @return Metadata of currently active configuration.
	 */
	public ComponentMetadata<? extends C> getConfigurationMetadata()
	{
		if(currentAddon == null)
			return null;
		return currentAddon.getAddonMetadata();
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
	void addActionListener(ActionListener listener)
	{
		actionListeners.add(listener);
	}
	void removeActionListener(ActionListener listener)
	{
		actionListeners.remove(listener);
	}
	private void displayAddonConfiguration(ComponentMetadata<? extends C> metadata)
	{
		if(currentAddon != null)
		{
			ComponentMetadata<?> currentMetadata = currentAddon.getAddonMetadata();
			if(currentMetadata!= null && metadata!= null && currentMetadata.getTypeIdentifier().equals(metadata.getTypeIdentifier()))
				return;
			currentAddon = null;
		}
		if(addonConfigurationPanel != null)
			remove(addonConfigurationPanel);
		if(metadata == null)
		{
			addonConfigurationPanel = createErrorUI("No choice available.", null);
			addFill(addonConfigurationPanel);
			revalidate();
			if(frame.isVisible())
				frame.pack();
			fireJobChanged();
			return;
		}
		try 
		{
			currentAddon = client.getAddonProvider().createComponentUI(metadata.getTypeIdentifier(), configurationType);
			
			if(initialConfiguration != null && initialConfiguration.getTypeIdentifier().equals(metadata.getTypeIdentifier()))
			{
				currentAddon.setConfiguration(initialConfiguration);
			}
			addonConfigurationPanel = currentAddon.toPanel(frame);
		} 
		catch (Exception e) 
		{
			addonConfigurationPanel = createErrorUI("Error loading controller input job configuration interface.", e);
			addFill(addonConfigurationPanel);
			client.sendError("Error loading controller input job configuration interface.",  e);
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
