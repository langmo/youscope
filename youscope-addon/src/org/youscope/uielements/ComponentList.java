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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.youscope.addon.component.ComponentMetadata;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.configuration.Configuration;
import org.youscope.common.util.TextTools;

/**
 * A list to select components to configure.
 * @author Moritz Lang
 *
 * @param <C> Basic type of components to configure, e.g. JobConfiguration.
 */
public class ComponentList<C extends Configuration> extends JPanel
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -3053857219631407551L;
	private final ArrayList<ComponentMetadata<? extends C>> componentMetadata = new ArrayList<>();
	private final JList<ComponentMetadata<? extends C>> list = new JList<>(new ListModel<ComponentMetadata<? extends C>>() {

		@Override
		public void addListDataListener(ListDataListener l) {
			// do nothing.
		}

		@Override
		public ComponentMetadata<? extends C> getElementAt(int index) {
			return componentMetadata.get(index);
		}

		@Override
		public int getSize() {
			return componentMetadata.size();
		}

		@Override
		public void removeListDataListener(ListDataListener l) {
			// do nothing.
		}
	});
	private final ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();
	
	/**
	 * Constructs a list with all components added being of the given configuration class, and implementing all of the given required interfaces.
	 * @param client YouScope client.
	 * @param configurationClass Configuration class of components, e.g. JobConfiguration.class.
	 * @param requiredInterface Additional interfaces which are required, e.g. TableProducerConfiguration.class.
	 */
	public ComponentList(YouScopeClient client, Class<C> configurationClass, Class<?>... requiredInterface)
	{
		this(configurationClass, getComponentMetadata(client, configurationClass, requiredInterface));
	}
	
	/**
	 * Constructs a new list with all components added being of the given configuration class.
	 * @param client YouScope client.
	 * @param configurationClass Configuration class of components, e.g. JobConfiguration.class.
	 */
	public ComponentList(YouScopeClient client, Class<C> configurationClass)
	{
		this(configurationClass, getComponentMetadata(client, configurationClass));
	}
	
	/**
	 * Constructs a new list with all components added being passed as a parameter.
	 * 
	 * @param configurationClass Configuration class of components, e.g. JobConfiguration.class.
	 * @param componentMetadata Metadata of the components which can be selected.
	 */
	public ComponentList(Class<C> configurationClass, List<ComponentMetadata<? extends C>> componentMetadata)
	{
		setLayout(new BorderLayout());
		add(list, BorderLayout.CENTER);
		setOpaque(false);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				fireActionEvent();
			}
		});
		list.setCellRenderer(new Renderer());
		this.componentMetadata.addAll(componentMetadata);
		setSelectedElement((String)null);
	}
	
	private final class Renderer extends JLabel implements ListCellRenderer<ComponentMetadata<? extends C>>
	{

		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = -2758571437497425039L;

		private Color background;
		private Color foreground;
		private Color selectedBackground;
		private Color selectedForeground;
		Renderer()
		{
			setOpaque(true);
			setBorder(new EmptyBorder(0,3,0,3));
			
			background = UIManager.getColor ("Table.background");
			if(background == null)
				background = getBackground();
			foreground = UIManager.getColor ("Table.foreground");
			if(foreground == null)
				foreground = getForeground();
			selectedBackground = foreground;
			selectedForeground = background;
		}
		@Override
		public Component getListCellRendererComponent(JList<? extends ComponentMetadata<? extends C>> list,
				ComponentMetadata<? extends C> metadata, int index, boolean isSelected, boolean cellHasFocus) {
			setText(TextTools.capitalize(metadata.getName()));
			Icon icon = metadata.getIcon();
			if(icon == null)
				setIcon(ImageLoadingTools.getResourceIcon("icons/block--plus.png", "Component"));
			else
				setIcon(icon);
			if(!isSelected)
			{
				setBackground(background);
				setForeground(foreground);
			}
			else
			{
				setBackground(selectedBackground);
				setForeground(selectedForeground);
			}
			setOpaque(true);
			return this;
		}
		
	}
	
	/**
	 * Returns the currently selected component, or null if no component is selected.
	 * @return Selected component's metadata.
	 */
	public ComponentMetadata<? extends C> getSelectedElement()
	{
		return list.getSelectedValue();
	}
	
	/**
	 * Returns the type identifier of the currently selected component, or null if no component is selected.
	 * @return Currently selected component's type identifier.
	 */
	public String getSelectedTypeIdentifier()
	{
		ComponentMetadata<? extends C> selectedValue = list.getSelectedValue();
		return selectedValue == null ? null : selectedValue.getTypeIdentifier();
	}
	
	/**
	 * Returns the type identifiers of the all components which can be selected.
	 * @return Type identifiers of all components.
	 */
	public String[] getAllTypeIdentifiers()
	{
		String[] typeIdentifiers = new String[componentMetadata.size()];
		for(int i=0; i<componentMetadata.size(); i++)
		{
			typeIdentifiers[i] = componentMetadata.get(i).getTypeIdentifier();
		}
		return typeIdentifiers;
	}
	
	/**
	 * Adds an listener which gets invoked if the selected element changes.
	 * @param listener listener to add.
	 */
	public void addActionListener(ActionListener listener)
	{
		actionListeners.add(listener);
	}
	/**
	 * Removes a previously added listener.
	 * @param listener listener to remove.
	 */
	public void removeActionListener(ActionListener listener)
	{
		actionListeners.remove(listener);
	}
	
	private void fireActionEvent()
	{
		for(ActionListener listener : actionListeners)
		{
			listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRST, "Component changed"));
		}
	}
	/**
	 * Returns the number of choices the user has. Useful for not displaying this list if user anyway does not have a choice...
	 * @return Number of choices the user has.
	 */
	public int getNumChoices()
	{
		return componentMetadata.size();
	}
	
	/**
	 * Sets the currently selected element. If element is not one of the selectable elements, does nothing. If element is null, selects a random element.
	 * @param element Element to select.
	 */
	public void setSelectedElement(ComponentMetadata<? extends C> element)
	{
		if(element == null)
			setSelectedElement((String)null);
		setSelectedElement(element.getTypeIdentifier());
	}
	
	/**
	 * Sets the currently selected element. If element is not one of the selectable elements, does nothing. If element is null, selects a random element.
	 * @param typeIdentifier Type identifier of the element to select.
	 */
	public void setSelectedElement(String typeIdentifier)
	{
		if(typeIdentifier == null)
		{
			if(componentMetadata.size() == 0)
				return;
			list.setSelectedIndex(0);
			return;
		}
		for(int i=0; i<componentMetadata.size(); i++)
		{
			if(componentMetadata.get(i).getTypeIdentifier().equals(typeIdentifier))
			{
				list.setSelectedIndex(i);
				return;
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
}
