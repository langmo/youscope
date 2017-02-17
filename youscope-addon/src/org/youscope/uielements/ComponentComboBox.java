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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

import org.youscope.addon.component.ComponentMetadata;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.configuration.Configuration;
import org.youscope.common.util.TextTools;

/**
 * A dropdown list to select components to configure.
 * @author Moritz Lang
 *
 * @param <C> Basic type of components to configure, e.g. JobConfiguration.
 */
public class ComponentComboBox<C extends Configuration> extends JButton 
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 4762690329904024235L;
	private final JPopupMenu componentChooser;
	private final HashMap<String, AddonMenuItem<ComponentMetadata<? extends C>>> elements = new HashMap<>();	
	private final ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();
	private AddonMenuItem<ComponentMetadata<? extends C>> selectedElement = null;
	private boolean customTextIcon = false;
	private int popupLocation = SwingConstants.BOTTOM;
	private final int numChoices;
	
	private final ActionListener elementSelectionListener = new ActionListener() 
	{
		@SuppressWarnings("rawtypes")
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			if(e.getSource() instanceof AddonMenuItem)
			{
				setSelectedElement(((AddonMenuItem)e.getSource()).getTypeIdentifier());
				fireActionEvent();
			}
		}
	};
	
	private final Icon downIcon = new Icon()
	{

		@Override
		public int getIconHeight() {
			return 4;
		}

		@Override
		public int getIconWidth() {
			return 8;
		}

		@Override 
		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(ComponentComboBox.this.getForeground());
			g.fillPolygon(new int[]{x,x+getIconWidth(),x+getIconWidth()/2}, new int[]{y,y,y+getIconHeight()}, 3);
		}

	};
			
	private final Icon rightIcon = new Icon()
	{

		@Override
		public int getIconHeight() {
			return 8;
		}

		@Override
		public int getIconWidth() {
			return 4;
		}

		@Override 
		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(ComponentComboBox.this.getForeground());
			g.fillPolygon(new int[]{x,x+getIconWidth(),x}, new int[]{y,y+getIconHeight()/2,y+getIconHeight()}, 3);
		}

	};
	
	/**
	 * Constructs a new combo box with all components added being of the given configuration class, and implementing all of the given required interfaces.
	 * @param client YouScope client.
	 * @param configurationClass Configuration class of components, e.g. JobConfiguration.class.
	 * @param requiredInterface Additional interfaces which are required, e.g. TableProducerConfiguration.class.
	 */
	public ComponentComboBox(YouScopeClient client, Class<C> configurationClass, Class<?>... requiredInterface)
	{
		this(configurationClass, getComponentMetadata(client, configurationClass, requiredInterface));
	}
	
	/**
	 * Constructs a new combo box with all components added being of the given configuration class.
	 * @param client YouScope client.
	 * @param configurationClass Configuration class of components, e.g. JobConfiguration.class.
	 */
	public ComponentComboBox(YouScopeClient client, Class<C> configurationClass)
	{
		this(configurationClass, getComponentMetadata(client, configurationClass));
	}
	
	@Override
	public void setText(String text)
	{
		customTextIcon = true;
		super.setText(text);
	}
	
	@Override
	public void setIcon(Icon icon)
	{
		customTextIcon = true;
		super.setIcon(icon);
	}
	
	private void updateVisuals()
	{
		if(customTextIcon)
		{
			return;
		}
		else if(selectedElement != null)
		{
			super.setText(selectedElement.getText());
			super.setIcon(selectedElement.getIcon());
		}
		else
		{
			super.setText("Component");
			super.setIcon(null);
		}
	}
	
	/**
	 * Returns the currently selected component, or null if no component is selected.
	 * @return Selected component's metadata.
	 */
	public ComponentMetadata<? extends C> getSelectedElement()
	{
		if(selectedElement != null)
			return selectedElement.getAddonMetadata();
		return null;
	}
	
	/**
	 * Returns the type identifier of the currently selected component, or null if no component is selected.
	 * @return Currently selected component's type identifier.
	 */
	public String getSelectedTypeIdentifier()
	{
		if(selectedElement != null)
			return selectedElement.getTypeIdentifier();
		return null;
	}
	
	/**
	 * Returns the type identifiers of the all components which can be selected.
	 * @return Type identifiers of all components.
	 */
	public String[] getAllTypeIdentifiers()
	{
		return elements.keySet().toArray(new String[0]);
	}
	
	@Override
	public void addActionListener(ActionListener listener)
	{
		actionListeners.add(listener);
	}
	@Override
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
	
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Icon icon;
		if(popupLocation == SwingConstants.BOTTOM)
		{
			icon = downIcon;
		}
		else
			icon = rightIcon;
	
		int x = getWidth() - icon.getIconWidth() - 10; 
		int y = (getHeight() - icon.getIconHeight()) / 2;
		icon.paintIcon(this, g, x, y);
			
	}
	/**
	 * Constructs a new combo box with all components added being passed as a parameter.
	 * 
	 * @param configurationClass Configuration class of components, e.g. JobConfiguration.class.
	 * @param componentMetadata Metadata of the components which can be selected.
	 */
	public ComponentComboBox(Class<C> configurationClass, List<ComponentMetadata<? extends C>> componentMetadata)
	{
		setHorizontalAlignment(SwingConstants.LEFT);
		componentChooser = new JPopupMenu();
		super.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	if(popupLocation == SwingConstants.BOTTOM)
                		componentChooser.show(ComponentComboBox.this, 0, getHeight());
                	else
                		componentChooser.show(ComponentComboBox.this, getWidth(), 0);
                }
            }); 
		numChoices = componentMetadata.size();
		createHierachy(componentMetadata);
		setSelectedElement((String)null);
	}
	/**
	 * Returns the number of choices the user has. Useful for not displaying this combobox if user anyway does not have a choice...
	 * @return Number of choices the user has.
	 */
	public int getNumChoices()
	{
		return numChoices;
	}
	
	/**
	 * Sets the location of the popup menu relative to the button. Available choices are
	 * {@link SwingConstants#BOTTOM} or {@link SwingConstants#RIGHT}. All other choices will invoke an {@link IllegalArgumentException}.
	 * @param location Location where the popup should be shown relative to the button.
	 * @throws IllegalArgumentException Thrown if location is invalid.
	 */
	public void setPopupLocation(int location) throws IllegalArgumentException
	{
		if(location == SwingConstants.BOTTOM || location == SwingConstants.RIGHT)
			popupLocation = location;
		else
			throw new IllegalArgumentException("Location must be either SwingConstants.BOTTOM or SwingConstants.RIGHT.");
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
		AddonMenuItem<ComponentMetadata<? extends C>> element;
		if(typeIdentifier == null)
		{
			if(elements.size() == 0)
				return;
			element = elements.values().iterator().next();
		}
		else
		{
			element = elements.get(typeIdentifier);
			if(element == null)
				return;
		}
		selectedElement = element;
		updateVisuals();
	}
	
		
	private void createHierachy(List<ComponentMetadata<? extends C>> componentMetadata)
	{
		JMenu rootMenu = new JMenu();
		for (ComponentMetadata<? extends C> addon : componentMetadata)
        {
			AddonMenuItem<ComponentMetadata<? extends C>> element = new AddonMenuItem<ComponentMetadata<? extends C>>(addon, ImageLoadingTools.DEFAULT_COMPONENT_ICON);
			element.addActionListener(elementSelectionListener);
			elements.put(element.getTypeIdentifier(), element);
            
			// Setup folder structure
			JMenu parentMenu = rootMenu;
			for(String classification : addon.getClassification())
			{
				// Iterate over all menus to check if it already exists
				boolean found = false;
				for(int i=0; i<parentMenu.getItemCount(); i++)
				{
					JMenuItem existingItem = parentMenu.getItem(i);
					if(!(existingItem instanceof JMenu))
						continue;
					if(((JMenu)existingItem).getText().compareToIgnoreCase(classification) == 0)
					{
						parentMenu = (JMenu)existingItem;
						found = true;
						break;
					}
				}
				
				if(!found)
				{
					JMenu newMenu = new JMenu(TextTools.capitalize(classification));
					if(ImageLoadingTools.FOLDER_ICON != null)
						newMenu.setIcon(ImageLoadingTools.FOLDER_ICON);
					parentMenu.add(newMenu);
					parentMenu = newMenu;
				}
			}
            parentMenu.add(element);
            
    	}
		
		shrinkOptions(rootMenu);
		
		for(Component component : rootMenu.getMenuComponents())
		{
			componentChooser.add(component);
		}      
	}
	private void shrinkOptions(JMenu parentMenu)
	{
		for(int i=0; i<parentMenu.getItemCount(); i++)
		{
			JMenuItem item = parentMenu.getItem(i);
			if(item!= null && item instanceof JMenu)
				shrinkOptions((JMenu) item);
		}
		if(parentMenu.getItemCount() == 1 && parentMenu.getMenuComponent(0) instanceof JMenu)
		{
			JMenu child = (JMenu)parentMenu.getMenuComponent(0);
			parentMenu.remove(child);
			for(Component component : child.getMenuComponents())
			{
				parentMenu.add(component);
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
