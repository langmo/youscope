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
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.youscope.addon.component.ComponentAddonUI;
import org.youscope.addon.component.ComponentMetadata;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.configuration.Configuration;

/**
 * A configuration panel allowing to choose between all configurations of a given type, and then to configure this configuration.
 * @author Moritz Lang
 *
 * @param <C> Type of configurations which can be chosen.
 */
public class SubConfigurationPanel <C extends Configuration> extends DynamicPanel
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 5007588084125311141L;
	private Component addonConfigurationPanel = null;
	private ComponentAddonUI<? extends C> currentAddon = null;
	private final Class<C> configurationClass;
	private final YouScopeClient client;
	private final YouScopeFrame frame;
	private C lastConfiguration;
	private final ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();
	private final ComponentComboBox<C> componentBox;
	private final ComponentList<C> componentList;
	/**
	 * The type of visual representation of the choice between the elements.
	 * @author Moritz Lang
	 *
	 */
	public enum Type
	{
		/**
		 * Different choices represented as dropdown list.
		 */
		DROPDOWN,
		/**
		 * Different choices represented as list.
		 */
		LIST;
	}
	
	/**
	 * Constructor. Loads automatically all components which conform to the given configuration class and required interfaces. Sets the type to dropdown.
	 * @param choiceString If there is one then more choice of configurations the user can select, this string is shown directly above the configuration type chooser. Can be null.
	 * @param lastConfiguration Last configuration which settings should be loaded. Can be null.
	 * @param configurationClass The base class of all configurations which should be choosable.
	 * @param client YouScope client.
	 * @param frame Containing frame.
	 * @param requiredInterface Additional interfaces which are required, e.g. TableProducerConfiguration.class.
	 */
	public SubConfigurationPanel(String choiceString, C lastConfiguration, Class<C> configurationClass, YouScopeClient client, YouScopeFrame frame, Class<?>... requiredInterface)
	{
		this(choiceString, lastConfiguration, configurationClass, Type.DROPDOWN, client, frame, getComponentMetadata(client, configurationClass, requiredInterface));
	}
	/**
	 * Constructor. Loads automatically all components which conform to the given configuration class and required interfaces.
	 * @param choiceString If there is one then more choice of configurations the user can select, this string is shown directly above the configuration type chooser. Can be null.
	 * @param lastConfiguration Last configuration which settings should be loaded. Can be null.
	 * @param configurationClass The base class of all configurations which should be choosable.
	 * @param type Type of visual representation of this sub-configuration menu.
	 * @param client YouScope client.
	 * @param frame Containing frame.
	 * @param requiredInterface Additional interfaces which are required, e.g. TableProducerConfiguration.class.
	 */
	public SubConfigurationPanel(String choiceString, C lastConfiguration, Class<C> configurationClass, Type type, YouScopeClient client, YouScopeFrame frame, Class<?>... requiredInterface)
	{
		this(choiceString, lastConfiguration, configurationClass, type, client, frame, getComponentMetadata(client, configurationClass, requiredInterface));
	}
	/**
	 * Constructor. Provides manually the list of components which should be displayed. Sets the type to dropdown.
	 * @param choiceString If there is one then more choice of configurations the user can select, this string is shown directly above the configuration type chooser. Can be null.
	 * @param lastConfiguration Last configuration which settings should be loaded. Can be null.
	 * @param configurationClass The base class of all configurations which should be choosable.
	 * @param client YouScope client.
	 * @param frame Containing frame.
	 * @param componentMetadata Metadata of the components which should be displayed.
	 */
	public SubConfigurationPanel(String choiceString, C lastConfiguration, Class<C> configurationClass, YouScopeClient client, YouScopeFrame frame, List<ComponentMetadata<? extends C>> componentMetadata)
	{
		this(choiceString, lastConfiguration, configurationClass, Type.DROPDOWN, client, frame, componentMetadata);
	}
	/**
	 * Constructor. Provides manually the list of components which should be displayed.
	 * @param choiceString If there is one then more choice of configurations the user can select, this string is shown directly above the configuration type chooser. Can be null.
	 * @param lastConfiguration Last configuration which settings should be loaded. Can be null.
	 * @param configurationClass The base class of all configurations which should be choosable.
	 * @param type Type of visual representation of this sub-configuration menu.
	 * @param client YouScope client.
	 * @param frame Containing frame.
	 * @param componentMetadata Metadata of the components which should be displayed.
	 */
	public SubConfigurationPanel(String choiceString, C lastConfiguration, Class<C> configurationClass, Type type, YouScopeClient client, YouScopeFrame frame, List<ComponentMetadata<? extends C>> componentMetadata)
	{
		this.client = client;
		this.frame = frame;
		this.lastConfiguration = lastConfiguration;
		this.configurationClass = configurationClass;
		
		ComponentMetadata<?> metadata;
		if(type == Type.DROPDOWN)
		{
			componentBox = new ComponentComboBox<C>(configurationClass, componentMetadata);
			componentList = null;
			if(lastConfiguration != null)
				componentBox.setSelectedElement(lastConfiguration.getTypeIdentifier());
			if(componentBox.getNumChoices()>1)
			{
				if(choiceString != null)
					add(new JLabel(choiceString));
				add(componentBox);
			}
			componentBox.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) {
					ComponentMetadata<?> metadata = componentBox.getSelectedElement(); 
					if(currentAddon != null)
					{
						ComponentMetadata<? extends C> oldMetadata = currentAddon.getAddonMetadata();
						if(oldMetadata!= null && metadata!=null && oldMetadata.getTypeIdentifier().equals(metadata.getTypeIdentifier()))
							return;
					}
					displayAddonConfiguration(metadata==null ? null : metadata.getTypeIdentifier());
				}
			});
			metadata = componentBox.getSelectedElement(); 
		}
		else
		{
			componentBox = null;
			componentList = new ComponentList<C>(configurationClass, componentMetadata);
			if(lastConfiguration != null)
				componentList.setSelectedElement(lastConfiguration.getTypeIdentifier());
			if(componentList.getNumChoices()>1)
			{
				if(choiceString != null)
					add(new JLabel(choiceString));
				add(new JScrollPane(componentList));
			}
			componentList.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) {
					ComponentMetadata<?> metadata = componentList.getSelectedElement(); 
					if(currentAddon != null)
					{
						ComponentMetadata<? extends C> oldMetadata = currentAddon.getAddonMetadata();
						if(oldMetadata!= null && metadata!=null && oldMetadata.getTypeIdentifier().equals(metadata.getTypeIdentifier()))
							return;
					}
					displayAddonConfiguration(metadata==null ? null : metadata.getTypeIdentifier());
				}
			});
			metadata = componentList.getSelectedElement(); 
		}
		addFillEmpty();
		displayAddonConfiguration(metadata==null ? null : metadata.getTypeIdentifier());
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
	/**
	 * Returns the current configuration, or null if no configuration option is present.
	 * @return Current configuration.
	 */
	public C getConfiguration()
	{	    	
		return currentAddon == null ? null : currentAddon.getConfiguration();
	}
	
	/**
	 * Sets the current configuration. That means, it tries to change the selected addon UI such that the configuration types match, and
	 * then initializes the addon UI with the given configuration.
	 * @param configuration Configuration to set.
	 */
	public void setConfiguration(C configuration)
	{
		if(configuration == null)
			return;
		String[] typeIdentifiers;
		if(componentBox != null)
			typeIdentifiers = componentBox.getAllTypeIdentifiers();
		else
			typeIdentifiers = componentList.getAllTypeIdentifiers();
		for(String id : typeIdentifiers)
		{
			if(id.equals(configuration.getTypeIdentifier()))
			{
				lastConfiguration = configuration;
				displayAddonConfiguration(configuration.getTypeIdentifier());
			}
		}
	}
	
	/**
	 * Sets the current configuration. That means, it tries to change the selected addon UI such that the configuration types matches the type identifier, and
	 * then initializes the addon UI with a fresh configuration.
	 * @param typeIdentifier Type identifier of configuration type to set.
	 */
	public void setConfiguration(String typeIdentifier)
	{
		if(typeIdentifier == null)
			return;
		String[] typeIdentifiers;
		if(componentBox != null)
			typeIdentifiers = componentBox.getAllTypeIdentifiers();
		else
			typeIdentifiers = componentList.getAllTypeIdentifiers();
		for(String id : typeIdentifiers)
		{
			if(id.equals(typeIdentifier))
			{
				displayAddonConfiguration(typeIdentifier);
			}
		}
	}
	
	private static <C extends Configuration> List<ComponentMetadata<? extends C>> getComponentMetadata(YouScopeClient client, Class<C> configurationClass, Class<?>... requiredInterface)
	{
		ArrayList<ComponentMetadata<? extends C>> components = new ArrayList<ComponentMetadata<? extends C>>();
		outerIter:for(ComponentMetadata<? extends C> metadata : client.getAddonProvider().getComponentMetadata(configurationClass))
		{
			for(Class<?> anInterface : requiredInterface)
			{
				if(anInterface != null && !anInterface.isAssignableFrom(metadata.getConfigurationClass()))
					continue outerIter;
			}
			components.add(metadata);
		}
		return components;
	}
	
	/**
	 * Adds an action listener which gets notified if the chosen configuration type changes.
	 * @param listener Listener to add.
	 */
	public void addActionListener(ActionListener listener)
	{
		actionListeners.add(listener);
	}
	/**
	 * Removes a previously added listener.
	 * @param listener Listener to remove.
	 */
	public void removeActionListener(ActionListener listener)
	{
		actionListeners.remove(listener);
	}
	private void displayAddonConfiguration(String typeIdentifier)
	{
		currentAddon = null;
		if(addonConfigurationPanel != null)
			remove(addonConfigurationPanel);
		int pos = getComponentCount()-1;
		if(typeIdentifier == null)
		{
			addonConfigurationPanel = createErrorUI("No choices available.", null);
			addFill(addonConfigurationPanel, pos);
			revalidate();
			if(frame.isVisible())
				frame.pack();
			fireAddonChanged();
			return;
		}
		try 
		{
			currentAddon = client.getAddonProvider().createComponentUI(typeIdentifier, configurationClass);
			
			if(lastConfiguration != null && lastConfiguration.getTypeIdentifier().equals(typeIdentifier))
			{
				currentAddon.setConfiguration(lastConfiguration);
			}
			addonConfigurationPanel = currentAddon.toPanel(frame);
		} 
		catch (Exception e) 
		{
			addonConfigurationPanel = createErrorUI("Error loading addon configuration interface.", e);
			addFill(addonConfigurationPanel, pos);
			client.sendError("Error loading addon configuration interface.",  e);
			revalidate();
			if(frame.isVisible())
				frame.pack();
			fireAddonChanged();
			return;
		}
		
		add(addonConfigurationPanel, pos);
		if(componentBox != null)
			componentBox.setSelectedElement(typeIdentifier);
		else
			componentList.setSelectedElement(typeIdentifier);
		revalidate();
		if(frame.isVisible())
			frame.pack();
		fireAddonChanged();
	}
	
	private void fireAddonChanged()
	{
		for(ActionListener listener : actionListeners)
		{
			listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRST, "addon changed"));
		}
	}
}
